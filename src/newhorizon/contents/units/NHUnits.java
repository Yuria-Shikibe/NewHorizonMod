package newhorizon.contents.units;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.*;
import mindustry.type.*;

import newhorizon.NewHorizon;
import newhorizon.contents.bullets.*;
import newhorizon.func.NHLightningBolt;
import newhorizon.contents.colors.NHColor;
import newhorizon.contents.effects.NHFx;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class NHUnits implements ContentList {

	public static
	UnitType
	hurricane, tarlidor;
	
	@Override
	public void load() {
		hurricane = new UnitType("hurricane") {
			{
				constructor = EntityMapping.map(3);
				abilities.add(
						new ForceFieldAbility(120.0F, 200F, 20000.0F, 600.0F),
						new RepairFieldAbility(1000f, 160f, 240f){{
							this.healEffect = new Effect(11.0F, (e) -> {
								Draw.color(NHColor.lightSky);
								Lines.stroke(e.fout() * 2.0F);
								Lines.poly(e.x, e.y, 6, 2.0F + e.finpow() * 79.0F);
							});
							this.activeEffect = new Effect(22.0F, (e) -> {
								Draw.color(NHColor.lightSky);
								Lines.stroke(e.fout() * 3.0F);
								Lines.poly(e.x, e.y, 6,4.0F + e.finpow() * e.rotation);
							});
						}}
				);
				trailLength = 30;
				trailScl = 0.72f;
				commandLimit = 6;
				lowAltitude = true;
				isCounted = true;
				this.health = 60000.0F;
				this.speed = 1.4F;
				this.accel = 0.04F;
				this.drag = 0.025F;
				this.flying = true;
				this.range = 640.0F;
				this.hitSize = 100.0F;
				this.armor = 12.0F;
				this.engineOffset = 55.0F;
				this.engineSize = 20.0F;
				this.rotateSpeed = 1.15F;
				buildSpeed = 2.8f;
				weapons.add(
						new Weapon() {{
							mirror = false;
							rotate = false;
							continuous = true;
							alternate = false;
							range = 480f;
							shake = 5f;
							shootY = 47f;
							reload = 220f;
							shots = 1;
							x = y = 0f;
							inaccuracy = 3.0F;
							ejectEffect = Fx.none;
							recoil = 4.4f;
							bullet = NHBullets.hurricaneLaser;
							chargeSound = Sounds.lasercharge2;
							shootSound = Sounds.beam;
							this.shootStatus = StatusEffects.slow;
							this.shootStatusDuration = this.bullet.lifetime + this.firstShotDelay + 40f;
							this.firstShotDelay = NHFx.skyLaserChargeSmall.lifetime - 1.0F;
						}},

						new Weapon("new-horizon-swepter") {{
							range = 300f;
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
							bullet = new ArtilleryBulletType(2.25f, 160) {
								@Override public float range(){return 300f;}
								@Override
								public void update(Bullet b) {
									Effect.shake(2, 2, b);
									if (b.timer(2, 8) && (b.lifetime - b.time) > NHLightningBolt.lifetime){
										for(int i : Mathf.signs){
											new Effect(25, e -> {
												Draw.color(NHColor.lightSky);
												Angles.randLenVectors(e.id, 4, 3 + 60 * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 13f));
												Lines.stroke((i < 0 ? e.fin() : e.fout()) * 3f);
												Lines.circle(e.x, e.y, (i > 0 ? e.fin() : e.fout()) * 33f);
											}).at(b.x + Mathf.range(8f), b.y + Mathf.range(8f), b.rotation());
										}
										NHLightningBolt.createRange(b, 240, 6, 1, splashDamage * b.damageMultiplier(), NHColor.lightSky, Mathf.chance(Time.delta * 0.13), 1.33f * NHLightningBolt.WIDTH);
									}
								}

								@Override
								public void init(Bullet b) {
									b.vel.scl(1 + drag * b.lifetime / b.type.speed);
									b.lifetime(b.lifetime * 1.2f);
								}

								@Override
								public void draw(Bullet b) {
									color(NHColor.lightSky);
									Fill.circle(b.x, b.y, 20);
									color(Color.white);
									Fill.circle(b.x, b.y, 12f);
								}

								@Override
								public void despawned(Bullet b) {
									for (int i = 0; i < Mathf.random(4f, 7f); i++) {
										Vec2 randomPos = new Vec2(b.x + Mathf.range(200), b.y + Mathf.range(200));
										hitSound.at(randomPos, Mathf.random(0.9f, 1.1f) );
										NHLightningBolt.create(new Vec2(b.x, b.y), randomPos, b.team(), NHColor.lightSky, 1.7f * NHLightningBolt.WIDTH, 2, hitPos -> {
											for (int j = 0; j < 4; j++) {
												Lightning.create(b.team(), NHColor.lightSky, this.splashDamage * b.damageMultiplier(), hitPos.getX(), hitPos.getY(), Mathf.random(360), Mathf.random(8, 12));
											}
											Damage.damage(b.team(), hitPos.getX(), hitPos.getY(), 80f, 8 * this.splashDamage * b.damageMultiplier());
											new Effect(25, e -> {
												color(NHColor.lightSky);
												e.scaled(12, t -> {
													stroke(3f * t.fout());
													circle(e.x, e.y, 3f + t.fin() * 80f);
												});
												Fill.circle(e.x, e.y, e.fout() * 8f);
												Angles.randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));
											}).at(hitPos);
										});
									}
									super.despawned(b);
								}

								{
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

						new Weapon(NewHorizon.NHNAME + "impulse") {{
							heatColor = NHColor.lightSky;
							range = 440f;
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
				);

			}
		};

		tarlidor = new UnitType("tarlidor") {
			{
				constructor = EntityMapping.map(32);
				abilities.add(new ShieldRegenFieldAbility(50.0F, 50F, 600.0F, 800.0F));
				engineOffset = 13.0F;
				engineSize = 6.5F;
				speed = 0.4f;
				hitSize = 20f;
				health = 12500f;
				buildSpeed = 1.8f;
				armor = 7f;
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

				weapons.add(
					new Weapon("new-horizon-stiken") {{
						range = 260f;
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
						bullet = new NHTrailBulletType(7.4f, 60) {
							@Override public float range(){return 260f;}
							{
								hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
								lifetime = 80f;
								pierce = pierceBuilding = true;
								width = 13f;
								height = 40f;
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
						shootSound = Sounds.laser;
					}},

                    new Weapon("new-horizon-arc-blaster") {
						{
							range = 320f;
							top = true;
							rotate = true;
							shootY = 12f;
							reload = 30f;

							shots = 3;
							inaccuracy = 6.0F;
							velocityRnd = 0.38f;
							x = 8f;
							alternate = false;
							ejectEffect = Fx.none;
							recoil = 1.7f;
							bullet = NHBullets.skyFrag;
							shootSound = Sounds.plasmaboom;
					}}
				);
			}

		};

		//Load End
	}

}














