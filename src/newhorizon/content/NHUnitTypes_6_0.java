//package newhorizon.content;
//
//import arc.graphics.Blending;
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.Fill;
//import arc.graphics.g2d.Lines;
//import arc.graphics.g2d.TextureRegion;
//import arc.math.Angles;
//import arc.math.Interp;
//import arc.math.Mathf;
//import arc.math.Rand;
//import arc.math.geom.Vec2;
//import arc.struct.ObjectSet;
//import arc.struct.Seq;
//import arc.util.Time;
//import arc.util.Tmp;
//import mindustry.ai.types.BuilderAI;
//import mindustry.ai.types.MinerAI;
//import mindustry.content.Fx;
//import mindustry.content.StatusEffects;
//import mindustry.entities.Damage;
//import mindustry.entities.Effect;
//import mindustry.entities.Units;
//import mindustry.entities.abilities.*;
//import mindustry.entities.bullet.BasicBulletType;
//import mindustry.entities.bullet.BulletType;
//import mindustry.entities.bullet.ContinuousLaserBulletType;
//import mindustry.entities.bullet.FlakBulletType;
//import mindustry.entities.effect.MultiEffect;
//import mindustry.entities.pattern.ShootPattern;
//import mindustry.entities.units.WeaponMount;
//import mindustry.game.Team;
//import mindustry.gen.*;
//import mindustry.graphics.Drawf;
//import mindustry.graphics.Layer;
//import mindustry.graphics.MultiPacker;
//import mindustry.graphics.Pal;
//import mindustry.type.UnitType;
//import mindustry.type.Weapon;
//import mindustry.type.ammo.PowerAmmoType;
//import mindustry.type.weapons.PointDefenseWeapon;
//import mindustry.type.weapons.RepairBeamWeapon;
//import mindustry.world.meta.BlockFlag;
//import newhorizon.NewHorizon;
//import newhorizon.expand.bullets.ChainBulletType;
//import newhorizon.expand.bullets.EffectBulletType;
//import newhorizon.expand.bullets.TrailFadeBulletType;
//import newhorizon.expand.entities.UltFire;
//import newhorizon.expand.units.EnergyUnit;
//import newhorizon.expand.units.SniperAI;
//import newhorizon.util.feature.PosLightning;
//import newhorizon.util.func.NHFunc;
//import newhorizon.util.func.NHInterp;
//import newhorizon.util.func.NHPixmap;
//import newhorizon.util.graphic.DrawFunc;
//import newhorizon.util.graphic.OptionalMultiEffect;
//
//import static arc.graphics.g2d.Draw.color;
//import static arc.graphics.g2d.Lines.lineAngle;
//import static arc.graphics.g2d.Lines.stroke;
//import static mindustry.Vars.*;
//
//public class NHUnitTypes_6_0{
//	private static final Color OColor = Color.valueOf("565666");
//
//	public static final byte OTHERS = Byte.MIN_VALUE, GROUND_LINE_1 = 0, AIR_LINE_1 = 1, AIR_LINE_2 = 2, ENERGY_LINE_1 = 3, NAVY_LINE_1 = 6;
//
//
//
//	public static Weapon pointDefenceWeaponC;
//
//	public static UnitType
//			guardian, //Energy
//			gather, saviour, rhino, //Air-Assist
//			assaulter, anvil, collapser, //Air-2
//			origin, thynomo, aliotiat, tarlidor, annihilation, sin, //Ground-1
//			sharp, branch, warper, striker, naxos, destruction, longinus, hurricane, //Air-1
//			relay, ghost, zarkov, declining; //Navy
//
//	static{
//		EntityMapping.nameMap.put(NewHorizon.name("declining"), EntityMapping.idMap[20]);
//		EntityMapping.nameMap.put(NewHorizon.name("zarkov"), EntityMapping.idMap[20]);
//		EntityMapping.nameMap.put(NewHorizon.name("ghost"), EntityMapping.idMap[20]);
//		EntityMapping.nameMap.put(NewHorizon.name("relay"), EntityMapping.idMap[20]);
//
//		EntityMapping.nameMap.put(NewHorizon.name("saviour"), EntityMapping.idMap[5]);
//
//		EntityMapping.nameMap.put(NewHorizon.name("origin"), EntityMapping.idMap[4]);
//		EntityMapping.nameMap.put(NewHorizon.name("thynomo"), EntityMapping.idMap[4]);
//		EntityMapping.nameMap.put(NewHorizon.name("aliotiat"), EntityMapping.idMap[4]);
//		EntityMapping.nameMap.put(NewHorizon.name("tarlidor"), EntityMapping.idMap[4]);
//		EntityMapping.nameMap.put(NewHorizon.name("annihilation"), EntityMapping.idMap[4]);
//		EntityMapping.nameMap.put(NewHorizon.name("sin"), EntityMapping.idMap[4]);
//
//		EntityMapping.nameMap.put(NewHorizon.name("guardian"), EnergyUnit::new);
//	}
//
//
//	@Override
//	public void load(){
//		loadWeapon();
//
//		//Others:
//
//		//Navy:
//
//		relay = new UnitType("relay"){{
//			outlineColor = OColor;
//			armor = 6;
//			buildBeamOffset = 6f;
//			hitSize = 20f;
//			drag = 0.06F;
//			itemCapacity = 20;
//			speed = 1.2F;
//			health = 800.0F;
//			accel = 0.12f;
//			rotateSpeed = 5f;
//
//			buildSpeed = 1.125f;
//
//			trailLength = 70;
//			trailScl = 1.65f;
//
//			weapons.add(new Weapon(NewHorizon.name("primary-weapon")){{
//				mirror = top = rotate = alternate = true;
//				reload = 60f;
//				shoot = new ShootPattern(){{
//					shotDelay = 6f;
//					shots = 4;
//				}};
//
//				x = 5f;
//				rotateSpeed = 12f;
//				y = -6f;
//				shootY = 18f;
//				velocityRnd = 0.075f;
//				inaccuracy = 5f;
//				shootSound = Sounds.missile;
//				bullet = new BasicBulletType(5f, 25f, "missile-large"){{
//					lifetime = 65f;
//					backColor = hitColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
//					frontColor = NHColor.lightSkyFront;
//
//					splashDamage = damage / 4f;
//					splashDamageRadius = 24f;
//
//					hitEffect = NHFx.blast(backColor, splashDamageRadius);
//					despawnEffect = NHFx.square45_6_45;
//					shootEffect = NHFx.shootCircleSmall(backColor);
//					smokeEffect = Fx.shootBigSmoke;
//
//					trailLength = 12;
//					trailWidth = 1.75f;
//
//					width = 7f;
//					height = 30f;
//
//					homingDelay = 5f;
//					homingPower = 0.02f;
//					homingRange = 200f;
//				}};
//			}});
//		}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//		};
//
//
//
//		//Grounds:
//
//
//
//
//		tarlidor = new UnitType("tarlidor"){{
//			outlineColor = OColor;
//			abilities.add(new ShieldRegenFieldAbility(50.0F, 50F, 600.0F, 800.0F));
//			weapons.add(
//				new NHWeapon("stiken"){{
//					top = false;
//					shake = 3f;
//					shootY = 13f;
//					reload = 50f;
//					shots = 2;
//					shotDelay = 7f;
//					x = 17.5f;
//					inaccuracy = 3.0F;
//					alternate = true;
//					ejectEffect = Fx.none;
//					recoil = 4.4f;
//					bullet = new ShieldBreakerType(4.25f, 40, 650f){{
//							drawSize = 500f;
//							trailLength = 18;
//							trailWidth = 3.5f;
//							spin = 2.75f;
//							despawnEffect = NHFx.square45_6_45;
//							hitEffect = new Effect(45f, e -> {
//								Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin());
//								Lines.stroke(2.5f * e.fout());
//								if(NHSetting.enableDetails())DrawFunc.randLenVectors(e.id, e.fin(Interp.pow3Out), 3, 6, 21f, (x1, y1, fin, fout) -> {
//									Lines.square(e.x + x1, e.y + y1, 14 * Interp.pow3Out.apply(fin), 45);
//								});
//								Lines.spikes(e.x, e.y, 28 * e.finpow(), 5 * e.fout() + 8 * NHInterp.parabola4Reversed.apply(e.fin()), 4, 45);
//							});
//							lifetime = 50f;
//							pierceCap = 8;
//							width = 20f;
//							height = 44f;
//							backColor = lightColor = lightningColor = hitColor = trailColor = NHColor.lightSkyBack;
//							shootEffect = NHFx.shootLineSmall(backColor);
//
//							frontColor = Color.royal.cpy().lerp(NHColor.lightSkyFront, 0.3f);
//							lightning = 3;
//							lightningDamage = damage / 4;
//							lightningLength = 3;
//							lightningLengthRand = 10;
//							smokeEffect = Fx.shootBigSmoke2;
//							hitShake = 4f;
//							hitSound = Sounds.plasmaboom;
//							shrinkX = shrinkY = 0f;
//					}};
//					shootSound = Sounds.laser;
//				}}, new NHWeapon("arc-blaster"){{
//					top = true;
//					rotate = true;
//					shootY = 12f;
//					reload = 45f;
//					shots = 2;
//					rotateSpeed = 5f;
//					inaccuracy = 6.0F;
//					velocityRnd = 0.38f;
//					shotDelay = 3.8f;
//					x = 8f;
//					alternate = false;
//					ejectEffect = Fx.none;
//					recoil = 1.7f;
//					bullet = NHBullets.skyFrag;
//					shootSound = Sounds.plasmaboom;
//				}}
//			);
//
//			engineOffset = 13.0F;
//			engineSize = 6.5F;
//			speed = 0.4f;
//			hitSize = 20f;
//			health = 7000f;
//			buildSpeed = 1.8f;
//			armor = 6f;
//			rotateSpeed = 3.3f;
//			fallSpeed = 0.016f;
//			mechStepParticles = true;
//			mechStepShake = 0.15f;
//			canBoost = true;
//			landShake = 6f;
//			boostMultiplier = 3.5f;
//		}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this); NHPixmap.outlineLegs(packer, this);}
//		};
//
//
//
//
//
//		//Air 1:
//
//
//
//		striker = new UnitType("striker"){{
//			outlineColor = OColor;
//			defaultController = SniperAI::new;
//			targetFlags = playerTargetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.turret, BlockFlag.generator, null};
//			weapons.add(new NHWeapon("striker-weapon"){{
//				mirror = false;
//				rotate = false;
//				continuous = true;
//				alternate = false;
//				shake = 4f;
//				heatColor = NHColor.lightSkyBack;
//				shootY = 13f;
//				reload = 420f;
//				shots = 1;
//				x = y = 0f;
//				predictTarget = false;
//				bullet = NHBullets.strikeLaser;
//				chargeSound = Sounds.none;
//				shootSound = Sounds.none;
//				shootStatus = StatusEffects.slow;
//				shootStatusDuration = bullet.lifetime + 60f;
//			}}, closeAATurret);
//			constructor = EntityMapping.map(3);
//			lowAltitude = true;
//			health = 5500.0F;
//			speed = 0.8F;
//			accel = 0.02F;
//			drag = 0.025F;
//			flying = true;
//			hitSize = 30.0F;
//			armor = 4.0F;
//			engineOffset = 28.5F;
//			engineSize = 6.0F;
//			rotateSpeed = 1.35F;
//			buildSpeed = 0.8f;
//		}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//		};
//
//
//
//		destruction = new UnitType("destruction"){{
//			outlineColor = OColor;
//
//			defaultController = SniperAI::new;
//
//			constructor = EntityMapping.map(3);
//			weapons.addAll(
//				closeAATurret.copy().setPos(37, -18), closeAATurret.copy().setPos(26, -8), new NHWeapon(){{
//					alternate = mirror = false;
//					top = rotate = true;
//					x = 0;
//					y = 0f;
//					reload = 300f;
//					shots = 1;
//					ejectEffect = Fx.none;
//					bullet = NHBullets.polyCloud;
//					shootSound = Sounds.plasmadrop;
//				}},
//				new NHWeapon("arc-blaster"){{
//					alternate = mirror = top = rotate = true;
//					x = 10f;
//					y = 4f;
//					recoil = 3f;
//					shootCone = 20f;
//					reload = 120f;
//					shots = 1;
//					inaccuracy = 6f;
//					shake = 5f;
//					shootY = 5f;
//					ejectEffect = Fx.none;
//					predictTarget = false;
//					bullet = new ChainBulletType(300){{
//						hitColor = NHColor.lightSkyBack;
//						hitEffect = NHFx.square(hitColor, 20f, 2, 16f, 3f);
//						smokeEffect = Fx.shootBigSmoke;
//						shootEffect = NHFx.shootLineSmall(hitColor);
//					}};
//					//bullet = NHBullets.longLaser;
//					shootSound = Sounds.laser;
//				}},
//				new Weapon(){{
//					x = shootX = shootY = 0;
//					y = -2;
//					xRand = 27f;
//
//					rotate = mirror = alternate = false;
//
//					bullet = NHBullets.destructionRocket;
//
//					shots = 20;
//					shotDelay = 3.5f;
//					reload = 300f;
//					inaccuracy = 2f;
//					velocityRnd = 0.1f;
//
//					shake = 1.25f;
//					shootSound = NHSounds.launch;
//					shootCone = 8f;
//				}}
//			);
//			armor = 15.0F;
//			health = 15000.0F;
//			speed = 0.45F;
//			rotateSpeed = 1.0F;
//			accel = 0.04F;
//			drag = 0.018F;
//			flying = true;
//			engineOffset = 16F;
//			engineSize = 6F;
//			hitSize = 36.0F;
//			buildSpeed = 1.25F;
//			drawShields = false;
//			commandLimit = 8;
//			lowAltitude = true;
//			singleTarget = false;
//			buildBeamOffset = 15F;
//			ammoCapacity = 800;
//			abilities.add(new RepairFieldAbility(500f, 160f, 240f){{
//				healEffect = NHFx.healEffectSky;
//				activeEffect = NHFx.activeEffectSky;
//			}});
//
//			targetFlags = playerTargetFlags = new BlockFlag[]{BlockFlag.turret, BlockFlag.factory, BlockFlag.reactor, BlockFlag.generator, BlockFlag.core, null};
//		}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//		};
//
//		longinus = new UnitType("longinus"){{
//			outlineColor = OColor;
//			constructor = EntityMapping.map(3);
//			lowAltitude = true;
//			health = 10000.0F;
//			speed = 0.5F;
//			accel = 0.02F;
//			drag = 0.025F;
//			flying = true;
//			circleTarget = false;
//			hitSize = 50.0F;
//			armor = 15.0F;
//			engineOffset = 46f;
//			engineSize = 12.0F;
//			rotateSpeed = 0.65f;
//			buildSpeed = 3f;
//			commandLimit = 6;
//			commandRadius = 220f;
//
//			defaultController = SniperAI::new;
//			targetFlags = playerTargetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.turret, BlockFlag.generator, null};
//
//			buildBeamOffset = 15f;
//
//			weapons.add(
//					closeAATurret.copy().setPos(13f, -23f),
//					closeAATurret.copy().setPos(19f, -28f),
//					new NHWeapon("longinus-weapon"){{
//						shootY = 20;
//						shots = 1;
//						x = 0;
//						y = 12.75f;
//						recoil = 10;
//						reload = 300f;
//						cooldownTime = 150f;
//						shake = 12f;
//
//						top = false;
//						mirror = rotate = false;
//						shootSound = NHSounds.railGunBlast;
//						soundPitchMax = 1.1f;
//						soundPitchMin = 0.9f;
//
//						bullet = new TrailFadeBulletType(20f, 2000f){{
//							recoil = 0.345f;
//							lifetime = 40f;
//							trailLength = 200;
//							trailWidth = 2F;
//							keepVelocity = false;
//
//							disableAccel();
//
//							trailColor = hitColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
//							frontColor = NHColor.lightSkyFront;
//							width = 10f;
//							height = 40f;
//
//							hitSound = Sounds.explosionbig;
//							despawnShake = hitShake = 18f;
//
//							lightning = 5;
//							lightningLength = 14;
//							lightningLengthRand = 22;
//							lightningDamage = 600;
//
//							smokeEffect = NHFx.square(hitColor, 80f, 8, 48f, 6f);
//							shootEffect = NHFx.instShoot(backColor);
//							despawnEffect = NHFx.lightningHitLarge;
//							hitEffect = NHFx.instHit(hitColor, 5, 80f);
//							despawnHit = true;
//						}
//
//							@Override
//							public float range(){
//								return 800;
//							}
//
//							@Override
//							public void hit(Bullet b, float x, float y){
//								super.hit(b, x, y);
//
//								UltFire.createChance(x, y, 42, 0.35f, b.team);
//							}
//						};
//
//						shootStatus = StatusEffects.slow;
//						shootStatusDuration = bullet.lifetime * 1.5f;
//					}
//						@Override
//						public void draw(Unit unit, WeaponMount mount){
//							float z = Draw.z();
//							Draw.z(z - 0.001f);
//							float
//									rotation = unit.rotation - 90,
//									weaponRotation  = rotation + (rotate ? mount.rotation : 0),
//									recoil = -((mount.reload) / reload * this.recoil),
//									wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, recoil),
//									wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, recoil);
//
//							if(shadow > 0){
//								Drawf.shadow(wx, wy, shadow);
//							}
//
//							if(outlineRegion.found() && top){
//								Draw.rect(outlineRegion,
//										wx, wy,
//										outlineRegion.width * Draw.scl * -Mathf.sign(flipSprite),
//										region.height * Draw.scl,
//										weaponRotation);
//							}
//
//							Draw.rect(region,
//									wx, wy,
//									region.width * Draw.scl * -Mathf.sign(flipSprite),
//									region.height * Draw.scl,
//									weaponRotation);
//
//							Draw.z(z);
//							if(heatRegion.found() && mount.heat > 0){
//								Draw.color(heatColor, mount.heat);
//								Draw.blend(Blending.additive);
//								Draw.rect(heatRegion,
//										wx, wy,
//										heatRegion.width * Draw.scl * -Mathf.sign(flipSprite),
//										heatRegion.height * Draw.scl,
//										weaponRotation);
//								Draw.blend();
//								Draw.color();
//							}
//						}
//					});
//		}
//			@Override
//			public void drawSoftShadow(Unit unit){
//				float z = Draw.z();
//				Draw.z(z - 0.01f);
//				super.drawSoftShadow(unit);
//				Draw.z(z);
//			}
//
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//		};
//
//		hurricane = new UnitType("hurricane"){{
//			drawShields = false;
//			outlineColor = OColor;
//				commandRadius = 240f;
//				constructor = EntityMapping.map(3);
//
//				weapons.add(NHWeapon.setPos(pointDefenceWeaponC.copy(), 22, -38));
//				weapons.add(NHWeapon.setPos(pointDefenceWeaponC.copy(), 19, -30));
//				weapons.add(NHWeapon.setPos(pointDefenceWeaponC.copy(), 16, -22));
//
//				weapons.add(
//					new NHWeapon(){{
//						predictTarget = false;
//						mirror = false;
//						rotate = false;
//						continuous = true;
//						alternate = false;
//						shake = 5f;
//						shootY = 47f;
//						reload = 220f;
//						shots = 1;
//						x = y = 0f;
//						ejectEffect = Fx.none;
//						recoil = 4.4f;
//						bullet = NHBullets.hurricaneLaser;
//						shootSound = Sounds.beam;
//						shootStatus = StatusEffects.slow;
//						shootStatusDuration = bullet.lifetime + 40f;
//					}},
//					new NHWeapon("swepter"){{
//						mirror = false;
//						top = true;
//						rotate = true;
//						alternate = false;
//						shake = 5f;
//						shootY = 17f;
//						reload = 300f;
//						shots = 1;
//						y = -40f;
//						x = 0f;
//						inaccuracy = 3.0F;
//						ejectEffect = Fx.none;
//						recoil = 4.4f;
//						bullet = NHBullets.hurricaneType;
//						shootSound = Sounds.laserblast;
//					}},
//					new NHWeapon("impulse"){{
//						heatColor = NHColor.lightSkyBack;
//						top = true;
//						rotate = true;
//						shootY = 12f;
//						reload = 50f;
//						x = 40f;
//						y = -30f;
//						shots = 4;
//						shotDelay = 10f;
//						inaccuracy = 6.0F;
//						velocityRnd = 0.38f;
//						alternate = false;
//						ejectEffect = Fx.none;
//						recoil = 1.7f;
//						shootSound = Sounds.plasmaboom;
//						bullet = new BasicBulletType(7.4f, 250, NHBullets.STRIKE){
//							@Override
//							public float range(){return 540f;}
//
//							{
//								drawSize = 200;
//								trailLength = 20;
//								trailWidth = 3f;
//								hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
//								lifetime = 140f;
//								pierce = pierceBuilding = true;
//								width = 16f;
//								height = 50f;
//								backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
//								frontColor = Color.white;
//								lightning = 3;
//								lightningDamage = damage / 2;
//								lightningLength = lightningLengthRand = 5;
//								smokeEffect = Fx.shootBigSmoke2;
//								hitShake = 4f;
//								hitSound = Sounds.plasmaboom;
//								shrinkX = shrinkY = 0f;
//							}
//						};
//					}}
//				);
//
//				abilities.add(new ForceFieldAbility(120.0F, 60F, 30000.0F, 1200.0F), new RepairFieldAbility(800f, 160f, 240f){{
//					healEffect = NHFx.healEffectSky;
//					activeEffect = NHFx.activeEffectSky;
//				}});
//
//				commandLimit = 6;
//				lowAltitude = true;
//				itemCapacity = 500;
//				health = 72000.0F;
//				speed = 1F;
//				accel = 0.04F;
//				drag = 0.025F;
//				flying = true;
//				hitSize = 100.0F;
//				armor = 60.0F;
//				engineOffset = 55.0F;
//				engineSize = 20.0F;
//				rotateSpeed = 1.15F;
//				buildSpeed = 2.8f;
//			}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//		};
//
//		//Air 2:
//
//
//
//

//		collapser = new UnitType("collapser"){{
//			outlineColor = OColor;
//			rotateShooting = false;
//			commandRadius = 240f;
//			abilities.add(new ForceFieldAbility(180f, 60, 80000, 900));
//			constructor = EntityMapping.map(3);
//			rotateShooting = false;
//
//			fallSpeed = 0.008f;
//			drawShields = false;
//
//			immunities = ObjectSet.with(NHStatusEffects.weak, NHStatusEffects.emp2, NHStatusEffects.emp3, NHStatusEffects.emp1, NHStatusEffects.scrambler, NHStatusEffects.scannerDown, NHStatusEffects.ultFireBurn, StatusEffects.melting, StatusEffects.burning, StatusEffects.shocked, StatusEffects.electrified);
//
//			deathExplosionEffect = new OptionalMultiEffect(NHFx.blast(NHColor.thurmixRed, 400f), new Effect(300F, 1600f, e -> {
//				Rand rand = NHFunc.rand;
//				float rad = 150f;
//				rand.setSeed(e.id);
//
//				Draw.color(Color.white, NHColor.thurmixRed, e.fin() + 0.6f);
//				float circleRad = e.fin(Interp.circleOut) * rad * 4f;
//				Lines.stroke(12 * e.fout());
//				Lines.circle(e.x, e.y, circleRad);
//				for(int i = 0; i < 16; i++){
//					Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
//					DrawFunc.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
//				}
//
//				e.scaled(120f, i -> {
//					Draw.color(Color.white, NHColor.thurmixRed, i.fin() + 0.4f);
//					Fill.circle(i.x, i.y, rad * i.fout());
//					Lines.stroke(18 * i.fout());
//					Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2f);
//					Angles.randLenVectors(i.id, 40, rad / 3, rad * i.fin(Interp.pow2Out), (x, y) -> {
//						lineAngle(i.x + x, i.y + y, Mathf.angle(x, y), i.fslope() * 25 + 10);
//					});
//
//					if(NHSetting.enableDetails())Angles.randLenVectors(i.id, (int)(rad / 4), rad / 6, rad * (1 + i.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
//						float angle = Mathf.angle(x, y);
//						float width = i.foutpowdown() * rand.random(rad / 6, rad / 3);
//						float length = rand.random(rad / 2, rad * 5) * i.fout(Interp.circleOut);
//
//						Draw.color(NHColor.thurmixRed);
//						DrawFunc.tri(i.x + x, i.y + y, width, rad / 3 * i.fout(Interp.circleOut), angle - 180);
//						DrawFunc.tri(i.x + x, i.y + y, width, length, angle);
//
//						Draw.color(Color.black);
//
//						width *= i.fout();
//
//						DrawFunc.tri(i.x + x, i.y + y, width / 2, rad / 3 * i.fout(Interp.circleOut) * 0.9f * i.fout(), angle - 180);
//						DrawFunc.tri(i.x + x, i.y + y, width / 2, length / 1.5f * i.fout(), angle);
//					});
//
//					Draw.color(Color.black);
//					Fill.circle(i.x, i.y, rad * i.fout() * 0.75f);
//				});
//
//				Drawf.light(e.x, e.y, rad * e.fslope() * 4f, NHColor.thurmixRed, 0.7f);
//			}).layer(Layer.effect + 0.001f));
//			fallEffect = NHFx.blast(NHColor.thurmixRed, 120f);
//
//			targetAir = targetGround = true;
//			weapons.addAll(
//					collapserCannon.copy().setPos(60, -50),
//					collapserCannon.copy().setPos(40, -20),
//					collapserCannon.copy().setPos(32, 60),
//					new NHWeapon("collapser-laser"){{
//						y = -42;
//						x = 0;
//						shootY = 25f;
//						shots = 3;
//						shotDelay = 15;
//						spacing = 3f;
//						inaccuracy = 3f;
//						reload = 150f;
//						rotateSpeed = 1.5f;
//						rotate = true;
//						top = true;
//						shootSound = NHSounds.flak;
//						mirror = alternate = false;
//						bullet = NHBullets.collapserBullet;
//					}},
//					new NHWeapon(){
//						float rangeWeapon = 520f;
//
//						@Override
//						public void draw(Unit unit, WeaponMount mount){
//							float z = Draw.z();
//
//							Tmp.v1.trns(unit.rotation, y);
//							float f = 1 - mount.reload / reload;
//							float rad = 12f;
//
//							float f1 = Mathf.curve(f,  0.4f, 1f);
//							Draw.z(Layer.bullet);
//							Draw.color(heatColor);
//							for(int i : Mathf.signs){
//								for(int j : Mathf.signs){
//									DrawFunc.tri(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f1 * rad / 3f + Mathf.num(j > 0) * 2f * (f1 + 1) / 2, (rad * 3f + Mathf.num(j > 0) * 20f) * f1, j * Time.time + 90 * i);
//								}
//							}
//
//							TextureRegion arrowRegion = NHContent.arrowRegion;
//
//							Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
//							Tmp.v2.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), rangeWeapon)).add(unit);
//
//							for (int l = 0; l < 4; l++) {
//								float angle = 45 + 90 * l;
//								for (int i = 0; i < 4; i++) {
//									Tmp.v3.trns(angle, (i - 4) * tilesize + tilesize).add(Tmp.v2);
//									float fS = (100 - (Time.time + 25 * i) % 100) / 100 * f1 / 4;
//									Draw.rect(arrowRegion, Tmp.v3.x, Tmp.v3.y, arrowRegion.width * fS, arrowRegion.height * fS, angle + 90);
//								}
//							}
//
//							Lines.stroke((1.5f + Mathf.absin( Time.time + 4, 8f, 1.5f)) * f1, heatColor);
//							Lines.square(Tmp.v2.x, Tmp.v2.y, 4 + Mathf.absin(8f, 4f), 45);
//
//							Lines.stroke(rad / 2.5f * mount.heat, heatColor);
//							Lines.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, rad * 2 * (1 - mount.heat));
//
//							Draw.color(heatColor);
//							Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad);
//							Lines.stroke(f * 1.5f);
//							DrawFunc.circlePercentFlip(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad + 5, Time.time, 20f);
//							Draw.color(Color.white);
//							Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad * 0.7f);
//
//							Draw.z(z);
//						}
//
//						@Override
//						protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float aimX, float aimY, float mountX, float mountY, float rotation, int side){
//							shootSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
//
//							BulletType ammo = bullet;
//							float lifeScl = ammo.scaleVelocity ? Mathf.clamp(Mathf.dst(shootX, shootY, aimX, aimY) / ammo.range()) : 1f;
//
//							Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
//							Tmp.v1.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), rangeWeapon)).add(unit);
//
//							Bullet b = bullet.create(unit, unit.team, Tmp.v1.x, Tmp.v1.y, 0);
//							b.vel.setZero();
//							b.set(Tmp.v1);
//							unit.apply(shootStatus, shootStatusDuration);
//
//							if(headless)return;
//							Vec2 vec2 = new Vec2().trns(unit.rotation, y).add(unit);
//							PosLightning.createEffect(vec2, b, NHColor.thurmixRed, 3, 2.5f);
//							for(int i = 0; i < 5; i++){
//								Time.run(i * 6f, () -> {
//									NHFx.chainLightningFade.at(vec2.x, vec2.y, Mathf.random(8, 14), NHColor.thurmixRed, b);
//								});
//							}
//
//							ejectEffect.at(mountX, mountY, rotation * side);
//							ammo.shootEffect.at(shootX, shootY, rotation);
//							ammo.smokeEffect.at(shootX, shootY, rotation);
//
//						}
//
//						{
//							y = 50;
//							x = 0;
//							shootY = 25f;
//							shots = 1;
//							reload = 1200f;
//							rotateSpeed = 100f;
//							rotate = true;
//							top = false;
//							mirror = alternate = predictTarget = false;
//							heatColor = NHColor.thurmixRed;
//							shootSound = NHSounds.hugeShoot;
//							bullet = new EffectBulletType(480f){
//								@Override
//								public float range(){
//									return rangeWeapon;
//								}
//
//								@Override
//								public void despawned(Bullet b){
//									super.despawned(b);
//
//									Vec2 vec = new Vec2().set(b);
//
//									float damageMulti = b.damageMultiplier();
//									Team team = b.team;
//									for(int i = 0; i < splashDamageRadius / (tilesize * 3.5f); i++){
//										int finalI = i;
//										Time.run(i * despawnEffect.lifetime / (splashDamageRadius / (tilesize * 2)), () -> {
//											Damage.damage(team, vec.x, vec.y, tilesize * (finalI + 6), splashDamage * damageMulti, true);
//										});
//									}
//
//									Units.nearby(team, vec.x, vec.y, splashDamageRadius * 2, u -> {
//										u.heal((1 - u.healthf()) / 3 * u.maxHealth());
//										u.apply(StatusEffects.overclock, 360f);
//									});
//
//									Units.nearbyEnemies(team, vec.x, vec.y, splashDamageRadius * 2, u -> {
//										if(u.isLocal())ScreenInterferencer.generate(360);
//									});
//
//									if(!NHSetting.enableDetails())return;
//									float rad = 120;
//									float spacing = 2.5f;
//
//									for(int k = 0; k < (despawnEffect.lifetime - NHFx.chainLightningFadeReversed.lifetime) / spacing; k++){
//										Time.run(k * spacing, () -> {
//											for(int j : Mathf.signs){
//												Vec2 v = Tmp.v6.rnd(rad * 2 + Mathf.random(rad * 4)).add(vec);
//												(j > 0 ? NHFx.chainLightningFade : NHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, vec);
//											}
//										});
//									}
//								}
//
//								@Override
//								public void update(Bullet b){
//									float rad = 120;
//
//									Effect.shake(8 * b.fin(), 6, b);
//
//									if(b.timer(1, 12)){
//										Seq<Teamc> entites = new Seq<>();
//
//										Units.nearbyEnemies(b.team, b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, entites::add);
//
//										Units.nearbyBuildings(b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, e -> {
//											if(e.team != b.team)entites.add(e);
//										});
//
//										entites.shuffle();
//										entites.truncate(15);
//
//										for(Teamc e : entites){
//											PosLightning.create(b, b.team, b, e, lightningColor, false, lightningDamage, 5 + Mathf.random(5), PosLightning.WIDTH, 1, p -> {
//												NHFx.lightningHitSmall.at(p.getX(), p.getY(), 0, lightningColor);
//											});
//										}
//									}
//
//									if(NHSetting.enableDetails() && b.lifetime() - b.time() > NHFx.chainLightningFadeReversed.lifetime)for(int i = 0; i < 2; i++){
//										if(Mathf.chanceDelta(0.2 * Mathf.curve(b.fin(), 0, 0.8f))){
//											for(int j : Mathf.signs){
//												Sounds.spark.at(b.x, b.y, 1f, 0.3f);
//												Vec2 v = Tmp.v6.rnd(rad / 2 + Mathf.random(rad * 2) * (1 + Mathf.curve(b.fin(), 0, 0.9f)) / 1.5f).add(b);
//												(j > 0 ? NHFx.chainLightningFade : NHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, b);
//											}
//										}
//									}
//
//									if(b.fin() > 0.05f && Mathf.chanceDelta(b.fin() * 0.3f + 0.02f)){
//										NHSounds.blaster.at(b.x, b.y, 1f, 0.3f);
//										Tmp.v1.rnd(rad / 4 * b.fin());
//										NHFx.shuttleLerp.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle(), hitColor, Mathf.random(rad, rad * 3f) * (Mathf.curve(b.fin(Interp.pow2In), 0, 0.7f) + 2) / 3);
//									}
//								}
//
//								@Override
//								public void draw(Bullet b){
//									float fin = Mathf.curve(b.fin(), 0, 0.02f);
//									float f = fin * Mathf.curve(b.fout(), 0f, 0.1f);
//									float rad = 120;
//
//									float circleF = (b.fout(Interp.pow2In) + 1) / 2;
//
//									Draw.color(hitColor);
//									Lines.stroke(rad / 20 * b.fin());
//									Lines.circle(b.x, b.y, rad * b.fout(Interp.pow3In));
//									Lines.circle(b.x, b.y, b.fin(Interp.circleOut) * rad * 3f * Mathf.curve(b.fout(), 0, 0.05f));
//
//									Rand rand = NHFunc.rand;
//									rand.setSeed(b.id);
//									for(int i = 0; i < (int)(rad / 3); i++){
//										Tmp.v1.trns(rand.random(360f) + rand.range(1f) * rad / 5 * b.fin(Interp.pow2Out), rad / 2.05f * circleF + rand.random(rad * (1 + b.fin(Interp.circleOut)) / 1.8f));
//										float angle = Tmp.v1.angle();
//										DrawFunc.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 28 + rand.random(0, 8), rad / 16 * (b.fin(Interp.exp5In) + 0.25f), angle);
//										DrawFunc.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 12 + rand.random(0, 2), rad / 12 * (b.fin(Interp.exp5In) + 0.5f) / 1.2f, angle - 180);
//									}
//
//									Angles.randLenVectors(b.id + 1, (int)(rad / 3), rad / 4 * circleF, rad * (1 + b.fin(Interp.pow3Out)) / 3, (x, y) -> {
//										float angle = Mathf.angle(x, y);
//										DrawFunc.tri(b.x + x, b.y + y, rad / 8 * (1 + b.fout()) / 2.2f, (b.fout() * 3 + 1) / 3 * 25 + rand.random(4, 12) * (b.fout(Interp.circleOut) + 1) / 2, angle);
//										DrawFunc.tri(b.x + x, b.y + y, rad / 8 * (1 + b.fout()) / 2.2f, (b.fout() * 3 + 1) / 3 * 9 + rand.random(0, 2) * (b.fin() + 1) / 2, angle - 180);
//									});
//
//									Drawf.light(b.x, b.y, rad * f * (b.fin() + 1) * 2, Draw.getColor(), 0.7f);
//
//									Draw.z(Layer.effect + 0.001f);
//									Draw.color(hitColor);
//									Fill.circle(b.x, b.y, rad * fin * circleF / 2f);
//									Draw.color(NHColor.thurmixRedDark);
//									Fill.circle(b.x, b.y, rad * fin * circleF * 0.75f / 2f);
//
//								}
//
//								{
//									hittable = false;
//									collides = false;
//									collidesTiles = collidesAir = collidesGround = true;
//									speed = 100;
//
//									despawnHit = true;
//									keepVelocity = false;
//
//									splashDamageRadius = 800f;
//									splashDamage = 800f;
//
//									lightningDamage = 200f;
//									lightning = 22;
//									lightningLength = 60;
//									lightningLengthRand = 60;
//
//									rangeWeapon = 400f;
//									hitShake = despawnShake = 40f;
//									drawSize = clipSize = 800f;
//									hitColor = lightColor = trailColor = lightningColor = NHColor.thurmixRed;
//
//									fragBullets = 6;
//									fragBullet = NHBullets.collapserBullet;
//									hitSound = NHSounds.hugeBlast;
//									hitSoundVolume = 4f;
//
//									fragLifeMax = 1.1f;
//									fragLifeMin = 0.7f;
//									fragVelocityMax = 0.6f;
//									fragVelocityMin = 0.2f;
//
//									status = NHStatusEffects.weak;
//									statusDuration = 300f;
//
//									shootEffect = NHFx.lightningHitLarge(hitColor);
//
//									hitEffect = NHFx.hitSpark(hitColor, 240f, 220, 900, 8, 27);
//									despawnEffect = NHFx.collapserBulletExplode;
//								}
//							};
//						}
//					}
//			);
//
//			hitSize = 120f;
//
//			commandLimit = 3;
//			speed = 0.5f;
//			health = 180000;
//			rotateSpeed = 0.65f;
//			engineSize = 30f;
//			buildSpeed = 10f;
//			engineOffset = 62f;
//			itemCapacity = 300;
//			armor = 180;
//			lowAltitude = true;
//			flying = true;
//		}
//			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
//
//			@Override
//			public void drawCell(Unit unit){
//				super.drawCell(unit);
//
//				Draw.z(Layer.effect + 0.001f);
//
//				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f));
//				Lines.stroke(3f + Mathf.absin(10f, 0.55f));
//				DrawFunc.circlePercent(unit.x, unit.y, unit.hitSize, unit.healthf(), 0);
//
//				for(int i = 0; i < 4; i++){
//					float rotation = Time.time * 1.5f + i * 90;
//					Tmp.v1.trns(rotation, hitSize * 1.1f).add(unit);
//					Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, rotation + 90);
//				}
//
//				Draw.z(Layer.flyingUnitLow);
//				Draw.reset();
//			}
//		};
//	}
//}
