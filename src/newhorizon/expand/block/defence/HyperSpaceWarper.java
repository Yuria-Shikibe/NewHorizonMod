package newhorizon.expand.block.defence;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.World;
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
import mindustry.world.meta.Stat;
import newhorizon.content.NHContent;
import newhorizon.expand.entities.Carrier;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.TableFunc;

import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class HyperSpaceWarper extends Block{
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
			t.table(i -> {
				i.row().add("[gray]Legends:").left().pad(OFFSET).growX().height(LEN).row();
			}).growX().fillY().row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.lancerLaser).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-friendly")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
			t.table(i -> {
				i.image().size(LEN).color(Pal.redderDust).left();
				i.add(Core.bundle.get("mod.ui.gravity-trap-field-hostile")).growX().padLeft(OFFSET / 2).row();
			}).padTop(OFFSET).growX().fillY().row();
		});
	}
	
	public class HyperSpaceWarperBuild extends Building{
		public float reload;
		public float warmup;
		
		public int teamIndex;
		public int target;
		public IntSeq selects = new IntSeq();
		
		public Vec2 targetV = new Vec2().set(this);
		public transient boolean isJammed = false;
		public transient Vec2 interceptedPos = new Vec2(x, y);
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			return super.init(tile, team, shouldAdd, rotation);
		}
		
		@Override
		public void placed(){
			super.placed();
		}
		
		public void setTarget(Point2 p){
			target = p.pack();
			targetV.set(World.unconv(p.x), World.unconv(p.y));
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
			targetV = TypeIO.readVec2(read);
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(target);
			write.f(warmup);
			write.f(reload);
			TypeIO.writeVec2(write, targetV);
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
		}
		
		@Override
		public void draw(){
			super.draw();
			
			Draw.z(Layer.bullet + 2f);
			Draw.color(team.color);
			
			TextureRegion arrowRegion = NHContent.arrowRegion;
			
			for (int l = 0; l < 4; l++) {
				float angle = 45 + 90 * l;
				float regSize = size / 12f;
				for (int i = 0; i < 4; i++) {
					Tmp.v1.trns(angle, (i - 4) * tilesize);
					float f = (100 - (Time.time - 25 * i) % 100) / 100;
					Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * regSize * f * warmup, arrowRegion.height * regSize * f * warmup, angle - 90);
				}
			}
			
			Drawf.light(team, tile, size * tilesize * 3 * warmup, team.color, 0.85f);
		}
		
		public boolean canTeleport(){
			return chargeCons() && consValid();
		}
		
		@Override
		public void buildConfiguration(Table table){
			table.table(p -> {
				p.table(Tex.paneSolid, t -> {
					t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> {
						TableFunc.pointSelectTable(table, this::configure);
					}).size(LEN * 4, LEN).row();
					
					t.button("@mod.ui.select-unit", Icon.filter, Styles.cleart, () -> {
						TableFunc.rectSelectTable(table, () -> configure(selectedUnit()));
					}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting).row();
					
					t.button("@mod.ui.transport-unit", Icon.download, Styles.cleart, () -> {
						configure(Math.max(4, (int)Mathf.sqrt(selects.size / Mathf.pi) + 2));
					}).size(LEN * 4, LEN).disabled(b -> NHVars.ctrl.isSelecting || !canTeleport()).row();
				}).fill();
				if(mobile)p.table(Tex.paneSolid, t -> {
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
						}catch(NumberFormatException e){
							xArea.clear();
							yArea.clear();
							Vars.ui.showErrorMessage(e.toString());
						}
					}).size(LEN * 4, LEN);
				}).fill();
			}).fill().row();
			
			if(!mobile)return;
			
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
				}).disabled(b -> NHVars.ctrl.isSelecting).growX().height(LEN);
			}).growX().height(LEN + OFFSET);
		}
		
		public void teleport(int spawnRange){
			Tmp.p1.set(Point2.unpack(target));
			if(selects.isEmpty() || world.tile(target) == null)return;
			
			Rand rand = NHFunc.rand;
			rand.setSeed(core().items.total());
			
			ObjectMap<Unit, Vec2> spawnPos = new ObjectMap<>(selects.size + 1);

			Seq<Tile> air = new Seq<>(), ground = new Seq<>(), navy = new Seq<>();
			
			Seq<Boolf<Tile>> request = NHFunc.formats();
			
			air.addAll(NHFunc.getAcceptableTiles(Tmp.p1.x, Tmp.p1.y, spawnRange, request.get(0)));
			navy.addAll(NHFunc.getAcceptableTiles(Tmp.p1.x, Tmp.p1.y, spawnRange, request.get(1)));
			ground.addAll(NHFunc.getAcceptableTiles(Tmp.p1.x, Tmp.p1.y, spawnRange, request.get(2)));
			
			isJammed = false;
			
			for(int id : selects.items){
				Unit u = Groups.unit.getByID(id);
				if(u != null){
					if(u.type.flying){
						if(air.isEmpty()){
							isJammed = true;
							return;
						}else spawnPos.put(u, new Vec2().set(air.remove(rand.nextInt(air.size))));
					}else if(WaterMovec.class.isAssignableFrom(u.type.constructor.get().getClass())){
						if(navy.isEmpty()){
							isJammed = true;
							return;
						}else spawnPos.put(u, new Vec2().set(navy.remove(rand.nextInt(navy.size))));
					}else{
						if(ground.isEmpty()){
							isJammed = true;
							return;
						}else spawnPos.put(u, new Vec2().set(ground.remove(rand.nextInt(ground.size))));
					}
				}
			}
			
			float angle = angleTo(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
			
			for(Unit u : spawnPos.keys()){
				Carrier c = Pools.obtain(Carrier.class, Carrier::new);
				c.init(u, spawnPos.get(u), angle);
				c.set(u);
				c.add();
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
		
		public IntSeq selectedUnit(){
			IntSeq units = new IntSeq();
			Groups.unit.each(unit -> unit != null && unit.team == team && unit.type.isCounted && NHVars.ctrl.rect.contains(Tmp.r2.setSize(unit.hitSize).setCenter(unit.x, unit.y)), unit -> {
				units.add(unit.id);
			});
			return units;
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			
			if(NHVars.ctrl.isSelecting){
				Draw.color(Pal.accent);
				Lines.stroke(1.75f);
				Lines.rect(NHVars.ctrl.rect);
				
				Draw.alpha(0.35f);
				Fill.quad(
					NHVars.ctrl.to.x, NHVars.ctrl.from.y,
					NHVars.ctrl.from.x, NHVars.ctrl.from.y,
					NHVars.ctrl.from.x, NHVars.ctrl.to.y,
					NHVars.ctrl.to.x, NHVars.ctrl.to.y
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
			
			Draw.reset();
			Color color = Pal.accent;
			Drawf.square(targetV.x, targetV.y, tilesize * 1.5f, 45, color);
			
			if(!selects.isEmpty()){
				onAveragePos(Tmp.v6);
				Drawf.square(Tmp.v6.x, Tmp.v6.y, tilesize * 1.5f, 45, color);
				DrawFunc.posSquareLink(color, 3f, tilesize / 2f, true, Tmp.v6.x, Tmp.v6.y, targetV.x, targetV.y);
				Drawf.arrow(Tmp.v6.x, Tmp.v6.y, targetV.x, targetV.y, tilesize * 2, tilesize, color);
				Draw.reset();
			}
			
			if(isJammed){
				DrawFunc.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, Pal.redderDust, true);
			}
		}
	}
	
}
