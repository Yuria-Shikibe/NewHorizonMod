package newhorizon.block.defence;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.UI;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.BlockStatus;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.block.special.CommandableBlock;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.effects.EffectTrail;
import newhorizon.func.DrawFuncs;
import newhorizon.func.TableFs;
import newhorizon.vars.NHCtrlVars;
import newhorizon.vars.NHVars;
import newhorizon.vars.NHWorldVars;
import org.jetbrains.annotations.NotNull;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.func.TableFs.LEN;

public class BombLauncher extends CommandableBlock{
	public TextureRegion bombRegion;
	public TextureRegion[] gunBarrelRegion = new TextureRegion[4];
	
	public Effect
		hitEffect = NHFx.boolSelector,
		shootEffect = NHFx.boolSelector,
		smokeEffect,
		trailEffect;
	
	public Color baseColor = Pal.redderDust;
	
	public int storage = 4;
	public float prepareDelay = 30f;
	public float reloadTime = 240f;
	public float bombLifetime = 120f;
	public float shake = 20f;
	public float range = 800f;
	public float spread = 100f;
	public float bombDamage = 600f, bombRadius = 120f;
	public float bombVelPerTile = 3f;
	public Sound hitSound = Sounds.explosionbig;
	
	public BombLauncher(String name){
		super(name);
		smokeEffect = NHFx.hugeSmoke;
		trailEffect = NHFx.trail;
		
		config(Point2.class, BombLauncherBuild::setTarget);
		config(Integer.class, BombLauncherBuild::commandAll);
	}
	
	@Override
	public void drawPlace(int x, int y, int rotation, boolean valid){
		super.drawPlace(x, y, rotation, valid);
		Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);
	}
	
	@Override
	public void setStats() {
		super.setStats();
		stats.add(Stat.range, range / tilesize, StatUnit.blocks);
	}
	
	@Override
	public void init(){
		super.init();
		if(hitEffect == NHFx.boolSelector)hitEffect = NHFx.lightningHitLarge(baseColor);
		if(shootEffect == NHFx.boolSelector)shootEffect = NHFx.square(baseColor, 50f, 6, size * tilesize, size / 1.5f);
	}
	
	@Override
	public void load(){
		super.load();
		bombRegion = Core.atlas.find(name + "-bomb", Core.atlas.find("launchpod"));
	}
	
	@Override
	public void setBars() {
		super.setBars();
		bars.add("progress",
			(BombLauncherBuild entity) -> new Bar(
				() -> Core.bundle.get("bar.progress"),
				() -> Pal.power,
				() -> (entity.reload % reloadTime) / reloadTime
			)
		);
		bars.add("storage",
			(BombLauncherBuild entity) -> new Bar(
				() -> Core.bundle.format("bar.capacity", UI.formatAmount(entity.storaged())),
				() -> Pal.ammo,
				() -> entity.reload / reloadTime * storage
			)
		);
	}
	
	public class BombLauncherBuild extends CommandableBlockBuild{
		public transient int lastTarget = -1;
		public int target;
		public float reload;
		public float countBack = prepareDelay;
		public boolean preparing = false;
		
		@Override
		@NotNull
		public CommandableBlockType getType(){
			return CommandableBlockType.attacker;
		}
		
		public void setTarget(Point2 p){
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build != null && build.getType() == CommandableBlockType.attacker){
					build.overlap();
				}
			}
			NHWorldVars.commandPos = target = p.pack();
		}
		
		@Override
		public boolean isCharging(){return consValid() && reload < reloadTime * storage;}
		
		@Override
		public boolean isPreparing(){
			return preparing && countBack > 0;
		}
		
		@Override
		public void setPreparing(){
			preparing = true;
			countBack = prepareDelay;
		}
		
		public int storaged(){return (int)(reload / reloadTime);}
		
		@Override
		public void draw(){
			super.draw();
			Draw.draw(Draw.z(), () -> Drawf.construct(x, y, bombRegion, baseColor, 0, (prepareDelay - countBack) / prepareDelay, efficiency(), countBack * 2f));
		}
		
		@Override
		public void updateTile(){
			if(reload < reloadTime * storage && consValid()){
				reload += efficiency() * Time.delta;
			}
			
			if(isPreparing()){
				countBack -= Time.delta * efficiency();
			}else if(preparing){
				countBack = prepareDelay;
				preparing = false;
				shoot(lastTarget);
			}
		}
		
		@Override
		public BlockStatus status(){
			return canCommand() ? BlockStatus.active : isCharging() ? BlockStatus.noOutput : BlockStatus.noInput;
		}
		
		@Override
		public boolean canCommand(){
			Tile tile = world.tile(NHWorldVars.commandPos);
			return tile != null && consValid() && storaged() > 0 && NHWorldVars.commandPos > 0 && within(tile, range);
		}
		
		@Override
		public boolean overlap(){
			target = -1;
			return false;
		}
		
		@Override
		public void read(Reads read, byte revision){
			super.read(read, revision);
			target = read.i();
			reload = read.f();
			preparing = read.bool();
			countBack = read.f();
		}
		
		@Override
		public void write(Writes write){
			super.write(write);
			write.i(target);
			write.f(reload);
			write.bool(preparing);
			write.f(countBack);
		}
		
		@Override
		public void drawConfigure(){
			super.drawConfigure();
			Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
			
			Seq<CommandableBlockBuild> builds = new Seq<>();
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build != this && build != null && build.team == team && build.getType() == CommandableBlockType.attacker && build.canCommand()){
					builds.add(build);
					DrawFuncs.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				}
			}
			
			for(CommandableBlockBuild build : builds){
				DrawFuncs.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
			}
			
			if(target > 0){
				Tmp.p1.set(Point2.unpack(target));
				DrawFuncs.posSquareLink(Pal.accent, 1, 2, true, x, y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				DrawFuncs.drawConnected(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), 10f, Pal.accent);
			}else if(NHWorldVars.commandPos > 0){
				Tmp.p1.set(Point2.unpack(NHWorldVars.commandPos));
				DrawFuncs.posSquareLink(Pal.place, 1, 2, true, x, y, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y));
				DrawFuncs.drawConnected(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), 10f, Pal.place);
			}
			
			if(isValid())builds.add(this);
			for(CommandableBlockBuild build : builds){
				float time = (build.dst(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y)) / tilesize * bombVelPerTile + bombLifetime * (1 + 2/3f)) / Time.toSeconds;
				DrawFuncs.overlayText("Delay: " + TableFs.format(time) + " Sec.", build.x, build.y, build.block.size * tilesize / 2f, time > 4.5f ? Pal.accent : Pal.lancerLaser, true);
			}
			
			DrawFuncs.overlayText("Participants: " + builds.size, World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), tilesize * 2f, Pal.accent, true);
		}
		
		public void commandAll(Integer pos){
			Tmp.p1.set(Point2.unpack(pos));
			Vars.ui.announce(Iconc.warning  + " Caution: Raid " +  Tmp.p1.x + ", " + Tmp.p1.y, 4f);
			NHFx.attackWarning.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), spread, team.color);
			NHFx.spawn.at(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y), spread, team.color);
			for(Player p : Groups.player){
				NHSounds.alarm.at(p);
			}
			for(CommandableBlockBuild build : NHWorldVars.commandables){
				if(build.team == team && build.getType() == CommandableBlockType.attacker && build.canCommand()){
					build.triggered(pos);
				}
			}
		}
		
		@Override
		public void triggered(Integer pos){
			if(isPreparing())return;
			setPreparing();
			lastTarget = pos;
		}
		
		public void shoot(Integer pos){
			Tile target = Vars.world.tile(pos);
			if(target == null || !within(target, range) || !consValid())return;
			reload = Math.max(0, reload - reloadTime);
			consume();
			Effect.shake(shake / 2, shake, this);
			shootEffect.at(this);
			smokeEffect.at(this);
			Rand rand = new Rand((long)Groups.all.size() << 8);
			BombEntity bomb = Pools.obtain(BombEntity.class, BombEntity::new);
			bomb.init(team, bombLifetime, this, target.drawx() + rand.range(spread), target.drawy() + rand.range(spread), true).setDamage(bombDamage, bombRadius);
			if(!Vars.net.client())bomb.add();
		}
		
		@Override
		public void buildConfiguration(Table table){
			super.buildConfiguration(table);
			table.table(Tex.paneSolid, t -> {
				t.button(Icon.upOpen, Styles.clearPartial2i, () -> {
					configure(target < 0 ? NHWorldVars.commandPos : target);
				}).size(LEN).disabled(b -> isPreparing());
				t.button("@mod.ui.select-target", Icon.move, Styles.cleart, LEN, () -> {
					TableFs.pointSelectTable(t, this::configure);
				}).size(LEN * 4, LEN).disabled(b -> NHCtrlVars.isSelecting).row();
			}).fill();
			
		}
		
		@Override
		public float range(){
			return range;
		}
	}
	
	public class BombEntity extends CommandEntity implements Damagec{
		public static final float width = 3.3f;
		public static final float floatX = 10f;
		public static final float floatY = 30f;
		public boolean added;
		public boolean parent;
		public Vec2 target;
		public float damage, radius;
		public transient float size;
		public EffectTrail trail;
		
		public BombEntity(){this(Team.derelict, 50f, Vec2.ZERO, -1, -1, false);}
		
		public BombEntity(Team team, float lifetime, Position from, float x, float y, boolean parent){
			this.team = team;
			this.lifetime = lifetime;
			this.parent = parent;
			this.x = from.getX();
			this.y = from.getY();
			target = new Vec2(x, y);
			trail = new EffectTrail(16, width, baseColor, Pal.gray);
		}
		
		public BombEntity init(Team team, float lifetime, Position from, float x, float y, boolean parent){
			this.team = team;
			this.lifetime = lifetime;
			this.parent = parent;
			this.x = from.getX();
			this.y = from.getY();
			target = new Vec2(x, y);
			trail = new EffectTrail(16, width, baseColor, Pal.gray);
			return this;
		}
		
		public BombEntity setDamage(float damage, float radius){
			this.damage = damage;
			this.radius = radius;
			return this;
		}
		
		public float cx(){
			return x + (parent ? fin(Interp.pow2In) : fout(Interp.pow2Out)) * (floatX + Mathf.randomSeedRange(id() + 3, floatX));
		}
		
		public float cy(){
			return y + (parent ? fin(Interp.pow2In) * 1.25f : fout(Interp.pow5Out)) * (floatY + Mathf.randomSeedRange(id() + 2, floatY));
		}
		
		@Override
		public void draw(){
			float scl = parent ? fin() : fout();
			
			float alpha = parent ? fout(Interp.pow5Out) : fin(Interp.pow5In);
			float scale = (1f - alpha) * 1.3f + 1f;
			float cx = cx(), cy = cy();
			float rotation = fin() * (130f + Mathf.randomSeedRange(id(), 50f));
			
			Draw.z(Layer.effect + 0.001f);
			
			float rad = 0.2f + fslope();
			
			if(parent){
				Draw.color(baseColor);
				Fill.light(cx, cy, 10, 25f * (rad + scale - 1f), Tmp.c2.set(Pal.engine).a(alpha), Tmp.c1.set(Pal.engine).a(0f));
			}
			
			Draw.alpha(alpha);
			trail.draw();
			Draw.color(baseColor);
			if(parent) for(int i = 0; i < 4; i++){
				Drawf.tri(cx, cy, 6f, 40f * (rad + scale - 1f) * Mathf.curve(fout(), 0, 0.5f), i * 90f + rotation);
			}
			
			Draw.color();
			
			float rw = bombRegion.width * Draw.scl * scale, rh = bombRegion.height * Draw.scl * scale;
			
			Draw.alpha(alpha);
			Draw.z(Layer.weather - 1);
			Draw.rect(bombRegion, cx, cy, rw, rh, rotation);
			
			Tmp.v1.trns(225, (parent ? fin(Interp.pow2In) * 1.25f : fout(Interp.pow5Out)) * (floatY + Mathf.randomSeedRange(id() + 2, floatY)));
			
			Draw.z(Layer.flyingUnit + 1);
			Draw.color(0, 0, 0, 0.22f * alpha);
			Draw.rect(bombRegion, cx + Tmp.v1.x, y + Tmp.v1.y, rw, rh, rotation);
			
			Draw.reset();
		}
		
		public void hit(){
			hitEffect.at(x, y);
			Effect.shake(shake, shake, x, y);
			hitSound.at(x, y, Mathf.random(0.9f, 1.1f));
			Bullet b = NHVars.groundHitter.create(this, team, x, y, 0, damage, 1, 1, null);
			b.fdata = radius;
		}
		
		@Override
		public void update(){
			time = Math.min(time + Time.delta, lifetime);
			trail.update(cx(), cy());
			if(Mathf.chance(0.2))trailEffect.at(cx(), cy(), size, baseColor);
			if(!parent) trail.width = width * Mathf.curve(fin(Interp.pow2In), 0.35f, 0.75f);
			else trail.width = width * Mathf.curve(fout(Interp.pow2In), 0, 0.35f);
			if(time >= lifetime){
				remove();
			}
		}
		
		@Override
		public void remove(){
			trail.disappear();
			
			if(parent){
				BombEntity next = Pools.obtain(BombEntity.class, BombEntity::new);
				next.init(team, lifetime / 1.5f, target, target.x, target.y, false).setDamage(bombDamage, bombRadius);
				if(!Vars.net.client())Time.run(dst(target) / tilesize * bombVelPerTile, next::add);
			}else hit();
			
			Groups.draw.remove(this);
			Groups.all.remove(this);
			added = false;
		}
		
		@Override public float damage(){return damage; }
		@Override public void damage(float damage){this.damage = damage; }
	}
}
