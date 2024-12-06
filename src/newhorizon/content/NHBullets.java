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
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.*;
import mindustry.entities.abilities.MoveEffectAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.*;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.units.AdaptedMissileUnitType;
import newhorizon.expand.units.unitEntity.PesterEntity;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.EffectWrapper;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class NHBullets{
	public static String CIRCLE_BOLT, STRIKE, MISSILE_LARGE = "missile-large", MINE_BULLET = "mine-bullet";
	
	public static UnitType airRaidMissile, skyMissile;
	
	public static BulletType
			lightningAir,
			
			artilleryHydro, artilleryMulti, artilleryNgt, artilleryFusion, artilleryPhase,
	
			shieldDestroyer, ancientArtilleryProjectile,
			ancientBall, ancientStd,
			pesterBlackHole, nuBlackHole, laugraBullet,
			collapserBullet,
			railGun1, railGun2, railGun3,
			declineProjectile, atomSeparator, blastEnergyPst, blastEnergyNgt,
			warperBullet, airRaidBomb,
			hyperBlastLinker, hyperBlast,
			arc_9000, eternity, arc_9000_frag,
			synchroZeta, synchroThermoPst, synchroFusion, synchroPhase,
			missileTitanium, missileThorium, missileZeta, missileNormal, missileStrike,
			ultFireball, basicSkyFrag, annMissile, guardianBullet, guardianBulletLightningBall, saviourBullet;
	
	private static void loadPriority(){
		arc_9000_frag = new FlakBulletType(3.75f, 200){
			{
				trailColor = lightColor = lightningColor = NHColor.darkEnrColor;
				backColor = NHColor.darkEnrColor;
				frontColor = NHColor.darkEnrFront;
				
				trailLength = 14;
				trailWidth = 2.7f;
				trailRotation = true;
				trailInterval = 3;
				
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
		
		lightningAir = new BulletType(0.0001f, 0f){{
			lifetime = Fx.lightning.lifetime;
			hitEffect = Fx.hitLancer;
			despawnEffect = Fx.none;
			status = StatusEffects.shocked;
			statusDuration = 10f;
			hittable = false;
			collidesGround = false;
			lightColor = Color.white;
		}};
		
		shieldDestroyer = new ShieldBreakerType(22f, 0, STRIKE, 3000){{
			fragSpawnSpacing = 0.5f;
			hitColor = trailColor = lightningColor = backColor = lightColor = NHColor.ancientLightMid;
			frontColor = NHColor.ancientLight;
			
			absorbable = hittable = false;
			collides = false;
			shrinkX = shrinkY = 0;
			trailLength = 60;
			trailWidth = 2.2f;
			
			width = 18f;
			height = 55f;
			
			hitSoundVolume = 3;
			hitSound = NHSounds.shock;
			suppressionRange = splashDamageRadius;
			suppressionDuration = 600;
			
			scaledSplashDamage = true;
			splashDamageRadius = 240;
			status = NHStatusEffects.entangled;
			statusDuration = 300f;
			despawnHit = true;
			
			
			trailEffect = NHFx.polyCloud(backColor, 30f, 8f, 18f, 4);
			trailChance = 0.4f;
			trailInterval = 2;
			
			hitEffect = NHFx.square45_8_45;
			despawnEffect = new OptionalMultiEffect(
				NHFx.hitSparkHuge,
				NHFx.smoothColorCircle(hitColor, splashDamageRadius + 50f, 95f),
				NHFx.spreadOutSpark(160f, splashDamageRadius + 40f, 72, 4, 72f, 13f, 4f, Interp.pow3Out)
			);
		}};
		
		warperBullet = new AccelBulletType(4f, 20f, CIRCLE_BOLT){
			{
				shrinkX = shrinkY = 0.35f;
				buildingDamageMultiplier = 1.5f;
				keepVelocity = false;
				
				velocityBegin = 0.5f;
				velocityIncrease = 3f;
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
			hitEffect = new OptionalMultiEffect(NHFx.hitSpark(backColor, 65f, 22, splashDamageRadius, 4, 16), NHFx.blast(backColor, splashDamageRadius / 2f));
			shootEffect = NHFx.hitSpark(backColor, 45f, 12, 60, 3, 8);
			smokeEffect = NHFx.hugeSmokeGray;
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
		
		artilleryHydro = new ArtilleryBulletType(3f, 120f){{
			scaledSplashDamage = true;
			splashDamage = damage;
			splashDamageRadius = 16;
			inaccuracy = 4;
			reloadMultiplier = 1.3f;
			
			backColor = hitColor = lightColor = lightningColor = trailColor = Pal.techBlue;
			frontColor = NHColor.lightSkyFront;
			
			sprite = MINE_BULLET;
			width = height = 10;
			shrinkX = shrinkY = 0.2f;
			
			trailWidth = 2.1f;
			trailLength = 16;
			trailInterp = Interp.slope;
			
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootSmokeDisperse;
			despawnEffect = EffectWrapper.wrap(NHFx.circleOut, backColor, splashDamageRadius + 5);
			hitEffect = NHFx.circleSplash;
			trailParam  = 1.2f;
		}};
		
		artilleryMulti = new ArtilleryBulletType(6f, 160f){{
			scaledSplashDamage = true;
			splashDamage = damage;
			splashDamageRadius = 8;
			inaccuracy = -1;
			
			backColor = hitColor = lightColor = lightningColor = trailColor = NHItems.multipleSteel.color;
			frontColor = NHColor.lightSkyFront;
			
			sprite = MINE_BULLET;
			width = height = 8;
			shrinkX = shrinkY = 0.2f;
			
			trailWidth = 1.8f;
			trailLength = 10;
			trailInterp = Interp.slope;
			
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootSmokeDisperse;
			despawnEffect = NHFx.square45_4_45;
			hitEffect = EffectWrapper.wrap(NHFx.lightningHitSmall, backColor, 10);
			trailParam = 1f;
			
			fragBullet = new ShrapnelBulletType(){{
				lifetime = 18f;
				length = 25f;
				damage = 180.0F;
				serrations = 2;
				status = StatusEffects.shocked;
				statusDuration = 60f;
				fromColor = NHColor.lightSkyFront;
				toColor = NHColor.lightSkyBack;
				serrationSpaceOffset = 40f;
				width = 4f;
				despawnEffect = hitEffect = Fx.none;
			}};
			
			fragBullets = 2;
			fragRandomSpread = 0;
			fragSpread = 72;
			fragAngle = 36f;
		}};
		
		artilleryNgt = new TrailFadeBulletType(2f, 250){{
			tracers = 1;
			tracerStroke = 1.2f;
			tracerSpacing = 4f;
			tracerUpdateSpacing = 2.3f;
			hitBlinkTrail = false;
			reloadMultiplier = 0.8f;
			
			collidesTiles = false;
			collides = false;
			collidesAir = false;
			scaleLife = true;
			hitShake = 1f;
			hitSound = Sounds.explosion;
			
			scaledSplashDamage = true;
			splashDamage = damage;
			splashDamageRadius = 55;
			inaccuracy = 2;
			
			lightning = 2;
			lightningLength = 3;
			lightningLengthRand = 6;
			
			bulletInterval = 3;
			intervalBullets = 1;
			intervalBullet = new AdaptedLightningBulletType(){{
				lightColor = lightningColor = NHItems.thermoCoreNegative.color;
				lightningLength = 3;
				lightningLengthRand = 5;
			}};
			
			backColor = hitColor = lightColor = lightningColor = trailColor = NHItems.thermoCoreNegative.color;
			frontColor = Color.white;
			
			sprite = MINE_BULLET;
			width = height = 14;
			shrinkX = shrinkY = 0.2f;
			shrinkInterp = Interp.slope;
			
			trailWidth = 2.6f;
			trailLength = 22;
			trailInterp = Interp.slope;
			
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootSmokeDisperse;
			despawnEffect = NHFx.blast(backColor, 40);
			hitEffect = NHFx.hitSparkHuge;
			
			fragBullet = new ShrapnelBulletType(){{
				lifetime = 18f;
				length = 22f;
				damage = 50.0F;
				serrations = 2;
				status = StatusEffects.shocked;
				statusDuration = 60f;
				fromColor = NHColor.lightSkyFront;
				toColor = NHColor.lightSkyBack;
				serrationSpaceOffset = 40f;
				width = 5f;
				despawnEffect = hitEffect = Fx.none;
			}};
			
			fragBullets = 4;
			fragRandomSpread = 0;
			fragSpread = 90;
			fragAngle = 45;
		}};
		
		artilleryFusion = new ArtilleryBulletType(6f, 180){{
			scaledSplashDamage = true;
			splashDamage = damage;
			splashDamageRadius = 63;
			inaccuracy = 3;
			
			backColor = hitColor = lightColor = lightningColor = trailColor = NHItems.fusionEnergy.color;
			frontColor = Color.white;
			
			sprite = MINE_BULLET;
			width = height = 10;
			shrinkX = shrinkY = 0.2f;
			
			trailWidth = 2f;
			trailLength = 10;
			trailInterp = Interp.slope;
			
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = Fx.shootSmokeDisperse;
			despawnEffect = NHFx.blast(backColor, splashDamageRadius * 0.66f);
			hitEffect = Fx.none;
			trailParam = 1.15f;
			
			fragBullets = 2;
			fragRandomSpread = 0;
			fragSpread = 72;
			fragAngle = 36f;
			
			incendAmount = 2;
			incendChance = 0.7f;
			incendSpread = splashDamageRadius;
		}};
		
		artilleryPhase = new AccelBulletType(6, 90){{
			velocityBegin = 3;
			velocityIncrease = 9;
			accelerateBegin = 0.075f;
			accelerateEnd = 0.77f;
			
			lifetime = 80;
			
			pierceCap = 2;
			inaccuracy = -1;
			
			backColor = hitColor = lightColor = lightningColor = trailColor = Items.phaseFabric.color;
			frontColor = Color.white;
			
			status = NHStatusEffects.emp3;
			statusDuration = 90f;
			
			shootEffect = EffectWrapper.wrap(NHFx.shootLine(30, 30), backColor);
			smokeEffect = Fx.shootSmokeDisperse;
			hitEffect = NHFx.hitSparkLarge;
			despawnEffect = NHFx.square45_4_45;
			
			ammoMultiplier = 6;
			reloadMultiplier = 3f;
			buildingDamageMultiplier = 0.75f;
			
			collidesAir = false;
			
			width = 12;
			height = 33;
			trailWidth = 2.6f;
			trailLength = 10;
			trailEffect = NHFx.polyTrail;
			trailParam = 4;
			trailInterval = 4;
		}
			
			@Override
			public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
				super.hitTile(b, build, x, y, initialHealth, direct);
				
				build.applySlowdown(0.2f, statusDuration);
				build.applyHealSuppression(statusDuration);
			}
		};
		
		ancientArtilleryProjectile = new ShieldBreakerType(7f, 6000, NHBullets.MISSILE_LARGE, 7000){{
			backColor = trailColor = lightColor = lightningColor = hitColor = NHColor.ancientLightMid;
			frontColor = NHColor.ancientLight;
			trailEffect = NHFx.hugeTrail;
			trailParam = 6f;
			trailChance = 0.2f;
			trailInterval = 3;
			
			lifetime = 200f;
			scaleLife = true;
			
			trailWidth = 5f;
			trailLength = 55;
			trailInterp = Interp.slope;
			
			lightning = 6;
			lightningLength = lightningLengthRand = 22;
			splashDamage = damage;
			lightningDamage = damage / 15;
			splashDamageRadius = 120;
			scaledSplashDamage = true;
			despawnHit = true;
			collides = false;
			
			shrinkY = shrinkX = 0.33f;
			width = 17f;
			height = 55f;
			
			despawnShake = hitShake = 12f;
			
			hitEffect = new MultiEffect(NHFx.square(hitColor, 200, 20 ,splashDamageRadius + 80, 10), NHFx.lightningHitLarge, NHFx.hitSpark(hitColor, 130, 85, splashDamageRadius * 1.5f, 2.2f, 10f), NHFx.subEffect(140, splashDamageRadius + 12, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
				float fout = Interp.pow2Out.apply(1 - fin);
				for(int s : Mathf.signs) {
					Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * NHFx.fout(fin, 0.25f), rot + s * 90);
				}
			})));
			despawnEffect = NHFx.circleOut(145f, splashDamageRadius + 15f, 3f);
			
			shootEffect = EffectWrapper.wrap(NHFx.missileShoot, hitColor);//NHFx.blast(hitColor, 45f);
			smokeEffect = NHFx.instShoot(hitColor, frontColor);
			
			despawnSound = hitSound = Sounds.largeExplosion;
			
			fragBullets = 22;
			fragBullet = new BasicBulletType(2f, 300, NHBullets.CIRCLE_BOLT){{
				width = height = 10f;
				shrinkY = shrinkX = 0.7f;
				backColor = trailColor = lightColor = lightningColor = hitColor = NHColor.ancientLightMid;
				frontColor = NHColor.ancientLight;
				trailEffect = Fx.missileTrail;
				trailParam = 3.5f;
				splashDamage = 80;
				splashDamageRadius = 40;
				
				lifetime = 18f;
				
				lightning = 2;
				lightningLength = lightningLengthRand = 4;
				lightningDamage = 30;
				
				hitSoundVolume /= 2.2f;
				despawnShake = hitShake = 4f;
				despawnSound = hitSound = Sounds.dullExplosion;
				
				trailWidth = 5f;
				trailLength = 35;
				trailInterp = Interp.slope;
				
				despawnEffect = NHFx.blast(hitColor, 40f);
				hitEffect = NHFx.hitSparkHuge;
			}};
			
			fragLifeMax = 5f;
			fragLifeMin = 1.5f;
			fragVelocityMax = 2f;
			fragVelocityMin = 0.35f;
		}};
		
		ancientStd = new AccelBulletType(2.85f, 120f){{
			frontColor = NHColor.ancientLight;
			backColor = lightningColor = hitColor = lightColor = NHColor.ancient;
			trailColor = NHColor.ancientLightMid;
			lifetime = 126f;
			knockback = 2f;
			ammoMultiplier = 8f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			
			status = NHStatusEffects.entangled;
			statusDuration = 30f;
			
			despawnSound = hitSound = Sounds.dullExplosion;
			hitSoundVolume /= 4f;
			
			velocityBegin = 8f;
			velocityIncrease = -5f;
			
			homingDelay = 20f;
			homingPower = 0.05f;
			homingRange = 120f;
			
			despawnHit = pierceBuilding = true;
			hitShake = despawnShake = 5f;
			lightning = 1;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 4;
			width = 10f;
			height = 35f;
			pierceCap = 8;
			shrinkX = shrinkY = 0;
			
			lightningDamage = damage * 0.85f;
			
			hitEffect = NHFx.hitSparkLarge;
			despawnEffect = NHFx.square45_6_45;
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = NHFx.hugeSmokeGray;
			trailEffect = NHFx.trailToGray;
			
			trailLength = 15;
			trailWidth = 2f;
			drawSize = 300f;
		}};
		
		ancientBall = new AccelBulletType(2.85f, 240f, MINE_BULLET){{
			frontColor = Color.white;
			backColor = lightningColor = trailColor = hitColor = lightColor = NHColor.ancient;
			lifetime = 95f;
			
			spin = 3f;
			
			status = NHStatusEffects.entangled;
			statusDuration = 300f;
			
			accelerateBegin = 0.15f;
			accelerateEnd = 0.95f;
			
			despawnSound = hitSound = Sounds.titanExplosion;
			
			velocityBegin = 8f;
			velocityIncrease = -7.5f;
			
			collides = false;
			scaleLife = scaledSplashDamage = true;
			despawnHit = true;
			hitShake = despawnShake = 18f;
			lightning = 4;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 10;
			width = height = 30;
			shrinkX = shrinkY = 0;
			
			splashDamageRadius = 120f;
			splashDamage = 800f;
			
			lightningDamage = damage * 0.85f;
			
			hitEffect = NHFx.hitSparkLarge;
			despawnEffect = NHFx.square45_6_45;
			trailEffect = NHFx.trailToGray;
			
			trailLength = 15;
			trailWidth = 5f;
			drawSize = 300f;
			
			shootEffect = NHFx.instShoot(backColor, frontColor);
			smokeEffect = NHFx.lightningHitLarge;
			
			hitEffect = new Effect(90, e -> {
				Draw.color(backColor, frontColor, e.fout() * 0.7f);
				Fill.circle(e.x, e.y, e.fout() * height / 1.25f);
				Lines.stroke(e.fout() * 3f);
				Lines.circle(e.x, e.y, e.fin() * 80);
				Lines.stroke(e.fout() * 2f);
				Lines.circle(e.x, e.y, e.fin() * 50);
				Angles.randLenVectors(e.id, 35, 18 + 100 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 12 + 4));
				
				Draw.color(frontColor);
				Fill.circle(e.x, e.y, e.fout() * height / 1.75f);
			});
			despawnEffect = new OptionalMultiEffect(NHFx.hitSparkHuge, NHFx.instHit(backColor, 3, 120f));
			
			fragBullets = 3;
			fragBullet = new LaserBulletType(){{
				length = 460f;
				damage = 4060f;
				width = 45f;
				
				status = NHStatusEffects.entangled;
				statusDuration = 120f;
				
				lifetime = 65f;
				
				splashDamage = 800;
				splashDamageRadius = 120;
				hitShake = 18f;
				
				lightningSpacing = 35f;
				lightningLength = 8;
				lightningDelay = 1.1f;
				lightningLengthRand = 15;
				lightningDamage = 450;
				lightningAngleRand = 40f;
				scaledSplashDamage = largeHit = true;
				
				lightningColor = trailColor = hitColor = lightColor = Items.surgeAlloy.color.cpy().lerp(Pal.accent, 0.055f);
				
				despawnHit = false;
				hitEffect = new Effect(90, 500, e -> {
					Draw.color(backColor, frontColor, e.fout() * 0.7f);
					Fill.circle(e.x, e.y, e.fout() * height / 1.55f);
					Lines.stroke(e.fout() * 3f);
					Lines.circle(e.x, e.y, e.fin(Interp.pow3Out) * 80);
					Angles.randLenVectors(e.id, 18, 18 + 100 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 7f));
					
					Draw.color(frontColor);
					Fill.circle(e.x, e.y, e.fout() * height / 2f);
				});
				
				sideAngle = 15f;
				sideWidth = 0f;
				sideLength = 0f;
				colors = new Color[]{hitColor.cpy().a(0.2f), hitColor, Color.white};
			}
				
				@Override
				public void despawned(Bullet b){
					//							super.despawned(b);
				}
				
				@Override
				public void init(Bullet b){
					Vec2 p = new Vec2().set(NHFunc.collideBuildOnLength(b.team, b.x, b.y, length, b.rotation(), bu -> true));
					
					float resultLength = b.dst(p), rot = b.rotation();
					
					b.fdata = resultLength;
					laserEffect.at(b.x, b.y, rot, resultLength * 0.75f);
					
					if(lightningSpacing > 0){
						int idx = 0;
						for(float i = 0; i <= resultLength; i += lightningSpacing){
							float cx = b.x + Angles.trnsx(rot,  i),
									cy = b.y + Angles.trnsy(rot, i);
							
							int f = idx++;
							
							for(int s : Mathf.signs){
								Time.run(f * lightningDelay, () -> {
									if(b.isAdded() && b.type == this){
										Lightning.create(b, lightningColor,
												lightningDamage < 0 ? damage : lightningDamage,
												cx, cy, rot + 90*s + Mathf.range(lightningAngleRand),
												lightningLength + Mathf.random(lightningLengthRand));
									}
								});
							}
						}
					}
				}
				
				@Override
				public void draw(Bullet b){
					float realLength = b.fdata;
					
					float f = Mathf.curve(b.fin(), 0f, 0.2f);
					float baseLen = realLength * f;
					float cwidth = width;
					float compound = 1f;
					
					Tmp.v1.trns(b.rotation(), baseLen);
					
					for(Color color : colors){
						Draw.color(color);
						Lines.stroke((cwidth *= lengthFalloff) * b.fout());
						Lines.lineAngle(b.x, b.y, b.rotation(), baseLen, false);
						
						Fill.circle(Tmp.v1.x + b.x, Tmp.v1.y + b.y, Lines.getStroke() * 2.2f);
						Fill.circle(b.x, b.y, 1f * cwidth * b.fout());
						compound *= lengthFalloff;
					}
					Draw.reset();
					Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * b.fout(), colors[0], 0.6f);
				}
			};
		}};
		
		pesterBlackHole = new EffectBulletType(120){{
			despawnHit = true;
			splashDamageRadius = 240;
			
			lightningDamage = 2000;
			lightning = 2;
			lightningLength = 4;
			lightningLengthRand = 8;
			
			scaledSplashDamage = true;
			collidesAir = collidesGround = collidesTiles = true;
			splashDamage = 3800;
			damage = 10000;
		}
			
			@Override
			public void draw(Bullet b){
				if(!(b.data instanceof Seq))return;
				Seq<Sized> data = (Seq<Sized>)b.data;
				
				Draw.color(b.team.color, Color.white, b.fin() * 0.7f);
				Draw.alpha(b.fin(Interp.pow3Out) * 1.1f);
				Lines.stroke(2 * b.fout());
				for(Sized s : data){
					if(s instanceof Building){
						Fill.square(s.getX(), s.getY(), s.hitSize() / 2);
					}else{
						Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5f + b.fout() * 2f), s.hitSize() / 2f * b.fslope() + 12 * b.fin(), 4, 45);
					}
				}
				
				Drawf.light(b.x, b.y, b.fdata, b.team.color, 0.3f + b.fin() * 0.8f);
			}
			
			public void hitT(Sized target, Entityc o, Team team, float x, float y){
				for(int i = 0; i < lightning; i++){
					Lightning.create(team, team.color, lightningDamage, x, y, Mathf.random(360), lightningLength + Mathf.random(lightningLengthRand));
				}
				
				if(target instanceof Unit){
					if(((Unit)target).health > 1000)PesterEntity.hitter.create(o, team, x, y, 0);
				}
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				
				if(!(b.data instanceof Seq))return;
				Seq<Sized> data = (Seq<Sized>)b.data;
				data.remove(d -> !((Healthc)d).isValid());
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				
				float rad = 33;
				
				Vec2 v = new Vec2().set(b);
				Team t = b.team;
				
				for(int i = 0; i < 5; i++){
					Time.run(i * 0.35f + Mathf.random(2), () -> {
						Tmp.v1.rnd(rad / 3).scl(Mathf.random());
						NHFx.shuttle.at(v.x + Tmp.v1.x, v.y + Tmp.v1.y, Tmp.v1.angle(), t.color, Mathf.random(rad * 3f, rad * 12f));
					});
				}
				
				if(!(b.data instanceof Seq))return;
				Entityc o = b.owner();
				Seq<Sized> data = (Seq<Sized>)b.data;
				for(Sized s : data){
					float size = Math.min(s.hitSize(), 85);
					Time.run(Mathf.random(44), () -> {
						if(Mathf.chance(0.32) || data.size < 8)NHFx.shuttle.at(s.getX(), s.getY(), 45, t.color, Mathf.random(size * 3f, size * 12f));
						hitT(s, o, t, s.getX(), s.getY());
					});
				}
				
				createSplashDamage(b, b.x, b.y);
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				if(!(b.data instanceof Float))return;
				float fdata = (Float)b.data();
				
				Seq<Sized> data = new Seq<>();
				
				Vars.indexer.eachBlock(null, b.x, b.y, fdata, bu -> bu.team != b.team, data::add);
				
				Groups.unit.intersect(b.x - fdata / 2, b.y - fdata / 2, fdata, fdata, u -> {
					if(u.team != b.team)data.add(u);
				});
				
				b.data = data;
				
				NHFx.circleOut.at(b.x, b.y, fdata * 1.25f, b.team.color);
			}
		};
		
		nuBlackHole = new EffectBulletType(20){{
			despawnHit = true;
			hitColor = NHColor.ancientLightMid;
			splashDamageRadius = 36;
			
			lightningDamage = 2000;
			lightning = 2;
			lightningLength = 4;
			lightningLengthRand = 8;
			
			scaledSplashDamage = true;
			collidesAir = collidesGround = collidesTiles = true;
			splashDamage = 0;
			damage = 10000;
		}
			
			@Override
			public void draw(Bullet b){
				if(!(b.data instanceof Seq))return;
				Seq<Sized> data = (Seq<Sized>)b.data;
				
				Draw.color(b.team.color, Color.white, b.fin() * 0.7f);
				Draw.alpha(b.fin(Interp.pow3Out) * 1.1f);
				Lines.stroke(2 * b.fout());
				for(Sized s : data){
					if(s instanceof Building){
						Fill.square(s.getX(), s.getY(), s.hitSize() / 2);
					}else{
						Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5f + b.fout() * 2f), s.hitSize() / 2f * b.fslope() + 12 * b.fin(), 4, 45);
					}
				}
				
				Drawf.light(b.x, b.y, b.fdata, hitColor, 0.3f + b.fin() * 0.8f);
			}
			
			public void hitT(Entityc o, Team team, float x, float y){
				for(int i = 0; i < lightning; i++){
					Lightning.create(team, team.color, lightningDamage, x, y, Mathf.random(360), lightningLength + Mathf.random(lightningLengthRand));
				}
				
				PesterEntity.hitter.create(o, team, x, y, 0, 3000, 1, 1, null);
			}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				
				if(!(b.data instanceof Seq) || b.timer(0, 5))return;
				Seq<Sized> data = (Seq<Sized>)b.data;
				data.remove(d -> !((Healthc)d).isValid());
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				
				float rad = 33;
				
				if(!(b.data instanceof Seq))return;
				Entityc o = b.owner();
				Seq<Sized> data = (Seq<Sized>)b.data;
				for(Sized s : data){
					float size = Math.min(s.hitSize(), 75);
					if(Mathf.chance(0.32) || data.size < 8){
						float sd = Mathf.random(size * 3f, size * 12f);
						
						NHFx.shuttleDark.at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45, b.team.color, sd);
					}
					hitT(o, b.team, s.getX(), s.getY());
				}
				
				createSplashDamage(b, b.x, b.y);
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				b.fdata = splashDamageRadius;
				
				Seq<Sized> data = new Seq<>();
				
				Vars.indexer.eachBlock(null, b.x, b.y, b.fdata, bu -> bu.team != b.team, data::add);
				
				Groups.unit.intersect(b.x - b.fdata / 2, b.y - b.fdata / 2, b.fdata, b.fdata, u -> {
					if(u.team != b.team)data.add(u);
				});
				
				b.data = data;
				
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
			
			speed = 6f;
			
			linkRange = 280f;
			
			maxHit = 12;
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
		
		railGun1 = new TrailFadeBulletType(35f, 1750, STRIKE) {{
			width = 12f;
			height = 36f;
			
			trailLength = 20;
			trailWidth = 2;
			trailInterval = 1f;
			trailRotation = true;
			despawnBlinkTrail = hitBlinkTrail = true;
			
			pierceArmor = pierce = pierceBuilding = true;
			pierceCap = 3;
			
			ammoMultiplier = 1f;
			
			lifetime = 20f;
			lightningColor = hitColor = frontColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color;
			chargeEffect = new OptionalMultiEffect(NHFx.genericCharge(backColor, 13, 90, 90), EffectWrapper.wrap(NHFx.square45_6_45_Charge, backColor));
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor, lightningColor.cpy().lerp(Color.white, 0.2f));
			hitEffect = NHFx.instHit(lightningColor);
			smokeEffect = Fx.smokeCloud;
			trailEffect = NHFx.instTrail(lightningColor, 60, true);
			despawnEffect = new OptionalMultiEffect(NHFx.instBomb(lightningColor), NHFx.hitSparkLarge, NHFx.square45_6_45);
			lightningDamage = damage / 8;
			buildingDamageMultiplier = 0.5f;
			hitShake = 8f;
			knockback = 14f;
			
			hitSound = Sounds.explosion;
			despawnSound = Sounds.explosionbig;
		}};
		
		railGun2 = new TrailFadeBulletType(45f, 2500, STRIKE) {{
			width = 16f;
			height = 50f;
			
			trailLength = 18;
			trailWidth = 2;
			trailInterval = 1f;
			trailChance = 0.4f;
			trailRotation = true;
			despawnBlinkTrail = hitBlinkTrail = true;
			
			pierceArmor = pierce = pierceBuilding = true;
			pierceCap = 6;
			
			ammoMultiplier = 1f;
			rangeChange = 60f;

			lifetime = 16f;
			frontColor = NHItems.irayrondPanel.color;
			lightningColor = hitColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color.cpy().lerp(NHColor.deeperBlue, 0.4f);
			chargeEffect = new OptionalMultiEffect(NHFx.genericCharge(backColor,18, 120, 90), EffectWrapper.wrap(NHFx.square45_6_45_Charge, backColor));
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor, lightningColor.cpy().lerp(Color.white, 0.2f));
			hitEffect = NHFx.instHit(lightningColor, 6, 120);
			smokeEffect = NHFx.hugeSmokeGray;
			trailEffect = NHFx.instTrail(lightningColor, 40, true);
			despawnEffect = new OptionalMultiEffect(NHFx.instBomb(lightningColor), NHFx.hitSparkHuge, NHFx.square45_8_45);
			lightningDamage = damage / 7.5f;
			buildingDamageMultiplier = 0.5f;
			hitShake = 12f;
			knockback = 22f;
			
			hitSound = Sounds.explosion;
			despawnSound = Sounds.explosionbig;
		}};
		
		railGun3 = new TrailFadeBulletType(60f, 3250, STRIKE) {{
			width = 16f;
			height = 50f;
			
			trailLength = 18;
			trailWidth = 2;
			trailInterval = 1f;
			trailChance = 0.4f;
			trailRotation = true;
			despawnBlinkTrail = hitBlinkTrail = true;
			
			pierceArmor = pierce = pierceBuilding = true;
			pierceCap = 2;
			
			ammoMultiplier = 1;
			rangeChange = 120;

			lifetime = 12.8f;
			frontColor = NHItems.irayrondPanel.color;
			lightningColor = hitColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color.cpy().lerp(NHColor.darkEnrColor, 0.6f);
			chargeEffect = new OptionalMultiEffect(NHFx.genericCharge(backColor,18, 120, 90), EffectWrapper.wrap(NHFx.square45_6_45_Charge, backColor));
			lightning = 6;
			lightningLength = 12;
			lightningLengthRand = 20;
			shootEffect = NHFx.instShoot(lightningColor, lightningColor.cpy().lerp(Color.white, 0.2f));
			hitEffect = NHFx.instHit(lightningColor, 6, 120);
			smokeEffect = NHFx.hugeSmoke;
			trailEffect = NHFx.instTrail(lightningColor, 40, true);
			despawnEffect = new OptionalMultiEffect(NHFx.instBomb(lightningColor), NHFx.sharpBlast(backColor, frontColor, 50f, 90f));
			lightningDamage = damage / 7;
			splashDamage = 2000;
			splashDamageRadius = 80;
			
			intervalBullets = 2;
			bulletInterval = 5f;
			intervalBullet = new AdaptedLightningBulletType(){{
				lightningColor = trailColor = hitColor = lightColor = NHItems.zeta.color;
				lightningLength = 4;
				lightningLengthRand = 15;
				damage = 200;
			}};
			
			buildingDamageMultiplier = 0.75f;
			
			hitShake = 22f;
			knockback = 32f;
			
			hitSound = Sounds.explosion;
			despawnSound = Sounds.explosionbig;
		}};
		
		blastEnergyPst = new AccelBulletType(0.85f, 140f, CIRCLE_BOLT){{
			frontColor = Color.white;
			backColor = lightningColor = trailColor = lightColor = NHItems.thermoCorePositive.color.cpy().lerp(Color.white, 0.15f);
			lifetime = 64f;
			ammoMultiplier = 4f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			velocityIncrease = 14f;
			hitShake = despawnShake = 2f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 4;
			homingPower = 0.165f;
			homingRange = 600f;
			homingDelay = 33;
			width = height = 9f;
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
			trailWidth = 2.2f;
			drawSize = 300f;
		}};
		
		blastEnergyNgt = new AccelBulletType(3.85f, 100f){{
			frontColor = Color.white;
			backColor = lightningColor = trailColor = lightColor = NHItems.thermoCoreNegative.color;
			lifetime = 44f;
			knockback = 4f;
			rangeChange = 120;
			ammoMultiplier = 8f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			velocityIncrease = 18f;
			hitShake = despawnShake = 5f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = 12;
			lightningLength = 4;
			width = 11f;
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
			
			reloadMultiplier = 1.25f;
			
			trailLength = 15;
			trailWidth = 2.44f;
			drawSize = 300f;
			
			inaccuracy = 0;
		}};
		
		atomSeparator = new ContinuousFlameBulletType(300){{
			shake = 3;
			hitColor = flareColor = lightColor = lightningColor = NHColor.lightSkyBack;
			colors = new Color[]{NHColor.lightSkyBack.cpy().mul(0.75f, 0.85f, 1f, 0.65f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.65f), NHColor.lightSkyBack.cpy().lerp(NHColor.deeperBlue, 0.5f), NHColor.deeperBlue};
			width = 6;
			length = 380f;
			oscScl = 0.9f;
			oscMag *= 2f;
			lifetime = 35f;
			lightning = 4;
			lightningLength = 2;
			lightningLengthRand = 18;
			flareLength = 75;
			flareWidth = 6;
			hitEffect = NHFx.shootCircleSmall(NHColor.lightSkyBack);
			shootEffect = NHFx.lightningHitLarge(NHColor.lightSkyBack);
			lightningDamage = damage / 6f;
			despawnHit = false;
			pierceArmor = true;
		}
			@Override
			public void update(Bullet b){
				super.update(b);
				
				if(Mathf.chanceDelta(0.11))for(int i = 0; i < lightning; i++){
					Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
				}
			}
			
			@Override
			public void hit(Bullet b, float x, float y){
				hitEffect.at(x, y, b.rotation(), hitColor);
				hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
				
				Effect.shake(hitShake, hitShake, b);
				
				Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
			}
		};
		
		declineProjectile = new TrailFadeBulletType(9.25f, 350f){{
			lifetime = 122f;
			
			tracerUpdateSpacing *= 6f;
			tracerSpacing *= 1.5f;
			
			hittable = false;
			
			tracers = 1;
			tracerStrokeOffset = tracerFadeOffset = 13;
			hitBlinkTrail = false;
			scaledSplashDamage = true;
			
			trailInterp = NHInterp.artilleryPlus;
			shrinkInterp = NHInterp.artilleryPlus;
			
			shrinkX = 0.75f;
			shrinkY = 0.4f;
			width = 25f;
			height = 55f;
			
			trailWidth = 4.7f;
			trailLength = 60;
			
			//				velocityBegin = 12f;
			//				velocityIncrease = 22f;
			//				accelInterp = Interp.pow3Out;
			//				accelerateBegin = 0f;
			//				accelerateEnd = 0.8f;
			
			maxRange = 740;
			pierce = pierceBuilding = false;
			collideTerrain = collideFloor = collidesGround = collidesTiles = false;
			scaleLife = true;
			
			lightning = 6;
			lightningLength = 4;
			lightningLengthRand = 32;
			
			splashDamageRadius = 76f;
			splashDamage = damage;
			lightningDamage = damage * 0.5f;
			backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
			
			knockback = 20f;
			
			frontColor = NHColor.lightSkyFront;
			shootEffect = despawnEffect = NHFx.square(backColor, 40f, 4, 40f, 6f);
			smokeEffect = NHFx.hugeSmokeGray;
			trailChance = 0.6f;
			trailEffect = NHFx.trailToGray;
			despawnShake = 22f;
			hitSound = Sounds.explosionbig;
			hitEffect = new OptionalMultiEffect(NHFx.blast(backColor,  45f), NHFx.crossBlast(backColor, 120f, 45f), NHFx.hitSpark(backColor, 150f, 45, 170f, 2f, 13));
			
			fragBullets = 7;
			fragBullet = NHBullets.basicSkyFrag;
			fragLifeMax = 0.5f;
			fragLifeMin = 0.25f;
			fragVelocityMax = 0.72f;
			fragVelocityMin = 0.075f;
		}
			public void removed(Bullet b){
				if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
					NHFx.trailFadeFast.at(b.x, b.y, trailWidth, trailColor, b.trail.copy());
				}
			}
			
			@Override
			public void init(Bullet b){
				super.init(b);
				b.lifetime *= Mathf.random(0.955f, 1.025f);
			}
			
			@Override
			public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
				super.hitTile(b, build, x, y, initialHealth, direct);
				
				UltFire.createChance(b, splashDamageRadius, 0.1f);
			}
		};
		
		
		
		airRaidBomb = new BasicBulletType(18f, 800f, NHBullets.STRIKE){{
			trailLength = 14;
			
			trailColor = backColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = Color.white;
			
			hitSound = Sounds.explosionbig;
			trailChance = 0.075f;
			trailEffect = NHFx.polyTrail;
			trailParam = 6;
			drawSize = 120f;
			
			collides = false;
			scaleLife = true;
			hitShake = despawnShake = 16f;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 20;
			shootEffect = NHFx.instShoot(backColor, frontColor);
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
		
		skyMissile = new AdaptedMissileUnitType("sky-missile"){{
			speed = 2.3f;
			accel = 0.45f;
			drag /= 2;
			
			trailAppearDelay = 22f;
			lifetime = 60f * 3.35f;
			
			armor = 5;
			
			outlineColor = NHUnitTypes.OColor;
			health = 800;
			
			homingDelay = 17f;
			lowAltitude = true;
			engineSize = 2f;
			engineOffset = 18f;
			engineColor = trailColor = NHColor.lightSkyBack;
			engineLayer = Layer.effect;
			trailLength = 13;
			deathExplosionEffect = Fx.none;
			loopSoundVolume = 0.1f;
			
			abilities.add(new MoveEffectAbility(){{
				effect = NHFx.skyTrail;
				y = -engineOffset;
				interval = 5f;
			}});
			
			weapons.add(new Weapon(){{
				shootCone = 360f;
				mirror = false;
				reload = 1f;
				shootOnDeath = true;
				
				shootSound = Sounds.plasmaboom;
				
				bullet = new ExplosionBulletType(40, 180f){{
					trailColor = lightColor = lightningColor = hitColor = NHColor.lightSky;
					
					hitSound = despawnSound = Sounds.none;
					
					scaledSplashDamage = true;
					splashDamageRadius = 40f;
					lightningDamage = damage;
					
					hitShake = despawnShake = 16f;
					lightning = 2;
					lightningCone = 360;
					lightningLengthRand = lightningLength = 5;
					
					shootEffect = new MultiEffect(NHFx.blast(NHColor.lightSky, 30f), NHFx.hitSparkHuge);
				}};
			}});
		}
			
			@Override
			public void draw(Unit unit){
				Draw.zTransform(f -> f - 2f);
				super.draw(unit);
				Draw.zTransform();
			}
		};
		
		airRaidMissile = new AdaptedMissileUnitType("air-raid-missile"){{
			speed = 10f;
			accel = 0.32f;
			drag /= 2;
			
			lifetime = 60f * 1.8f;
			
			targetPriority = 0f;
			
			rotateSpeed = 3.5f;
			baseRotateSpeed = 3.5f;
			
			armor = 10;
			
			outlineColor = Pal.darkOutline;
			health = 4000;
			homingDelay = 17f;
			lowAltitude = true;
			engineSize = 2.75f;
			engineOffset = 23f;
			engineColor = trailColor = NHColor.darkEnrColor;
			engineLayer = Layer.effect;
			trailLength = 45;
			deathExplosionEffect = Fx.none;
			loopSoundVolume = 0.1f;
			
			abilities.add(new MoveEffectAbility(){{
				effect = NHFx.hugeSmoke;
				rotation = 180f;
				y = -22f;
				color = Color.grays(0.6f).lerp(NHColor.darkEnrColor, 0.5f).a(0.9f);
				interval = 5f;
			}});
			
			clipSize = 620;
			
			weapons.add(new Weapon(){{
				shootCone = 360f;
				mirror = false;
				reload = 1f;
				shootOnDeath = true;
				
				shootSound = Sounds.explosionbig;
				predictTarget = false;
				shake = 12;
				
				bullet = new ExplosionBulletType(4200, 150f){{
					trailColor = lightColor = lightningColor = hitColor = NHColor.darkEnrColor;
					
					suppressionRange = 600f;
					suppressionDuration = 600f;
					
					hitSound = despawnSound = Sounds.none;
					status = NHStatusEffects.emp3;
					
					lightningDamage = damage = splashDamage / 1.5f;
					scaledSplashDamage = true;
					
					
					splashDamageRadius = 200f;
					hitShake = despawnShake = 16f;
					lightning = 6;
					lightningCone = 360;
					lightningLengthRand = lightningLength = 15;
					
					fragLifeMin = 0.6f;
					fragLifeMax = 1f;
					fragVelocityMin = 0.4f;
					fragVelocityMax = 0.6f;
					fragBullets = 8;
					fragBullet = arc_9000_frag;
					
					shootEffect = new OptionalMultiEffect(
							NHFx.largeDarkEnergyHit,
							NHFx.blast(NHColor.darkEnrColor, 140f),
							NHFx.largeDarkEnergyHitCircle,
							NHFx.subEffect(150, splashDamageRadius * 0.66f, 13, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
								float fout = Interp.pow2Out.apply(1 - fin);
								float finpow = Interp.pow3Out.apply(fin);
								Tmp.v1.trns(rot, 25 * finpow);
								for(int s : Mathf.signs) {
									Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(finpow, 0, 0.3f) * NHFx.fout(fin, 0.15f), rot + s * 90);
								}
							}))
					);
				}};
			}});
		}};
		
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
			
			despawnEffect = NHFx.square(backColor, 85f, 5, 52, 5);
			hitEffect = NHFx.hitSparkLarge;
			
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
		
		laugraBullet = new AccelBulletType(200, STRIKE){{
			status = NHStatusEffects.entangled;
			statusDuration = 320f;
			
			lightOpacity = 0.7f;
			healPercent = 20f;
			
			reflectable = false;
			knockback = 3f;
			impact = true;
			
			velocityBegin = 1f;
			velocityIncrease = 18f;
			accelerateBegin = 0.05f;
			accelerateEnd = 0.55f;
			
			pierce = pierceBuilding = true;
			pierceCap = 5;
			
			lightningColor = backColor = trailColor = hitColor = lightColor = NHColor.ancient;
			lightRadius = 70f;
			shootEffect = new EffectWrapper(NHFx.shootLine(33f, 32), backColor);
			smokeEffect = NHFx.hugeSmokeLong;
			lifetime = 40f;
			
			frontColor = Color.white;
			
			lightning = 2;
			lightningDamage = damage / 4f + 10f;
			lightningLength = 7;
			lightningLengthRand = 16;
			
			splashDamageRadius = 36f;
			splashDamage = damage / 2f;
			
			width = 13f;
			height = 35f;
			speed = 8f;
			trailLength = 20;
			trailWidth = 2.3f;
			trailInterval = 1.76f;
			hitShake = 8f;
			trailRotation = true;
			keepVelocity = true;
			
			hitSound = Sounds.plasmaboom;
			
			trailEffect = new Effect(10f, e -> {
				color(trailColor, Color.white, e.fout() * 0.66f);
				for(int s : Mathf.signs){
					DrawFunc.tri(e.x, e.y, 3f, 30f * Mathf.curve(e.fin(), 0, 0.1f) * e.fout(0.9f), e.rotation + 145f * s);
				}
			});
			
			hitEffect = new OptionalMultiEffect(NHFx.square45_6_45, NHFx.hitSparkLarge);
			despawnEffect = NHFx.lightningHitLarge;
		}
			
			@Override
			public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
				super.hitTile(b, build, x, y, initialHealth, direct);
				
				if(build.block.armor > 10 || build.block.absorbLasers)b.time(b.lifetime());
			}
		};
		
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
				speed = 6f;
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
			backColor = trailColor = lightColor = lightningColor = hitColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.2f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			splashDamageRadius = 4f;
			splashDamage = damage / 3;
			despawnEffect = Fx.smoke;
			hitEffect = NHFx.hitSpark;
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
		
		arc_9000 = new LightningLinkerBulletType(2.75f, 200){{
			trailWidth = 4.5f;
			trailLength = 66;
			
			chargeEffect = new OptionalMultiEffect(NHFx.darkEnergyCharge, NHFx.darkEnergyChargeBegin);
			
			spreadEffect = slopeEffect = Fx.none;
			trailEffect = NHFx.hitSparkHuge;
			trailInterval = 5;
			
			backColor = trailColor = hitColor = lightColor = lightningColor = NHColor.darkEnrColor;
			frontColor = NHColor.darkEnr;
			randomGenerateRange = 340f;
			randomLightningNum = 3;
			linkRange = 280f;
			range = 800f;
			
			drawSize = 500f;
			
			drag = 0.0035f;
			fragLifeMin = 0.3f;
			fragLifeMax = 1f;
			fragVelocityMin = 0.3f;
			fragVelocityMax = 1.25f;
			fragBullets = 14;
			intervalBullets = 2;
			intervalBullet = fragBullet = arc_9000_frag;
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 120f;
			splashDamage = 1000;
			lightningDamage = 375f;
			
			collidesTiles = true;
			pierce = false;
			collides = false;
			ammoMultiplier = 1f;
			lifetime = 300;
			despawnEffect = NHFx.circleOut(hitColor, splashDamageRadius * 1.5f);
			hitEffect = NHFx.largeDarkEnergyHit;
			shootEffect = NHFx.darkEnergyShootBig;
			smokeEffect = NHFx.darkEnergySmokeBig;
			hitSpacing = 3;
		}
			
			@Override
			public void update(Bullet b){
				super.update(b);
				
				if(NHSetting.enableDetails() && b.timer(1, 6))for(int j = 0; j < 2; j++){
					NHFunc.randFadeLightningEffect(b.x, b.y, Mathf.random(360), Mathf.random(7, 12), backColor, Mathf.chance(0.5));
				}
			}
			
			@Override
			public void draw(Bullet b){
				Draw.color(backColor);
				DrawFunc.surround(b.id, b.x, b.y, size * 1.45f, 14, 7,11, (b.fin(NHInterp.parabola4Reversed) + 1f) / 2 * b.fout(0.1f));
				
				drawTrail(b);
				
				color(backColor);
				Fill.circle(b.x, b.y, size);
				
				Draw.z(NHFx.EFFECT_MASK);
				color(frontColor);
				Fill.circle(b.x, b.y, size * 0.62f);
				Draw.z(NHFx.EFFECT_BOTTOM);
				color(frontColor);
				Fill.circle(b.x, b.y, size * 0.66f);
				Draw.z(Layer.bullet);
				
				Drawf.light(b.x, b.y, size * 1.85f, backColor, 0.7f);
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
				
				shootEffect = NHFx.hugeSmokeGray;
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
		
		
		guardianBulletLightningBall = new LightningLinkerBulletType(3f, 120){{
			lifetime = 120;
			keepVelocity = false;
			
			lightningDamage = damage = splashDamage = 80;
			splashDamageRadius = 50f;
			
			homingDelay = 20f;
			homingRange = 300f;
			homingPower = 0.025f;
			
			smokeEffect = shootEffect = Fx.none;
			
			effectLingtning = 0;
			
			maxHit = 6;
			hitShake = despawnShake = 5f;
			hitSound = despawnSound = Sounds.plasmaboom;
			
			size = 7.2f;
			trailWidth = 3f;
			trailLength = 16;
			
			linkRange = 80f;
			
			scaleLife = false;
			despawnHit = false;
			
			collidesAir = collidesGround = true;
			
			despawnEffect = hitEffect = new OptionalMultiEffect(NHFx.lightningHitLarge, NHFx.hitSparkHuge);
			
			trailEffect = slopeEffect = NHFx.trailFromWhite;
			spreadEffect = new Effect(32f, e -> {
				randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
					color(e.color);
					Fill.circle(e.x + x, e.y + y, e.fout() * size / 2f);
					color(Color.black);
					Fill.circle(e.x + x, e.y + y, e.fout() * (size / 3f - 1f));
				});
			}).layer(Layer.effect + 0.00001f);
		}
			
			private final Effect RshootEffect = new Effect(24.0F, e -> {
				e.scaled(10.0F, (b) -> {
					Draw.color(e.color);
					Lines.stroke(b.fout() * 3.0F + 0.2F);
					Lines.circle(b.x, b.y, b.fin() * 70.0F);
				});
				Draw.color(e.color);
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, 8.0F * e.fout(), 85.0F, e.rotation + 90.0F * i);
				}
				
				if(!NHSetting.enableDetails())return;
				
				Draw.color(Color.black);
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, 3F * e.fout(), 38.0F, e.rotation + 90.0F * i);
				}
			});
			
			private final Effect RsmokeEffect = NHFx.hitSparkLarge;
			
			public Color getColor(Bullet b){
				return Tmp.c1.set(b.team.color).lerp(Color.white, 0.1f + Mathf.absin(4f, 0.15f));
			}
			
			@Override
			public void update(Bullet b) {
				updateTrail(b);
				updateHoming(b);
				updateWeaving(b);
				updateBulletInterval(b);
				
				Effect.shake(hitShake, hitShake, b);
				if(b.timer(5, hitSpacing)) {
					slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), Mathf.random(2f, 4f), b.team.color);
					spreadEffect.at(b.x, b.y, b.team.color);
					PosLightning.createRange(b, collidesAir, collidesGround, b, b.team, linkRange, maxHit, b.team.color, Mathf.chanceDelta(randomLightningChance), lightningDamage, lightningLength, PosLightning.WIDTH, boltNum, p -> {
						liHitEffect.at(p.getX(), p.getY(), b.team.color);
					});
				}
				
				if(Mathf.chanceDelta(0.1)){
					slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), Mathf.random(2f, 4f), b.team.color);
					spreadEffect.at(b.x, b.y, b.team.color);
				}
				
				if(randomGenerateRange > 0f && Mathf.chance(Time.delta * randomGenerateChance) && b.lifetime - b.time > PosLightning.lifetime)PosLightning.createRandomRange(b, b.team, b, randomGenerateRange, backColor, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
					randomGenerateSound.at(hitPos, Mathf.random(0.9f, 1.1f));
					Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius / 8, splashDamage * b.damageMultiplier() / 8, collidesAir, collidesGround);
					NHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);
					
					hitModifier.get(hitPos);
				});
				
				if(Mathf.chanceDelta(effectLightningChance) && b.lifetime - b.time > Fx.chainLightning.lifetime && NHSetting.enableDetails()){
					for(int i = 0; i < effectLingtning; i++){
						Vec2 v = randVec.rnd(effectLightningLength + Mathf.random(effectLightningLengthRand)).add(b).add(Tmp.v1.set(b.vel).scl(Fx.chainLightning.lifetime / 2));
						Fx.chainLightning.at(b.x, b.y, 12f, b.team.color, v.cpy());
						NHFx.lightningHitSmall.at(v.x, v.y, 20f, b.team.color);
					}
				}
			}
			
			@Override
			public void init(Bullet b) {
				super.init(b);
				
				b.lifetime *= Mathf.randomSeed(b.id, 0.875f , 1.125f);
				
				RsmokeEffect.at(b.x, b.y, b.team.color);
				RshootEffect.at(b.x, b.y, b.rotation(), b.team.color);
			}
			
			@Override
			public void drawTrail(Bullet b){
				if(trailLength > 0 && b.trail != null){
					float z = Draw.z();
					Draw.z(z - 0.0001f);
					b.trail.draw(getColor(b), trailWidth);
					Draw.z(z);
				}
			}
			
			@Override
			public void draw(Bullet b) {
				drawTrail(b);
				
				Draw.color(Tmp.c1);
				Fill.circle(b.x, b.y, size);
				
				float[] param = {
					9f, 28f, 1f,
					9f, 22f, -1.25f,
					12f, 16f, -0.45f,
				};
				
				for(int i = 0; i < param.length / 3; i++){
					for(int j : Mathf.signs){
						Drawf.tri(b.x, b.y, param[i * 3] * b.fout(), param[i * 3 + 1] * b.fout(), b.rotation() + 90.0F * j + param[i * 3 + 2] * Time.time);
					}
				}
				
				Draw.color(Color.black);
				
				Fill.circle(b.x, b.y, size / 6.125f + size / 3 * Mathf.curve(b.fout(), 0.1f, 0.35f));
				
				Drawf.light(b.x, b.y, size * 6.85f, b.team.color, 0.7f);
			}
			
			@Override
			public void despawned(Bullet b) {
				PosLightning.createRandomRange(b, b.team, b, randomGenerateRange, b.team.color, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
					Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
					NHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);
					liHitEffect.at(hitPos);
					for (int j = 0; j < lightning; j++) {
						Lightning.create(b, b.team.color, lightningDamage < 0.0F ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
					}
					hitSound.at(hitPos, Mathf.random(0.9f, 1.1f));
					
					hitModifier.get(hitPos);
				});
				
				if(despawnHit){
					hit(b);
				}else{
					createUnits(b, b.x, b.y);
				}
				
				if(!fragOnHit){
					createFrags(b, b.x, b.y);
				}
				
				despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
				despawnSound.at(b);
				
				Effect.shake(despawnShake, despawnShake, b);
			}
			
			@Override
			public void hit(Bullet b, float x, float y){
				hitEffect.at(x, y, b.rotation(), b.team.color);
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
					Lightning.create(b, b.team.color, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
				}
			}
			
			@Override
			public void removed(Bullet b){
				if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
					Fx.trailFade.at(b.x, b.y, trailWidth, b.team.color, b.trail.copy());
				}
			}
		};
		
		guardianBullet = new AccelBulletType(2f, 180){
			{
				width = 22f;
				height = 40f;
				
				velocityBegin = 1f;
				velocityIncrease = 11f;
				accelInterp = NHInterp.inOut;
				accelerateBegin = 0.045f;
				accelerateEnd = 0.675f;
				
				pierceCap = 3;
				splashDamage = damage / 4;
				splashDamageRadius = 24f;
				
				trailLength = 30;
				trailWidth = 3f;
				
				lifetime = 160f;
				
				trailEffect = NHFx.trailFromWhite;
				
				pierceArmor = true;
				trailRotation = false;
				trailChance = 0.35f;
				trailParam = 4f;
				
				homingRange = 640F;
				homingPower = 0.075f;
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
			public void update(Bullet b){
				super.update(b);
			}
			
			public void updateTrailEffects(Bullet b){
				if(trailChance > 0){
					if(Mathf.chanceDelta(trailChance)){
						trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
					}
				}
				
				if(trailInterval > 0f){
					if(b.timer(0, trailInterval)){
						trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
					}
				}
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
		
		eternity = new AccelBulletType(10f, 1000f){
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
				ammoMultiplier = 1;

				hittable = false;

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
