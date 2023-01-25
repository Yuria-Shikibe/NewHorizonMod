package newhorizon.expand.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Sized;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.bullets.EffectBulletType;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import java.nio.FloatBuffer;

import static mindustry.Vars.world;

public class PesterEntity extends UnitEntity{
	public static final float CHECK_RELOAD = 12f;
	public static final float CHECK_DAMAGE = 3000f;
	public static final float SALVO_RELOAD = 480f;
	public static final float SHOOT_DELAY = 60f;
	
	public static final float CHECK_RANGE = 320;
	
	public static Building tmpBuilding = null;
	public static final ObjectIntMap<Healthc> checked = new ObjectIntMap<>();
	
	public static final BulletType hitter = new EffectBulletType(15f){{
		speed = 0;
		
		scaledSplashDamage = true;
		collidesTiles = collidesGround = collides = collidesAir = true;
		damage = 500;
		splashDamage = 600f;
		lightningDamage = 200f;
		lightColor = lightningColor = trailColor = hitColor = NHColor.thurmixRed;
		lightning = 5;
		lightningLength = 12;
		lightningLengthRand = 16;
		splashDamageRadius = 60f;
		hitShake = despawnShake = 20f;
		hitSound = despawnSound = Sounds.explosionbig;
		hitEffect = despawnEffect = new OptionalMultiEffect(NHFx.square45_8_45, NHFx.hitSparkHuge, NHFx.crossBlast_45);
	}
		public Color color(Bullet b){
			return b.team.color;
		}
		
		@Override
		public void despawned(Bullet b){
			if(despawnHit){
				hit(b);
			}else{
				createUnits(b, b.x, b.y);
			}
			
			if(!fragOnHit){
				createFrags(b, b.x, b.y);
			}
			
			despawnEffect.at(b.x, b.y, b.rotation(), color(b));
			despawnSound.at(b);
			
			Effect.shake(despawnShake, despawnShake, b);
		}
		
		@Override
		public void hit(Bullet b, float x, float y){
			hitEffect.at(x, y, b.rotation(), color(b));
			hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
			
			Effect.shake(hitShake, hitShake, b);
			
			if(fragOnHit){
				createFrags(b, x, y);
			}
			createPuddles(b, x, y);
			createIncend(b, x, y);
			createUnits(b, x, y);
			
			if(suppressionRange > 0){
				//bullets are pooled, require separate Vec2 instance
				Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
			}
			
			createSplashDamage(b, x, y);
			
			for(int i = 0; i < lightning; i++){
				Lightning.create(b, color(b), lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
			}
		}
	};
	
	public static final Effect toBeBlasted = new Effect(SHOOT_DELAY, e -> {
		Draw.color(e.color, Color.white, e.fin());
		
		Lines.stroke(2 * e.fin());
		Lines.circle(e.x, e.y, e.rotation * Interp.pow4Out.apply(e.fout()));
		
		Lines.spikes(e.x, e.y, 1.82f * e.rotation * Interp.pow2Out.apply(e.fout()), e.fin() * e.rotation / 6f, 4, 45);
	}).followParent(true);
	
	public float hatredCheckReload = CHECK_RELOAD;
	public float salvoReload = 0;
	public transient float salvoReload_LAST_ = 0;
	public transient float salvoReload_TARGET_ = 0;
	public float warmup = 0;
	
	public ObjectFloatMap<Healthc> hatred = new ObjectFloatMap<>();
	public Seq<Healthc> nextTargets = new Seq<>();
	
	@Override public int classId(){return EntityRegister.getID(PesterEntity.class);}
	
	public Healthc findOwner(Entityc ent){
		Healthc target = null;
		
		int itr = 0;
		while(ent instanceof Bullet){
			if(itr > 4)break;
			
			ent = ((Bullet)ent).owner();
			
			if(ent instanceof Unit){
				Unit u = ent.as();
				if(u.controller() instanceof MissileAI){
					Unit o = ((MissileAI)u.controller()).shooter;
					if(!(o.controller() instanceof MissileAI)){
						target = o;
						break;
					}
				}else{
					target = u;
					break;
				}
			}else if(ent instanceof Building){
				target = (Healthc)ent;
				break;
			}
			
			itr++;
		}
		
		return target;
	}
	
	public void collision(Hitboxc other, float x, float y){
		if (other instanceof Bullet) {
			Bullet bullet = (Bullet)other;
			controller.hit(bullet);
			
			if(bullet.team == team || bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage < 60)return;
			
			//Target the source of the bullet;
			Healthc target = findOwner(bullet);
			
			if(target != null){
				float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75f, 1.25f) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
				hatred.increment(target, v, v);
			}
		}
	}
	
	@Override
	public void update(){
		super.update();
		
		if(hatred.size > 0)hatredCheckReload -= Time.delta;
		if(hatredCheckReload < 0){
			hatredCheckReload = CHECK_RELOAD;
			
			Groups.bullet.intersect(x - CHECK_RANGE, y - CHECK_RANGE, CHECK_RANGE * 2, CHECK_RANGE * 2, bullet -> {
				if(bullet.team != team){
					Healthc target = findOwner(bullet);
					
					if(target != null){
						float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75f, 1.25f) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
						hatred.increment(target, v, v);
					}
				}
			});
			
			for(ObjectFloatMap.Entry<Healthc> e : hatred.entries()){
				//??Why this happens??
				if(e.key == null)continue;
				
				if(!e.key.isValid()){
					hatred.remove(e.key, 0);
					continue;
				}
				
				
				if(e.value > CHECK_DAMAGE){
					nextTargets.add(e.key);
					e.value -= CHECK_DAMAGE;
				}
			}
		}
		
		if(nextTargets.size > 0){
			salvoReload += Time.delta;
			
			if(salvoReload > SALVO_RELOAD){
				shootAtHatred();
				salvoReload = 0;
			}
		}
	}
	
	public void shootAtHatred(){
		Tmp.v1.trns(rotation, -type.engineOffset).add(x, y);
		
		float ex = Tmp.v1.x, ey = Tmp.v1.y;
		
		int itr = 0;
		for(Healthc hel : nextTargets){
			if(!hel.isValid())continue;
			
			tmpBuilding = null;
			
			boolean found = World.raycast(World.toTile(ex), World.toTile(ey), World.toTile(hel.getX()), World.toTile(hel.getY()),
					(x, y) -> (tmpBuilding = world.build(x, y)) != null && tmpBuilding.team != team && checked.get(tmpBuilding, 0) < 2);
			
			Healthc t = found ? tmpBuilding : hel;
			int c = checked.increment(t, 0, 1);
			if(c <= 3){
				Time.run(itr * 2f, () -> shoot(t));
				itr++;
			}
		}
		checked.clear();
		
		nextTargets.clear();
		
		if(!Vars.headless && itr > 0){
			NHSounds.hugeShoot.at(ex, ey);
			NHFx.crossSpinBlast.at(ex, ey, 0, team.color, self());
		}
	}
	
	public void shoot(Healthc h){
		if(Vars.state.isGame() && h.isValid()){
			toBeBlasted.at(h.getX(), h.getY(), h instanceof Sized ? ((Sized)h).hitSize() : 30f, team.color, h);
			Fx.chainLightning.at(x, y, 0, team.color, h);
			
			Time.run(SHOOT_DELAY, () -> {
				if(Vars.state.isGame() && h.isValid()){
					hitter.create(this, team, h.getX(), h.getY(), 0);
					heal(500);
				}
			});
		}
	}
	
	@Override
	public void draw(){
		super.draw();
		
		float z = Draw.z();
		
		Draw.z(Layer.effect + 0.001f);
		
		Draw.color(team.color, Color.white, Mathf.absin(4f, 0.3f));
		Lines.stroke((3f + Mathf.absin(10f, 0.55f)) * Mathf.curve(1 - salvoReload / SALVO_RELOAD, 0, 0.075f));
		if(salvoReload > 5f)DrawFunc.circlePercent(x, y, hitSize * 1.35f, salvoReload / SALVO_RELOAD, 0);
		
		Draw.z(z);
	}
	
	@Override
	public void writeSync(Writes write){
		write.f(salvoReload);
		super.writeSync(write);
	}
	
	@Override
	public void writeSyncManual(FloatBuffer buffer){
		buffer.put(rotation);
		buffer.put(x);
		buffer.put(y);
		buffer.put(salvoReload);
	}
	
	@Override
	public void readSync(Reads read){
		if(!isLocal()) {
			salvoReload_LAST_ = salvoReload;
			salvoReload_TARGET_ = read.f();
		}else{
			read.f();
			salvoReload_LAST_ = salvoReload;
			salvoReload_TARGET_ = salvoReload;
		}
		
		super.readSync(read);
	}
	
	@Override
	public void readSyncManual(FloatBuffer buffer){
		super.readSyncManual(buffer);
		salvoReload_LAST_ = salvoReload;
		salvoReload_TARGET_ = buffer.get();
	}
	
	@Override
	public void interpolate(){
		super.interpolate();
//		if (lastUpdated != 0L && updateSpacing != 0L) {
//			float timeSinceUpdate = (float)Time.timeSinceMillis(lastUpdated);
//			float alpha = Math.min(timeSinceUpdate / (float)updateSpacing, 2.0F);
//			salvoReload = Mathf.slerp(salvoReload_LAST_, salvoReload_TARGET_, alpha);
//		} else if (lastUpdated != 0L) {
//			salvoReload = salvoReload_TARGET_;
//		}
	}
	
	@Override
	public void snapSync(){
		super.snapSync();
		salvoReload_LAST_ = salvoReload_TARGET_;
		salvoReload = salvoReload_TARGET_;
	}
	
	@Override
	public void snapInterpolation(){
		super.snapInterpolation();
		salvoReload_LAST_ = salvoReload;
		salvoReload_TARGET_ = salvoReload;
	}
	
	public boolean isSyncHidden(Player player) {
		return nextTargets.isEmpty() && hatred.isEmpty() && !this.isShooting() && this.inFogTo(player.team());
	}
}
