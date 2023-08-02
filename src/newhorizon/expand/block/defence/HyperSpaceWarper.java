package newhorizon.expand.block.defence;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntSeq;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.Stat;
import newhorizon.content.NHContent;
import newhorizon.expand.entities.Carrier;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.TableFunc;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.*;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class HyperSpaceWarper extends Block{
	private static Tile furthest;
	
	public float reloadTime = Time.toMinutes;
	public DrawBlock drawer = new DrawDefault();
	public Effect completeEffect = Fx.none;
	public float completeEffectChance = 0.075f;

	public Effect triggerEffect = new Effect(16, e -> {
		color(e.color);
		stroke(3f - e.fin() * 2f);
		Lines.square(e.x, e.y, tilesize / 2f * e.rotation + e.fin() * 5f);
	});
	
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
	public void load(){
		super.load();
		
		drawer.load(this);
	}
	
	
	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
		drawer.drawPlan(this, plan, list);
	}
	
	@Override
	public TextureRegion[] icons(){
		return drawer.finalIcons(this);
	}
	
	@Override
	public void getRegionsToOutline(Seq<TextureRegion> out){
		drawer.getRegionsToOutline(this, out);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		
		addBar("upgradeProgress",
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
			t.row().left();
			t.add("").row();
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
		
		public int target;
		public IntSeq selects = new IntSeq();
		
		public Vec2 targetV = new Vec2().set(this);
		public transient boolean isJammed = false;
		public transient Vec2 interceptedPos = new Vec2(x, y);
		public transient float totalProgress = 0;
		
		@Override
		public float warmup(){
			return reload / reloadTime;
		}
		
		@Override
		public float progress(){
			return reload / reloadTime;
		}
		
		@Override
		public float totalProgress(){
			return totalProgress;
		}
		
		@Override
		public Building init(Tile tile, Team team, boolean shouldAdd, int rotation){
			return super.init(tile, team, shouldAdd, rotation);
		}
		
		@Override
		public boolean onConfigureBuildTapped(Building other){
			if(this != other){
				return false;
			}else{
				return true;
			}
		}
		
		public boolean chargeValid(){return reload > reloadTime;}
		
		@Override
		public void updateTile(){
			if(efficiency() > 0){
				totalProgress += edelta();
				if(!chargeValid()){
					reload += efficiency() * delta();
				}else if(Mathf.chanceDelta(completeEffectChance)){
					completeEffect.at(x + Mathf.range(tilesize * size / 2), y + Mathf.range(tilesize * size / 2), team.color);
				}
			}
			
			if(efficiency() > 0 && chargeValid()){
				if(Mathf.equal(warmup, 1, 0.0015F))warmup = 1f;
				else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
			}else{
				if(Mathf.equal(warmup, 0, 0.0015F))warmup = 0f;
				else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
			}
		}
		
		@Override
		public void draw(){
			drawer.draw(this);
			
			Draw.z(Layer.bullet + 2f);
			Draw.color(team.color);
			
			TextureRegion arrowRegion = NHContent.arrowRegion;
			
			for (int l = 0; l < 4; l++) {
				float angle = 45 + 90 * l;
				float regSize = size / 12f;
				for (int i = 0; i < 4; i++) {
					float angle2 = angle + Mathf.sign(i % 2 == 0) * DrawFunc.rotator_90(DrawFunc.cycle(25 * i, 100), 0.05f * i + 0.01f);
					Tmp.v1.trns(angle2, (i - 4) * tilesize);
					float f = (100 - (Time.time - 25 * i) % 100) / 100;
					Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * regSize * f * warmup, arrowRegion.height * regSize * f * warmup, angle2 - 90);
				}
			}
			
			Drawf.light(tile, size * tilesize * 3 * warmup, team.color, 0.85f);
		}
		
		public boolean canTeleport(){
			return chargeValid() || cheating();
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
		public void buildConfiguration(Table table){
			table.table(p -> {
				p.update(() -> {
					control.input.commandMode = true;
				});
				p.table(Tex.paneSolid, t -> {
					t.button("@mod.ui.select-target", Icon.move, Styles.cleart, () -> {
						TableFunc.selectPos(table, this::configure);
					}).size(LEN * 4, LEN).row();
					
					t.button("@mod.ui.select-unit", Icon.filter, Styles.cleart, () -> {
						configure(Vars.control.input.selectedUnits.mapInt(Unit::id));
					}).size(LEN * 4, LEN).disabled(b -> control.input.selectedUnits.isEmpty()).row();
					
					t.button("@mod.ui.transport-unit", Icon.download, Styles.cleart, () -> {
						configure(Math.max(4, (int)Mathf.sqrt(selects.size / Mathf.pi) + 2));
					}).size(LEN * 4, LEN).disabled(b -> !canTeleport()).row();
					
					if(mobile)t.button("@back", Icon.leftOpen, Styles.cleart, () -> {
						deselect();
						control.input.inv.hide();
						p.update(() -> {});
						Core.app.post(() -> {
							control.input.commandMode = control.input.commandRect = false;
						});
					}).size(LEN * 4, LEN).row();
				}).fill();
				/*if(mobile)p.table(Tex.paneSolid, t -> {
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
				}).fill();*/
			}).fill().row();
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
					}else if(u instanceof WaterMovec){
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
			
			triggerEffect.at(x, y, team.color);
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
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			
			Seq<Unit> selectedUnits = control.input.selectedUnits;
			
			selectedUnits.removeAll(u -> !u.isCommandable());
			
			Draw.color(team.color);
			
			//draw command overlay UI
			for(Unit unit : selectedUnits){
				Drawf.square(unit.x, unit.y, unit.hitSize / 1.4f + 1f, DrawFunc.rotator_90() + 45);
			}
			
			if(!selects.isEmpty())for(int id : selects.items){
				Unit unit = Groups.unit.getByID(id);
				if(unit != null){
					Drawf.square(unit.x, unit.y, unit.hitSize / 3f, -DrawFunc.rotator_90() + 45);
					
					for(int i = 0; i < 4; i++){
						Tmp.v1.trns(DrawFunc.rotator_90() + 45 + i * 90, unit.hitSize / 1.9f);
						DrawFunc.arrow(NHContent.arrowRegion, unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, 1, Tmp.v1.angle() + 90, team.color.cpy().lerp(Color.white, 0.05f + Mathf.absin(4f, 0.1f)));
					}
				}
			}
			
			if(!selects.isEmpty()){
				onAveragePos(Tmp.v6);
				Drawf.square(Tmp.v6.x, Tmp.v6.y, tilesize * 1.5f, 45, team.color);
				DrawFunc.posSquareLink(team.color, 3f, tilesize / 2f, true, Tmp.v6.x, Tmp.v6.y, targetV.x, targetV.y);
				Drawf.arrow(Tmp.v6.x, Tmp.v6.y, targetV.x, targetV.y, tilesize * 2, tilesize, team.color);
				Draw.reset();
			}
			
			Drawf.square(targetV.x, targetV.y, tilesize * 1.75f, 45 + DrawFunc.rotator_90(), team.color);
			
			if(isJammed){
				DrawFunc.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, Pal.redderDust, true);
			}
		}
	}
	
}
