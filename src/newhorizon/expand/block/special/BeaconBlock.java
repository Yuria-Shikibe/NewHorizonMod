package newhorizon.expand.block.special;

import arc.Core;
import arc.Events;
import arc.func.Floatf;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.expand.entities.NHGroups;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.cutscene.CutsceneEvent;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;
import newhorizon.util.feature.cutscene.UIActions;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.tilesize;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

/**
 * Use for a new feature game mode;<p>
 *
 * All {@link BeaconBuild} shouldn't be removed during a game;
 *
 * @author Yuria
 * */
public class BeaconBlock extends Block{
	public static final Floatf<Unit> transformer = unit -> unit.hitSize * 4f + Mathf.sqrt(unit.health);
	
	public float captureRange = 200;
	public float captureReload = 1500;
	public float captureSpeedPerUnit = 1.25f;
	public float shockwaveRange = 200f;
	public float recoverSpeed = 1f;
	public float warmupSpeed = 0.02f;
	public Floatf<Unit> scl = u -> Mathf.curve(u.health, 500, 6000) + 0.075f;
	public Floatf<Float> transform = Mathf::sqrt;
	
	public int drawSectors = 4;
	public float drawSectorRotateSpeed = 0.45f;
	public float sectorRad = 0.13f;
	public int fieldSize = 30;
	
	public Effect capture;
	
	public BeaconBlock(String name){
		super(name);
		
		destructible = false;
		health = 10000000;
		enableDrawStatus = true;
		targetable = false;
		sync = true;
		alwaysUnlocked = true;
		update = true;
		solid = true;
		flags = EnumSet.of(BlockFlag.rally);
		canOverdrive = false;
		buildVisibility = BuildVisibility.sandboxOnly;
		category = Category.effect;
	}
	
	@Override
	public boolean isHidden(){
		return super.isHidden() || !NHVars.state.captureMod();
	}
	
	@Override
	public void setStats(){
		super.setStats();
		
		stats.remove(Stat.health);
	}
	
	@Override
	public void init(){
		super.init();
		
		clipSize = Math.max(clipSize, captureRange * 2);
		capture = new Effect(90f, size * tilesize, e -> {
			Draw.mixcol(e.color, 1);
			Draw.scl(1.5f * e.foutpow());
			Draw.rect(teamRegion, e.x, e.y);
		});
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.remove("health");
		bars.add("progress",
			(BeaconBuild entity) -> new Bar(
				() -> entity.stalemate ?
						Core.bundle.get("mod.ui.stalemate") : entity.capturing() ? Core.bundle.get("mod.ui.capturing") : entity.completeCapture ? Core.bundle.get("mod.ui.recapturing") : Core.bundle.get("mod.ui.maintaining"),
				() -> entity.team.color,
				() -> entity.reload / captureReload
			)
		);
	}
	
	public class BeaconBuild extends Building{
		public float reload = 0;
		public Team capturingTeam = Team.derelict;
		public int raider = 0;
		protected boolean stalemate = false;
		public float progress;
		
		protected boolean completeCapture = true;
		
		protected float captureWarmup = 0;
		
		public int fieldSize(){
			return fieldSize;
		}
		
		@Override
		public boolean canPickup(){
			return false;
		}
		
		@Override
		public boolean collide(Bullet other){
			return false;
		}
		
		@Override
		public boolean collision(Bullet other){
			return false;
		}
		
		@Override
		public void damage(float damage){
		}
		
		public void drawStatus(){
			float multiplier = block.size > 1 ? 1 : 0.64f;
			float brcx = x + (block.size * tilesize / 2f) - (tilesize * multiplier / 2f);
			float brcy = y - (block.size * tilesize / 2f) + (tilesize * multiplier / 2f);
			
			Draw.z(Layer.power + 1);
			Draw.color(Pal.gray);
			Fill.square(brcx, brcy, 2.5f * multiplier, 45);
			Draw.color(status().color);
			Fill.square(brcx, brcy, 1.5f * multiplier, 45);
			Draw.color();
		}
		
		@Override
		public void draw(){
			super.draw();
			
			if(!enabled)return;
			
			Draw.color(team.color);
			float z = Draw.z();
			Draw.z(Layer.effect);
			float sine = Mathf.absin(6f, 1f);
			Lines.stroke(2 + sine);
			for(int i = 0; i < drawSectors; i++){
				float rot = i * 360f / drawSectors + progress;
				Lines.swirl(x, y, captureRange + tilesize * (2 + sine / 2f), sectorRad, rot);
			}
			
			DrawFunc.circlePercent(x, y, captureRange, reload / captureReload, 0);
			
			if(captureWarmup > 0.005f)for(int i = 0; i < 3; i++){
				float f = (100 - (progress + 25 * i) % 100) / 40 * captureWarmup;
				Draw.scl(f);
				for(int j : Mathf.signs){
					float xOffset = (captureRange + tilesize * (5 + i * 3f)) * j + x;
					Draw.rect(NHContent.arrowRegion, xOffset, y, 90 * j);
				}
			}
			
			Draw.scl(1);
			Draw.z(Layer.bullet);
			float cRad = tilesize * (size / 2f + 1.5f);
			
			Seq<Unit> nearby = Groups.unit.intersect(x - captureRange, y - captureRange, captureRange * 2f, captureRange * 2f);
			nearby.each(u -> u.within(this, captureRange), u -> {
				Draw.color(u.team.color);
				float ang = angleTo(u);
				float f = Mathf.curve(1 - dst(u) / captureRange, 0, 0.1f);
				Tmp.v1.trns(ang, cRad + tilesize + u.hitSize / 8f).add(this);
				Tmp.v2.trns(ang, u.hitSize() * -1.05f).add(u);
				
				Lines.stroke(2 * f);
				Lines.swirl(x, y, cRad + tilesize + u.hitSize / 8f, 0.1f, ang - 14.25f);
				
				Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, false);
				Lines.circle(u.x, u.y, u.hitSize() * 1.05f);
			});
			
			Lines.stroke(2);
			Draw.color(team.color);
			Lines.circle(x, y, cRad * (1 + sine / 6f));
			
			Draw.z(z);
			Draw.reset();
		}
		
		@Override
		public BlockStatus status(){
			if(stalemate)return BlockStatus.noOutput;
			if(recapturing())return BlockStatus.active;
			return BlockStatus.noInput;
		}
		
		@Override
		public void updateTile(){
			capturingTeam = Team.derelict;
			stalemate = false;
			raider = 0;
			
			boolean effect = timer.get(30f);
			
			if(!NHVars.state.mode_beaconCapture || !Vars.state.teams.isActive(team)){
				enabled(false);
				return;
			}else enabled(true);
			
			Seq<Unit> nearby = Groups.unit.intersect(x - captureRange, y - captureRange, captureRange * 2f, captureRange * 2f);
			nearby.filter(u -> u.within(this, captureRange));
			
			boolean hasAlly = false, hasHostile = false;
			float captureSpeedScl = 0;
			
			for(Unit unit : nearby){
//				if(effect)NHFx.slidePoly.at(unit.x, unit.y, unit.hitSize, unit.team.color, this);
				
				if(unit.team == team){
					hasAlly = true;
				}else{
					hasHostile = true;
					raider += 1;
					captureSpeedScl += scl.get(unit);
					capturingTeam = unit.team;
				}
			}
			
			
			stalemate = hasAlly && hasHostile;
			
			if(hasHostile)captureWarmup = Mathf.lerp(captureWarmup, 1, warmupSpeed);
			else captureWarmup = Mathf.lerp(captureWarmup, 0, warmupSpeed);
			
			if(!stalemate){
				if(hasAlly){
					reload = Math.min(reload + recoverSpeed * Time.delta * 3f, captureReload);
				}else if(hasHostile){
					captureSpeedScl = transform.get(captureSpeedScl);
					reload = Math.max(reload - captureSpeedScl * captureSpeedPerUnit * Time.delta * captureWarmup, -2f);
					progress += captureSpeedScl * captureSpeedPerUnit * Time.delta * captureWarmup / 2f;
				}else if(recapturing()){
					//If so, there must have no other team's units nearby;
					if(!hasNoOwner())reload = Math.min(captureReload, reload + recoverSpeed * Time.delta);
				}
			}else capturingTeam = Team.derelict; //If stalemated, the capture process shouldn't continue;
			
			progress += Time.delta * drawSectorRotateSpeed;
			
			if(reload < 0){
				reload = 0;
				capture();
			}
			
			if(reload > captureReload - 10f)completeCapture = true;
		}
		
		public boolean hasNoOwner(){
			return team == Team.derelict;
		}
		
		@Override
		public void add(){
			if(!added){
				NHGroups.beacon.add(this);
				Events.fire(BeaconCapturedEvent.class, new BeaconCapturedEvent(this, true));
			}
			
			
//			if(!UIActions.disabled())EventListeners.actAfterLoad.add(() -> Core.app.post(() -> {
//				new BeaconCaptureCutsceneEvent(
//					this,
//					() -> stalemate ? Core.bundle.get("mod.ui.stalemate") : capturing() ? Core.bundle.get("mod.ui.capturing") : Core.bundle.get("mod.ui.recapturing"),
//					() -> capturing() ? Tmp.c1.set(team.color).lerp(capturingTeam.color, Mathf.absin(8f, 1)) : team.color,
//					() -> reload / captureReload
//				).setup();
//			}));
	
			
			super.add();
		}
		
		@Override
		public void remove(){
			if(added){
				Events.fire(BeaconCapturedEvent.class, new BeaconCapturedEvent(this, false));
				NHGroups.beacon.remove(this);
			}
			
			super.remove();
		}
		
		public void capture(){
			completeCapture = false;
			NHFx.circle.at(x, y, captureRange, capturingTeam.color);
			capture.at(x, y, capturingTeam.color);
			if(!UIActions.disabled()){
				UIActions.actionSeqMinor(UIActions.labelAct(Core.bundle.format("mod.ui.beacon-captured", team.color, team, capturingTeam.color, capturingTeam), 2f, 0.5f, Interp.fade, t -> {
					t.image(fullIcon).size(LEN - OFFSET * 2f).padRight(OFFSET * 1.5f).scaling(Scaling.fit);
				}));
			}
			
			if(!Vars.net.client())Vars.indexer.eachBlock(null, x, y, fieldSize() * tilesize, b -> b.team != team, Building::kill);
			
			Events.fire(BeaconCapturedEvent.class, new BeaconCapturedEvent(this, false));
			changeTeam(capturingTeam);
			Events.fire(BeaconCapturedEvent.class, new BeaconCapturedEvent(this, true));
		}
		
		public boolean capturing(){
			return capturingTeam != Team.derelict;
		}
		
		public boolean recapturing(){
			return completeCapture && raider == 0 && capturingTeam == Team.derelict;
		}
		
		@Override
		public void read(Reads read, byte r){
			reload = read.f();
			captureWarmup = read.f();
			completeCapture = read.bool();
		}
		
		@Override
		public void write(Writes write){
			write.f(reload);
			write.f(captureWarmup);
			write.bool(completeCapture);
		}
	}
	
	public static class BeaconCaptureCutsceneEvent extends CutsceneEvent{
		public BeaconBuild building;
		public Floatp progress;
		public Prov<Color> color;
		public Prov<CharSequence> info;
		
		public BeaconCaptureCutsceneEvent(BeaconBuild building, Prov<CharSequence> info, Prov<Color> color, Floatp progress){
			this.name = building.toString();
			this.building = building;
			this.progress = progress;
			this.color = color;
			this.info = info;
			
			cannotBeRemove = false;
			removeAfterTriggered = false;
			exist = e -> building.isValid();
			removeAfterVictory = true;
			
			entityType = () -> new CutsceneEventEntity(){@Override public boolean serialize(){return false;}};
		}
		
		@Override
		public CutsceneEventEntity setup(){
			CutsceneEventEntity event = super.setup();
			event.set(building);
			return event;
		}
		
		@Override
		public void setupTable(CutsceneEventEntity e, Table table){
			e.infoT = new Table(Tex.sideline, t -> {
				t.margin(OFFSET);
				t.table(c -> {
					c.left();
					c.image(building.block.fullIcon).size(LEN - OFFSET).scaling(Scaling.fit);
					c.add("(" + building.tileX() + ", " + building.tileY() + ')').padLeft(OFFSET);
					c.button("Check Target", Icon.eye, Styles.transt, () -> {
						UIActions.checkPosition(e);
					}).disabled(b -> UIActions.lockingInput()).growX().height(LEN - OFFSET * 1.5f).padLeft(OFFSET).marginLeft(OFFSET).pad(OFFSET / 3);
				}).growX().fillY().padBottom(OFFSET / 3f).left().row();
				
				t.add(new Bar(info, color, progress)).growX().height(30).marginLeft(OFFSET).marginRight(OFFSET);
			}).margin(OFFSET).marginRight(LEN);
			
			e.infoT.pack();
			
			table.add(e.infoT).margin(OFFSET).row();
		}
	}
	
	public static class BeaconCapturedEvent{
		public final BeaconBuild build;
		public final boolean add;
		
		public BeaconCapturedEvent(BeaconBuild build, boolean add){
			this.build = build;
			this.add = add;
		}
	}
}
