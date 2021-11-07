package newhorizon.expand.block.defence;

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
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.world.Tile;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;

import static mindustry.Vars.tilesize;

public class BombLauncher extends CommandableAttackerBlock{
	public TextureRegion bombRegion;
	public TextureRegion[] gunBarrelRegion = new TextureRegion[4];
	
	public Sound shootSound = NHSounds.launch;
	public Color baseColor = Pal.redderDust;
	
	public float bombVelPerTile = 2f;
	
	public BombLauncher(String name){
		super(name);
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
	}
	
	@Override
	public void init(){
		super.init();
		if(bulletHitter.shootEffect == NHFx.boolSelector)bulletHitter.shootEffect = NHFx.square(baseColor, 50f, 6, size * tilesize * 2f, size);
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
		public float delayTime(int target){
			tmpPoint.set(Point2.unpack(target));
			return (dst(World.unconv(tmpPoint.x), World.unconv(tmpPoint.y)) / tilesize * bombVelPerTile + bulletHitter.lifetime * (1 + 2f/3f) ) / Time.toSeconds;
		}
		
		@Override
		public void shoot(Integer pos){
			Tile target = Vars.world.tile(pos);
			if(target == null || !within(target, range) || !consValid())return;
			reload = Math.max(0, reload - reloadTime);
			consume();
			Effect.shake(bulletHitter.despawnShake / 2, bulletHitter.despawnShake, this);
			bulletHitter.shootEffect.at(this);
			bulletHitter.smokeEffect.at(this);
			shootSound.at(this);
			Rand rand = new Rand((long)Groups.all.size() << 8);
			BombEntity bomb = Pools.obtain(BombEntity.class, BombEntity::new);
			bomb.init(team, bulletHitter.lifetime, this, target.drawx() + rand.range(spread), target.drawy() + rand.range(spread), true);
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
		public Trail trail;
		
		public BombEntity(){this(Team.derelict, 50f, Vec2.ZERO, -1, -1, false);}
		
		public BombEntity(Team team, float lifetime, Position from, float x, float y, boolean parent){
			this.team = team;
			this.lifetime = lifetime;
			this.parent = parent;
			this.x = from.getX();
			this.y = from.getY();
			target = new Vec2(x, y);
			trail = new Trail(16);
		}
		
		public BombEntity init(Team team, float lifetime, Position from, float x, float y, boolean parent){
			this.team = team;
			this.lifetime = lifetime;
			this.parent = parent;
			this.x = from.getX();
			this.y = from.getY();
			target = new Vec2(x, y);
			trail = new Trail(16);
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
			trail.draw(baseColor, width);
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
			Bullet b = bulletHitter.create(this, team, x, y, 0, 0, 0.001f);
		}
		
		@Override
		public void update(){
			time = Math.min(time + Time.delta, lifetime);
			trail.update(cx(), cy());
			if(Mathf.chance(bulletHitter.trailChance))bulletHitter.trailEffect.at(cx(), cy(), bulletHitter.trailParam, baseColor);
			if(time >= lifetime){
				remove();
			}
		}
		
		@Override
		public void remove(){
			Fx.trailFade.at(x, y, width, baseColor, trail.copy());
			
			if(parent){
				BombEntity next = Pools.obtain(BombEntity.class, BombEntity::new);
				next.init(team, lifetime / 1.5f, target, target.x, target.y, false);
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
