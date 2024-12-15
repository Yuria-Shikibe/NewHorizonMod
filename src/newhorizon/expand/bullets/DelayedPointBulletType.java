package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Hitboxc;
import mindustry.graphics.Drawf;

import static mindustry.Vars.world;

public class DelayedPointBulletType extends BulletType{
	protected static float cdist = 0f;
	protected static Position result;
	protected static Color[] colors = {new Color(1, 1, 1, 0f).a(-5f), new Color(1, 1, 1, 1f), new Color(1, 1, 1, 1f)};
	protected static float lengthFalloff = 0.5f;
	
	public float errorCorrectionRadius = 16;
	
	public float width = 8f;
	public float trailSpacing = 10f;
	
	public float delayEffectLifeTime = 30f;
	
	public static Effect laser = new Effect(60f, 2000f, b -> {
		if(!(b.data instanceof Position))return;
		Position target = b.data();
		
		float tX = target.getX();
		float tY = target.getY();
		float cwidth = b.rotation;
		float compound = 1f;
		
		for(int i = 0; i < colors.length; i++){
			Draw.color(Tmp.c1.set(b.color).lerp(colors[i], i * 0.3f + 0.1f));
			Lines.stroke((cwidth *= lengthFalloff) * b.fout());
			Lines.line(b.x, b.y, tX, tY, false);
			
			Fill.circle(b.x, b.y, 1.25f * cwidth * b.fout());
			Fill.circle(tX, tY, 1.25f * cwidth * b.fout());
			
			compound *= lengthFalloff;
		}
		Draw.reset();
		
		Drawf.light(b.x, b.y, tX, tY, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
	}).followParent(false);
	
	public DelayedPointBulletType(){
		scaleLife = true;
		speed = 0.001f;
		collides = false;
		reflectable = false;
		keepVelocity = false;
		backMove = false;
		hittable = absorbable = false;
		despawnHit = false;
		setDefaults = false;
	}
	
	@Override
	public void init(){
		super.init();
		
		lifetime = range / speed;
		drawSize = range * 2f;
	}
	
	@Override
	public void init(Bullet b){
		float px = b.x + b.lifetime * b.vel.x,
				py = b.y + b.lifetime * b.vel.y,
				rot = b.rotation();
		
		cdist = 0f;
		result = null;
		
		Units.nearbyEnemies(b.team, px - errorCorrectionRadius, py - errorCorrectionRadius, errorCorrectionRadius*2f, errorCorrectionRadius*2f, e -> {
			if(e.dead() || !e.checkTarget(collidesAir, collidesGround) || !e.hittable()) return;
			
			e.hitbox(Tmp.r1);
			if(!Tmp.r1.contains(px, py)) return;
			
			float dst = e.dst(px, py) - e.hitSize;
			if((result == null || dst < cdist)){
				result = e;
				cdist = dst;
			}
		});
		
		if(result == null)result = new Vec2(px, py);
		
		b.data = result;
		
		b.lifetime = delayEffectLifeTime;
		
		b.rotation(rot);
		
		super.init(b);
	}
	
	@Override
	public void draw(Bullet b){
		if(!(b.data instanceof Position))return;
		Position target = (Position)b.data();
		
		float tX = target.getX();
		float tY = target.getY();
		float cwidth = width / 1.4f * b.fout();
		float compound = 1f;
		
		for(int i = 0; i < colors.length; i++){
			Draw.color(Tmp.c1.set(hitColor).lerp(colors[i], i * 0.3f + 0.1f));
			float s = (cwidth *= lengthFalloff) * (b.fout() * 2 + 1) / 3;
			Lines.stroke(s);
			Lines.line(b.x, b.y, tX, tY, false);
			
			Fill.circle(b.x, b.y, 1.5f * s);
			Fill.circle(tX, tY, 1.5f * s);
			
			Fill.circle(Mathf.lerp(b.x, tX, b.fin()), Mathf.lerp(b.y, tY, b.fin()), 2.25f * (s + width / 5f) * (1 + b.fout()) * 0.5f * b.fin());
			
			compound *= lengthFalloff;
		}
		Draw.reset();
		
		Drawf.light(b.x, b.y, tX, tY, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
	}
	
	@Override
	public void despawned(Bullet b){
		if(!(b.data instanceof Position) || !b.isAdded())return;
		Position target = (Position)b.data();
		
		float tX = target.getX();
		float tY = target.getY();
		
		float rot = b.rotation();
		
		Geometry.iterateLine(0f, b.x, b.y, tX, tY, trailSpacing, (x1, y1) -> {
			trailEffect.at(x1, y1, rot, trailColor);
		});
		
		laser.at(b.x, b.y, width, hitColor, target);
		b.set(tX, tY);
		
		if(target instanceof Hitboxc){
			hitEntity(b, (Hitboxc)target, 0);
			if(!despawnHit)hit(b, tX, tY);
		}else if(collidesTiles){
			Building build = Vars.world.buildWorld(tX, tY);
			if(build != null && build.team != b.team){
				build.collision(b);
				if(!despawnHit)hit(b, build.x, build.y);
			}
		}else if(despawnHit)hit(b, tX, tY);
		
		b.hit = true;
		super.despawned(b);
	}
	
	
	@Override
	public Bullet create(Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY){
		Bullet bullet = Bullet.create();//Pools.obtain(AdaptedBullet.class, AdaptedBullet::new);
		bullet.type = this;
		bullet.owner = owner;
		bullet.team = team;
		bullet.time = 0f;
		bullet.originX = x;
		bullet.originY = y;
		bullet.aimTile = world.tileWorld(aimX, aimY);
		bullet.aimX = aimX;
		bullet.aimY = aimY;
		bullet.initVel(angle, speed * velocityScl);
		if(backMove){
			bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
		}else{
			bullet.set(x, y);
		}
		bullet.lifetime = lifetime * lifetimeScl;
		bullet.data = data;
		bullet.drag = drag;
		bullet.hitSize = hitSize;
		bullet.mover = mover;
		bullet.damage = (damage < 0 ? this.damage : damage) * bullet.damageMultiplier();
		//worldReset trail
		if(bullet.trail != null){
			bullet.trail.clear();
		}
		bullet.add();
		
		return bullet;
	}
	
	@Override
	public void handlePierce(Bullet b, float initialHealth, float x, float y){
	}
	
	public static class AdaptedBullet extends Bullet{
		static{
			Pools.get(AdaptedBullet.class, AdaptedBullet::new, 1000);
		}
		
		@Override
		public void update(){
			//WHY???
			super.update();
		}
	}
}
