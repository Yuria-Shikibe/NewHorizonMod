package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.MoveLightningAbility;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.bullet.SapBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.EntityMapping;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.AmmoTypes;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.bullets.LightningLinkerBulletType;
import newhorizon.bullets.NHTrailBulletType;
import newhorizon.bullets.ShieldBreaker;
import newhorizon.units.AutoOutlineUnitType;
import newhorizon.units.AutoOutlineWeapon;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class NHUnits implements ContentList {

	public static
	UnitType
	hurricane, tarlidor, striker, annihilation, warper;
	
	@Override
	public void load() {
		warper = new AutoOutlineUnitType("warper"){{
			constructor = EntityMapping.map(3);
			weapons.add(new Weapon(){{
				minShootVelocity = 0.01F;
				top = true;
				rotate = false;
				alternate = true;
				mirror = false;
				x = 0f;
				y = -10f;
				shootCone = 30f;
				reload = 60f;
				shots = 5;
				shotDelay = 7f;
				inaccuracy = 6f;
				ejectEffect = Fx.none;
				bullet = NHBullets.warperBullet;
				shootSound = NHSounds.launch;
			}});
			abilities.add(
					new MoveLightningAbility(10, 16, 0.2f, 12, 4, 6, NHColor.lightSky)
			);
			targetAir = false;
			maxRange = 200;
			engineOffset = 14.0F;
			engineSize = 4f;
			speed = 5f;
			faceTarget = true;
			accel = 0.04F;
			drag = 0.0075F;
			circleTarget = true;
			hitSize = 14f;
			health = 1000f;
			buildSpeed = 0.8f;
			baseRotateSpeed = 1.5f;
			rotateSpeed = 2.5f;
			armor = 3.5f;
			flying = true;
			hovering = false;
			canDrown = false;
			ammoType = AmmoTypes.thorium;
		}};
		
		annihilation = new AutoOutlineUnitType("annihilation",
			new AutoOutlineWeapon("large-launcher"){{
				top = false;
				rotate = false;
				alternate = true;
				shake = 3.5f;
				shootY = 16f;
				x = 20f;
				recoil = 5.4f;
				
				shootCone = 30f;
				reload = 30f;
				shots = 4;
				inaccuracy = 4.0F;
				ejectEffect = Fx.none;
				bullet = new ShrapnelBulletType() {{
					length = 280;
					damage = 160.0F;
					status = StatusEffects.shocked;
					statusDuration = 60f;
					fromColor = NHColor.lightSky.cpy().lerp(Color.white, 0.3f);
					toColor = NHColor.lightSky;
					shootEffect = NHFx.lightningHitSmall(NHColor.lightSky);
					smokeEffect = new MultiEffect(NHFx.lightSkyCircleSplash, new Effect(lifetime + 10f, e -> {
						Draw.color(fromColor, toColor, e.fin());
						Fill.circle(e.x, e.y, (width / 1.75f) * e.fout());
					}));
				}};
				shootSound = Sounds.shotgun;
			}},
			new AutoOutlineWeapon(""){{
				mirror = false;
				rotate = true;
				alternate = true;
				rotateSpeed = 25f;
				x = 0;
				y = 8f;
				recoil = 2.7f;
				shootY = 7f;
				shootCone = 40f;
				reload = 60f;
				shots = 2;
				shotDelay = 8f;
				inaccuracy = 5.0F;
				ejectEffect = Fx.none;
				bullet = NHBullets.annMissile;
				shootSound = NHSounds.launch;
			}}
		){{
			constructor = EntityMapping.map(32);
			abilities.add(
					new ForceFieldAbility(64.0F, 5F, 5000.0F, 900.0F)
			);
			range = 320f;
			engineOffset = 15.0F;
			engineSize = 6.5F;
			speed = 0.3f;
			hitSize = 29f;
			health = 25000f;
			buildSpeed = 2.8f;
			armor = 11f;
			rotateSpeed = 1.8f;
			hovering = true;
			canDrown = true;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.15f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
			ammoType = AmmoTypes.powerHigh;
		}};

		
		striker = new AutoOutlineUnitType("striker",
			new AutoOutlineWeapon("anti-air-pulse-laser"){{
				shake = 0f;
				shots = 3;
				shotDelay = 6f;
				rotate = top = true;
				heatColor = Pal.accent;
				shootSound = Sounds.missile;
				shootY = 3f;
				x = 11f;
				y = -7f;
				reload = 30f;
				bullet = new SapBulletType(){{
					keepVelocity = false;
					sapStrength = 0.4F;
					length = 90.0F;
					damage = 35.0F;
					shootEffect = Fx.shootSmall;
					hitColor = color = Pal.accent;
					despawnEffect = Fx.none;
					width = 0.48F;
					lifetime = 20.0F;
					knockback = -1.24F;
				}};
			}}
		){{
			weapons.add(
				new AutoOutlineWeapon("striker-weapon") {{
					mirror = false;
					rotate = false;
					continuous = true;
					alternate = false;
					shake = 4f;
					heatColor = Pal.accent;
					shootY = 13f;
					reload = 420f;
					shots = 1;
					x = y = 0f;
					bullet = NHBullets.strikeLaser;
					chargeSound = Sounds.none;
					shootSound = Sounds.none;
					shootStatus = StatusEffects.slow;
					shootStatusDuration = bullet.lifetime + 60f;
				}}
			);
			constructor = EntityMapping.map(3);
			lowAltitude = true;
			faceTarget = true;
			isCounted = true;
			health = 5500.0F;
			speed = 0.6F;
			accel = 0.02F;
			drag = 0.025F;
			flying = true;
			hitSize = 30.0F;
			armor = 4.0F;
			engineOffset = 28.5F;
			engineSize = 6.0F;
			rotateSpeed = 1.35F;
			buildSpeed = 0.8f;
		}};

		hurricane = new AutoOutlineUnitType("hurricane",
			new AutoOutlineWeapon() {{
				mirror = false;
				rotate = false;
				continuous = true;
				alternate = false;
				shake = 5f;
				shootY = 47f;
				reload = 220f;
				shots = 1;
				x = y = 0f;
				ejectEffect = Fx.none;
				recoil = 4.4f;
				bullet = NHBullets.hurricaneLaser;
				chargeSound = Sounds.lasercharge2;
				shootSound = Sounds.beam;
				shootStatus = StatusEffects.slow;
				shootStatusDuration = bullet.lifetime + firstShotDelay + 40f;
				firstShotDelay = NHFx.chargeEffectSmall(new Color(), 60f).lifetime - 1.0F;
			}},
			new AutoOutlineWeapon("swepter") {{
				mirror = false;
				top = true;
				rotate = true;
				alternate = false;
				shake = 5f;
				shootY = 17f;
				reload = 180f;
				shots = 1;
				y = - 40f;
				x = 0f;
				inaccuracy = 3.0F;
				ejectEffect = Fx.none;
				recoil = 4.4f;
				bullet = new LightningLinkerBulletType(2.3f, 200){{
					range = 340f;
					
					outColor = lightColor = lightningColor = NHColor.lightSky;
					innerColor = Color.white;
					generateDelay = 6f;
					randomGenerateRange = 280f;
					randomLightningNum = 5;
					boltNum = 3;
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
						color(NHColor.lightSky);
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
						color(NHColor.lightSky);
						Fill.circle(e.x, e.y, e.fout() * 32);
						color(Color.white);
						Fill.circle(e.x, e.y, e.fout() * 20);
					});
					smokeEffect = new Effect(40f, 100, e -> {
						color(NHColor.lightSky);
						stroke(e.fout() * 3.7f);
						circle(e.x, e.y, e.fin() * 100 + 15);
						stroke(e.fout() * 2.5f);
						circle(e.x, e.y, e.fin() * 60 + 15);
						randLenVectors(e.id, 15, 7f + 60f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 16f));
					});
				}};
				shootSound = Sounds.laserblast;
			}},
			new AutoOutlineWeapon("impulse") {{
				heatColor = NHColor.lightSky;
				top = true;
				rotate = true;
				shootY = 12f;
				reload = 50f;
				x = 40f;
				y = -30f;
				shots = 3;
				shotDelay = 10f;
				inaccuracy = 6.0F;
				velocityRnd = 0.38f;
				alternate = false;
				ejectEffect = Fx.none;
				recoil = 1.7f;
				shootSound = Sounds.plasmaboom;
				bullet = new NHTrailBulletType(7.4f, 60, NewHorizon.NHNAME + "strike") {
					@Override public float range(){return 440f;}
					{
						hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
						lifetime = 140f;
						pierce = pierceBuilding = true;
						width = 16f;
						height = 50f;
						backColor = lightColor = lightningColor = trailColor = NHColor.lightSky;
						frontColor = Color.white;
						lightning = 3;
						lightningDamage = damage / 2;
						lightningLength = lightningLengthRand = 5;
						smokeEffect = Fx.shootBigSmoke2;
						hitShake = 4f;
						hitSound = Sounds.plasmaboom;
						shrinkX = shrinkY = 0f;
					}
				};
			}}
			){{
				constructor = EntityMapping.map(3);
				abilities.add(
						new ForceFieldAbility(120.0F, 6F, 20000.0F, 1200.0F),
						new RepairFieldAbility(800f, 160f, 240f){{
							healEffect = new Effect(11.0F, (e) -> {
								Draw.color(NHColor.lightSky);
								Lines.stroke(e.fout() * 2.0F);
								Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * 79.0F);
							});
							activeEffect = new Effect(22.0F, (e) -> {
								Draw.color(NHColor.lightSky);
								Lines.stroke(e.fout() * 3.0F);
								Lines.poly(e.x, e.y, 6,4.0F + e.finpow() * e.rotation);
							});
						}}
				);
				commandLimit = 6;
				lowAltitude = true;
				isCounted = true;
				faceTarget = true;
				itemCapacity = 500;
				health = 30000.0F;
				speed = 1.4F;
				accel = 0.04F;
				drag = 0.025F;
				flying = true;
				hitSize = 100.0F;
				armor = 12.0F;
				engineOffset = 55.0F;
				engineSize = 20.0F;
				rotateSpeed = 1.15F;
				buildSpeed = 2.8f;
			}
		};

		tarlidor = new AutoOutlineUnitType("tarlidor",
			new AutoOutlineWeapon("stiken"){{
				top = false;
				shake = 3f;
				shootY = 13f;
				reload = 50f;
				shots = 2;
				shotDelay = 7f;
				x = 17.5f;
				inaccuracy = 3.0F;
				alternate = true;
				ejectEffect = Fx.none;
				recoil = 4.4f;
				bullet = new ShieldBreaker(6.25f, 50, 650f) {
					@Override public float range(){return 280f;}
					{
						hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
						lifetime = 90f;
						pierceCap = 8;
						width = 20f;
						height = 44f;
						backColor = lightColor = lightningColor = trailColor = NHColor.lightSky;
						frontColor = Color.white;
						lightning = 3;
						lightningDamage = damage / 4;
						lightningLength = 3;
						lightningLengthRand = 10;
						smokeEffect = Fx.shootBigSmoke2;
						hitShake = 4f;
						hitSound = Sounds.plasmaboom;
						shrinkX = shrinkY = 0f;
					}
				};
				shootSound = Sounds.laser;
			}},
			new AutoOutlineWeapon("arc-blaster"){{
				top = true;
				rotate = true;
				shootY = 12f;
				reload = 45f;
				shots = 2;
				rotateSpeed = 5f;
				inaccuracy = 6.0F;
				velocityRnd = 0.38f;
				x = 8f;
				alternate = false;
				ejectEffect = Fx.none;
				recoil = 1.7f;
				bullet = NHBullets.skyFrag;
				shootSound = Sounds.plasmaboom;
			}}){{
				constructor = EntityMapping.map(32);
				abilities.add(new ShieldRegenFieldAbility(50.0F, 50F, 600.0F, 800.0F));
				engineOffset = 13.0F;
				engineSize = 6.5F;
				speed = 0.4f;
				hitSize = 20f;
				health = 9000f;
				buildSpeed = 1.8f;
				armor = 8f;
				rotateSpeed = 3.3f;
				hovering = true;
				canDrown = true;
				fallSpeed = 0.016f;
				mechStepParticles = true;
				mechStepShake = 0.15f;
				canBoost = true;
				landShake = 6f;
				boostMultiplier = 3.5f;
				ammoType = AmmoTypes.powerHigh;
			}};

		//Load End
	}

}














