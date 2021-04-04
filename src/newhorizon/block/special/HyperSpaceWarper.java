package newhorizon.block.special;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.func.DrawFuncs;
import newhorizon.func.Functions;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.vars.NHCtrlVars;
import newhorizon.vars.NHWorldVars;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static mindustry.core.World.toTile;
import static newhorizon.func.TableFs.LEN;
import static newhorizon.func.TableFs.OFFSET;
import static newhorizon.vars.NHVars.allTeamSeq;

public class HyperSpaceWarper extends Block{
	private static final Rect tmpRect = new Rect();
	private static Tile furthest;
	
	public float reloadTime = 1200f;
	
	public HyperSpaceWarper(String name){
		super(name);
		update = configurable = true;
		canOverdrive = false;
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
		
		public transient Vec2 targetV = new Vec2(), selectVFrom = new Vec2(), selectVTo = new Vec2();
		public transient boolean loaded = false;
		public transient boolean isSelect = false, isJammed = false;
		public transient Vec2 interceptedPos = new Vec2(x, y);
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			NHWorldVars.advancedLoad.add(this);
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
				isSelect = false;
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
					reload += efficiency() * Time.delta;
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
			table.table(Tex.paneSolid, t -> {
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> {
					isSelect = true;
					
					Table floatTable = new Table(Tex.clear){{
						update(() -> {
							if(Vars.state.isMenu())remove();
						});
						touchable = Touchable.enabled;
						setFillParent(true);
						
						addListener(new InputListener(){
							public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
								targetV.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
								return true;
							}
						});
					}};
					
					Table pTable = new Table(Tex.clear){{
						update(() -> {
							if(Vars.state.isMenu()){
								remove();
							}else{
								Vec2 v = Core.camera.project(World.toTile(targetV.x) * tilesize, World.toTile(targetV.y) * tilesize);
								setPosition(v.x, v.y, 0);
							}
						});
						button(Icon.cancel, Styles.emptyi, () -> {
							configure(Tmp.p1.set(World.toTile(targetV.x), World.toTile(targetV.y)));
							remove();
							floatTable.remove();
							isSelect = isJammed = false;
						}).center();
					}};
					
					Core.scene.root.addChildAt(Math.max(table.getZIndex() - 1, 0), pTable);
					Core.scene.root.addChildAt(Math.max(table.getZIndex() - 2, 0), floatTable);
				}).size(LEN * 4, LEN).disabled(b -> isSelect).row();
				
				t.button("@mod.ui.select-unit", Icon.filter, Styles.cleart, () -> {
					isSelect = true;
					
					Table pTable = new Table(Tex.pane){{
						Rect r = selectedRect();
						
						update(() -> {
							if(Vars.state.isMenu())remove();
							else{
								Vec2 v = Core.camera.project(r.x + r.width / 2, r.y - OFFSET);
								setPosition(v.x, v.y, 0);
							}
							
							if(NHCtrlVars.pressDown){
								touchable = Touchable.disabled;
							}else touchable = Touchable.enabled;
						});
						table(Tex.paneSolid, t -> {
							t.button(Icon.upOpen, Styles.clearFulli, () -> {
								configure(selectedUnit());
								remove();
								isSelect = false;
							}).size(LEN * 4, LEN).disabled(b -> NHCtrlVars.pressDown);
						}).size(LEN * 4, LEN);
					}};
					
					Table floatTable = new Table(Tex.clear){{
						update(() -> {
							if(Vars.state.isMenu() || !isSelect){
								selectVFrom.set(0, 0);
								selectVTo.set(0, 0);
								remove();
							}
						});
						touchable = Touchable.enabled;
						setFillParent(true);
						
						addListener(new InputListener(){
							public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
								NHCtrlVars.pressDown = !NHCtrlVars.pressDown;
								if(NHCtrlVars.pressDown)selectVFrom.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
								return false;
							}
							
							public boolean mouseMoved(InputEvent event, float x, float y){
								if(NHCtrlVars.pressDown){
									selectVTo.set(Core.camera.unproject(x, y)).clamp(0, 0, world.unitHeight(), world.unitWidth());
									return false;
								}
								return true;
							}
						});
						
						Core.scene.root.addChildAt(Math.max(table.getZIndex() - 2, 0), pTable);
						Core.scene.root.addChildAt(Math.max(table.getZIndex() - 1, 0), this);
					}};
					
				}).size(LEN * 4, LEN).disabled(b -> isSelect).row();
				
				t.button("@mod.ui.transport-unit", Icon.download, Styles.cleart, () -> {
					configure(80);
				}).size(LEN * 4, LEN).disabled(b -> isSelect || !canTeleport() || !targetValid(World.toTile(targetV.x), World.toTile(targetV.y))).row();
			}).fill();
			table.table(Tex.paneSolid, t -> {
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
		}
		
		public void teleport(int spawnRange){
			Tmp.p1.set(Point2.unpack(target));
			if(selects.isEmpty() || world.tile(target) == null || !targetValid(Tmp.p1.x, Tmp.p1.y))return;
			final long seed = (long)Groups.unit.size() + Groups.build.size() << 8;
			final Seq<Tile> tileSeq = new Seq<>();
			final Seq<Vec2> vectorSeq = new Seq<>();
			final Seq<Unit> selectUnits = new Seq<>();
			Rand r = new Rand(seed);
			
			int grounds = 0;
			
			for(int id : selects.items){
				Unit u = Groups.unit.getByID(id);
				if(u != null){
					if(!u.type.flying)grounds++;
					selectUnits.add(u);
				}
			}
			
			tileSeq.addAll(Functions.getAcceptableTiles(Tmp.p1.x, Tmp.p1.y, toTile(spawnRange), tile -> !tile.floor().isDeep() && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes));
			
			if(tileSeq.size < grounds - 1){
				isJammed = true;
				return;
			}
			
			randLenVectors(seed, selects.size, spawnRange, (sx, sy) -> vectorSeq.add(new Vec2(sx, sy).add(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize)));
			
			float angle = angleTo(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
			
			for(Unit u : selectUnits){
				NHSounds.hyperspace.at(u);
				NHFx.hyperSpaceEntrance.at(u.x, u.y, u.rotation, Pal.place, u);
				
				if(u.type.flying){
					Vec2 to = vectorSeq.pop();
					transport(u, to.x, to.y, angle);
				}else{
					Tile to = tileSeq.get(r.random(tileSeq.size - 1));
					transport(u, to.x * tilesize, to.y * tilesize, angle);
				}
			}
			
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
					IntSeq teams = NHWorldVars.intercepted.get(t);
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
		
		public void transport(Unit u, float tx, float ty, float angle){
			Unit unit = u.type.create(team);
			unit.set(tx, ty);
			unit.rotation = angle;
			u.remove();
			Time.run(NHFx.hyperSpaceEntrance.lifetime, () -> {
				NHSounds.hyperspace.at(tx, ty);
				NHFx.hyperSpaceEntrance.at(tx, ty, angle, Pal.accent, unit);
				Time.run(NHFx.hyperSpaceEntrance.lifetime, () -> {
					if(!Vars.net.client())unit.add();
					selects.add(unit.id);
				});
			});
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
			
			NHWorldVars.drawGully(teamIndex);
			
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
			if(NHCtrlVars.pressDown && !selectedUnit().isEmpty()){
				for(int id : selectedUnit().items){
					Draw.mixcol(Pal.accent, 3);
					Unit u = Groups.unit.getByID(id);
					if(u == null)continue;
					Draw.rect(u.type.shadowRegion, u.x, u.y, u.rotation - 90);
				}
			}else if(!NHCtrlVars.pressDown && !selects.isEmpty()){{
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
}
