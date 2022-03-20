package newhorizon.util.feature;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import arc.util.async.Threads;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.core.Logic;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Tex;
import mindustry.net.Packet;
import mindustry.ui.BorderImage;
import mindustry.ui.Styles;
import newhorizon.expand.block.special.BeaconBlock;
import newhorizon.expand.entities.NHGroups;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.annotation.HeadlessDisabled;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.func.Map2D;
import newhorizon.util.func.NHGeom;
import newhorizon.util.func.NHSetting;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.world;
import static newhorizon.util.ui.TableFunc.OFFSET;

@SuppressWarnings("CodeBlock2Expr")
public class BeaconCaptureCore implements Runnable{
	public static final String SCORE_KEY = "capture-scores";
	public static final long updateInterval = 3;
	
	public int winScore = 100_000;
	public float scl = 0.2f;
	
	public Seq<Runnable> scoreUpdateListener = new Seq<>();
	public Seq<Runnable> pixmapUpdateListener = new Seq<>();
	
	public TaskQueue taskQueue = new TaskQueue();
	
	public Map2D<Point2> tiles;
	
	public final IntMap<ObjectSet<Team>> buildCaptured;
	public final IntMap<ObjectSet<Team>> unitCaptured;
	public final IntMap<ObjectSet<Team>> combined;
	public final ObjectMap<BeaconBlock.BeaconBuild, Seq<BeaconBlock.BeaconBuild>> calculatedPair = new ObjectMap<>();
	
	public IntIntMap capturedArea = new IntIntMap();
	public IntIntMap lastCapturedArea = new IntIntMap();
	public boolean calculatingArea = false;
	
	public IntIntMap scores = new IntIntMap();
	
	public float drawReload = 0;
	public static final float drawReloadTime = 45f;
	
	public float scoreReload = 0;
	public static final float scoreReloadTime = 240f;
	
	public float unitFieldReload = 0;
	public static final float unitFieldReloadTime = 45f;
	
	@HeadlessDisabled public Pixmap pixmap = new Pixmap(1, 1);
	@HeadlessDisabled public Texture texture;
	@HeadlessDisabled public TextureRegion textureRegion = new TextureRegion();
	
	public Thread updateThread;
	
	public BeaconCaptureCore(){
		start();
		
		buildCaptured = new IntMap<>();
		unitCaptured = new IntMap<>();
		combined = new IntMap<>();
		
		tiles = new Map2D<>(proj(world.width()), proj(world.height()));
		
		if(!Vars.headless)texture = new Texture(1, 1);
		
		Vars.state.teams.active.each(teamData -> {
			scores.put(teamData.team.id, 0);
		});
		
		readScore();
		
		if(!Vars.headless){
			Core.app.post(() -> Core.app.post(() -> {
				updatePixmap();
				updateBeacons();
				new CaptureProgress().setup();
			}));
		}
	}
	
	public void updateBeacons(){
		taskQueue.post(() -> {
			synchronized(buildCaptured){
				buildCaptured.clear();
			}
		});
		taskQueue.post(() -> {
			Vars.state.teams.active.each(teamData -> {
				Seq<BeaconBlock.BeaconBuild> builds = NHGroups.beacon.copy().filter(b -> b.team == teamData.team);
				if(builds.any()){
					if(builds.size == 1){
						BeaconBlock.BeaconBuild beacon = builds.first();
						synchronized(buildCaptured){
							NHGeom.square(proj(beacon.tileX()), proj(beacon.tileY()), proj(beacon.fieldSize() / 2), ((x, y) -> {
								switchField(x, y, beacon.team, true);
							}));
						}
					}else{
						synchronized(buildCaptured){
							synchronized(calculatedPair){
								calculatedPair.clear();
								builds.each(src -> builds.each(tgt -> {
									if(src != tgt && (!calculatedPair.containsKey(src) || !calculatedPair.get(src).contains(tgt)))updatePair(src, tgt, true);
								}));
							}
						}
					}
				}
			});
		});
		taskQueue.post(this::updateCombine);
	}
	
	public void updateBeacon(BeaconBlock.BeaconBuild beacon, boolean add){
		calculatedPair.clear();
		taskQueue.post(() -> {
			synchronized(buildCaptured){
				Seq<BeaconBlock.BeaconBuild> all = NHGroups.beacon.copy().removeAll(b -> b == beacon || b.team != beacon.team);
				if(all.isEmpty()){
					NHGeom.square(proj(beacon.tileX()), proj(beacon.tileY()), proj(beacon.fieldSize() / 2), ((x, y) -> {
						switchField(x, y, beacon.team, add);
					}));
				}else{
					for(BeaconBlock.BeaconBuild build : all){
						updatePair(beacon, build, add);
					}
				}
			}
		});
		
		taskQueue.post(this::updateCombine);
	}
	
	public synchronized void updateUnitCapture(){
		unitCaptured.clear();
		
		Vars.state.teams.active.each(teamData -> {
			Seq<Position> linkable = new Seq<>();
			linkable.addAll(NHGroups.beacon.copy().filter(b -> b.team == teamData.team));
			linkable.addAll(teamData.cores);
			if(teamData.team == Vars.state.rules.waveTeam)linkable.addAll(Vars.spawner.getSpawns());
			teamData.units.each(unit -> {
				if(unit.hitSize > 8 && unit.type.health > 1000){
					float maxDst = BeaconBlock.maxLinkDst.get(unit);
					
					Position closest = Geometry.findClosest(unit.x, unit.y, linkable);
					if(unit.dst(closest) < maxDst){
						NHGeom.square((int)(unit.tileX() * scl), (int)(unit.tileY() * scl), (int)(unit.hitSize / 8 * scl), ((x, y) -> {
							putUnit(x, y, unit.team);
						}));
					}
				}
			});
		});
	}
	
	public synchronized void updateCombine(){
		calculatingArea = true;
		
		combined.clear();
		capturedArea.clear();
		combined.putAll(buildCaptured);
		combined.putAll(unitCaptured);
		
		combined.values().toArray().each(v -> {
			v.each(team -> {
				capturedArea.increment(team.id);
			});
		});
		
		lastCapturedArea = new IntIntMap(capturedArea);
		calculatingArea = false;
	}
	
	public int proj(int v){
		return (int)(v * scl);
	}
	
	/** The two building should be the same team! */
	public void updatePair(BeaconBlock.BeaconBuild src, BeaconBlock.BeaconBuild target, boolean add){
		Seq<BeaconBlock.BeaconBuild> ced = calculatedPair.get(target);
		if(ced != null)ced.add(src);
		else calculatedPair.put(target, Seq.with(src));
		
		NHGeom.raycast(proj(src.tileX()), proj(src.tileY()), proj(target.tileX()), proj(target.tileY()), proj(target.fieldSize()), proj(target.fieldSize()), ((x, y) -> {
			switchField(x, y, target.team, add);
		}));
	}
	
	public void update(){
		drawReload += Time.delta;
		scoreReload += Time.delta;
		if(NHVars.state.unitHoldField)unitFieldReload += Time.delta;
		
		if(!Vars.state.isPaused()){
			if(unitFieldReload > unitFieldReloadTime){
				taskQueue.post(this::updateUnitCapture);
				taskQueue.post(this::updateCombine);
				
				unitFieldReload = 0;
			}
			
			if(scoreReload > scoreReloadTime && taskQueue.size() == 0){
				if(!Vars.net.client()){
					updateScore();
					writeScore();
					scoreReload = 0;
				}
			}
		}
		
		if(Vars.headless)return;
		
		if(drawReload > drawReloadTime && taskQueue.size() == 0){
			updatePixmap();
			
			drawReload = 0;
		}
	}
	
	public void updatePixmap(){
		Pixmap pixmap = new Pixmap(tiles.width, tiles.height);
		
		Color c1 = new Color(), c2 = new Color();
		
		pixmap.fill(c1.set(0, 0, 0, 0.6f));
		
		Seq<int[]> toBlit = new Seq<>();
		
		for(IntMap.Entry<ObjectSet<Team>> entry : combined.entries()){
			if(entry.value == null)continue;
			
			int x = tiles.x(entry.key);
			int y = tiles.y(entry.key);
			
			entry.value.each(team -> {
				c1.set(team.color);
				c2.set(pixmap.get(x, tiles.height - y)).a(1);
				pixmap.set(x, tiles.height - y, c2.lerp(c1, 0.65f).a(1f));
				if(NHSetting.enableDetails())toBlit.add(new int[]{x, tiles.height - y});
			});
		}
		
		if(NHSetting.enableDetails())toBlit.each(arr -> {
			c2.set(pixmap.get(arr[0], arr[1]));
			pixmap.set(arr[0], arr[1], c2.mul(1.05f).lerp(Color.white, 0.125f));
		});
	
		
		this.pixmap = pixmap;
		this.texture = new Texture(pixmap);
		
		pixmapUpdateListener.each(Runnable::run);
	}
	
	public void updateScore(){
		scoreUpdateListener.each(Runnable::run);
		
		for(IntIntMap.Entry entry : capturedArea.entries()){
			scores.increment(entry.key, entry.value);
		}
		
		afterUpdateScore();
		if(Vars.net.server())syncScore();
	}
	
	public void afterUpdateScore(){
		for(IntIntMap.Entry entry : scores.entries()){
			if(entry.value > winScore){
				NHVars.state.beaconCaptureCore = null;
				NHVars.state.mode_beaconCapture = false;
				
				if(!Vars.net.client()){
					Team winner = Team.get(entry.key);
					if(winner == null)winner = Team.derelict;
					
					if(Vars.headless){
						Call.updateGameOver(winner);
						
//						Vars.state.teams.active.each(teamData -> teamData.cores.each(c -> Time.run(Mathf.random(60f), c::kill)));
						return;
					}
					
					if(!Vars.state.isCampaign())Call.gameOver(winner);
					else Logic.sectorCapture();
				}
				
				return;
			}
		}
	}
	
	/** Check captured fields*/
	@SuppressWarnings("BusyWait")
	@Override
	public void run(){
		while(true){
			try{
				if(Vars.state.isPlaying()){
					taskQueue.run();
				}
				
				try{
					Thread.sleep(updateInterval);
				}catch(InterruptedException e){
					//stop looping when interrupted externally
					break;
				}
			}catch(Exception e){
				Log.err(e);
			}
		}
	}
	
	public void syncScore(){
		Vars.net.send(new SyncScorePacket(scores), true);
	}
	
	public void writeScore(){
		Jval jval = Jval.newObject();
		
		for(IntIntMap.Entry entry : scores.entries()){
			jval.put(String.valueOf(entry.key), entry.value);
		}
		
		Vars.state.rules.tags.put(SCORE_KEY, jval.toString());
	}
	
	public void readScore(){
		if(Vars.state.rules.tags.containsKey(SCORE_KEY)){
			Jval jval = Jval.read(Vars.state.rules.tags.get(SCORE_KEY));
			for(ObjectMap.Entry<String, Jval> entry : jval.asObject()){
				scores.put(Integer.parseInt(entry.key), entry.value.asInt());
			}
		}
	}
	
	public int convertIndex(int x, int y){
		return (int)(x * scl) + (int)(y * scl);
	}
	
	public synchronized void put(int x, int y, Team team){
		int index = tiles.index(x, y);
		if(!tiles.has(index))return;
		ObjectSet<Team> map = buildCaptured.get(index);
		if(map == null) buildCaptured.put(index, map = new ObjectSet<>(Vars.state.teams.active.size));
		map.add(team);
	}
	
	public synchronized void remove(int x, int y, Team team){
		int index = tiles.index(x, y);
		ObjectSet<Team> map = buildCaptured.get(index);
		if(map != null)map.remove(team);
	}
	
	public synchronized void putUnit(int x, int y, Team team){
		int index = tiles.index(x, y);
		if(!tiles.has(index))return;
		ObjectSet<Team> map = unitCaptured.get(index);
		if(map == null)unitCaptured.put(index, map = new ObjectSet<>(Vars.state.teams.active.size));
		map.add(team);
	}
	
	public synchronized void removeUnit(int x, int y, Team team){
		int index = x + y * tiles.width;
		ObjectSet<Team> map = unitCaptured.get(index);
		if(map != null)map.remove(team);
	}
	
	public synchronized void switchField(int x, int y, Team team, boolean add){
		if(add)put(x, y, team);
		else remove(x, y, team);
	}
	
	public void exit(){
		pixmapUpdateListener.clear();
		scoreUpdateListener.clear();
		
		stop();
	}
	
	public void stop(){
		if(updateThread != null){
			updateThread.interrupt();
			updateThread = null;
		}
		
		taskQueue.clear();
	}
	
	public void start(){
		stop();
		updateThread = Threads.daemon("Beacon Capture Calculator", this);
	}
	
	public class ScoreProgressBar extends Element{
		{
			color.set(0, 0, 0, 0.6f);
		}
		
		public int lastArea = 0;
		
		@Override
		public void draw(){
			super.draw();
			
			if(!calculatingArea){
				lastArea = 0;
				for(IntIntMap.Entry entry : lastCapturedArea.entries()){
					lastArea += entry.value;
				}
			}
			
			Draw.alpha(parentAlpha * color.a);
			Draw.tint(color);
			DrawFunc.fillRect(x, y, width, height);
			
			Draw.alpha(parentAlpha);
			float widthOffset = 0;
			
			float allyScoreScale = (float)lastCapturedArea.get(Vars.player.team().id) / (float)lastArea;
			Draw.tint(Vars.player.team().color);
			DrawFunc.fillRect(x, y, width * allyScoreScale * 0.9f, height);
			widthOffset += width * allyScoreScale;
			
			
			for(IntIntMap.Entry entry : lastCapturedArea.entries()){
				if(entry.key == Vars.player.team().id)continue;
				allyScoreScale = (float)entry.value / (float)lastArea;
				Draw.tint(Team.get(entry.key).color);
				DrawFunc.fillRect(x + widthOffset, y, width * allyScoreScale, height);
				widthOffset += width * allyScoreScale;
			}
			
			float progress = Mathf.curve(scoreReload / scoreReloadTime, 0.075f, 0.925f);
			float progressBack = Mathf.clamp(progress - 0.15f * (1 - progress));
			float progressFront = Mathf.clamp(progress * 1.15f);
			float fb = Tmp.c2.set(Color.white).a(parentAlpha).toFloatBits();
			float fc = Tmp.c2.set(Color.white).a(0).toFloatBits();
			Fill.quad(x + width * progressBack, y, fc, x + width * progressBack, y + height, fc, x + progressFront * width, y + height, fb, x + progressFront * width, y, fb);
			
			Lines.stroke(2f, Color.gray);
			Draw.alpha(parentAlpha);
			Lines.rect(x, y, width, height);
		}
	}
	
	public class ScoreTotalBar extends Element{
		{
			color.set(0, 0, 0, 0.6f);
		}
		
		@Override
		public void draw(){
			
			
			super.draw();
			
			Seq<int[]> sc = new Seq<>(scores.size);
			for(IntIntMap.Entry entry : scores.entries()){
				sc.add(new int[]{entry.key, entry.value});
			}
			
			sc.sortComparing(arr -> -arr[1]);
			
			Draw.alpha(parentAlpha * color.a);
			Draw.tint(color);
			DrawFunc.fillRect(x, y, width, height);
			
			Draw.alpha(parentAlpha);
			for(int[] arr : sc){
				float scl = (float)arr[1] / winScore;
				
				Color tc = Team.get(arr[0]).color;
				float fc = Tmp.c1.set(tc).mul(1.25f).a(parentAlpha).toFloatBits();
				float fb = Tmp.c2.set(Tmp.c1).lerp(tc, Mathf.curve(scl, 0, 0.125f)).a(parentAlpha).toFloatBits();
				Fill.quad(x, y, fc, x, y + height, fc, x + scl * width, y + height, fb, x + scl * width, y, fb);
			}
			
			Lines.stroke(2f, Color.gray);
			Draw.alpha(parentAlpha);
			Lines.rect(x, y, width, height);
		}
	}
	
	public class CaptureProgress extends CutsceneEvent{
		public CaptureProgress(){
			this.name = "BeaconCaptureProgress";
		
			
			cannotBeRemove = false;
			removeAfterTriggered = false;
			exist = e -> NHVars.state.captureMod();
			removeAfterVictory = true;
			
			entityType = () -> new CutsceneEventEntity(){@Override public boolean serialize(){return false;}};
		}
		
		@Override
		public CutsceneEventEntity setup(){
			CutsceneEventEntity entity = entityType.get();
			entity.setType(this);
			
			entity.add();
			return entity;
		}
		
		@Override
		public void setupTable(CutsceneEventEntity e, Table table){
			e.infoT = new Table(Tex.sideline, t -> {
				t.margin(OFFSET * 2f);
				t.stack(new ScoreProgressBar(), new Label("  " + Core.bundle.get("mod.ui.capture-proportion"), Styles.outlineLabel)).growX().height(36f).pad(OFFSET / 2).padLeft(OFFSET).row();
				t.stack(new ScoreTotalBar(), new Label("  " + Core.bundle.get("mod.ui.capture-progress"), Styles.outlineLabel)).growX().height(36f).pad(OFFSET / 2).padLeft(OFFSET).row();
				t.table(c -> {
					c.left();
					
					Prov<TextureRegion> im = () -> {
						textureRegion.set(texture);
						return textureRegion;
					};
					
					c.add(new BorderImage(texture, 2f).border(Color.gray)).update(i -> {
						i.setDrawable(im.get());
						i.layout();
					}).scaling(Scaling.fit).visible(true).margin(OFFSET / 2f).left().padRight(OFFSET / 2f);
					c.pane(i -> {
						i.margin(OFFSET).align(Align.topLeft);
						
						Seq<Label> labels = new Seq<>(scores.size);
						for(IntIntMap.Entry entry : scores.entries()){
							labels.add(new Label(""));
						}
						
						i.update(() -> {
							int index = 0;
							for(IntIntMap.Entry entry : scores.entries()){
								if(index < labels.size){
									Team team = Team.get(entry.key);
									labels.get(index).setText(Core.bundle.format("mod.ui.capture-score", "#" + team.color, team, entry.value, TableFunc.format((float)entry.value / winScore * 100), TableFunc.format((float)lastCapturedArea.get(entry.key) / winScore * 100)));
								}
								index++;
							}
						});
						
						i.add(Core.bundle.format("mod.ui.capture-total-score", winScore)).padBottom(OFFSET).left().growX().row();
						
						labels.each(l -> {
							i.add(l).padBottom(OFFSET / 2f).left().growX().row();
						});
					}).grow().margin(OFFSET).left();
				}).padTop(OFFSET / 2f).margin(OFFSET).growX().fillY();
			});
			
			e.infoT.pack();
			
			table.add(e.infoT).left().row();
		}
	}
	
	public static class SyncScorePacket extends Packet{
		
		private byte[] DATA;
		public IntIntMap scores = new IntIntMap();
		
		public SyncScorePacket(IntIntMap scores){
			this.scores = scores;
		}
		
		public SyncScorePacket(){
			this.DATA = NODATA;
		}
		
		public void write(Writes WRITE){
			WRITE.i(scores.size);
			
			for(IntIntMap.Entry entry : scores.entries()){
				WRITE.i(entry.key);
				WRITE.i(entry.value);
			}
		}
		
		public void read(Reads READ, int LENGTH){
			this.DATA = READ.b(LENGTH);
		}
		
		public void handled(){
			BAIS.setBytes(this.DATA);
			
			int size = READ.i();
			
			for(int i = 0; i < size; i++){
				scores.put(READ.i(), READ.i());
			}
		}
		
		public int getPriority(){
			return priorityLow;
		}
		
		public void handleClient(){
			if(NHVars.state.captureMod()){
				NHVars.state.beaconCaptureCore.scores = scores;
				NHVars.state.beaconCaptureCore.scoreReload = 0;
				
				NHVars.state.beaconCaptureCore.scoreUpdateListener.each(Runnable::run);
				NHVars.state.beaconCaptureCore.afterUpdateScore();
			}
		}
	}
}
