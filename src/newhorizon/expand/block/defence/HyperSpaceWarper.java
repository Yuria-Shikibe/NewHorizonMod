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
import arc.math.Scaled;
import arc.math.geom.Intersector;
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
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.io.TypeIO;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.Stat;
import newhorizon.content.NHContent;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.vars.NHVars;
import newhorizon.util.feature.NHBaseEntity;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.func.EntityRegister;
import newhorizon.util.func.NHFunc;
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
	
	public static class Carrier extends NHBaseEntity implements Teamc, Rotc, Scaled{
		protected static final float dstPerMove = tilesize * 1.5f;
		
		public float rotation = 0;
		public float finalRot = 0;
		public Unit unit;
		public UnitPayload toCarry;
		public Team team;
		public Vec2 to;
		
		public transient long lastUpdated, updateSpacing;
		
		public transient float x_LAST_, x_TARGET_, y_LAST_, y_TARGET_;
		
		public transient Vec2 vel = new Vec2();
		
		public Trail trail = new Trail(1);
		
		protected boolean dumped = false, onMove = false, contained, adjusted, intercepted = false, complete = false;
		protected float time = 0, lifetime = 540f,surviveTime = 0, surviveLifetime = 6000;
		
		protected transient boolean onGoing = true;
		
		public static void create(Unit unit, Vec2 to, float rot){
			Carrier c = Pools.obtain(Carrier.class, Carrier::new);
			c.init(unit, to, rot);
			c.set(unit);
			c.add();
		}
		
		public static void create(Unit unit, Vec2 to){
			Carrier c = Pools.obtain(Carrier.class, Carrier::new);
			c.init(unit, to, unit.angleTo(to));
			c.set(unit);
			c.add();
		}
		
		public void init(Unit unit, Vec2 to, float rotation){
			finalRot = rotation;
			size = unit.hitSize * 2f;
			this.unit = unit;
			this.to = to;
			team(unit.team());
			contained = toCarry != null;
			trail = new Trail(60);
		}
		
		@Override
		public void add(){
			super.add();
			NHSounds.hyperspace.at(this);
		}
		
		@Override
		public void draw(){
			Draw.z(Layer.effect);
			if((!complete && time > lifetime / 2) || onMove || (contained && time < lifetime / 2) && onGoing){
				trail.draw(team.color, 4f);
			}
			
			if(!onMove && team != null)Drawf.light(team, this, size * fslope(), team.color, 0.8f);
			if(!onMove && unit != null && !unit.isNull()){
				float height = Mathf.curve(fslope() * fslope(), 0f, 0.3f) * 1.1f;
				float width = Mathf.curve(fslope() * fslope(), 0.35f, 0.75f) * 1.1f;
				
				if((contained && !Units.canCreate(team, unit.type)) || (surviveTime > 0)){
					Draw.z(Layer.bullet - 0.2f);
					Draw.color(team.color.cpy().mul(1.15f), Pal.gray, new Rand(id).random(-0.25f, 0.25f) / 4f);
					Draw.alpha(0.2f);
					Fill.rect(x, y, Draw.scl * unit.type.shadowRegion.height * width + 1f, Draw.scl * unit.type.shadowRegion.width * height, rotation);
					
					Draw.color(Pal.ammo);
					Draw.z(Layer.bullet - 0.1f);
					float size = this.size / 3;
					float sin = Mathf.absin(Time.time * DrawFunc.sinScl, 8f, 2f);
					float length = size / 1.5f + sin;
					
					Draw.rect(Icon.warning.getRegion(), x, y, size / 1.5f, size / 1.5f);
					
					Draw.alpha(surviveTime / surviveLifetime);
					for(int i = 0; i < 4; i++){
						
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
			if(Vars.net.client()){
				Vars.netClient.clearRemovedEntity(unit.id);
			}
		}
		
		protected boolean drop(){
			toCarry.set(x, y, rotation);
			return toCarry.dump();
		}
		
		@Override
		public void update(){
			onGoing = true;
			
			if(!Vars.headless)trail.update(x, y, 1);
			
			if(!contained && unit != null && unit.isValid() && !complete){
				set(unit);
				rotation(unit.rotation);
			}
			
			if(time > lifetime / 2 && !adjusted){
				if(!contained && unit != null && unit.isValid()){
					contained = true;
					adjusted = true;
					pickup();
				}else if(toCarry != null && !dumped){
					onGoing = dumped = drop();
					contained = !dumped;
					if(surviveTime > surviveLifetime){
						if(!net.client()){
							toCarry.set(x, y, rotation);
							toCarry.unit.add();
						}
						dumped = true;
						contained = false;
					}
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
			else if(!onMove)surviveTime += Time.delta;
			
			if(onMove && contained && (intercepted || (Mathf.equal(x, to.getX(), 12f) && Mathf.equal(y, to.getY(), 12f)))){
				if(!intercepted)set(to);
				time = 0;
				onMove = false;
				adjusted = false;
				rotation = finalRot;
				NHSounds.hyperspace.at(this);
			}
			
			if(onMove && contained){
				if(!headless)trail.update(x, y);
				
				vel.set(to).sub(x, y).nor().scl(dstPerMove * Time.delta);
				
				x += vel.x;
				y += vel.y;
				
				NHVars.world.gravityTraps.intersect(x - 4, y - 4, tilesize, tilesize, b -> {
					if(b.team() != team && b.active() && Intersector.isInsideHexagon(x, y, b.range * 2f, b.getX(), b.getY())){
						intercepted = true;
						toCarry.unit.damage(toCarry.unit.health * 0.3f);
						PosLightning.createEffect(b, this, b.team().color, 2, PosLightning.WIDTH);
						NHFx.square45_4_45.at(x, y, team.color);
					}
				});
			}
		}
		
		@Override
		public int classId(){
			return EntityRegister.getID(getClass());
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.bool(contained);
			write.bool(complete);
			TypeIO.writeVec2(write, to);
			write.f(time);
			write.f(surviveTime);
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
			surviveTime = read.f();
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
		public CoreBlock.CoreBuild core(){
			return team.core();
		}
		
		@Override
		public CoreBlock.CoreBuild closestCore(){
			return state.teams.closestCore(x, y, team);
		}
		
		@Override
		public CoreBlock.CoreBuild closestEnemyCore(){
			return state.teams.closestEnemyCore(x, y, team);
		}
		
		@Override
		public Team team(){
			return team;
		}
		
		@Override
		public void team(Team team){
			this.team = team;
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
			return "Carrier{" + "rotation=" + rotation + ", unit=" + unit + ", toCarry=" + toCarry + ", team=" + team + ", to=" + to + ", intercepted=" + intercepted + ", complete=" + complete + ", onMove=" + onMove + ", contained=" + contained + ", adjusted=" + adjusted + ", time=" + time + ", lifetime=" + lifetime + '}';
		}
	}
}
