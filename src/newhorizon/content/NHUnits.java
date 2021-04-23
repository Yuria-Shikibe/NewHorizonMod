package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.ai.types.MinerAI;
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
import mindustry.type.AmmoTypes;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.bullets.NHTrailBulletType;
import newhorizon.bullets.PosLightningType;
import newhorizon.bullets.ShieldBreaker;
import newhorizon.units.AutoOutlineUnitType;
import newhorizon.units.AutoOutlineWeapon;

public class NHUnits implements ContentList {
	public static
	AutoOutlineWeapon
	posLiTurret, closeAATurret;
	
	public static
	UnitType
	hurricane, tarlidor, striker, annihilation, warper, destruction, gather, aliotiat;
	
	public void loadWeapon(){
		posLiTurret = new AutoOutlineWeapon("pos-li-blaster"){{
			shake = 1f;
			shots = 1;
			rotate = top = alternate = true;
			reload = 30f;
			shootY = 4f;
			shootSound = Sounds.spark;
			heatColor = NHColor.lightSky;
			bullet = new PosLightningType(20f){{
				lightningColor = NHColor.lightSky;
				maxRange = 160f;
				hitEffect = NHFx.lightningHitSmall(lightningColor);
				lightningLength = 1;
				lightningLengthRand = 4;
			}};
		}};
		
		closeAATurret = new AutoOutlineWeapon("anti-air-pulse-laser"){{
			shake = 0f;
			shots = 3;
			shotDelay = 6f;
			rotate = top = true;
			heatColor = NHColor.lightSky;
			shootSound = Sounds.missile;
			shootY = 3f;
			recoil = 2f;
			x = 9.5f;
			y = -7f;
			reload = 30f;
			bullet = new SapBulletType(){{
				keepVelocity = false;
				sapStrength = 0.4F;
				length = 100.0F;
				damage = 35.0F;
				shootEffect = Fx.shootSmall;
				hitColor = color = NHColor.lightSky;
				despawnEffect = Fx.none;
				width = 0.48F;
				lifetime = 20.0F;
				knockback = -1.24F;
			}};
		}};
	}
	
	@Override
	public void load() {
		loadWeapon();
		gather = new AutoOutlineUnitType("gather"){{
			defaultController = MinerAI::new;
			constructor = EntityMapping.map(3);
			hitSize = 16f;
			flying = true;
			drag = 0.06F;
			accel = 0.12F;
			itemCapacity = 120;
			speed = 1.2F;
			health = 1200.0F;
			engineSize = 3.4F;
			engineOffset = 9.2F;
			range = 80.0F;
			isCounted = false;
			ammoType = AmmoTypes.powerLow;
			mineTier = 5;
			mineSpeed = 10F;
			lowAltitude = true;
		}};
		
		aliotiat = new AutoOutlineUnitType("aliotiat",
			posLiTurret.copy().setPos(10f, 3f).setDelay(closeAATurret.reload / 2f),
			posLiTurret.copy().setPos(6f, -2f)
		){{
			constructor = EntityMapping.map(32);
			engineOffset = 10.0F;
			engineSize = 4.5F;
			speed = 0.35f;
			hitSize = 17f;
			health = 1200f;
			buildSpeed = 1.2f;
			armor = 5f;
			rotateSpeed = 2.8f;
			hovering = true;
			canDrown = true;
			singleTarget = false;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.15f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
			ammoType = AmmoTypes.power;
		}};
		
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
						spin = 2.75f;
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
			}}
		){{
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
			new AutoOutlineWeapon(){{
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
			singleTarget = false;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.15f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
			ammoType = AmmoTypes.powerHigh;
		}};
		
		warper = new AutoOutlineUnitType("warper"){{
			constructor = EntityMapping.map(3);
			weapons.add(new Weapon(){{
				top = true;
				rotate = true;
				alternate = true;
				mirror = false;
				x = 0f;
				y = -10f;
				reload = 6f;
				inaccuracy = 3f;
				ejectEffect = Fx.none;
				bullet = NHBullets.warperBullet;
				shootSound = NHSounds.blaster;
			}});
			abilities.add(new MoveLightningAbility(10, 16, 0.2f, 12, 4, 6, NHColor.lightSky));
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
		
		striker = new AutoOutlineUnitType("striker", closeAATurret){{
			weapons.add(new AutoOutlineWeapon("striker-weapon") {{
					mirror = false;
					rotate = false;
					continuous = true;
					alternate = false;
					shake = 4f;
					heatColor = NHColor.lightSky;
					shootY = 13f;
					reload = 420f;
					shots = 1;
					x = y = 0f;
					bullet = NHBullets.strikeLaser;
					chargeSound = Sounds.none;
					shootSound = Sounds.none;
					shootStatus = StatusEffects.slow;
					shootStatusDuration = bullet.lifetime + 60f;
				}});
			constructor = EntityMapping.map(3);
			lowAltitude = true;
			faceTarget = true;
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
		
		destruction = new AutoOutlineUnitType("destruction",
				closeAATurret.copy().setPos(37, -18),
				closeAATurret.copy().setPos(26, -8),
				new AutoOutlineWeapon(){{
					alternate = mirror = false;
					top = rotate = true;
					x = 0;
					y = 0f;
					reload = 300f;
					shots = 1;
					ejectEffect = Fx.none;
					bullet = NHBullets.polyCloud;
					shootSound = Sounds.plasmadrop;
				}},
				new AutoOutlineWeapon("arc-blaster"){{
					alternate = mirror = top = rotate = true;
					x = 10f;
					y = 4f;
					recoil = 3f;
					shootCone = 20f;
					reload = 120f;
					shots = 1;
					inaccuracy = 6f;
					shake = 5f;
					shootY = 5f;
					ejectEffect = Fx.none;
					bullet = NHBullets.longLaser;
					shootSound = Sounds.laser;
				}}
		){{
			constructor = EntityMapping.map(3);
			armor = 25.0F;
			health = 25000.0F;
			speed = 0.65F;
			rotateSpeed = 1.0F;
			accel = 0.04F;
			drag = 0.018F;
			flying = true;
			engineOffset = 16F;
			engineSize = 6F;
			hitSize = 36.0F;
			buildSpeed = 1.25F;
			drawShields = false;
			commandLimit = 8;
			lowAltitude = true;
			singleTarget = false;
			buildBeamOffset = 15F;
			ammoCapacity = 800;
			ammoResupplyAmount = 60;
			abilities.add(
					new ForceFieldAbility(100.0F, 4.0F, 4000.0F, 360.0F),
					new RepairFieldAbility(500f, 160f, 240f){{
						healEffect = NHFx.healEffect;
						activeEffect = NHFx.activeEffect;
					}}
			);
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
				bullet = NHBullets.hurricaneType;
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
				bullet = new NHTrailBulletType(7.4f, 60, NewHorizon.MOD_NAME + "strike") {
					@Override public float range(){return 440f;}
					{
						trailWeaveMag = 4f;
						trailWeaveScale = 2f;
						flip = combine = true;
						trails = 2;
						trailOffset = 10f;
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
						healEffect = NHFx.healEffect;
						activeEffect = NHFx.activeEffect;
					}}
				);
				commandLimit = 6;
				lowAltitude = true;
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
		//Load End
	}

}














