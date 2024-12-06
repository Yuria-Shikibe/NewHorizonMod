package newhorizon.expand.units.unitEntity;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
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
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.world.meta.BlockGroup;
import newhorizon.NHSetting;
import newhorizon.content.*;
import newhorizon.expand.bullets.EffectBulletType;
import newhorizon.expand.entities.EntityRegister;
import newhorizon.expand.game.NHUnitSorts;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import java.nio.FloatBuffer;

import static mindustry.Vars.world;

public class PesterEntity extends UnitEntity{
	public static final float BOSS_WEAPON_RANGE = 80 * Vars.tilesize;
	public static final float REFLECT_RANGE = 120 * Vars.tilesize;
	
	public static final float CHECK_RELOAD = 12f;
	public static final float CHECK_BOSS_RELOAD = 60;
	public static final float CHECK_DAMAGE = 3000f;
	public static final float SALVO_RELOAD = 480f;
	public static final float BOSS_RELOAD = 600f;
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
	
	public boolean isBoss = false;
	
	public Teamc bossTarget;
	public Teamc lastTarget;
	public transient Vec2 lastTargetPos = new Vec2();
	
	public float bossWeaponReload;
	public float bossWeaponWarmup;
	public float bossWeaponProgress;
	public float bossTargetShiftLerp = 0;
	public float bossTargetSearchReload = CHECK_BOSS_RELOAD;
	
	
	public transient float bossWeaponReload_LAST_ = 0;
	public transient float bossWeaponReload_TARGET_ = 0;
	
	
	public float hatredCheckReload = CHECK_RELOAD;
	
	public float salvoReload = 0;
	public transient float salvoReload_LAST_ = 0;
	public transient float salvoReload_TARGET_ = 0;
	
	public ObjectFloatMap<Healthc> hatred = new ObjectFloatMap<>();
	public Seq<Healthc> nextTargets = new Seq<>();
	
	protected Trail[] trails = {};
	
	@Override public int classId(){return EntityRegister.getID(PesterEntity.class);}
	
	@Override
	public void setType(UnitType type){
		super.setType(type);
		if(!Vars.net.active())lastTargetPos.set(x, y);
		
		if(!Vars.headless && trails.length != 4){
			trails = new Trail[4];
			for(int i = 0; i < trails.length; i++){
				trails[i] = new Trail(type.trailLength);
			}
		}
	}
	
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
					}else target = o;
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
		isBoss = super.isBoss() || hasEffect(NHStatusEffects.overphased);
		
		super.update();
		
		bossTargetSearchReload -= Time.delta;
		if(bossTargetSearchReload < 0 && isBoss){
			bossTargetSearchReload = CHECK_BOSS_RELOAD;
			
			bossTarget = Units.bestTarget(team, x, y, BOSS_WEAPON_RANGE, e -> true, e -> !(e.block.group == BlockGroup.walls), NHUnitSorts.regionalHPMaximum_All);
		}
		
		if(bossTarget != null){
			if(bossTargetShiftLerp <= 0.0075f && lastTarget == bossTarget){
				lastTargetPos.set(lastTarget.x(), lastTarget.y());
			}else{
				if(bossTargetShiftLerp <= 0.0075f)bossTargetShiftLerp = 1f;
				bossTargetShiftLerp = Mathf.lerpDelta(bossTargetShiftLerp, 0f, 0.075f);
				lastTargetPos.lerp(bossTarget, 0.075f * Time.delta);
			}
		}
		
		lastTarget = bossTarget;
		
		if(lastTarget != null && lastTarget.isAdded()){
			bossWeaponWarmup = Mathf.lerpDelta(bossWeaponWarmup, 1, 0.0075f);
			bossWeaponProgress += Time.delta * bossWeaponWarmup * (0.86f + Mathf.absin(37f, 1f) + Mathf.absin(77f, 1f)) * (0.9f - (bossWeaponReload / BOSS_RELOAD) * 0.7f);
			bossWeaponReload += Time.delta * bossWeaponWarmup;
		}else{
			if(bossWeaponWarmup <= 0){
				bossWeaponWarmup = 0;
				lastTargetPos.set(this);
			}else if(bossWeaponWarmup < 0.35f){
				bossWeaponWarmup -= Time.delta / 3f;
			}
			bossWeaponWarmup = Mathf.lerpDelta(bossWeaponWarmup, 0, 0.0075f);
		}
		
		if(bossWeaponReload > BOSS_RELOAD){
			bossWeaponReload = bossWeaponWarmup = bossWeaponProgress = 0;
			shootBossTarget();
			lastTargetPos.set(x, y);
		}
		
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
				
				if(!e.key.isValid() || !within(e.key, REFLECT_RANGE) || ((Teamc)e.key).team() == team){
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
			salvoReload += Time.delta * (1 + Mathf.num(isBoss) * reloadMultiplier);
			
			if(salvoReload > SALVO_RELOAD){
				shootAtHatred();
				salvoReload = 0;
			}
		}
	}
	
	
	public void shootBossTarget(){
		Bullet b = NHBullets.pesterBlackHole.create(self(), team, lastTargetPos.x, lastTargetPos.y, 0, 1, 1, 1, NHBullets.pesterBlackHole.splashDamageRadius);
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
		
		if(NHSetting.enableDetails() && !Vars.headless && isBoss){
			Rand rand = NHFunc.rand;
			for(int i = 0; i < trails.length; i++){
				Trail trail = trails[i];
				
				float scl = rand.random(0.75f, 1.5f) * Mathf.sign(rand.range(1)) * (i + 1) / 1.25f;
				float s = rand.random(0.75f, 1.25f);
				
				Tmp.v1.trns(
						Time.time * scl * rand.random(0.5f, 1.5f) + i * 360f / trails.length + rand.random(360),
						hitSize * (1.1f + 0.5f * i) * 0.75f
				).add(this).add(
						Mathf.sinDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize * 0.75f * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f),
						Mathf.cosDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize * 0.75f * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f)
				);
				trail.update(Tmp.v1.x, Tmp.v1.y, 1 + Mathf.absin(4f, 0.2f));
			}
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
	
	public void drawBossWeapon(){
		if(bossWeaponWarmup > 0.01f){
			float fin = bossWeaponReload / BOSS_RELOAD, fout = 1 - fin;
			float fadeS = Mathf.curve(fout, 0.0225f, 0.06f);
			float fadeS2 = Mathf.curve(fout, 0.09f, 0.185f);
			float fade = bossWeaponWarmup * Mathf.curve(fout, 0, 0.025f) * NHInterp.bounce5In.apply(fadeS);
			
			Tmp.v2.trns(bossWeaponProgress / 17f, Mathf.sin(bossWeaponProgress, 30f, 60f) * fout, Mathf.cos(bossWeaponProgress + 177f, 17f, 35f) * fout);
			Tmp.v3.set(Mathf.sin(bossWeaponProgress, 30, 15) * fout, Mathf.sin(bossWeaponProgress + Mathf.pi * 0.3f, 43, 12) * fout);
			
			float str = 3.5f * fade;
			
			float addtionRot = (-DrawFunc.rotator_120(DrawFunc.cycle(bossWeaponProgress, 45, 490f), 0.24f) + Mathf.absin(33f, 220f)) * fadeS2 + bossWeaponProgress;
			
			Tmp.v1.trns(bossWeaponProgress / 6f, fout * 160f, Mathf.absin(bossWeaponProgress, 288, 33)).scl(Mathf.curve(fout, 0.025f, 0.525f));
			Tmp.v4.set(Tmp.v1).add(lastTargetPos).add(Tmp.v2).add(Tmp.v3);
			
			//Draw tri aim
			Lines.stroke(str, Tmp.c1);
			Lines.poly(Tmp.v4.x, Tmp.v4.y, 3, 50f + 80f * fout, addtionRot);
			
			Lines.stroke(str * 3, Color.black);
			Lines.spikes(Tmp.v4.x, Tmp.v4.y, 25f + 40f * fout, Lines.getStroke(), 3, addtionRot + 60);
			
			Lines.stroke(str, Tmp.c1);
			Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
			Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);
			
			Tmp.v4.set(Tmp.v1).rotate(270f * fout + bossWeaponProgress * 0.035f).add(lastTargetPos).add(Tmp.v5.set(Tmp.v2).lerp(Tmp.v3, Mathf.absin(8f, 1f)));
			DrawFunc.circlePercent(Tmp.v4.x, Tmp.v4.y, 200f - 60f * fin, fin * 1.035f, Time.time / 2f);
			Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
			Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);
			
			float fCurveOut = Mathf.curve(fout, 0, 0.03f) * fadeS2;
			
			Tmp.v4.set(Tmp.v1).rotate(130f * fout + bossWeaponProgress * 0.075f).add(lastTargetPos).add(Tmp.v5.set(Tmp.v3).lerp(Tmp.v2, Mathf.absin(12f, 2f) - 1f));
			Lines.spikes(Tmp.v4.x, Tmp.v4.y, 16 + 60f * fout, 32 * fout + 28, 3, addtionRot + Mathf.absin(33f, 220f) * fCurveOut
					- DrawFunc.rotator_120(DrawFunc.cycle(bossWeaponProgress, 0, 360f), 0.14f) * 2 * fCurveOut
					+ DrawFunc.rotator_120(DrawFunc.cycle(bossWeaponProgress, 70, 450f), 0.22f) * fCurveOut + 60
			);
			
			Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
			Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);
			
			Tmp.v4.set(lastTargetPos).add(Mathf.sin(Time.time, 36, 12) * fout, Mathf.cos(Time.time, 36, 12) * fout);
			Lines.spikes(Tmp.v4.x, Tmp.v4.y, 12 + 40 * fout, 16 * fout + 8, 4, 45 + DrawFunc.rotator_90());
			
			Fill.circle(lastTargetPos.x, lastTargetPos.y, Lines.getStroke() * 5f);
			Draw.color(Color.black);
			Fill.circle(lastTargetPos.x, lastTargetPos.y, Lines.getStroke() * 3.8f);
			
			Draw.color(Tmp.c1);
			
			for(int i : Mathf.signs){
				float d = 220 * i * fout + 2 * i;
				float phi = Mathf.absin(8 + i * 2f, 12f) * fout;
				Lines.lineAngle(lastTargetPos.x + d + 1f * i, lastTargetPos.y + phi, 90 - i * 90, (682 + i * 75) + 220 * fin);
				Lines.lineAngleCenter(lastTargetPos.x + d, lastTargetPos.y + phi, 45, (188 + i * 20) * fout + 80);
			}
			
			Lines.stroke(str / 2.2f);
			Lines.spikes(lastTargetPos.x, lastTargetPos.y, NHBullets.pesterBlackHole.splashDamageRadius, 12 * fade, 30, Time.time * 0.38f);
		}
	}
	
	@Override
	public void draw(){
		super.draw();
		
		Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4f, 0.3f));//.a(NHInterp.bounce5In.apply(fadeS));
		
		Draw.reset();
		
		float z = Draw.z();
		
		Draw.z(Layer.effect - 0.001f);
		
		drawBossWeapon();
		
		Draw.color(Tmp.c1);
		
		if(isBoss){
			Tmp.v1.trns(rotation, -type.engineOffset).add(x, y);
			
			float cameraFin = (1 + 2 * DrawFunc.cameraDstScl(Tmp.v1.x, Tmp.v1.y, Vars.mobile ? 200 : 320)) / 3f;
			float triWidth = hitSize * 0.033f * cameraFin;
			
			for(int i : Mathf.signs){
				Fill.tri(Tmp.v1.x, Tmp.v1.y + triWidth, Tmp.v1.x, Tmp.v1.y - triWidth, Tmp.v1.x + i * cameraFin * hitSize * (15 + Mathf.absin(12f, 3f)), Tmp.v1.y);
			}
		}
		
		Lines.stroke((3f + Mathf.absin(10f, 0.55f)) * Mathf.curve(1 - salvoReload / SALVO_RELOAD, 0, 0.075f));
		if(salvoReload > 5f)DrawFunc.circlePercent(x, y, hitSize * 1.35f, salvoReload / SALVO_RELOAD, 0);
		
		Draw.z(Layer.bullet);
		
		if(NHSetting.enableDetails() && isBoss){
			for(int i = 0; i < trails.length; i++){
				Tmp.c1.set(team.color).mul(1 + i * 0.005f).lerp(Color.white, 0.015f * i + Mathf.absin(4f, 0.3f) +  Mathf.clamp(hitTime) / 5f);
				trails[i].drawCap(Tmp.c1, type.trailScl);
				trails[i].draw(Tmp.c1, type.trailScl);
			}
		}
		
		Draw.z(z);
	}
	
	@Override
	public void writeSync(Writes write){
		super.writeSync(write);
		write.f(salvoReload);
		write.f(bossWeaponReload);
	}
	
	@Override
	public void readSync(Reads read){
		super.readSync(read);
		
		if(!isLocal()) {
			salvoReload_LAST_ = salvoReload;
			salvoReload_TARGET_ = read.f();
			bossWeaponReload_LAST_ = bossWeaponReload;
			bossWeaponReload_TARGET_ = read.f();
		}else{
			read.f();
			salvoReload_LAST_ = salvoReload;
			salvoReload_TARGET_ = salvoReload;
			read.f();
			bossWeaponReload_LAST_ = bossWeaponReload;
			bossWeaponReload_TARGET_ = bossWeaponReload;
		}
	}
	
	@Override
	public void writeSyncManual(FloatBuffer buffer){
		super.writeSyncManual(buffer);
//		Log.info("put");
//		buffer.put(salvoReload);
//		buffer.put(bossWeaponReload);
	}
	
	@Override
	public void readSyncManual(FloatBuffer buffer){
		super.readSyncManual(buffer);
//		Log.info("read");
//		salvoReload_LAST_ = salvoReload;
//		salvoReload_TARGET_ = buffer.get();
//		bossWeaponReload_LAST_ = bossWeaponReload;
//		bossWeaponReload_TARGET_ = buffer.get();
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
		bossWeaponReload_LAST_ = bossWeaponReload_TARGET_;
		bossWeaponReload = bossWeaponReload_TARGET_;
	}
	
	@Override
	public void snapInterpolation(){
		super.snapInterpolation();
		salvoReload_LAST_ = salvoReload;
		salvoReload_TARGET_ = salvoReload;
		bossWeaponReload_LAST_ = bossWeaponReload;
		bossWeaponReload_TARGET_ = bossWeaponReload;
	}
	
	public boolean isSyncHidden(Player player) {
		return nextTargets.isEmpty() && hatred.isEmpty() && !this.isShooting() && this.inFogTo(player.team());
	}
	
	@Override
	public boolean isBoss(){
		return isBoss;
	}
}
