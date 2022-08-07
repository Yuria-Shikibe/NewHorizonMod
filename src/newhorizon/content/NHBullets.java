package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.EmpBulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.headless;

public class NHBullets{
	public static String CIRCLE_BOLT, STRIKE, MISSILE_LARGE = "missile-large";
	
	public static BulletType ultFireball, basicSkyFrag, annMissile, guardianBullet, saviourBullet;
	
	public static void load(){
		CIRCLE_BOLT = NewHorizon.name("circle-bolt");
		STRIKE = NewHorizon.name("strike");
		
		saviourBullet = new EmpBulletType(){{
			float rad = 100f;
			
			rangeOverride = 400f;
			scaleLife = true;
			lightOpacity = 0.7f;
			healPercent = 20f;
			timeIncrease = 3f;
			timeDuration = 60f * 20f;
			powerDamageScl = 3f;
			damage = 100;
			hitColor = lightColor = Pal.heal;
			lightRadius = 70f;
			shootEffect = Fx.hitEmpSpark;
			smokeEffect = Fx.healWave;
			lifetime = 60f;
			lightningColor = backColor = Pal.heal;
			frontColor = Color.white;
			
			lightning = 3;
			lightningDamage = damage;
			lightningLength = 7;
			lightningLengthRand = 16;
			
			width = 16f;
			height = 35f;
			speed = 8f;
			trailLength = 20;
			trailWidth = 2.7f;
			trailColor = Pal.heal;
			trailInterval = 3f;
			splashDamage = damage * 0.75f;
			splashDamageRadius = rad;
			hitShake = 4f;
			trailRotation = true;
			status = StatusEffects.electrified;
			hitSound = Sounds.plasmaboom;
			
			trailEffect = new Effect(16f, e -> {
				color(Pal.heal);
				for(int s : Mathf.signs){
					DrawFunc.tri(e.x, e.y, 4f, 30f * Mathf.curve(e.fin(), 0, 0.1f) * e.fout(0.9f), e.rotation + 135f * s);
				}
			});
			
			hitEffect = new OptionalMultiEffect(NHFx.blast(backColor, rad), NHFx.hitSpark(backColor, 120f, 40, rad * 1.7f, 2.5f, 12f));
			despawnEffect = NHFx.crossBlast(backColor, rad * 1.8f, 45);
		}
			
			@Override
			public void hit(Bullet b){
				super.hit(b);
				
				NHFunc.extinguish(b, splashDamageRadius, 3000);
			}
		};
		
		basicSkyFrag = new BasicBulletType(3.8f, 50){
			{
				speed = 12f;
				trailLength = 12;
				trailWidth = 2f;
				lifetime = 60;
				despawnEffect = NHFx.square45_4_45;
				hitEffect = new Effect(45f, e -> {
					Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin());
					Lines.stroke(1.75f * e.fout());
					if(NHSetting.enableDetails())Lines.spikes(e.x, e.y, 28 * e.finpow(), 5 * e.fout() + 8 * e.fin(NHInterp.parabola4Reversed), 4, 45);
					Lines.square(e.x, e.y, 14 * e.fin(Interp.pow3Out), 45);
				});
				knockback = 4f;
				width = 15f;
				height = 37f;
				lightningDamage = damage * 0.65f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				lightning = 2;
				lightningLength = lightningLengthRand = 3;
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.2f;
				trailEffect = NHFx.skyTrail;
				drag = 0.015f;
				hitShake = 2f;
				hitSound = Sounds.explosion;
			}
			
			@Override
			public void hit(Bullet b){
				super.hit(b);
				UltFire.createChance(b, 12, 0.0075f);
			}
		};
		
		ultFireball = new FireBulletType(1f, 10){{
			colorFrom = NHColor.lightSkyFront;
			colorMid = NHColor.lightSkyBack;
			
			lifetime = 12f;
			radius = 4f;
			
			trailEffect = NHFx.ultFireBurn;
		}
			@Override
			public void draw(Bullet b){
				Draw.color(colorFrom, colorMid, colorTo, b.fin());
				Fill.square(b.x, b.y, radius * b.fout(), 45);
				Draw.reset();
			}
			
			@Override
			public void update(Bullet b){
				if(Mathf.chanceDelta(fireTrailChance)){
					UltFire.create(b.tileOn());
				}
				
				if(Mathf.chanceDelta(fireEffectChance)){
					trailEffect.at(b.x, b.y);
				}
				
				if(Mathf.chanceDelta(fireEffectChance2)){
					trailEffect2.at(b.x, b.y);
				}
			}
		};
		
		annMissile = new BasicBulletType(5.6f, 80f, STRIKE){{
				trailColor = lightningColor = backColor = lightColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				lightning = 3;
				lightningCone = 360;
				lightningLengthRand = lightningLength = 9;
				splashDamageRadius = 60;
				splashDamage = lightningDamage = damage * 0.7f;
				
				range = 320f;
				
				scaleLife = true;
				
				width = 12f;
				height = 30f;
				trailLength = 15;
				drawSize = 250f;
				
				trailParam = 1.4f;
				trailChance = 0.35f;
				lifetime = 50f;
				
				homingDelay = 10f;
				homingPower = 0.05f;
				homingRange = 150f;
				
				hitEffect = NHFx.lightningHitLarge(NHColor.lightSkyBack);
				
				shootEffect = NHFx.hugeSmoke;
				smokeEffect = new Effect(45f, e -> {
					color(lightColor, Color.white, e.fout() * 0.7f);
					randLenVectors(e.id, 8, 5 + 55 * e.fin(), e.rotation, 45, (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
				});
				despawnEffect = new Effect(32f, e -> {
					color(Color.gray);
					Angles.randLenVectors(e.id + 1, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
					color(lightColor, Color.white, e.fin());
					stroke(e.fout() * 2);
					Fill.circle(e.x, e.y, e.fout() * e.fout() * 13);
					randLenVectors(e.id, 4, 7 + 40 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3));
				});
			}};
		
		
		guardianBullet = new BasicBulletType(10f, 160){
			{
				width = 22f;
				height = 40f;
				
//				accelInterp = NHInterp.inOut;
				
				pierceCap = 3;
				splashDamage = damage / 4;
				splashDamageRadius = 24f;
				
				trailLength = 30;
				trailWidth = 3f;
				
//				accelerateBegin = 0.4f;
//				accelerateEnd = 0.75f;
//
//				velocityBegin = 1.85f;
//				velocityIncrease = 12f;
				
				lifetime = 160f;
				
				trailEffect = NHFx.trailFromWhite;
//				trailInterp = NHInterp.inOut;
				
				trailRotation = false;
				trailChance = 0.35f;
				trailParam = 4f;
				
				homingRange = 640F;
				homingPower = 0.6f;
				homingDelay = 5;
				
				lightning = 3;
				lightningLengthRand = 10;
				lightningLength = 5;
				lightningDamage = damage / 4;
				
				shootEffect = smokeEffect = Fx.none;
				hitEffect = despawnEffect = new OptionalMultiEffect(new Effect(65f, b -> {
					Draw.color(b.color);
					
					Fill.circle(b.x, b.y, 6f * b.fout(Interp.pow3Out));
					
					Angles.randLenVectors(b.id, 6, 35 * b.fin() + 5, (x, y) -> Fill.circle(b.x + x, b.y + y, 4 * b.fout(Interp.pow2Out)));
				}), NHFx.hitSparkLarge);
				
				despawnHit = false;
				
				rangeOverride = 480f;
			}
			
			@Override
			public void hit(Bullet b, float x, float y){
				b.hit = true;
				hitEffect.at(x, y, b.rotation(), b.team.color);
				hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
				
				Effect.shake(hitShake, hitShake, b);
				
				if(splashDamageRadius > 0 && !b.absorbed){
					Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
					
					if(status != StatusEffects.none){
						Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
					}
				}
				
				for(int i = 0; i < lightning; i++) Lightning.create(b, b.team.color, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
				
				if(!(b.owner instanceof Unit))return;
				Unit from = (Unit)b.owner;
				if(from.dead || !from.isAdded() || from.healthf() > 0.99f) return;
				NHFx.chainLightningFade.at(b.x, b.y, Mathf.random(12, 20), b.team.color, from);
				from.heal(damage / 8);
			}
			
			@Override
			public void despawned(Bullet b){
				despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
				Effect.shake(despawnShake, despawnShake, b);
			}
			
			@Override
			public void removed(Bullet b){
				if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
					Fx.trailFade.at(b.x, b.y, trailWidth, b.team.color, b.trail.copy());
				}
			}
			
			@Override
			public void init(Bullet b) {
				super.init(b);
				
				b.vel.rotate(NHFunc.rand(b.id).random(360));
			}
			
			
			@Override
			public void draw(Bullet b) {
				Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4f, 0.3f));
				
				if(trailLength > 0 && b.trail != null){
					float z = Draw.z();
					Draw.z(z - 0.01f);
					b.trail.draw(Tmp.c1, trailWidth);
					Draw.z(z);
				}
				
				Draw.color(b.team.color, Color.white, 0.35f);
				DrawFunc.arrow(b.x, b.y, 5, 35, -6, b.rotation());
				Draw.color(Tmp.c1);
				DrawFunc.arrow(b.x, b.y, 5, 35, 12, b.rotation());
				
				Draw.reset();
			}
			
			@Override
			public void update(Bullet b) {
				if(!headless && trailLength > 0){
					if(b.trail == null){
						b.trail = new Trail(trailLength);
					}
					b.trail.length = trailLength;
					b.trail.update(b.x, b.y, trailInterp.apply(b.fin()));
				}
				
//				b.vel.setLength(velocityBegin + accelInterp.apply(b.fin()) * velocityIncrease);
				
				if(homingPower > 0.0001f && b.time >= homingDelay){
					Runnable aim = () -> {
						Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> ((e.isGrounded() && collidesGround) || (e.isFlying() && collidesAir)) && !b.collided.contains(e.id), t -> collidesGround);
						if(target != null){
							b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 60f * b.fin()));
						}
					};
					
					if(b.owner instanceof Unit){
						Unit u = (Unit)b.owner;
						if(u.isShooting())b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(u.aimX, u.aimY), homingPower * Time.delta * 60f * b.fin()));
						else aim.run();
					}else aim.run();
				}
				
				if(trailChance > 0){
					if(Mathf.chanceDelta(trailChance)){
						trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
					}
				}
			}
		};
	}
}
