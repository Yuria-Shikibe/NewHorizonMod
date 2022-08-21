package newhorizon.content;

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
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.LightningLinkerBulletType;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class NHBullets{
	public static String CIRCLE_BOLT, STRIKE, MISSILE_LARGE = "missile-large";
	
	public static BulletType
			warperBullet,
			hyperBlastLinker, hyperBlast,
			arc_9000, eternity,
			synchroZeta, synchroThermoPst, synchroFusion, synchroPhase,
			missileTitanium, missileThorium, missileZeta, missileNormal, missileStrike,
			ultFireball, basicSkyFrag, annMissile, guardianBullet, saviourBullet;
	
	private static void loadPriority(){
		warperBullet = new BasicBulletType(4f, 20f, CIRCLE_BOLT){
			{
				shrinkX = shrinkY = 0.35f;
				buildingDamageMultiplier = 1.5f;
				keepVelocity = false;
				
//				velocityIncrease = 4f;
//				accelerateBegin = 0.01f;
//				accelerateEnd = 0.9f;
				
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
			
			linkRange = 280f;
			
			maxHit = 8;
			drag = 0.085f;
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 120f;
			splashDamage = lightningDamage = damage / 4f;
			lifetime = 50f;
			
			scaleLife = false;
			
			despawnEffect = NHFx.lightningHitLarge(hitColor);
			hitEffect = new OptionalMultiEffect(NHFx.hitSpark(backColor, 65f, 22, splashDamageRadius, 4, 16), NHFx.blast(backColor, splashDamageRadius));
			shootEffect = NHFx.hitSpark(backColor, 45f, 12, 60, 3, 8);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		hyperBlast = new BasicBulletType(3.3f, 400){{
			lifetime = 60;
			
			trailLength = 15;
			drawSize = 250f;
			
			drag = 0.0075f;
			
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
	}
	
	public static void load(){
		CIRCLE_BOLT = NewHorizon.name("circle-bolt");
		STRIKE = NewHorizon.name("strike");
		
		loadPriority();
		
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
					Fx.rand.setSeed(e.id);
					Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin());
					Lines.stroke(1.75f * e.fout());
					if(NHSetting.enableDetails())Lines.spikes(e.x, e.y, Fx.rand.random(14, 28) * e.finpow(), Fx.rand.random(1, 5) * e.fout() + Fx.rand.random(5, 8) * e.fin(NHInterp.parabola4Reversed), 4, 45);
					Lines.square(e.x, e.y, Fx.rand.random(4, 14) * e.fin(Interp.pow3Out), 45);
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
		
		missileStrike = new MissileBulletType(4.2f, 18){{
			width = 8f;
			height = 8f;
			shrinkY = 0f;
			pierceCap = 6;
			drag = -0.01f;
			homingPower = 0.125f;
			homingRange = 120f;
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
		
		missileNormal = new MissileBulletType(4.2f, 12){{
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
		
		arc_9000 = new LightningLinkerBulletType(2.75f, 550){{
			trailWidth = 4.5f;
			trailLength = 66;
			
			chargeEffect = new OptionalMultiEffect(NHFx.darkEnergyCharge, NHFx.darkEnergyChargeBegin);
			
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
			intervalBullets = 2;
			intervalBullet = fragBullet = new FlakBulletType(3.75f, 200){
				@Override
				public void update(Bullet b){
					if(b.timer(0, 3)){
						trailEffect.at(b.x, b.y, b.rotation());
					}
				}
				
				{
					frontColor = trailColor = lightColor = lightningColor = NHColor.darkEnrColor;
					backColor = NHColor.darkEnrColor;
					
					trailLength = 12;
					trailWidth = 1f;
					
					trailEffect = NHFx.polyTrail(backColor, frontColor, 4.65f, 22f);
					trailChance = 0f;
					despawnEffect = hitEffect = NHFx.darkErnExplosion;
					knockback = 12f;
					lifetime = 90f;
					width = 17f;
					height = 42f;
					collidesTiles = false;
					splashDamageRadius = 60f;
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
				
				lifetime = 160f;
				
				trailEffect = NHFx.trailFromWhite;
				
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
		};
		
		eternity = new BasicBulletType(10f, 1000f){
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
				
				Drawf.light(b.x, b.y, rad, hitColor, 0.5f);
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
				
				
				Angles.randLenVectors(b.id, 8, splashDamageRadius / 1.25f, ((x, y) -> {
					float nowX = b.x + x;
					float nowY = b.y + y;
					
					//					hitEffect.at(nowX, nowY, 0, hitColor);
					//					hit(b, nowX, nowY);
					
					Vec2 vec2 = new Vec2(nowX, nowY);
					Team team = b.team;
					float mul = b.damageMultiplier();
					Time.run(Mathf.random(6f, 24f) + Mathf.sqrt(x * x + y * y) / splashDamageRadius * 3f, () -> {
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
				splashDamage = 4000f;
				pierceArmor = true;
				drag = 0.01f;
				trailInterp = Interp.pow10Out;
				
				despawnSound = Sounds.plasmaboom;
				hitSound = Sounds.explosionbig;
				hitShake = 60;
				despawnShake = 100;
				lightning = 12;
				lightningDamage = 2000f;
				lightningLength = 50;
				lightningLengthRand = 80;
				
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
				
				scaleLife = true;
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
