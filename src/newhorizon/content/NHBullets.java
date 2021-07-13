package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
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
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import newhorizon.NewHorizon;
import newhorizon.bullets.*;
import newhorizon.feature.PosLightning;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class NHBullets implements ContentList{
	public static String CIRCLE_BOLT, STRIKE;
	
	public static
	BulletType
		longRangeShoot, longRangeShootRapid, longRangeShootSplash, mineShoot,
		artilleryIrd, artilleryFusion, artilleryPlast, artilleryThermo, artilleryPhase, artilleryMissile,
		railGun1, railGun2, hurricaneType, polyCloud, missileTitanium, missileThorium, missileZeta, missile, missileStrike,
		strikeLaser, tear, skyFrag, hurricaneLaser, hyperBlast, huriEnergyCloud, warperBullet,
		none, supSky, darkEnrLightning, darkEnrlaser, decayLaser, longLaser, rapidBomb, airRaid,
		blastEnergyPst, blastEnergyNgt, curveBomb, strikeRocket, annMissile, collapserBullet, collapserLaserSmall,
		strikeMissile, boltGene, empFrag, empBlot2, empBlot3;
		
	
	public void loadFragType(){
		BulletType
		collapserLaserSmall = new AdaptedContinuousLaserBulletType(200){
			{
				lightColor = hitColor = NHColor.thurmixRed;
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{hitColor.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), hitColor.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.thurmixRedLight, Color.white};
				width = 5f;
				length = 650f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 160f;
				hitEffect = NHFx.instHit(hitColor, 2, 60f);
				shootEffect = Fx.none;
				smokeEffect = NHFx.hugeSmoke;
			}
			
			@Override
			public void draw(Bullet b){
				super.draw(b);
				float f = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1.0F - (b.time - (lifetime - fadeTime)) / fadeTime : 1.0F);
				color(hitColor);
				Fill.circle(b.x, b.y, (width + 2f) * f);
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						color(hitColor);
						Drawf.tri(b.x, b.y, (width * 1.25f) * f, 86f + Mathf.absin(Time.time * j, 6f, 20f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						color(Color.white);
						Drawf.tri(b.x, b.y, (width / 2f) * f, 63f + Mathf.absin(Time.time * j, 6f, 12f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				color(Color.white);
				Fill.circle(b.x, b.y, (width - 2f) * f);
				Draw.reset();
			}
		};
		
		collapserBullet = new LightningLinkerBulletType(){{
			range = 500f;
			drawSize = 300f;
			damage = 200;
			outColor = trailColor = lightColor = lightningColor = hitColor = NHColor.thurmixRed;
			size = 10f;
			innerColor = NHColor.thurmixRedLight;
			range = 600f;
			spreadEffect = Fx.none;

			trailWidth = 8f;
			trailLength = 20;
			
			speed = 4f;
			
			setDisableAccel();
			
			generateDelay = 6f;
			boltNum = 3;
			linkRange = 280f;
			
			maxHit = 8;
			drag = 0.0065f;
			hitSound = Sounds.explosionbig;
			splashDamageRadius = 60f;
			splashDamage = lightningDamage = damage / 3f;
			lifetime = 130f;
			despawnEffect = NHFx.lightningHitLarge(hitColor);
			hitEffect = NHFx.instHit(hitColor, 4, 80f);
			shootEffect = Fx.none;
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
			trailEffect = NHFx.trail;
			hitShake = 3f;
			hitSound = Sounds.plasmaboom;
		}};
		
		skyFrag = new BasicBulletType(3.3f, 85){
			@Override
			public float range(){return 180f;}
			
			{
				lifetime = 60;
				despawnEffect = hitEffect = NHFx.lightSkyCircleSplash;
				knockback = 12f;
				width = 15f;
				height = 37f;
				lightningDamage = damage * 0.75f;
				backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
				frontColor = Color.white;
				lightning = 3;
				lightningLength = 8;
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.6f;
				trailEffect = NHFx.skyTrail;
				hitShake = 2f;
				hitSound = Sounds.explosion;
			}
		};
	}
	
	public void load(){
		CIRCLE_BOLT = NewHorizon.configName("circle-bolt");
		STRIKE = NewHorizon.configName("strike");
		
		loadFragType();
		
		artilleryIrd = new AdaptedArtilleryBulletType(5f, 200f, "large-bomb"){{
			collidesTiles = collidesGround = true;
			reloadMultiplier = 1.25f;
			shrinkX = shrinkY = 0;
			spin = 4f;
			hitShake = 4f;
			width = height = 27.0F;
			splashDamage = damage;
			splashDamageRadius = 12f;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(frontColor, 0.55f);
			hitEffect = NHFx.instHit(backColor, 2, 40f);
			despawnEffect = NHFx.crossBlast(backColor, 80f);
			shootEffect = NHFx.shootLineSmall(backColor);
		}};
		
		artilleryFusion = new ArtilleryBulletType(4f, 100f, CIRCLE_BOLT){{
			collidesTiles = collidesGround = true;
			width = height = 32.0F;
			hitShake = 6f;
			splashDamage = damage;
			splashDamageRadius = 24f;
			incendSpread = 6f;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = NHItems.fusionEnergy.color.cpy().lerp(frontColor, 0.15f);
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 80f);
			shootEffect = NHFx.shootCircleSmall(backColor);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		artilleryPlast = new ArtilleryBulletType(4f, 90f){{
			collidesTiles = collidesGround = true;
			reloadMultiplier = 0.95f;
			hitShake = 4f;
			width = 18f;
			height = 36f;
			splashDamage = damage / 4;
			splashDamageRadius = 12f;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = Items.plastanium.color.cpy().lerp(frontColor, 0.15f);
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = Fx.plasticExplosion;
			shootEffect = Fx.plasticExplosion;
			smokeEffect = NHFx.hugeSmoke;
			fragBullets = 6;
			fragBullet = Bullets.fragPlasticFrag;
			fragVelocityMin = fragLifeMin = 0.085f;
			fragVelocityMax = fragLifeMax = 1.025f;
		}};
		
		artilleryThermo = new AdaptedArtilleryBulletType(2f, 180f, "large-bomb"){{
			collidesTiles = collidesGround = true;
			reloadMultiplier = 0.95f;
			shrinkX = shrinkY = 0;
			spin = 2f;
			hitShake = 8f;
			width = height = 40.0F;
			splashDamage = damage;
			splashDamageRadius = 36f;
			lightning = 3;
			lightningDamage = damage / 4;
			lightningLength = lightningLengthRand = 10;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = NHItems.thermoCorePositive.color;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor, 100);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.hugeSmoke;
		}};
		
		artilleryPhase = new ArtilleryBulletType(8f, 110f){{
			collidesTiles = collidesGround = true;
			hitShake = 2f;
			width = 14f;
			height = 35f;
			collides = true;
			splashDamage = damage / 8;
			splashDamageRadius = 4f;
			frontColor = Color.white;
			backColor = trailColor = lightColor = lightningColor = Items.phaseFabric.color.cpy().lerp(frontColor, 0.15f);
			hitEffect = NHFx.instHit(backColor, 2, 25f);
			despawnEffect = NHFx.crossBlast(backColor, 100);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = NHFx.hugeSmoke;
			status = NHStatusEffects.emp2;
			statusDuration = 60f;
		}};
		
		artilleryMissile = new ArtilleryBulletType(5f, 100f){{
			collidesTiles = collidesGround = true;
			hitShake = 2f;
			width = 14f;
			height = 35f;
			splashDamage = damage;
			splashDamageRadius = 8f;
			trailParam = 3.5f;
			trailEffect = NHFx.trail;
			homingDelay = 20f;
			homingPower = 0.05f;
			homingRange = 250f;
			frontColor = Pal.bulletYellow;
			backColor = trailColor = lightColor = lightningColor = Pal.bulletYellowBack;
			hitEffect = Fx.flakExplosionBig;
			despawnEffect = NHFx.shootCircleSmall(backColor);
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
		}};
		
		longRangeShoot = new ShieldBreaker(1, 800, "bullet", 2000){{
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.blue, 0.15f);
			frontColor = Color.white;
			
			velocityBegin = velocityEnd = 1f;
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
		
		longRangeShootRapid = new ShieldBreaker(6, 800, "bullet", 2000){{
			trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack.cpy().lerp(Color.blue, 0.15f);
			frontColor = Color.white;
			
			velocityBegin = velocityEnd = 6f;
			
			trailWidth = 3f;
			trailLength = 15;
			lifetime = 120f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			trailChance = 0.035f;
			trailEffect = NHFx.trail;
			drawSize = 60f;
			hitShake = despawnShake = 2f;
			shootEffect = NHFx.instShoot(backColor);
			smokeEffect = Fx.shootBigSmoke2;
			hitEffect = NHFx.instHit(backColor, 2, 80f);
			despawnEffect = NHFx.crossBlast(backColor, 30f);
			height = 47f;
			width = 14f;
		}};
		
		longRangeShootSplash = new ShieldBreaker(1, 800, "bullet", 2000){{
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 8;
			splashDamageRadius = 60f;
			splashDamage = lightningDamage = 0.5f * damage;
			velocityBegin = velocityEnd = 1f;
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
		
		mineShoot = new FlakBulletType(0.75f, 100){{
			sprite = "large-bomb";
			collidesGround = true;
			trailColor = backColor = lightColor = lightningColor = Pal.ammo;
			frontColor = Color.white;
			
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 12;
			splashDamageRadius = 80f;
			splashDamage = damage / 2;
			lightningDamage = damage / 8;
			
			spin = 4f;
			
			lifetime = 850f;
			shrinkX = shrinkY = 0;
			hitSound = Sounds.explosionbig;
			trailChance = 0.035f;
			trailParam = 6f;
			trailEffect = NHFx.trail;
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
			lifetime = 75f;
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
			lifetime = 75f;
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
			backColor = trailColor = lightColor = Items.titanium.color.cpy().lerp(Color.white, 0.2f);
			frontColor = backColor.cpy().lerp(Color.white, 0.7f);
			hitEffect = NHFx.lightningHitSmall(backColor);
			despawnEffect = NHFx.shootCircleSmall(backColor);
			lifetime = 75f;
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
			lifetime = 75f;
			hitEffect = NHFx.instHit(backColor, 2, 30f);
			despawnEffect = NHFx.shootCircleSmall(backColor);
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
			lifetime = 75f;
			lightningDamage = damage / 2;
			lightning = 2;
			lightningLength = 10;
		}};
		
		polyCloud = new SpeedUpBulletType(0.05f, 70){
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
			splashDamage = lightningDamage = damage / 2;
			splashDamageRadius = 12f;
			backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
			frontColor = Color.white;
			trailEffect = NHFx.polyCloud(backColor, 45, 10, 32, 4);
			trailChance = 0;
			pierce = pierceBuilding = true;
			velocityBegin = 0.25f;
			velocityEnd = 8;
			accelerateBegin = 0.05f;
			accelerateEnd = 0.95f;
			lifetime = 140f;
			hitShake = 2;
			hitSound = Sounds.plasmaboom;
			hitEffect = NHFx.shootCircleSmall(backColor);
			despawnEffect = NHFx.lightningHitLarge(backColor);
			
			status = NHStatusEffects.emp1;
			statusDuration = 30f;
		}
			@Override
			public void update(Bullet b){
				if(Mathf.chanceDelta(0.45f))trailEffect.at(b.x, b.y, b.rotation());
				if(b.timer(4, 6))b.collided.clear();
				super.update(b);
			}
			
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				PosLightning.createRandomRange(b, b.team, b, splashDamageRadius * 30, lightColor, Mathf.chanceDelta(lightning / 10f), 0, 0, PosLightning.WIDTH, 2 + Mathf.random(1), lightning, hitPos -> {
					Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
					NHFx.lightningHitLarge(lightColor).at(hitPos);
					NHFx.crossBlast(lightColor).at(hitPos);
					for (int j = 0; j < lightning; j++) {
						Lightning.create(b, lightningColor, lightningDamage < 0.0F ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
					}
					hitSound.at(hitPos, Mathf.random(0.9f, 1.1f));
				});
			}
			
			@Override
			public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
				super.hitEntity(b, other, initialHealth);
				if(other instanceof Buildingc){
					b.time += b.lifetime() / 100f;
				}
			}
		};
		
		hurricaneType = new LightningLinkerBulletType(2.3f, 200){{
			setDisableAccel();
			
			range = 340f;
			
			trailWidth = 8f;
			trailLength = 40;
			
			outColor = trailColor = lightColor = lightningColor = NHColor.lightSkyBack;
			innerColor = Color.white;
			generateDelay = 6f;
			randomGenerateRange = 280f;
			randomLightningNum = 5;
			boltNum = 1;
			linkRange = 280f;
			
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
		}};
		
		railGun1 = new BasicBulletType(35f, 2200, STRIKE) {{
			width = 12f;
			height = 36f;
			
			trailLength = 20;
			trailWidth = 2;
			trailInterval = 1f;
			trailRotation = true;
			
			pierce = pierceBuilding = true;
			
			lifetime = 25f;
			lightningColor = frontColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color;
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor);
			hitEffect = NHFx.instHit(lightningColor);
			smokeEffect = Fx.smokeCloud;
			trailEffect = NHFx.instTrail(lightningColor, 40, true);
			despawnEffect = new MultiEffect(NHFx.instBomb(lightningColor), NHFx.crossBlast(lightningColor));lightningDamage = damage / 7;
			buildingDamageMultiplier = 1.25f;
			hitShake = 8f;
			knockback = 14f;
		}};
		
		railGun2 = new BasicBulletType(40f, 3000, STRIKE) {{
			width = 16f;
			height = 50f;
			
			trailLength = 18;
			trailWidth = 2;
			trailInterval = 1f;
			trailChance = 0.4f;
			trailRotation = true;
			
			pierce = pierceBuilding = true;
			
			lifetime = 20f;
			lightningColor = frontColor = backColor = trailColor = lightColor = NHItems.irayrondPanel.color;
			lightning = 4;
			lightningLength = 6;
			lightningLengthRand = 10;
			shootEffect = NHFx.instShoot(lightningColor);
			hitEffect = NHFx.instHit(lightningColor, 6, 120);
			smokeEffect = Fx.smokeCloud;
			trailEffect = NHFx.instTrail(lightningColor, 60, true);
			despawnEffect = new MultiEffect(NHFx.instBomb(lightningColor), NHFx.crossBlast(lightningColor));
			lightningDamage = damage / 7;
			buildingDamageMultiplier = 1.25f;
			hitShake = 12f;
			knockback = 22f;
		}};
		
		warperBullet = new SpeedUpBulletType(0.35f, 20f, CIRCLE_BOLT){
			@Override
			public float range(){return 220f;}
			
			{
				shrinkX = shrinkY = 0.35f;
				buildingDamageMultiplier = 3.5f;
				keepVelocity = false;
				
				velocityEnd = 4f;
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
				trailEffect = NHFx.trail;
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
			trailEffect = NHFx.trail;
			hitShake = 3f;
			hitSound = Sounds.plasmaboom;
		}};
		
		hurricaneLaser = new AdaptedContinuousLaserBulletType(640){
			{
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.lightSkyBack.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.lightSkyBack, Color.white};
				width = 7f;
				length = 500f;
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
				color(NHColor.lightSkyBack);
				Fill.circle(b.x, b.y, 24f * f);
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						color(NHColor.lightSkyBack);
						Drawf.tri(b.x, b.y, 16f * f, 86f + Mathf.absin(Time.time * j, 6f, 20f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				for(int i : Mathf.signs){
					for(int j : Mathf.signs){
						color(Color.white);
						Drawf.tri(b.x, b.y, 7f * f, 63f + Mathf.absin(Time.time * j, 6f, 12f) * f, 90 + 90 * i + Time.time * j);
					}
				}
				
				color(Color.white);
				Fill.circle(b.x, b.y, 17f * f);
				Draw.reset();
			}
		};
		
		huriEnergyCloud = new SpeedUpBulletType(6, 60){
			@Override
			public float range(){return 400f;}
			
			@Override
			public void update(Bullet b){
				b.vel().scl(Mathf.curve(b.finpow(), 0.12f, 0.85f));
				new Effect(40, e -> {
					Draw.color(e.color, Pal.gray, e.fin());
					Vec2 trnsB = new Vec2();
					trnsB.trns(e.rotation, e.fin() * (22));
					Fill.poly(e.x + trnsB.x, e.y + trnsB.y, 6, e.fout() * 6, e.rotation);
				}).at(b.x, b.y, b.rotation(), NHColor.lightSkyBack);
				super.update(b);
			}
			
			{
				width = height = 0f;
				splashDamage = 25;
				splashDamageRadius = 20;
				homingDelay = 60f;
				homingPower = 0.15f;
				homingRange = 200f;
				lifetime = 210;
				pierceBuilding = pierce = true;
				
				hitEffect = new Effect(40, e -> {
					Draw.color(e.color);
					Angles.randLenVectors(e.id, 2, 60 * e.fin(), 0, 360, (x, y) -> Fill.poly(e.x + x, e.y + y, 6, 4 * e.fout()));
				});
				shootEffect = new Effect(25f, e -> {
					Draw.color(e.color, Color.white, e.fin() * 0.5f);
					Drawf.tri(e.x, e.y, 3 * e.fout(), 40 * e.fout(), e.rotation + 90);
					Drawf.tri(e.x, e.y, 3 * e.fout(), 40 * e.fout(), e.rotation + 270);
				});
				smokeEffect = new Effect(25f, e -> {
					Draw.color(e.color, Color.white, e.fin() * 0.5f);
					Angles.randLenVectors(e.id, 3, 40 * e.fin(), e.rotation, 55, (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3));
				});
			}
		};
		
		none = new BasicBulletType(0, 1, "none"){{
			instantDisappear = true;
			trailEffect = smokeEffect = shootEffect = hitEffect = despawnEffect = Fx.none;
		}};
		
		darkEnrLightning = new PosLightningType(150){{
			lightningColor = NHColor.darkEnrColor;
			maxRange = 800f;
			boltNum = 1;
		}};
		
		supSky = new PosLightningType(135f){{
			lightningColor = NHColor.lightSkyBack;
			maxRange = 250f;
		}};
		
		darkEnrlaser = new ContinuousLaserBulletType(1300){
			{
				strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
				tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
				shake = 3;
				colors = new Color[]{NHColor.darkEnrColor.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.darkEnrColor.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.darkEnrColor, NHColor.darkEnr};
				width = 18f;
				length = 1000f;
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
		
		decayLaser = new AdaptedLaserBulletType(1700){{
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
		
		rapidBomb = new SpeedUpBulletType(9f, 280, NewHorizon.configName("strike")){{
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
		
		airRaid = new SpeedUpBulletType(9f, 450, STRIKE){{
			hitSound = Sounds.explosionbig;
			trailChance = 0.075f;
			trailEffect = NHFx.polyTrail;
			
			trailLength = 23;
			drawSize = 500f;
			
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
			backColor = lightningColor = trailColor = lightColor = NHItems.thermoCorePositive.color.cpy().lerp(Color.white, 0.025f);
			lifetime = 90f;
			ammoMultiplier = 4f;
			accelerateBegin = 0.1f;
			accelerateEnd = 0.85f;
			velocityEnd = 14f;
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
			trailEffect = NHFx.trail;
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
			velocityEnd = 18f;
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
			trailEffect = NHFx.trail;
			
			trailLength = 15;
			trailWidth = 3f;
			drawSize = 300f;
		}};
		
		curveBomb = new CurveBulletType(4f, 350f){
			@Override
			public void despawned(Bullet b){
				super.despawned(b);
				PosLightning.createRange(b, 200, 4, NHColor.thurmixRed, Mathf.chanceDelta(0.3f), PosLightning.WIDTH, 3, p -> {
					NHFx.lightningHitLarge(NHColor.thurmixRed).at(p);
					Damage.damage(b.team, p.getX(), p.getY(), splashDamageRadius / 6, splashDamage * b.damageMultiplier() / 6, collidesAir, collidesGround);
				});
			}
			
			{
				collidesAir = false;
				scaleVelocity = true;
				splashDamage = 80f;
				splashDamageRadius = 40f;
				
				hitShake = 8;
				hitSound = Sounds.explosionbig;
				drawSize = 400;
				lightColor = lightningColor = NHColor.thurmixRed;
				
				shootEffect = new Effect(90f, 160f, e -> {
					color(lightColor, NHColor.thurmixRedLight, e.fout());
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 90);
					Drawf.tri(e.x, e.y, 5 * e.fout(), Mathf.curve(e.fout(), 0, 0.1f) * 80, e.rotation + 270);
				});
				
				despawnEffect = new Effect(32f, e -> {
					color(Color.gray);
					Angles.randLenVectors(e.id + 1, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F));
					
					color(lightColor, NHColor.thurmixRedLight, e.fout());
					stroke(e.fout() * 2);
					circle(e.x, e.y, e.fin() * 50);
					Fill.circle(e.x, e.y, e.fout() * e.fout() * 13);
					randLenVectors(e.id, 4, 7 + 40 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8 + 3));
				});
				
				smokeEffect = new Effect(45f, e -> {
					color(lightColor, NHColor.thurmixRedLight, e.fout());
					randLenVectors(e.id, 10, 5 + 55 * e.fin(), e.rotation, 45, (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 3f));
				});
			}
			
		};
		
		strikeRocket = new BasicBulletType(9, 330, STRIKE){{
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
		
		annMissile = new BasicBulletType(6.6f, 50f, STRIKE){
			@Override
			public float range(){return 280f;}
			
			{
				trailLength = 12;
				
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
				lifetime = 85f;
				
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
		
		strikeMissile = new BasicBulletType(5, 80, STRIKE){{
			trailColor = lightningColor = backColor = lightColor = NHColor.thurmixRedLight;
			frontColor = NHColor.thurmixRedLight;
			lightning = 3;
			lightningCone = 360;
			lightningLengthRand = lightningLength = 9;
			splashDamageRadius = 60;
			splashDamage = lightningDamage = damage * 0.7f;
			lifetime = 180f;
			
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
		
		boltGene = new LightningLinkerBulletType(2.75f, 550){{
			trailWidth = 4.5f;
			trailLength = 66;
			
			setDisableAccel();
			
			outColor = trailColor = NHColor.darkEnrColor;
			innerColor = NHColor.darkEnr;
			generateDelay = 4f;
			randomGenerateRange = 280f;
			randomLightningNum = 6;
			boltNum = 1;
			linkRange = 280f;
			range = 800f;
			
			drawSize = 500f;
			
			lightningColor = NHColor.darkEnrColor;
			
			drag = 0.0065f;
			fragLifeMin = 0.3f;
			fragBullets = 11;
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
					
					trailEffect = NHFx.polyTrail(backColor, innerColor, 4.65f, 22f);
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
			drawSize = 40;
			splashDamageRadius = 120f;
			splashDamage = 300;
			lightningDamage = damage * 0.75f;
			collidesTiles = true;
			pierce = false;
			collides = false;
			ammoMultiplier = 1;
			lifetime = 300;
			hitEffect = Fx.none;
			despawnEffect = Fx.none;
			hitEffect = NHFx.largeDarkEnergyHit;
			shootEffect = NHFx.darkEnergyShootBig;
			smokeEffect = NHFx.darkEnergySmokeBig;
		}};
		
		empBlot2 = new ArtilleryBulletType(4f, 10f, NewHorizon.configName("circle-bolt")){{
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
			trailEffect = NHFx.trail;
			trailParam = width / 3;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor);
			collidesAir = collidesGround = true;
			collides = false;
			hitSound = Sounds.plasmaboom;
			
			fragBullet = empFrag;
			fragBullets = 2;
			fragVelocityMin = fragLifeMin = 0.95f;
			fragVelocityMax = fragLifeMax = 1.05f;
		}};
		
		empBlot3 = new ArtilleryBulletType(4f, 15f, NewHorizon.configName("circle-bolt")){{
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
			trailEffect = NHFx.trail;
			trailParam = width / 3;
			hitEffect = NHFx.lightningHitLarge(backColor);
			despawnEffect = NHFx.crossBlast(backColor);
			collidesAir = collidesGround = true;
			collides = false;
			hitSound = Sounds.plasmaboom;
			
			fragBullet = empFrag;
			fragBullets = 3;
			fragVelocityMin = fragLifeMin = 0.95f;
			fragVelocityMax = fragLifeMax = 1.05f;
		}};
	}
	
}














