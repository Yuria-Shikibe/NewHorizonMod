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
import arc.math.geom.Point3;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.effect.MultiEffect;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.bullets.EffectBulletType;
import newhorizon.content.NHFx;
import newhorizon.effects.EffectTrail;
import newhorizon.vars.NHVars;

import static mindustry.Vars.tilesize;

public class BombLauncher extends CommandableAttackerBlock{
	public TextureRegion bombRegion;
	public TextureRegion[] gunBarrelRegion = new TextureRegion[4];
	
	public Effect
		hitEffect = NHFx.boolSelector,
		shootEffect = NHFx.boolSelector,
		smokeEffect,
		trailEffect;
	
	public Color baseColor = Pal.redderDust;
	
	
	public float bombLifetime = 120f;
	public float shake = 20f;
	public float bombDamage = 800f, bombRadius = 160f;
	public float bombVelPerTile = 2f;
	public Sound hitSound = Sounds.explosionbig;
	
	public int lightning = 3;
	public int lightningLength = 5;
	public int lightningLengthRand = 10;
	
	public BombLauncher(String name){
		super(name);
		smokeEffect = NHFx.hugeSmoke;
		trailEffect = NHFx.trail;
		storage = 4;
		range = 800f;
		spread = 80f;
		prepareDelay = 30f;
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
		stats.add(Stat.damage, bombDamage, StatUnit.none);
	}
	
	@Override
	public void init(){
		super.init();
		bulletHitter = new EffectBulletType(3f){
			{damage = 1f; collidesGround = absorbable = true; hitSize = 0;}
			
			@Override
			public void despawned(Bullet b){
				if(!b.absorbed) Damage.damage(b.team, b.x, b.y, b.fdata, b.damage, collidesAir, collidesGround);
				if(b.data instanceof Point3)for(int i = 0; i < lightning; i++){
					Lightning.create(b, baseColor, b.damage, b.x, b.y, Mathf.random(360f), lightningLength + Mathf.range(lightningLengthRand));
				}
			}
		};
		
		if(hitEffect == NHFx.boolSelector)hitEffect = new MultiEffect(NHFx.lightningHitLarge(baseColor), NHFx.crossBlast(baseColor, bombRadius * 1.25f));
		if(shootEffect == NHFx.boolSelector)shootEffect = NHFx.square(baseColor, 50f, 6, size * tilesize * 2f, size);
	}
	
	@Override
	public void load(){
		super.load();
		bombRegion = Core.atlas.find(name + "-bomb", Core.atlas.find("launchpod"));
	}
	
	public class BombLauncherBuild extends CommandableAttackerBlockBuild{
		@Override
		public void draw(){
			super.draw();
			Draw.draw(Draw.z(), () -> Drawf.construct(x, y, bombRegion, baseColor, 0, (prepareDelay - countBack) / prepareDelay, efficiency(), countBack * 2f));
		}

		@Override
		public float delayTime(){
			Tmp.p1.set(Point2.unpack(NHVars.world.commandPos));
			return (dst(World.unconv(Tmp.p1.x), World.unconv(Tmp.p1.y)) / tilesize * bombVelPerTile + bombLifetime * (1 + 2/3f)) / Time.toSeconds;
		}
		
		@Override
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
			bomb.add();
		}
	}
	
	public class BombEntity extends AttackerEntity{
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
			Draw.z(Layer.flyingUnit + 1);
			Draw.rect(bombRegion, cx, cy, rw, rh, rotation);
			Drawf.light(team, cx, cy, 50f * (parent ? fout() : fin()), baseColor, 0.7f);
			
			Tmp.v1.trns(225, (parent ? fin(Interp.pow2In) * 1.25f : fout(Interp.pow5Out)) * (floatY + Mathf.randomSeedRange(id() + 2, floatY)));
			
			Draw.z(Layer.legUnit + 1);
			Draw.color(0, 0, 0, 0.22f * alpha);
			Draw.rect(bombRegion, cx + Tmp.v1.x, y + Tmp.v1.y, rw, rh, rotation);
			
			Draw.reset();
		}
		
		public void hit(){
			hitEffect.at(x, y);
			Effect.shake(shake, shake, x, y);
			hitSound.at(x, y, Mathf.random(0.9f, 1.1f));
			Bullet b = bulletHitter.create(this, team, x, y, 0, damage, 1, 1, new Point3(lightning, lightningLength, lightningLengthRand));
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
				Time.run(target.dst(this) / tilesize * bombVelPerTile, next::add);
			}else hit();
			
			Groups.draw.remove(this);
			Groups.all.remove(this);
			added = false;
		}
		
		@Override public float damage(){return damage; }
		@Override public void damage(float damage){this.damage = damage; }
	}
}
