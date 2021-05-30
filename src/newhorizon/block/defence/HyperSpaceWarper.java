package newhorizon.block.defence;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.Scaled;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.Stat;
import newhorizon.block.special.JumpGate;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;
import newhorizon.feature.NHBaseEntity;
import newhorizon.func.DrawFuncs;
import newhorizon.func.NHFunc;
import newhorizon.func.TableFs;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.vars.NHVars;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.core.World.toTile;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;
import static newhorizon.vars.NHVars.allTeamSeq;

public class HyperSpaceWarper extends Block{
	public static final int classID = 127;
	
	static{
		EntityMapping.idMap[classID] = Carrier::new;
	}
	
	private static final Rect tmpRect = new Rect();
	private static Tile furthest;
	
	public float reloadTime = 1200f;
	
	public HyperSpaceWarper(String name){
		super(name);
		update = configurable = true;
		canOverdrive = true;
		solid = true;
		
		config(Point2.class, HyperSpaceWarperBuild::setTarget);
		config(IntSeq.class, HyperSpaceWarperBuild::setSelects);
		config(Integer.class, HyperSpaceWarperBuild::teleport);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		
		bars.add("upgradeProgress",
			(HyperSpaceWarperBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.lancerLaser,
				() -> entity.reload / reloadTime
			)
		);
	}
	
	@Override
	public void setStats(){
		super.setStats();
		stats.add(Stat.output, (t) -> {
			t.row().add("[gray]Legends:").left().pad(OFFSET).growX().height(LEN).row();
			t.image().size(LEN).color(Pal.lancerLaser).padTop(OFFSET);
			t.add("[lightgray]Friendly Force Field").fill().padLeft(OFFSET / 2).row();
			t.image().size(LEN).color(Pal.accent).padTop(OFFSET);
			t.add("[lightgray]Hostile & Friendly Mixed Force Field").fill().padLeft(OFFSET / 2).row();
			t.image().size(LEN).color(Pal.redderDust).padTop(OFFSET);
			t.add("[lightgray]Hostile Force Field").fill().padLeft(OFFSET / 2).row();
		});
	}
	
	public class HyperSpaceWarperBuild extends Building implements BeforeLoadc{
		public float reload;
		public float warmup;
		
		public int teamIndex;
		public int target;
		public IntSeq selects = new IntSeq();
		
		public transient boolean isTransport = false;
		
		public transient Vec2 targetV = new Vec2(), selectVFrom = new Vec2(), selectVTo = new Vec2();
		public transient boolean loaded = false;
		public transient boolean isJammed = false;
		public transient Vec2 interceptedPos = new Vec2(x, y);
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			NHVars.world.advancedLoad.add(this);
			return super.init(tile, team, shouldAdd, rotation);
		}
		
		@Override
		public void placed(){
			super.placed();
			beforeLoad();
		}
		
		public void setTarget(Point2 p){
			target = p.pack();
		}
		
		public void setSelects(IntSeq seq){
			selects = seq;
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			target = read.i();
			warmup = read.f();
			reload = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(target);
			write.f(warmup);
			write.f(reload);
		}
		
		@Override
		public void remove(){
			super.remove();
		}
		
		@Override
		public boolean onConfigureTileTapped(Building other){
			if(this != other){
				NHVars.ctrl.isSelecting = false;
				return false;
			}else{
				return true;
			}
		}
		
		public boolean chargeCons(){return reload > reloadTime;}
		
		@Override
		public void updateTile(){
			if(consValid()){
				if(!chargeCons()){
					reload += efficiency() * delta();
				}
			}
			
			if(efficiency() > 0 && chargeCons()){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
			
			if(!loaded)beforeLoad();
		}
		
		@Override
		public void draw(){
			super.draw();
			
			Draw.z(Layer.bullet + 2f);
			Draw.color(team.color);
			
			for (int l = 0; l < 4; l++) {
				float angle = 45 + 90 * l;
				float regSize = size / 12f;
				for (int i = 0; i < 4; i++) {
					Tmp.v1.trns(angle, (i - 4) * tilesize);
					float f = (100 - (Time.time - 25 * i) % 100) / 100;
					TextureRegion arrowRegion = ((JumpGate)NHBlocks.jumpGate).arrowRegion;
					Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * regSize * f * warmup, arrowRegion.height * regSize * f * warmup, angle - 90);
				}
			}
			
		}
		
		public boolean canTeleport(){
			return chargeCons() && consValid();
		}
		
		@Override
		public void buildConfiguration(Table table){
			super.buildConfiguration(table);
			table.table(p -> {
				p.table(Tex.paneSolid, t -> {
					t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> {
						TableFs.pointSelectTable(table, this::configure);
					}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting || isTransport).row();
					
					t.button("@mod.ui.select-unit", Icon.filter, Styles.cleart, () -> {
						NHVars.ctrl.isSelecting = true;
						
						NHVars.ctrl.pressDown = false;
						
						Table pTable = new Table(Tex.pane){{
							Rect r = selectedRect();
							
							update(() -> {
								if(Vars.state.isMenu())remove();
								else{
									Vec2 v = Core.camera.project(r.x + r.width / 2, r.y - OFFSET);
									setPosition(v.x, v.y, 0);
								}
								
								if(NHVars.ctrl.pressDown){
									touchable = Touchable.disabled;
								}else touchable = Touchable.enabled;
							});
							table(Tex.paneSolid, t -> {
								t.button(Icon.upOpen, Styles.clearFulli, () -> {
									configure(selectedUnit());
									remove();
									NHVars.ctrl.isSelecting = false;
								}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.pressDown);
							}).size(LEN * 4, LEN);
						}};
						
						Table floatTable = new Table(Tex.clear){{
							update(() -> {
								if(Vars.state.isMenu() || !NHVars.ctrl.isSelecting){
									selectVFrom.set(0, 0);
									selectVTo.set(0, 0);
									remove();
								}
							});
							touchable = Touchable.enabled;
							setFillParent(true);
							if(mobile){
								addListener(new InputListener(){
									public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
										if(!NHVars.ctrl.pressDown){
											selectVFrom.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
											selectVTo.set(selectVFrom);
										}
										else selectVTo.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
										NHVars.ctrl.pressDown = !NHVars.ctrl.pressDown;
										return false;
									}
								});
							}else addListener(new InputListener(){
								public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
									NHVars.ctrl.pressDown = !NHVars.ctrl.pressDown;
									if(NHVars.ctrl.pressDown)selectVFrom.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
									return false;
								}
								
								public boolean mouseMoved(InputEvent event, float x, float y){
									if(NHVars.ctrl.pressDown){
										selectVTo.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
										return false;
									}
									return true;
								}
							});
							
							Core.scene.root.addChildAt(Math.max(table.getZIndex() - 2, 0), pTable);
							Core.scene.root.addChildAt(Math.max(table.getZIndex() - 1, 0), this);
						}};
						
					}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting || isTransport).row();
					
					t.button("@mod.ui.transport-unit", Icon.download, Styles.cleart, () -> {
						configure(80);
					}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting || !canTeleport() || !targetValid(World.toTile(targetV.x), World.toTile(targetV.y))).row();
				}).fill();
				p.table(Tex.paneSolid, t -> {
					TextArea xArea = new TextArea("");
					TextArea yArea = new TextArea("");
					t.table(Tex.clear, t2 -> {
						t2.add("[accent]X: ").left();
						t2.add(xArea).left();
					}).size(LEN * 4, LEN).row();
					t.table(Tex.clear, t2 -> {
						t2.add("[accent]Y: ").left();
						t2.add(yArea).left();
					}).size(LEN * 4, LEN).row();
					t.button("@confirm", Icon.upOpen, Styles.cleart, () -> {
						try{
							int ix = Mathf.clamp((int)Float.parseFloat(xArea.getText()), 0, world.width());
							int iy = Mathf.clamp((int)Float.parseFloat(yArea.getText()), 0, world.height());
							configure(Tmp.p1.set(ix, iy));
							targetValid(ix, iy);
						}catch(NumberFormatException e){
							xArea.clear();
							yArea.clear();
							Vars.ui.showErrorMessage(e.toString());
						}
					}).size(LEN * 4, LEN);
				}).fill();
			}).fill().row();
			table.table(Tex.paneSolid, t -> {
				t.pane(p -> {
					int index = 0;
					for(Unit u : Groups.unit){
						if(u.team != team || !u.type.isCounted || selects.contains(u.id))continue;
						if(index % 6 == 0)p.row();
						p.button(new TextureRegionDrawable(u.type.icon(Cicon.full)), Styles.cleari, LEN, () -> {
							selects.add(u.id);
							configure(selects);
						}).size(LEN).update(b -> {
							if(selects.contains(u.id))b.remove();
						});
						index++;
					}
				}).grow().row();
			}).growX().height(LEN * 3f).row();
			table.table(Tex.paneSolid, t -> {
				t.button("@remove", Icon.cancel, Styles.cleart, () -> {
					selects.clear();
				}).disabled(b -> NHVars.ctrl.isSelecting || isTransport).growX().height(LEN);
			}).growX().height(LEN + OFFSET);
		}
		
		public void teleport(int spawnRange){
			Tmp.p1.set(Point2.unpack(target));
			if(selects.isEmpty() || world.tile(target) == null || !targetValid(Tmp.p1.x, Tmp.p1.y))return;
			long seed = (long)Groups.unit.size() + Groups.build.size() << 8;
			Seq<Tile> tileSeq = new Seq<>();
			Seq<Vec2> vectorSeq = new Seq<>();
			Seq<Unit> selectUnits = new Seq<>();
			Rand r = new Rand(seed);
			
			int grounds = 0, air = 0;
			
			for(int id : selects.items){
				Unit u = Groups.unit.getByID(id);
				if(u != null){
					if(!u.type.flying){grounds++;}
					else {air++;}
					selectUnits.add(u);
				}
			}
			
			tileSeq.addAll(NHFunc.getAcceptableTiles(Tmp.p1.x, Tmp.p1.y, toTile(spawnRange), tile -> !tile.floor().isDeep() && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes));
			randLenVectors(seed, selects.size, spawnRange, (sx, sy) -> vectorSeq.add(new Vec2(sx, sy).add(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize)));
			
			if(tileSeq.size < grounds - 1 || vectorSeq.size < air - 1){
				isJammed = true;
				return;
			}
			
			float angle = angleTo(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
			
			for(Unit u : selectUnits){
				Vec2 to = new Vec2();
				
				if(u.type.flying){
					to.set(vectorSeq.pop());
				}else{
					to.set(tileSeq.get(r.random(tileSeq.size - 1)));
				}
				
				Carrier c = Pools.obtain(Carrier.class, Carrier::new);
				c.init(u, to, angle);
				c.set(u);
				c.add();
			}
			
			isTransport = true;
			selects.clear();
			consume();
			reload = 0f;
		}
		
		public Vec2 onAveragePos(Vec2 vec2){
			if(selects.isEmpty())return vec2;
			float avgX = 0f, avgY = 0f;
			for(int id : selects.items){
				Unit u = Groups.unit.getByID(id);
				if(u == null)continue;
				avgX += u.x;
				avgY += u.y;
			}
			
			avgX /= selects.size;
			avgY /= selects.size;
			return vec2.set(avgX, avgY);
		}
		
		public boolean targetValid(int tx, int ty){
			onAveragePos(Tmp.v6);
			boolean valid = !Geometry.raycast(
				World.toTile(Tmp.v6.x),
				World.toTile(Tmp.v6.y),
				tx,
				ty,
				(x, y) -> {
					Tile t = world.tile(x, y);
					if(t == null)return false;
					IntSeq teams = NHVars.world.intercepted.get(t);
					boolean anyOther = false;
					for(int i = 0; i < teams.size; i++){
						if(i == teamIndex)continue;
						if(teams.items[i] > 0){
							interceptedPos.set(x * tilesize, y * tilesize);
							anyOther = true;
							break;
						}
					}
					return anyOther;
				}
			);
			if(valid)interceptedPos.set(tx * tilesize, ty * tilesize);
			return valid;
		}
		
		public Rect selectedRect(){
			return tmpRect.setSize(Math.abs(selectVTo.x - selectVFrom.x), Math.abs(selectVTo.y - selectVFrom.y)).setCenter((selectVFrom.x + selectVTo.x) / 2f, (selectVFrom.y + selectVTo.y) / 2f);
		}
		
		public IntSeq selectedUnit(){
			IntSeq units = new IntSeq();
			Groups.unit.each(unit -> unit != null && unit.team == team && unit.type.isCounted && selectedRect().contains(Tmp.r2.setSize(unit.hitSize).setCenter(unit.x, unit.y)), unit -> {
				units.add(unit.id);
			});
			return units;
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			
			NHVars.world.drawGully(teamIndex);
			
			if(Tmp.v5.set(selectVTo).sub(selectVFrom).len2() > 1){
				Draw.color(Pal.accent);
				Lines.stroke(1.75f);
				Lines.rect(selectedRect());
				
				Draw.alpha(0.35f);
				Fill.quad(
					selectVTo.x, selectVFrom.y,
					selectVFrom.x, selectVFrom.y,
					selectVFrom.x, selectVTo.y,
					selectVTo.x, selectVTo.y
				);
			}
			
			Draw.reset();
			if(NHVars.ctrl.pressDown && !selectedUnit().isEmpty()){
				for(int id : selectedUnit().items){
					Draw.mixcol(Pal.accent, 3);
					Unit u = Groups.unit.getByID(id);
					if(u == null)continue;
					Draw.rect(u.type.shadowRegion, u.x, u.y, u.rotation - 90);
				}
			}else if(!NHVars.ctrl.pressDown && !selects.isEmpty()){{
				for(int id : selects.items){
					Draw.mixcol(Pal.accent, 3);
					Unit u = Groups.unit.getByID(id);
					if(u == null)continue;
					Draw.rect(u.type.shadowRegion, u.x, u.y, u.rotation - 90);
				}
			}}
			
			if(!selects.isEmpty()){
				Draw.reset();
				boolean valid = targetValid(Tmp.p1.x, Tmp.p1.y);
				Color color = valid ? Pal.accent : Pal.redderDust;
				Drawf.square(interceptedPos.x, interceptedPos.y, tilesize * 1.5f, 45, color);
				onAveragePos(Tmp.v6);
				Drawf.square(Tmp.v6.x, Tmp.v6.y, tilesize * 1.5f, 45, color);
				DrawFuncs.posSquareLink(color, 3f, tilesize / 2f, true, Tmp.v6.x, Tmp.v6.y, interceptedPos.x, interceptedPos.y);
				Drawf.arrow(Tmp.v6.x, Tmp.v6.y, interceptedPos.x, interceptedPos.y, tilesize * 2, tilesize, color);
				Draw.reset();
			}
			
			if(isJammed){
				DrawFuncs.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, Pal.redderDust, true);
			}
		}
		
		@Override
		public void beforeLoad(){
			loaded = true;
			teamIndex = allTeamSeq.indexOf(team);
			selects.clear();
		}
	}
	
	public static class Carrier extends NHBaseEntity implements Teamc, Rotc, Scaled{
		protected static final float dstPerMove = tilesize * 2.5f;
		
		public float rotation = 0;
		public float finalRot = 0;
		public Unit unit;
		public UnitPayload toCarry;
		public Team team;
		public Vec2 to;
		public int teamIndex = -1;
		protected boolean dumped = false, onMove = false, contained, adjusted, intercepted = false, complete = false;
		protected float time = 0, lifetime = 540f;
		
		public void init(Unit unit, Vec2 to, float rotation){
			finalRot = rotation;
			size = unit.hitSize * 2f;
			this.unit = unit;
			this.to = to;
			team(unit.team());
			contained = toCarry != null;
			NHSounds.hyperspace.at(this);
		}
		
		@Override
		public void draw(){
			if(!onMove && unit != null && !unit.isNull()){
				float height = Mathf.curve(fslope() * fslope(), 0f, 0.3f) * 1.1f;
				float width = Mathf.curve(fslope() * fslope(), 0.35f, 0.75f) * 1.1f;
				
				if(contained && !Units.canCreate(team, unit.type)){
					Draw.z(Layer.bullet - 0.2f);
					Draw.color(team.color.cpy().mul(1.15f), Pal.gray, new Rand(id).random(-0.25f, 0.25f) / 4f);
					Draw.alpha(0.2f);
					Fill.rect(x, y, Draw.scl * unit.type.shadowRegion.height * width + 1f, Draw.scl * unit.type.shadowRegion.width * height, rotation);
					
					Draw.color(Pal.ammo);
					Draw.z(Layer.bullet - 0.1f);
					float size = this.size / 3;
					Draw.rect(Icon.warning.getRegion(), this.unit.x, this.unit.y, 16f, 16f);
					
					float sin = Mathf.absin(Time.time * DrawFuncs.sinScl, 8f, 2f);
					
					for(int i = 0; i < 4; i++){
						float length = size / 1.5f + sin;
						Tmp.v1.trns(i * 90, -length);
						Draw.rect(NHContent.pointerRegion,x + Tmp.v1.x, y + Tmp.v1.y, size, size, i * 90 - 90f);
					}
					Draw.reset();
				}else{
					Draw.z(Layer.effect);
					
					Draw.color(team.color.cpy().mul(1.15f), Pal.gray, new Rand(id).random(-0.25f, 0.25f) / 4f);
					Fill.rect(x, y, Draw.scl * unit.type.shadowRegion.height * width + 1f, Draw.scl * unit.type.shadowRegion.width * height, rotation);
				}
			}
		}
		
		protected void pickup(){
			unit.remove();
			toCarry = new UnitPayload(unit);
			Fx.unitPickup.at(unit);
			if (Vars.net.client()) {
				Vars.netClient.clearRemovedEntity(unit.id);
			}
		}
		
		protected boolean drop(){
			toCarry.set(x, y, rotation);
			return toCarry.dump();
		}
		
		@Override
		public void update(){
			boolean onGoing = true;
			
			if(!contained && unit != null && unit.isValid() && !complete){
				set(unit);
				rotation(unit.rotation);
			}
			if(time > lifetime / 2 && !adjusted){
				if(!contained && unit != null && unit.isValid()){
					pickup();
					contained = true;
					adjusted = true;
				}else if(toCarry != null && !dumped){
					onGoing = dumped = drop();
					contained = !dumped;
				}
			}
			
			if(dumped){
				complete = true;
				adjusted = true;
			}
			
			if(time > lifetime){
				if(!contained)remove();
				else if(!onMove)onMove = true;
			}
			
			
			if(!onMove && onGoing)time += Time.delta;
			
			if(onMove && contained && Mathf.equal(x, to.getX(), 1f) && Mathf.equal(y, to.getY(), 1f)){
				time = 0;
				onMove = false;
				adjusted = false;
				rotation = finalRot;
				NHSounds.hyperspace.at(this);
			}
			
			if(onMove && contained){
				trns(Tmp.v1.set(to).sub(x, y).nor().scl(dstPerMove * Time.delta).clamp(to.getX() - x, to.getY() - y, 100, 100));
				if(NHVars.world.intercepted.get(world.tile(World.toTile(x), World.toTile(y))).count(0) < Team.all.length){
					time = 0;
					onMove = false;
					adjusted = false;
					toCarry.unit.health(toCarry.unit.health() * 0.75f);
					onMove = false;
					rotation = finalRot;
					NHSounds.hyperspace.at(this);
				}
			}
		}
		
		@Override
		public int classId(){
			return classID;
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.bool(contained);
			write.bool(complete);
			TypeIO.writeVec2(write, to);
			write.f(time);
			write.f(rotation);
			write.f(finalRot);
			write.bool(onMove);
			write.bool(adjusted);
			write.bool(dumped);
			
			if(contained)TypeIO.writePayload(write, toCarry);
		}
		
		@Override
		public void read(Reads read){
			super.read(read);
			contained = read.bool();
			complete = read.bool();
			to = TypeIO.readVec2(read);
			time = read.f();
			rotation = read.f();
			finalRot = read.f();
			onMove = read.bool();
			adjusted = read.bool();
			dumped = read.bool();
			
			if(contained)toCarry = (UnitPayload)TypeIO.readPayload(read);
			else remove();
			
			afterRead();
		}
		
		@Override
		public void afterRead(){
			if(contained && toCarry != null && toCarry.unit != null && !toCarry.unit.isNull()){
				init(toCarry.unit, to, finalRot);
				add();
			}else{
				remove();
			}
		}
		
		@Override
		public boolean cheating(){
			return toCarry != null && toCarry.unit.cheating();
		}
		
		@Override
		public Building core(){
			return team.core();
		}
		
		@Override
		public Building closestCore(){
			return state.teams.closestCore(x, y, team);
		}
		
		@Override
		public Building closestEnemyCore(){
			return state.teams.closestEnemyCore(x, y, team);
		}
		
		@Override
		public Team team(){
			return team;
		}
		
		@Override
		public void team(Team team){
			this.team = team;
			teamIndex = NHFunc.getTeamIndex(team);
		}
		
		@Override
		public float rotation(){
			return rotation;
		}
		
		@Override
		public void rotation(float rotation){
			this.rotation = rotation;
		}
		
		@Override
		public float fin(){
			return Math.min(time, lifetime) / lifetime;
		}
		
		@Override
		public String toString(){
			return "Carrier{" + "rotation=" + rotation + ", unit=" + unit + ", toCarry=" + toCarry + ", team=" + team + ", to=" + to + ", teamIndex=" + teamIndex + ", intercepted=" + intercepted + ", complete=" + complete + ", onMove=" + onMove + ", contained=" + contained + ", adjusted=" + adjusted + ", time=" + time + ", lifetime=" + lifetime + '}';
		}
	}
}
