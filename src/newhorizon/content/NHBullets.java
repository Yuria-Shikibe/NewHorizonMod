package newhorizon.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentList;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.*;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.func.NHSetting;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.headless;

public class NHBullets implements ContentList{
	public static String CIRCLE_BOLT, STRIKE, MISSILE_LARGE = "missile-large";
	
	public static
	BulletType
		ultFireball, saviourBullet,
		synchroZeta, synchroThermoPst, synchroFusion, synchroPhase,
		longRangeShoot, longRangeShootRapid, longRangeShootSplash, mineShoot,
		artilleryIrd, artilleryFusion, artilleryPlast, artilleryThermo, artilleryPhase, artilleryMissile,
		railGun1, railGun2, hurricaneType, polyCloud, missileTitanium, missileThorium, missileZeta, missile, missileStrike,
		strikeLaser, tear, skyFrag, hurricaneLaser, hyperBlast, hyperBlastLinker, huriEnergyCloud, warperBullet,
		none, supSky, darkEnrLightning, darkEnrlaser, decayLaser, longLaser, rapidBomb, airRaid,
		blastEnergyPst, blastEnergyNgt, curveBomb, strikeRocket, annMissile, collapserBullet, collapserLaserSmall, guardianBullet,
		strikeMissile, arc_9000, empFrag, empBlot2, empBlot3, antiAirSap, eternity, airRaidMissile, destructionRocket;
		
	
	public void loadFragType(){
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
		
		guardianBullet = new SpeedUpBulletType(0.25f, 160){
			{
				width = 22f;
				height = 40f;
				
				accelInterp = NHInterp.inOut;
				
				pierceCap = 3;
				splashDamage = damage / 4;
				splashDamageRadius = 24f;
				
				trailLength = 30;
				trailWidth = 3f;
				
				accelerateBegin = 0.4f;
				accelerateEnd = 0.75f;
				
				velocityBegin = 1.85f;
				velocityIncrease = 12f;
				
				lifetime = 220f;
				
				trailEffect = NHFx.trailFromWhite;
				trailInterp = Interp.elasticOut;
				
				trailRotation = false;
				trailChance = 0.35f;
				trailParam = 4f;
				
				homingRange = 640F;
				homingPower = 0.1f;
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
			}
			
			@Override
			public float range(){
				return 480;
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
				
				for(int i = 0; i < lightning; i++)Lightning.create(b, b.team.color, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
				
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
				
				b.vel.setLength(velocityBegin + accelInterp.apply(b.fin()) * velocityIncrease);
				
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
		
		collapserBullet = new LightningLinkerBulletType(){{
			effectLightningChance = 0.15f;
			damage = 200;
			backColor = trailColor = lightColor = lightningColor = hitColor = NHColor.thurmixRed;
			size = 10f;
			frontColor = NHColor.thurmixRedLight;
			range = 600f;
			spreadEffect = Fx.none;

			trailWidth = 8f;
			trailLength = 20;
			
			speed = 4f;
			
			disableAccel();
			
			linkRange = 280f;
			
			maxHit = 8;
			drag = 0.0065f;
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 60f;
			splashDamage = lightningDamage = damage / 3f;
			lifetime = 130f;
			despawnEffect = NHFx.lightningHitLarge(hitColor);
			hitEffect = NHFx.sharpBlast(hitColor, frontColor, 35, splashDamageRadius * 1.25f);
			shootEffect = NHFx.hitSpark(backColor, 45f, 12, 60, 3, 8);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		hyperBlastLinker = new LightningLinkerBulletType(){{
			effectLightningChance = 0.15f;
			damage = 220;
			backColor = trailColor = lightColor = lightningColor = hitColor = NHColor.thermoPst;
			size = 8f;
			frontColor = NHColor.thermoPst.cpy().lerp(Color.white, 0.25f);
			range = 200f;
			
			trailWidth = 8f;
			trailLength = 20;
			
			speed = 5f;
			
			disableAccel();
			
			linkRange = 280f;
			
			maxHit = 8;
			drag = 0.085f;
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 120f;
			splashDamage = lightningDamage = damage / 4f;
			lifetime = 50f;
			
			scaleVelocity = false;
			
			despawnEffect = NHFx.lightningHitLarge(hitColor);
			hitEffect = new OptionalMultiEffect(NHFx.hitSpark(backColor, 65f, 22, splashDamageRadius, 4, 16), NHFx.blast(backColor, splashDamageRadius));
			shootEffect = NHFx.hitSpark(backColor, 45f, 12, 60, 3, 8);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		empFrag = new BasicBulletType(3.3f, 3){{
			lifetime = 13;
			drag = 0.01f;
			pierceCap = 4;
			width = 12f;
			height = 28f;
			splashDamageRadius = 20f;
			splashDamage = lightningDamage = damage * 0.75f;
			backColor = lightningColor = trailColor = lightColor = NHColor.lightSkyBack;
			despawnEffect = hitEffect = NHFx.shootCircleSmall(backColor);
			frontColor = Color.white;
			lightning = 3;
			lightningLengthRand = 8;
			smokeEffect = Fx.shootBigSmoke2;
			trailChance = 0.6f;
			trailEffect = NHFx.trailToGray;
			hitShake = 3f;
			hitSound = Sounds.plasmaboom;
		}};
		
		skyFrag = new BasicBulletType(3.8f, 70){
			{
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
				splashDamage = damage * 0.15f;
				splashDamageRadius = 24f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				lightning = 2;
				lightningLength = lightningLengthRand = 8;
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.6f;
				trailEffect = NHFx.skyTrail;
				drag = 0.015f;
				hitShake = 2f;
				hitSound = Sounds.explosion;
			}
			
			@Override
			public void hit(Bullet b){
				super.hit(b);
				UltFire.createChance(b, 12, 0.1f);
			}
		};
	}
	
	public void load(){
		CIRCLE_BOLT = NewHorizon.name("circle-bolt");
		STRIKE = NewHorizon.name("strike");
		
		loadFragType();
		
		saviourBullet = new EmpBulletType(){{
			float rad = 100f;
			
			maxRange = 400f;
			scaleVelocity = true;
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
			
			@Override public float range(){return maxRange;}
		};
		
		synchroZeta = new BasicBulletType(8f, 65f){{
			lifetime = 48f;
			
			width = 8f;
			height = 42f;
			
			shrinkX = 0;
			
			trailWidth = 1.7f;
			trailLength = 9;
			
			trailColor = backColor = hitColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.royal, 0.45f);
			frontColor = backColor.cpy().lerp(Color.white, 0.35f);
			
			shootEffect = NHFx.square(backColor, 45f, 5, 38, 4);
			smokeEffect = Fx.shootBigSmoke;
			
			despawnEffect = hitEffect = new OptionalMultiEffect(NHFx.hitSparkLarge, NHFx.square(backColor, 85f, 5, 52, 5), NHFx.hugeSmoke);
			
			ammoMultiplier = 4;
		}};
		
		synchroFusion = new BasicBulletType(8f, 65f){{
			lifetime = 48f;
			
			width = 8f;
			height = 42f;
			
			shrinkX = 0;
			
			trailWidth = 1.7f;
			trailLength = 9;
			
			trailColor = backColor = hitColor = lightColor = lightningColor = NHItems.fusionEnergy.color;
			frontColor = Color.white;
			
			shootEffect = NHFx.square(backColor, 45f, 5, 38, 4);
			smokeEffect = Fx.shootBigSmoke;
			
			splashDamage = damage;
			splashDamageRadius = 32f;
			incendAmount = 6;
			incendChance = 0.25f;
			incendSpread = splashDamageRadius * 0.75f;
			
			despawnEffect = hitEffect = new OptionalMultiEffect(NHFx.circleOut(backColor, splashDamageRadius * 1.25f), NHFx.hitSparkLarge);
			
			ammoMultiplier = 6;
			
			reloadMultiplier = 0.9f;
			
			status = StatusEffects.melting;
			statusDuration = 120f;
		}};
		
		synchroPhase = new BasicBulletType(8f, 65f){{
			lifetime = 48f;
			
			width = 8f;
			height = 42f;
			
			shrinkX = 0;
			
			trailWidth = 1.7f;
			trailLength = 9;
			
			trailColor = backColor = hitColor = lightColor = lightningColor = Items.phaseFabric.color;
			frontColor = Color.white;
			
			shootEffect = NHFx.square(backColor, 45f, 5, 38, 4);
			frontColor = backColor.cpy().lerp(Color.white, 0.35f);
			
			despawnEffect = hitEffect = NHFx.square(backColor, 85f, 5, 52, 5);
			
			status = NHStatusEffects.emp2;
			statusDuration = 180f;
			
			pierceCap = 4;
			
			reloadMultiplier = 1.15f;
			
			ammoMultiplier = 6;
		}};
		
		synchroThermoPst = new BasicBulletType(8f, 80f){{
			lifetime = 48f;
			
			width = 8f;
			height = 42f;
			
			shrinkX = 0;
			
			trailWidth = 1.7f;
			trailLength = 9;
			
			trailColor = backColor = hitColor = lightColor = lightningColor = NHColor.thermoPst;
			frontColor = backColor.cpy().lerp(Color.white, 0.35f);
			
			shootEffect = NHFx.square(backColor, 45f, 5, 38, 4);
			smokeEffect = Fx.shootBigSmoke;
			
			despawnEffect = hitEffect = NHFx.lightningHitLarge;
			
			lightningDamage = damage;
			lightningLength = 6;
			lightningLengthRand = 12;
			lightning = 3;
			
			ammoMultiplier = 8;
			
			reloadMultiplier = 0.8f;
			
			status = StatusEffects.melting;
			statusDuration = 120f;
		}};
		
		airRaidMissile = new SpeedUpBulletType(2f, 800f, NHBullets.STRIKE){{
			trailLength = 14;
			
			trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = Color.white;
			
			hitSound = Sounds.explosionbig;
			trailChance = 0.075f;
			trailEffect = NHFx.polyTrail;
			drawSize = 120f;
			
			velocityIncrease = 25f;
			accelerateBegin = 0f;
			accelerateEnd = 0.65f;
			
			collides = false;
			scaleVelocity = true;
			hitShake = despawnShake = 16f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 20;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.square(NHColor.darkEnrColor, 50f, 3, 80f, 5f);
			shrinkX = shrinkY = 0;
			splashDamageRadius = 100f;
			splashDamage = lightningDamage = damage;
			height = 66f;
			width = 20f;
			lifetime = 120f;
			
			despawnEffect = NHFx.instHit(backColor, 4, 180f);
			hitEffect = new OptionalMultiEffect(NHFx.largeDarkEnergyHit, NHFx.square(NHColor.darkEnrColor, 100f, 3, 80f, 8f), NHFx.largeDarkEnergyHitCircle);
		}};
		
		antiAirSap = new SapBulletType(){{
			keepVelocity = false;
			sapStrength = 0.4F;
			length = 120.0F;
			damage = 35.0F;
			shootEffect = Fx.shootSmall;
			hitColor = color = NHColor.lightSkyBack;
			despawnEffect = Fx.none;
			width = 0.48F;
			lifetime = 20.0F;
			knockback = -1.24F;
			status = StatusEffects.slow;
		}};
		
		artilleryIrd = new AdaptedArtilleryBulletType(5f, 200f, "large-bomb"){{
			collidesTiles = collidesGround = true;
			collidesAir = collides = false;
			reloadMultiplier = 1.75f;
			shrinkX = shrinkY = 0;
			spin = 4f;
			hitShake = 4f;
			
			trailInterp = NHInterp.parabola4ReversedOver;
			trailLength = NHSetting.enableDetails() ? 25 : 8;
			trailWidth = 2f;
			
			width = height = 27.0F;
			splashDamage = damage;
			splashDamageRadius = 22f;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(frontColor, 0.55f);
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			hitEffect = NHFx.instHit(backColor, 2, 40f);
			despawnEffect = NHFx.crossBlast(backColor, 80f);
			shootEffect = NHFx.shootLineSmall(backColor);
		}};
		
		artilleryFusion = new ArtilleryBulletType(4f, 180f, MISSILE_LARGE){{
			collidesTiles = collidesGround = true;
			collidesAir = collides = false;
			width = height = 32.0F;
			hitShake = 6f;
			splashDamage = damage;
			
			trailInterp = NHInterp.parabola4ReversedOver;
			trailLength = NHSetting.enableDetails() ? 50 : 15;
			trailWidth = 2f;
			
			splashDamageRadius = 44f;
			incendSpread = 6f;
			incendChance = 0.3f;
			incendAmount = 5;
			makeFire = true;
			frontColor = Color.white;
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			backColor = trailColor = lightColor = lightningColor = NHItems.fusionEnergy.color.cpy().lerp(frontColor, 0.05f);
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 80f);
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		artilleryPlast = new ArtilleryBulletType(4f, 120f){{
			collidesTiles = collidesGround = true;
			collidesAir = collides = false;
			reloadMultiplier = 0.95f;
			hitShake = 4f;
			width = 18f;
			height = 36f;
			splashDamage = damage;
			splashDamageRadius = 22f;
			frontColor = Color.white;
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			backColor = trailColor = lightColor = lightningColor = hitColor = Items.plastanium.color.cpy().lerp(frontColor, 0.15f);
			hitEffect = NHFx.lightningHitLarge;
			despawnEffect = Fx.plasticExplosion;
			shootEffect = Fx.plasticExplosion;
			smokeEffect = NHFx.hugeSmoke;
			fragBullets = 2;
			fragBullet = Bullets.fragPlastic;
			fragLifeMin = 0.085f;
			fragLifeMax = 1.225f;
			fragVelocityMax = 0.4f;
			fragVelocityMin = 0.075f;
		}};
		
		artilleryThermo = new AdaptedArtilleryBulletType(3.5f, 250f, "large-bomb"){{
			collidesTiles = collidesGround = true;
			collidesAir = collides = false;
			reloadMultiplier = 0.95f;
			shrinkX = shrinkY = 0;
			spin = 2f;
			hitShake = 8f;
			
			trailInterp = NHInterp.parabola4ReversedOver;
			trailLength = NHSetting.enableDetails() ? 60 : 20;
			trailWidth = 2f;
			
			width = height = 40.0F;
			splashDamage = damage;
			splashDamageRadius = 32f;
			lightning = 3;
			lightningDamage = damage / 3;
			lightningLength = lightningLengthRand = 10;
			frontColor = Color.white;
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			backColor = trailColor = lightColor = lightningColor = NHItems.thermoCorePositive.color;
			hitEffect = NHFx.blast(backColor, 50);
			despawnEffect = NHFx.hitSpark(backColor, 80, 15, 80, 1.7f, 12);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		artilleryPhase = new ArtilleryBulletType(8f, 120f){{
			collidesTiles = collidesGround = true;
			hitShake = 3f;
			width = 14f;
			height = 35f;
			collides = true;
			
			ammoMultiplier = 8;
			reloadMultiplier = 3.5f;
			
			trailInterp = NHInterp.parabola4ReversedOver;
			trailLength = NHSetting.enableDetails() ? 20 : 10;
			trailWidth = 1.5f;
			
			splashDamage = damage / 4;
			splashDamageRadius = 20f;
			frontColor = Color.white;
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			backColor = trailColor = lightColor = lightningColor = Items.phaseFabric.color.cpy().lerp(frontColor, 0.15f);
			hitEffect = NHFx.circleSplash(backColor, 45f, 4, 38f, 6);
			despawnEffect = NHFx.crossBlast(backColor, 100);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.hugeSmoke;
			status = NHStatusEffects.emp2;
			statusDuration = 60f;
		}};
		
		artilleryMissile = new ArtilleryBulletType(7f, 150f){{
			collidesTiles = collidesGround = true;
			hitShake = 2f;
			width = 14f;
			height = 35f;
			
			collides = true;
			
			trailLength = 20;
			trailWidth = 2f;
			
			splashDamage = damage;
			splashDamageRadius = 12f;
			trailParam = 3.5f;
			trailChance = 0.08f;
			trailEffect = NHFx.trailToGray;
			homingDelay = 20f;
			homingPower = 0.075f;
			homingRange = 250f;
			frontColor = Pal.bulletYellow;
			backColor = trailColor = lightColor = lightningColor = Pal.bulletYellowBack;
			hitEffect = Fx.flakExplosionBig;
			despawnEffect = NHFx.shootCircleSmall(backColor);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
		}};
		
		longRangeShoot = new ShieldBreaker(1, 1500, "bullet", 2000){{
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.blue, 0.15f);
			frontColor = Color.white;
			
			velocityBegin = velocityIncrease = 1f;
			trailInterval = 5f;
			trailWidth = 3f;
			trailLength = 50;
			lifetime = 650f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			drawSize = 60f;
			hitShake = despawnShake = 2f;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
			hitEffect = NHFx.instHit(backColor, 2, 80f);
			despawnEffect = NHFx.crossBlast(backColor, 30f);
			height = 47f;
			width = 14f;
		}};
		
		longRangeShootRapid = new ShieldBreaker(6, 1500, "bullet", 2000){{
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.blue, 0.15f);
			frontColor = Color.white;
			
			velocityBegin = velocityIncrease = 6f;
			
			trailWidth = 3f;
			trailLength = 15;
			lifetime = 120f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			trailChance = 0.035f;
			trailEffect = NHFx.trailToGray;
			drawSize = 60f;
			hitShake = despawnShake = 2f;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
			hitEffect = NHFx.instHit(backColor, 2, 80f);
			despawnEffect = NHFx.crossBlast(backColor, 30f);
			height = 47f;
			width = 14f;
		}};
		
		longRangeShootSplash = new ShieldBreaker(1, 1500, "bullet", 2000){{
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 8;
			splashDamageRadius = 60f;
			splashDamage = lightningDamage = 0.5f * damage;
			velocityBegin = velocityIncrease = 1f;
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.blue, 0.15f);
			frontColor = Color.white;
			
			trailInterval = 5f;
			trailWidth = 3f;
			trailLength = 50;
			lifetime = 650f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			drawSize = 60f;
			hitShake = despawnShake = 6f;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
			hitEffect = NHFx.instHit(backColor, 2, 80f);
			despawnEffect = NHFx.lightningHitLarge(backColor);
			height = 47f;
			width = 14f;
		}};
		
		mineShoot = new FlakBulletType(0.75f, 150){{
			sprite = "large-bomb";
			collidesGround = true;
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
			frontColor = NHColor.lightSkyFront;
			
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 12;
			splashDamageRadius = 80f;
			splashDamage = damage;
			lightningDamage = damage / 4;
			
			spin = 4f;
			
			lifetime = 850f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			trailChance = 0.035f;
			trailParam = 6f;
			trailEffect = NHFx.trailToGray;
			drawSize = 60f;
			hitShake = despawnShake = 2f;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.hugeSmoke;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 80f);
			height = 30f;
			width = 30f;
		}};
		
		missileStrike = new MissileBulletType(4.2f, 18){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			pierceCap = 6;
			drag = -0.01f;
			homingPower = 0.125f;
			homingRange = range();
			splashDamageRadius = 6f;
			splashDamage = damage / 8;
			ammoMultiplier = 10f;
			backColor = trailColor = lightColor = NHItems.presstanium.color.cpy().lerp(Color.white, 0.3f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			hitEffect = NHFx.lightningHitSmall(backColor);
			despawnEffect = NHFx.shootCircleSmall(backColor);
			lifetime = 58f;
			inaccuracy = 0;
			
			if(NHSetting.enableDetails()){
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 12;
			}
		}
			@Override
			public void update(Bullet b){
				if(homingPower > 0.0001f && b.time >= homingDelay){
					Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> !b.collided.contains(e.id), t -> collidesGround);
					if(target != null){
						b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
					}
				}
				
				if(trailChance > 0){
					if(Mathf.chanceDelta(trailChance)){
						trailEffect.at(b.x, b.y, trailParam, trailColor);
					}
				}
			}
		};
		
		missile = new MissileBulletType(4.2f, 12){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			drag = -0.01f;
			splashDamageRadius = 8f;
			splashDamage = damage / 2;
			ammoMultiplier = 3f;
			hitEffect = Fx.flakExplosionBig;
			despawnEffect = Fx.flakExplosion;
			lifetime = 58f;
			
			inaccuracy = 0;
			
			if(NHSetting.enableDetails()){
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 12;
			}
		}};
		
		missileTitanium = new MissileBulletType(4.2f, 20){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			pierceCap = 1;
			drag = -0.01f;
			splashDamageRadius = 6f;
			splashDamage = damage / 4;
			ammoMultiplier = 3f;
			backColor = lightColor = Items.titanium.color.cpy().lerp(Color.white, 0.2f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			hitEffect = NHFx.lightningHitSmall(backColor);
			despawnEffect = NHFx.shootCircleSmall(backColor);
			lifetime = 58f;
			
			inaccuracy = 0;
			
			if(NHSetting.enableDetails()){
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 12;
			}
		}};
		
		missileThorium = new MissileBulletType(4.2f, 34){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			pierceCap = 2;
			knockback = 8;
			drag = -0.01f;
			ammoMultiplier = 3f;
			backColor = trailColor = lightColor = Items.thorium.color.cpy().lerp(Color.white, 0.2f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			homingPower = 0.08f;
			lifetime = 58f;
			hitEffect = NHFx.instHit(backColor, 2, 30f);
			despawnEffect = NHFx.shootCircleSmall(backColor);
			
			inaccuracy = 0;
			
			if(NHSetting.enableDetails()){
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 12;
			}
		}};
		
		missileZeta = new MissileBulletType(4.2f, 18){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			drag = -0.01f;
			ammoMultiplier = 3f;
			backColor = trailColor = lightColor = lightningColor =  NHItems.zeta.color.cpy().lerp(Color.white, 0.2f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			splashDamageRadius = 4f;
			splashDamage = damage / 3;
			hitEffect = Fx.smoke;
			despawnEffect = NHFx.lightningHitLarge(backColor);
			lifetime = 58f;
			lightningDamage = damage / 2;
			lightning = 2;
			lightningLength = 10;
			
			inaccuracy = 0;
			
			if(NHSetting.enableDetails()){
				trailColor = NHColor.trail;
				trailWidth = 1f;
				trailLength = 12;
			}
		}};
		
		polyCloud = new SpeedUpBulletType(0.05f, 50){
			@Override public float range(){return 360f;}
		{
			buildingDamageMultiplier = 0.2f;
			width = height = 0;
			trailLength = 0;
			trailWidth = 0;
			lightning = 3;
			lightningLength = 2;
			lightningLengthRand = 18;
			homingDelay = 15f;
			homingPower = 10f;
			homingRange = 320f;
			splashDamage = lightningDamage = damage / 4;
			splashDamageRadius = 12f;
			backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
			frontColor = Color.white;
			trailEffect = NHFx.polyCloud(backColor, 45, 10, 32, 4);
			trailChance = 0;
			pierce = pierceBuilding = true;
			velocityBegin = 1.25f;
			velocityIncrease = 8;
			accelerateBegin = 0.05f;
			accelerateEnd = 0.65f;
			lifetime = 100f;
			hitShake = 2;
			hitSound = Sounds.plasmaboom;
			hitEffect = NHFx.shootCircleSmall(backColor);
			despawnEffect = NHFx.lightningHitLarge(backColor);
			
			status = NHStatusEffects.scannerDown;
			statusDuration = 60f;
		}
			@Override
			public void update(Bullet b){
				if(Mathf.chanceDelta(0.45f))trailEffect.at(b.x, b.y, b.rotation());
				b.collided.clear();
				super.update(b);
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				PosLightning.createRandomRange(b, b.team, b, splashDamageRadius * 30, lightColor, Mathf.chanceDelta(lightning / 10f), 0, 0, PosLightning.WIDTH, 2 + Mathf.random(1), lightning, hitPos -> {
					Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
					NHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), lightningColor);
					NHFx.crossBlast(lightColor).at(hitPos);
					for (int j = 0; j < lightning; j++) {
						Lightning.create(b, lightningColor, lightningDamage < 0.0F ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
					}
					hitSound.at(hitPos, Mathf.random(0.9f, 1.1f));
				});
				
				UltFire.createChance(b.x, b.y, splashDamageRadius * 6, 0.5f, b.team);
			}
			
			@Override
			public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
				super.hitEntity(b, other, initialHealth);
				if(other instanceof Buildingc){
					b.time += b.lifetime() / 90f;
				}
			}
			
			@Override
			public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
				UltFire.create(build.tile);
				
				if(build.team != b.team && direct){
					hit(b);
				}
			}
		};
		
		hurricaneType = new LightningLinkerBulletType(2.5f, 250){{
			disableAccel();
			
			range = 340f;
			
			trailWidth = 8f;
			trailLength = 40;
			
			backColor = trailColor = lightColor = lightningColor = NHColor.lightSkyBack;
			frontColor = Color.white;
			randomGenerateRange = 280f;
			randomLightningNum = 5;
			linkRange = 280f;
			
			scaleVelocity = true;
			
			hitModifier = UltFire::create;
			
			drag = 0.0065f;
			fragLifeMin = 0.3f;
			fragBullets = 11;
			fragBullet = NHBullets.skyFrag;
			hitSound = Sounds.explosionbig;
			drawSize = 40;
			splashDamageRadius = 240;
			splashDamage = 80;
			lifetime = 300;
			despawnEffect = Fx.none;
			hitEffect = new Effect(50, e -> {
				color(NHColor.lightSkyBack);
				Fill.circle(e.x, e.y, e.fout() * 44);
				stroke(e.fout() * 3.2f);
				circle(e.x, e.y, e.fin() * 80);
				stroke(e.fout() * 2.5f);
				circle(e.x, e.y, e.fin() * 50);
				Angles.randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
					stroke(e.fout() * 3.2f);
					lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
				});
				color(Color.white);
				Fill.circle(e.x, e.y, e.fout() * 30);
			});
			shootEffect = new Effect(30f, e -> {
				color(NHColor.lightSkyBack);
				Fill.circle(e.x, e.y, e.fout() * 32);
				color(Color.white);
				Fill.circle(e.x, e.y, e.fout() * 20);
			});
			smokeEffect = new Effect(40f, 100, e -> {
				color(NHColor.lightSkyBack);
				stroke(e.fout() * 3.7f);
				circle(e.x, e.y, e.fin() * 100 + 15);
				stroke(e.fout() * 2.5f);
				circle(e.x, e.y, e.fin() * 60 + 15);
				randLenVectors(e.id, 15, 7f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 16f));
			});
		}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				
				UltFire.createChance(b, splashDamageRadius / 2f, 0.5f);
			}
		};
		
		railGun1 = new TrailFadeBulletType(35f, 2200, STRIKE) {{
			width = 12f;
			height = 36f;
			
			trailLength = 20;
			trailWidth = 2;
			trailInterval = 1f;
			trailRotation = true;
			
			pierce = pierceBuilding = true;
			pierceCap = 3;
			
			ammoMultiplier = 1f;
			
			lifetime = 20f;
			lightningColor = frontColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color;
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor);
			hitEffect = NHFx.instHit(lightningColor);
			smokeEffect = Fx.smokeCloud;
			trailEffect = NHFx.instTrail(lightningColor, 40, true);
			despawnEffect = new OptionalMultiEffect(NHFx.instBomb(lightningColor), NHFx.crossBlast(lightningColor));
			lightningDamage = damage / 7;
			buildingDamageMultiplier = 1.25f;
			hitShake = 8f;
			knockback = 14f;
			
			hitSound = Sounds.explosion;
			despawnSound = Sounds.explosionbig;
		}};
		
		railGun2 = new TrailFadeBulletType(40f, 3000, STRIKE) {{
			width = 16f;
			height = 50f;
			
			trailLength = 18;
			trailWidth = 2;
			trailInterval = 1f;
			trailChance = 0.4f;
			trailRotation = true;
			
			pierce = pierceBuilding = true;
			pierceCap = 6;
			
			ammoMultiplier = 1f;
			
			lifetime = 16f;
			lightningColor = frontColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color;
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor);
			hitEffect = NHFx.instHit(lightningColor, 6, 120);
			smokeEffect = Fx.smokeCloud;
			trailEffect = NHFx.instTrail(lightningColor, 60, true);
			despawnEffect = new OptionalMultiEffect(NHFx.instBomb(lightningColor), NHFx.crossBlast(lightningColor));
			lightningDamage = damage / 7;
			buildingDamageMultiplier = 1.25f;
			hitShake = 12f;
			knockback = 22f;
			
			hitSound = Sounds.explosion;
			despawnSound = Sounds.explosionbig;
		}};
		
		warperBullet = new SpeedUpBulletType(0.35f, 20f, CIRCLE_BOLT){
			{
				shrinkX = shrinkY = 0.35f;
				buildingDamageMultiplier = 1.5f;
				keepVelocity = false;
				
				velocityIncrease = 4f;
				accelerateBegin = 0.01f;
				accelerateEnd = 0.9f;
				
				homingPower = 0;
				hitColor = trailColor = lightningColor = backColor = lightColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				splashDamageRadius = 20;
				splashDamage = damage * 0.3f;
				
				width = height = 8f;
				trailChance = 0.2f;
				trailParam = 1.75f;
				trailEffect = NHFx.trailToGray;
				lifetime = 120f;
				
				collidesAir = false;
				
				hitSound = Sounds.explosion;
				hitEffect = NHFx.square45_4_45;
				shootEffect = NHFx.circleSplash;
				smokeEffect = Fx.shootBigSmoke;
				despawnEffect = NHFx.crossBlast(hitColor, 50);
			}
		};
		
		strikeLaser = new DelayLaserType(400f, 60f){
			@Override
			public void effectDraw(Bullet b){
				Draw.color(NHColor.lightSkyBack);
				float fin = Mathf.clamp(b.time / 60f);
				float fout = Mathf.clamp(1f - fin);
				float fslope = (0.5F - Math.abs(fin - 0.5F)) * 2.0F;
				if(b.time < 60f){
					randLenVectors(b.id, 6, 3 + 50 * fout, (x, y) -> Fill.circle(b.x + x, b.y + y, fin * fin * 5f));
					Lines.stroke(fslope * 2.0F);
					Lines.circle(b.x, b.y, fout * 40f);
					randLenVectors(b.id + 1, 16, 3 + 70 * fout, (x, y) -> lineAngle(b.x + x, b.y + y, Mathf.angle(x, y), fslope * 18 + 5));
				}
			}
			
			{
				colors = new Color[]{NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.3f), NHColor.lightSkyBack, Color.white};
				length = 340f;
				width = 25f;
				lengthFalloff = 0.6f;
				sideLength = 90f;
				sideWidth = 1.35f;
				sideAngle = 40f;
				lightningSpacing = 40.0F;
				lightningLength = 2;
				lightningDelay = 1.1F;
				lightningLengthRand = 10;
				lightningDamage = 180.0F;
				lightningAngleRand = 40.0F;
				lightningColor = NHColor.lightSkyBack;
				smokeEffect = shootEffect = Fx.none;
				hitEffect = NHFx.laserHit(NHColor.lightSkyBack);
				splashDamage = 82.0F;
				splashDamageRadius = 20.0F;
				collidesGround = true;
				lifetime = 38.0F;
				status = StatusEffects.blasted;
				statusDuration = 60.0F;
			}
		};
		
		tear = new ShieldBreaker(3.4f, 60f, 2500f){{
			pierceCap = 8;
			spin = 2.75f;
			width = 16f;
			height = 37f;
			
			trailLength = 12;
			trailWidth = 3f;
			
			shrinkX = shrinkY = 0.001f;
			splashDamage = lightningDamage = damage * 0.6f;
			backColor = lightColor = lightningColor = trailColor = Items.plastanium.color;
			frontColor = Color.white;
			lightning = 2;
			lightningLengthRand = 8;
			lightningLength = 3;
			lifetime = 90f;
			hitSound = Sounds.plasmaboom;
			hitShake = 4f;
			splashDamageRadius = 20f;
			despawnEffect = hitEffect = Fx.plasticExplosionFlak;
			smokeEffect = Fx.shootBigSmoke2;
			shootEffect = Fx.plasticExplosion;
		}};
		
		hyperBlast = new SpeedUpBulletType(3.3f, 400){{
			lifetime = 60;
			
			trailLength = 15;
			drawSize = 250f;
			
			despawnEffect = hitEffect = NHFx.lightningHitLarge(NHItems.thermoCorePositive.color);
			knockback = 12f;
			width = 15f;
			height = 37f;
			splashDamageRadius = 40f;
			splashDamage = lightningDamage = damage * 0.75f;
			backColor = lightColor = lightningColor = trailColor = NHItems.thermoCorePositive.color;
			frontColor = Color.white;
			lightning = 3;
			lightningLength = 8;
			smokeEffect = Fx.shootBigSmoke2;
			trailChance = 0.6f;
			trailEffect = NHFx.trailToGray;
			hitShake = 3f;
			hitSound = Sounds.plasmaboom;
		}};
		
		hurricaneLaser = new AdaptedContinuousLaserBulletType(650){
			{
				incendAmount = 0;
				incendSpread = 0;
				incendChance = 0;
				
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.lightSkyBack.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.6f), NHColor.lightSkyFront, Color.white};
				width = 7f;
				length = 540f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 160f;
				lightColor = hitColor = NHColor.lightSkyBack;
				hitEffect = NHFx.lightSkyCircleSplash;
				shootEffect = NHFx.chargeEffectSmall(NHColor.lightSkyBack, 60f);
				smokeEffect = NHFx.lightSkyCircleSplash;
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				Sounds.laserblast.at(b);
			}
			
			@Override
			public void hit(Bullet b, float x, float y){
				super.hit(b, x, y);
				
				if(Mathf.chanceDelta(0.075))UltFire.create(x, y, b.team());
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				if(b.timer(0, 8)){
					NHFx.lightSkyCircleSplash.at(b);
				}
			}
			
			@Override
			public void draw(Bullet b){
				super.draw(b);
				float f = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1.0F - (b.time - (lifetime - fadeTime)) / fadeTime : 1.0F);
				Draw.color(NHColor.lightSkyBack);
				Fill.circle(b.x, b.y, 18 * f);
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						Draw.color(NHColor.lightSkyBack);
						DrawFunc.tri(b.x, b.y, 7f * f, 86f + Mathf.absin(Time.time * j, 6f, 20f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						Draw.color(Color.white);
						DrawFunc.tri(b.x, b.y, 3f * f, 63f + Mathf.absin(Time.time * j, 6f, 12f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				color(Color.white);
				Fill.circle(b.x, b.y, 13 * f);
				Draw.reset();
			}
		};
		
		none = new BasicBulletType(0, 1, "none"){{
			instantDisappear = true;
			trailEffect = smokeEffect = shootEffect = hitEffect = despawnEffect = Fx.none;
		}};
		
		darkEnrLightning = new PosLightningType(100){{
			lightningColor = NHColor.darkEnrColor;
			maxRange = 800f;
			boltNum = 1;
		}};
		
		supSky = new PosLightningType(65f){{
			lightningColor = hitColor = NHColor.lightSkyBack;
			maxRange = 250f;
		}};
		
		darkEnrlaser = new ContinuousLaserBulletType(420){
			{
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.darkEnrColor.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.darkEnrColor, NHColor.darkEnr};
				width = 18f;
				length = 800f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 35f;
				lightColor = NHColor.darkEnrColor;
				hitEffect = NHFx.darkEnrCircleSplash;
				shootEffect = NHFx.darkEnergyShootBig;
				smokeEffect = NHFx.darkEnergySmokeBig;
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				if(b.timer(0, 8)){
					NHFx.darkEnergySpread.at(b);
				}
			}
			
			@Override
			public void draw(Bullet b){
				super.draw(b);
				color(NHColor.darkEnrColor);
				Fill.circle(b.x, b.y, 26f);
				color(NHColor.darkEnr);
				Fill.circle(b.x, b.y, 9f + 9f * b.fout());
				Draw.reset();
			}
		};
		
		decayLaser = new AdaptedLaserBulletType(600){{
			colors = new Color[]{NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.3f), NHColor.darkEnrColor, Color.white};
			laserEffect = NHFx.darkEnergyLaserShoot;
			length = 880f;
			width = 22f;
			lengthFalloff = 0.6f;
			sideLength = 90f;
			sideWidth = 1.35f;
			sideAngle = 35f;
			largeHit = true;
			hitColor = NHColor.darkEnrColor;
			shootEffect = NHFx.darkEnergyShoot;
			smokeEffect = NHFx.darkEnergySmoke;
		}};
		
		longLaser = new AdaptedLaserBulletType(350){{
			colors = new Color[]{NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.3f), NHColor.lightSkyBack, Color.white};
			length = 360f;
			width = 30f;
			lengthFalloff = 0.6f;
			sideLength = 68f;
			sideWidth = 0.9f;
			sideAngle = 40f;
			largeHit = false;
			hitColor = NHColor.lightSkyBack;
			smokeEffect = Fx.shootBigSmoke2;
			shootEffect = Fx.none;
		}};
		
		rapidBomb = new SpeedUpBulletType(9f, 75, NewHorizon.name("strike")){{
			trailLength = 8;
			trailWidth = 1.75f;
			
			hitSound = Sounds.explosion;
			drawSize = 120f;
			hitShake = despawnShake = 1.3f;
			lightning = 2;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 4;
			splashDamageRadius = 18f;
			splashDamage = lightningDamage = 0.35f * damage;
			height = 42f;
			width = 11f;
			lifetime = 100;
			trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = Color.white;
			hitEffect = NHFx.darkEnrCircleSplash;
		}};
		
		airRaid = new SpeedUpBulletType(9f, 250, STRIKE){{
			hitSound = Sounds.explosionbig;
			trailChance = 0.075f;
			trailEffect = NHFx.polyTrail;
			
			trailLength = 23;
			
			homingPower = 0.08f;
			homingRange = 400f;
			homingDelay = 12;
			scaleVelocity = true;
			hitShake = despawnShake = 5f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 12;
			shootEffect = NHFx.darkEnergyShoot;
			smokeEffect = NHFx.darkEnergySmoke;
			shrinkX = shrinkY = 0;
			splashDamageRadius = 120f;
			splashDamage = lightningDamage = 0.65f * damage;
			height = 66f;
			width = 20f;
			lifetime = 500;
			trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = Color.white;
			
			hitEffect = NHFx.mediumDarkEnergyHit;
		}};
		
		blastEnergyPst = new SpeedUpBulletType(0.85f, 100f, CIRCLE_BOLT){{
			backColor = lightningColor = trailColor = lightColor = NHItems.thermoCorePositive.color.cpy().lerp(Color.white, 0.15f);
			lifetime = 82f;
			ammoMultiplier = 4f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			velocityIncrease = 14f;
			hitShake = despawnShake = 2f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 4;
			homingPower = 0.195f;
			homingRange = 600f;
			homingDelay = 12;
			width = height = 16f;
			splashDamageRadius = 30f;
			lightningDamage = damage * 0.65f;
			splashDamage = 0.65f * damage;
			shrinkX = shrinkY = 0;
			hitEffect = NHFx.crossBlast(backColor);
			despawnEffect = NHFx.hyperBlast(backColor);
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootBigSmoke;
			trailEffect = NHFx.trailToGray;
			trailChance = 0.23f;
			trailParam = 2.7f;
			
			trailLength = 15;
			trailWidth = 3.5f;
			drawSize = 300f;
		}};
		
		blastEnergyNgt = new SpeedUpBulletType(3.85f, 80f){{
			backColor = lightningColor = trailColor = lightColor = NHItems.thermoCoreNegative.color.cpy().lerp(Color.white, 0.025f);
			lifetime = 48f;
			knockback = 4f;
			ammoMultiplier = 8f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			velocityIncrease = 18f;
			hitShake = despawnShake = 5f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 4;
			width = 14f;
			height = 46f;
			pierceCap = 4;
			shrinkX = shrinkY = 0;
			splashDamageRadius = 120f;
			lightningDamage = damage * 0.85f;
			splashDamage = 0.85f * damage;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor);
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootBigSmoke;
			trailEffect = NHFx.trailToGray;
			
			trailLength = 15;
			trailWidth = 3f;
			drawSize = 300f;
			
			inaccuracy = 0;
		}};
		
		curveBomb = new SpeedUpBulletType(4, 80f, "shell"){{
			trailColor = lightningColor = backColor = lightColor = NHColor.thurmixRed;
			frontColor = NHColor.thurmixRedLight;
			lightning = 2;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 6;
			homingPower = 0;
			scaleVelocity = true;
			
			velocityBegin = 2f;
			velocityIncrease = 8f;
			accelInterp = Interp.pow3In;
			trailLength = 15;
			trailWidth = 3.5f;
			drawSize = 250f;
			
			splashDamage = lightningDamage = damage * 0.7f;
			splashDamageRadius = 40f;
			
			width = 22f;
			height = 35f;
			
			hitShake = 3f;
			hitSound = Sounds.explosion;
			hitEffect = NHFx.crossBlast(backColor);
			
			smokeEffect = Fx.shootBigSmoke;
			shootEffect = NHFx.shootCircleSmall(backColor);
			
			despawnEffect = NHFx.lightningHitLarge(backColor);
		}};
		
		strikeRocket = new BasicBulletType(9, 200, STRIKE){{
			trailColor = lightningColor = backColor = lightColor = NHColor.darkEnrColor;
			frontColor = NHColor.darkEnrFront;
			lightning = 2;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 6;
			homingPower = 0;
			lifetime = 100f;
			
			weaveMag = 1.50F;
			weaveScale = 4.0F;
			trailLength = 15;
			drawSize = 250f;
			
			splashDamage = lightningDamage = damage * 0.7f;
			splashDamageRadius = 40f;
			
			width = 9f;
			height = 25f;
			
			hitShake = 3f;
			hitSound = Sounds.explosion;
			hitEffect = NHFx.darkErnExplosion;
			
			smokeEffect = new Effect(45f, e -> {
				color(lightColor, Color.white, e.fout() * 0.7f);
				randLenVectors(e.id, 8, 5 + 55 * e.fin(), e.rotation, 45, (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
			});
			
			shootEffect = NHFx.hugeSmoke;
			
			despawnEffect = new Effect(32f, e -> {
				color(Color.gray);
				Angles.randLenVectors(e.id + 1, 4, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
				color(lightColor, Color.white, e.fin());
				stroke(e.fout() * 2);
				circle(e.x, e.y, e.fin() * 40);
				Fill.circle(e.x, e.y, e.fout() * e.fout() * 13);
				randLenVectors(e.id, 4, 7 + 30 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3));
			});
		}};
		
		destructionRocket = new SpeedUpBulletType(200f, NewHorizon.name("ann-missile")){{
			velocityBegin = 4f;
			velocityIncrease = 8f;
			
			absorbable = false;
			splashDamage = damage;
			splashDamageRadius = 20f;
			incendAmount = 2;
			incendChance = 0.08f;
			incendSpread = 24f;
			makeFire = true;
			lifetime += 12f;
			trailColor = NHColor.trail;
			trailEffect = NHFx.trailToGray;
			trailParam = 2f;
			trailChance = 0.2f;
			trailLength = 15;
			trailWidth = 1.2f;
			
			width = 5;
			height = 18f;
			
			backColor = hitColor = lightColor = lightningColor = NHColor.lightSkyBack;
			frontColor = NHColor.lightSkyFront;
			
			smokeEffect = Fx.none;
			shootEffect = Fx.none;
			hitEffect = NHFx.blast(backColor, splashDamageRadius * 0.75f);
			despawnEffect = NHFx.hitSparkLarge;
			
			collidesAir = false;
			collides = false;
			scaleVelocity = true;
			
			hitShake = despawnShake = 2f;
			despawnSound = hitSound = Sounds.explosion;
		}
			
			@Override
			public void load(){
				backRegion = frontRegion = Core.atlas.find(sprite + NHPixmap.PCD_SUFFIX);
			}
			
			@Override
			public void drawTrail(Bullet b){
				if(trailLength > 0 && b.trail != null){
					b.trail.draw(trailColor, trailWidth);
					b.trail.drawCap(trailColor, trailWidth);
				}
			}
			
			@Override
			public void updateTrail(Bullet b){
				if(!headless && trailLength > 0 && b.time > 5f){
					if(b.trail == null){
						b.trail = new Trail(trailLength);
					}
					b.trail.length = trailLength;
					b.trail.update(b.x, b.y, trailInterp.apply(b.fin()));
				}
			}
			
			@Override
			public void draw(Bullet b){
				drawTrail(b);
				
				float z = Draw.z();
				Draw.z(Layer.flyingUnitLow - 0.2f);
				Tmp.v1.trns(b.rotation(), height / 1.75f).add(b);
				Drawf.shadow(Tmp.v1.x, Tmp.v1.y, height / 1.25f);
				Draw.rect(backRegion, Tmp.v1.x, Tmp.v1.y, b.rotation() - 90);
				Draw.z(z);
			}
			
			public void hit(Bullet b, float x, float y){
				hitEffect.at(x, y, b.rotation(), hitColor);
				hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
				
				Effect.shake(hitShake, hitShake, b);
				
				if(splashDamageRadius > 0 && !b.absorbed){
					Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
					
					if(status != StatusEffects.none){
						Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
					}
					
					if(makeFire){
						UltFire.createChance(x, y, splashDamageRadius, 0.35f, b.team);
					}
				}
				
				for(int i = 0; i < lightning; i++){
					Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
				}
			}
			
			public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
				UltFire.create(build.tile);
				
				if(build.team != b.team && direct){
					hit(b);
				}
			}
		};
		
		annMissile = new BasicBulletType(6.6f, 80f, STRIKE){
			@Override
			public float range(){return 280f;}
			
			{
				trailColor = lightningColor = backColor = lightColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				lightning = 3;
				lightningCone = 360;
				lightningLengthRand = lightningLength = 9;
				splashDamageRadius = 60;
				splashDamage = lightningDamage = damage * 0.7f;
				
				width = 12f;
				height = 30f;
				trailLength = 15;
				drawSize = 250f;
				
				trailParam = 1.4f;
				trailChance = 0.35f;
				lifetime = 60f;
				
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
			}
		};
		
		strikeMissile = new SpeedUpBulletType(5, 80, STRIKE){{
			trailColor = lightningColor = backColor = lightColor = NHColor.thurmixRedLight;
			frontColor = NHColor.thurmixRedLight;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 9;
			splashDamageRadius = 60;
			splashDamage = lightningDamage = damage * 0.7f;
			lifetime = 70f;
			
			velocityBegin = 3f;
			velocityIncrease = 8f;
			
			accelerateBegin = 0.05f;
			accelerateEnd = 0.8f;
			
			accelInterp = Interp.pow2In;
			
			collidesAir = false;
			hitEffect = NHFx.thurmixHit;
			width = 15f;
			height = 55f;
			trailLength = 15;
			drawSize = 250f;
			
			smokeEffect = new Effect(45f, e -> {
				color(lightColor, Color.white, e.fout() * 0.7f);
				randLenVectors(e.id, 8, 5 + 55 * e.fin(), e.rotation, 45, (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
			});
			
			shootEffect = NHFx.hugeSmoke;
			
			despawnEffect = new Effect(32f, e -> {
				color(Color.gray);
				Angles.randLenVectors(e.id + 1, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
				color(lightColor, Color.white, e.fin());
				stroke(e.fout() * 2);
				circle(e.x, e.y, e.fin() * 50);
				Fill.circle(e.x, e.y, e.fout() * e.fout() * 13);
				randLenVectors(e.id, 4, 7 + 40 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3));
			});
		}};
		
		arc_9000 = new LightningLinkerBulletType(2.75f, 550){{
			trailWidth = 4.5f;
			trailLength = 66;
			
			disableAccel();
			
			backColor = trailColor = hitColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = NHColor.darkEnr;
			randomGenerateRange = 280f;
			randomLightningNum = 6;
			linkRange = 280f;
			range = 800f;
			
			drawSize = 500f;
			
			drag = 0.0065f;
			fragLifeMin = 0.1f;
			fragLifeMax = 1f;
			fragVelocityMin = 0.1f;
			fragVelocityMax = 1.25f;
			fragBullets = 12;
			fragBullet = new ArtilleryBulletType(3.75f, 200){
				@Override
				public void update(Bullet b){
					if(b.timer(0, 3)){
						trailEffect.at(b.x, b.y, b.rotation());
					}
				}
				
				{
					backColor = lightColor = lightningColor = NHColor.darkEnrColor;
					frontColor = Color.white;
					
					trailEffect = NHFx.polyTrail(backColor, frontColor, 4.65f, 22f);
					trailChance = 0f;
					despawnEffect = hitEffect = NHFx.darkErnExplosion;
					knockback = 12f;
					lifetime = 90f;
					width = 17f;
					height = 42f;
					collidesTiles = false;
					splashDamageRadius = 80f;
					splashDamage = damage * 0.6f;
					lightning = 3;
					lightningLength = 8;
					smokeEffect = Fx.shootBigSmoke2;
					hitShake = 8f;
					hitSound = Sounds.plasmaboom;
					status = StatusEffects.sapped;
					
					statusDuration = 60f * 10;
				}
			};
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 120f;
			splashDamage = 300;
			lightningDamage = damage * 0.75f;
			
			collidesTiles = true;
			pierce = false;
			collides = false;
			
			ammoMultiplier = 1;
			lifetime = 300;
			hitEffect = Fx.none;
			despawnEffect = NHFx.circleOut(hitColor, splashDamageRadius * 1.5f);
			hitEffect = NHFx.largeDarkEnergyHit;
			shootEffect = NHFx.darkEnergyShootBig;
			smokeEffect = NHFx.darkEnergySmokeBig;
		}
			
			@Override
			public void draw(Bullet b){
				Draw.color(backColor);
				DrawFunc.surround(b.id, b.x, b.y, size * 1.45f, 14, 7,11, b.fin(NHInterp.parabola4Reversed));
			
				super.draw(b);
			}
		};
		
		empBlot2 = new EmpBulletType(){{
			sprite = CIRCLE_BOLT;
			speed = 7.5f;
			drag = 0.0095f;
			damage = 10f;
			status = NHStatusEffects.emp2;
			ammoMultiplier = 3;
			statusDuration = 45f;
			backColor = lightningColor = trailColor = lightColor = NHColor.lightSkyBack.cpy().lerp(Color.white, 0.025f);
			width = height = 14f;
			shrinkX = shrinkY = 0;
			splashDamageRadius = 80f;
			splashDamage = lightningDamage = damage / 3;
			hitShake = despawnShake = 5f;
			lightning = 4;
			lightningCone = 360;
			lightningLengthRand = 16;
			lightningLength = 4;
			trailChance = 0.55f;
			trailEffect = NHFx.trailToGray;
			trailParam = width / 3;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 85, 45);
			collidesAir = collidesGround = true;
			collides = false;
			hitSound = Sounds.plasmaboom;
			inaccuracy = 2;
			fragBullet = empFrag;
			fragBullets = 2;
			fragVelocityMin = fragLifeMin = 0.95f;
			fragVelocityMax = fragLifeMax = 1.05f;
		}};
		
		empBlot3 = new EmpBulletType(){{
			sprite = CIRCLE_BOLT;
			speed = 7.5f;
			drag = 0.0095f;
			damage = 15f;
			
			status = NHStatusEffects.emp3;
			ammoMultiplier = 3;
			statusDuration = 60f;
			backColor = lightningColor = trailColor = lightColor = NHColor.lightSkyBack.cpy().lerp(Color.white, 0.05f);
			width = height = 14f;
			shrinkX = shrinkY = 0;
			splashDamageRadius = 120f;
			splashDamage = lightningDamage = damage / 3;
			hitShake = despawnShake = 5f;
			lightning = 4;
			lightningCone = 360;
			lightningLengthRand = 18;
			lightningLength = 4;
			trailChance = 0.55f;
			trailEffect = NHFx.trailToGray;
			trailParam = width / 3;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 120, 45);
			inaccuracy = 2;
			collidesAir = collidesGround = true;
			collides = false;
			hitSound = Sounds.plasmaboom;
			
			fragBullet = empFrag;
			fragBullets = 3;
			fragVelocityMin = fragLifeMin = 0.95f;
			fragVelocityMax = fragLifeMax = 1.05f;
		}};
		
		eternity = new SpeedUpBulletType(10f, 1000f){
			@Override
			public void draw(Bullet b){
				super.draw(b);
				
				Draw.color(NHColor.darkEnrColor, Color.white, b.fout() * 0.25f);
				
				float rand = Mathf.randomSeed(b.id, 60f);
				float extend = Mathf.curve(b.fin(Interp.pow10Out), 0.075f, 1f);
				float rot = b.fout(Interp.pow10In);
				
				float chargeCircleFrontRad = 20;
				float width = chargeCircleFrontRad * 1.2f;
				Fill.circle(b.x, b.y, width * (b.fout() + 4) / 3.5f);
				
				float rotAngle = b.fdata;
				
				for(int i : Mathf.signs){
					DrawFunc.tri(b.x, b.y, width * b.foutpowdown(), 200 + 570 * extend, rotAngle + 90 * i - 45);
				}
				
				for(int i : Mathf.signs){
					DrawFunc.tri(b.x, b.y, width * b.foutpowdown(), 200 + 570 * extend, rotAngle + 90 * i + 45);
				}
				
				if(NHSetting.enableDetails()){
					float cameraFin = (1 + 2 * DrawFunc.cameraDstScl(b.x, b.y, Vars.mobile ? 200 : 320)) / 3f;
					float triWidth = b.fout() * chargeCircleFrontRad * cameraFin;
					
					for(int i : Mathf.signs){
						Fill.tri(b.x, b.y + triWidth, b.x, b.y - triWidth, b.x + i * cameraFin * chargeCircleFrontRad * (23 + Mathf.absin(10f, 0.75f)) * (b.fout() * 1.25f + 1f), b.y);
					}
				}
				
				float rad = splashDamageRadius * b.fin(Interp.pow5Out) * Interp.circleOut.apply(b.fout(0.15f));
				Lines.stroke(8f * b.fin(Interp.pow2Out));
				Lines.circle(b.x, b.y, rad);
				
				Draw.color(Color.white);
				Fill.circle(b.x, b.y, width * (b.fout() + 4) / 5.5f);
				
				Drawf.light(b.team, b.x, b.y, rad, hitColor, 0.5f);
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				b.fdata = Mathf.randomSeed(b.id, 90);
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				b.fdata += b.vel.len() / 3f;
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				
				float rad = 120;
				float spacing = 3f;
				
				
				Angles.randLenVectors(b.id, 7, splashDamageRadius / 1.25f, ((x, y) -> {
					float nowX = b.x + x;
					float nowY = b.y + y;
					
//					hitEffect.at(nowX, nowY, 0, hitColor);
//					hit(b, nowX, nowY);
					
					Vec2 vec2 = new Vec2(nowX, nowY);
					Team team = b.team;
					float mul = b.damageMultiplier();
					Time.run(Mathf.random(6f, 12f) + Mathf.sqrt(x * x + y * y) / splashDamageRadius * 2f, () -> {
						if(Mathf.chanceDelta(0.4f))hitSound.at(vec2.x, vec2.y, hitSoundPitch, hitSoundVolume);
						despawnSound.at(vec2);
						Effect.shake(hitShake, hitShake, vec2);
						
						for(int i = 0; i < lightning / 2; i++){
							Lightning.create(team, lightningColor, lightningDamage, vec2.x, vec2.y, Mathf.random(360f), lightningLength + Mathf.random(lightningLengthRand));
						}
						
						hitEffect.at(vec2.x, vec2.y, 0, hitColor);
						hitSound.at(vec2.x, vec2.y, hitSoundPitch, hitSoundVolume);
						
						if(fragBullet != null){
							for(int i = 0; i < fragBullets; i++){
								fragBullet.create(team.cores().firstOpt(), team, vec2.x, vec2.y, Mathf.random(360), Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
							}
						}
						
						if(splashDamageRadius > 0 && !b.absorbed){
							Damage.damage(team, vec2.x, vec2.y, splashDamageRadius, splashDamage * mul, collidesAir, collidesGround);
							
							if(status != StatusEffects.none){
								Damage.status(team, vec2.x, vec2.y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
							}
						}
					});
				}));
			}
			
			{
				drawSize = 1200f;
				width = height = shrinkX = shrinkY = 0;
				collides = false;
				despawnHit = false;
				collidesAir = collidesGround = collidesTiles = true;
				splashDamage = 2000f;
				
				velocityBegin = 6f;
				velocityIncrease = -5.9f;
				
				accelerateEnd = 0.75f;
				accelerateBegin = 0.1f;
				
				accelInterp = Interp.pow2;
				trailInterp = Interp.pow10Out;
				
				despawnSound = Sounds.plasmaboom;
				hitSound = Sounds.explosionbig;
				hitShake = 60;
				despawnShake = 100;
				lightning = 12;
				lightningDamage = 2000f;
				lightningLength = 30;
				lightningLengthRand = 50;
				
				status = NHStatusEffects.end;
				statusDuration = 1200f;
				//					ammoMultiplier = 0.1f;
				
				fragBullets = 1;
				fragBullet = NHBullets.arc_9000;
				fragVelocityMin = 0.4f;
				fragVelocityMax = 0.6f;
				fragLifeMin = 0.5f;
				fragLifeMax = 0.7f;
				
				trailWidth = 12F;
				trailLength = 120;
				
				scaleVelocity = true;
				splashDamageRadius = 400f;
				hitColor = lightColor = lightningColor = trailColor = NHColor.darkEnrColor;
				Effect effect = NHFx.crossBlast(hitColor, 420f, 45);
				effect.lifetime += 180;
				despawnEffect = NHFx.circleOut(hitColor, splashDamageRadius);
				hitEffect = new OptionalMultiEffect(NHFx.blast(hitColor, 200f), new Effect(180F, 600f, e -> {
					float rad = 120f;
					
					float f = (e.fin(Interp.pow10Out) + 8) / 9 * Mathf.curve(Interp.slowFast.apply(e.fout(0.75f)), 0f, 0.85f);
					
					Draw.alpha(0.9f * e.foutpowdown());
					Draw.color(Color.white, e.color, e.fin() + 0.6f);
					Fill.circle(e.x, e.y, rad * f);
					
					e.scaled(45f, i -> {
						Lines.stroke(7f * i.fout());
						Lines.circle(i.x, i.y, rad * 3f * i.finpowdown());
						Lines.circle(i.x, i.y, rad * 2f * i.finpowdown());
					});
					
					
					Draw.color(Color.white);
					Fill.circle(e.x, e.y, rad * f * 0.75f);
					
					Drawf.light(e.x, e.y, rad * f * 2f, Draw.getColor(), 0.7f);
				}).layer(Layer.effect + 0.001f), effect, new Effect(260, 460f, e -> {
					Draw.blend(Blending.additive);
					Draw.z(Layer.flyingUnit - 0.8f);
					float radius = e.fin(Interp.pow3Out) * 230;
					Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(NHColor.darkEnrColor).a(e.fout(Interp.pow10Out)));
					Draw.blend();
				}));
			}
		};
	}
	
}














