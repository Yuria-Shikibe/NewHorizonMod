package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.ObjectSet;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.abilities.EnergyFieldAbility;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.StatusFieldAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.ShapePart;
import mindustry.entities.pattern.ShootHelix;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSine;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.ItemAmmoType;
import mindustry.type.ammo.PowerAmmoType;
import mindustry.type.weapons.PointDefenseWeapon;
import mindustry.type.weapons.RepairBeamWeapon;
import mindustry.world.meta.BlockFlag;
import newhorizon.NHSetting;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.PosLightningType;
import newhorizon.expand.bullets.ShieldBreakerType;
import newhorizon.expand.bullets.TrailFadeBulletType;
import newhorizon.expand.entities.UltFire;
import newhorizon.expand.units.EnergyUnit;
import newhorizon.expand.units.HealFieldAbility;
import newhorizon.expand.units.SniperAI;
import newhorizon.util.func.NHInterp;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.Vars.tilePayload;

public class NHUnitTypes{
	private static final Color OColor = Color.valueOf("565666");
	
	public static final byte OTHERS = Byte.MIN_VALUE, GROUND_LINE_1 = 0, AIR_LINE_1 = 1, AIR_LINE_2 = 2, ENERGY_LINE_1 = 3, NAVY_LINE_1 = 6;
	
	public static Weapon
		basicCannon, laserCannon;
	
	public static Weapon
			posLiTurret, closeAATurret, collapserCannon, collapserLaser, multipleLauncher, smallCannon,
			mainCannon, pointDefenceWeaponC
			
			;
	
	public static UnitType
			sin,
			guardian,
			declining,
			longinus, anvil,
			saviour;
	
	static{
			EntityMapping.nameMap.put(NewHorizon.name("declining"), EntityMapping.idMap[20]);
			EntityMapping.nameMap.put(NewHorizon.name("zarkov"), EntityMapping.idMap[20]);
			EntityMapping.nameMap.put(NewHorizon.name("ghost"), EntityMapping.idMap[20]);
			EntityMapping.nameMap.put(NewHorizon.name("relay"), EntityMapping.idMap[20]);

			EntityMapping.nameMap.put(NewHorizon.name("saviour"), EntityMapping.idMap[5]);

			EntityMapping.nameMap.put(NewHorizon.name("origin"), EntityMapping.idMap[4]);
			EntityMapping.nameMap.put(NewHorizon.name("thynomo"), EntityMapping.idMap[4]);
			EntityMapping.nameMap.put(NewHorizon.name("aliotiat"), EntityMapping.idMap[4]);
			EntityMapping.nameMap.put(NewHorizon.name("tarlidor"), EntityMapping.idMap[4]);
			EntityMapping.nameMap.put(NewHorizon.name("annihilation"), EntityMapping.idMap[4]);
			EntityMapping.nameMap.put(NewHorizon.name("sin"), EntityMapping.idMap[4]);

			EntityMapping.nameMap.put(NewHorizon.name("guardian"), EnergyUnit::new);
		}
	
	public static Weapon copyAnd(Weapon weapon, Cons<Weapon> modifier){
		Weapon n = weapon.copy();
		modifier.get(n);
		return n;
	}
	
	public static Weapon copyAndMove(Weapon weapon, float x, float y){
		Weapon n = weapon.copy();
		n.x = x;
		n.y = y;
		return n;
	}
	
	private static void loadPreviousWeapon(){
		laserCannon = new Weapon(NewHorizon.name("laser-cannon")){{
			mirror = top = alternate = autoTarget = rotate = true;
			predictTarget = controllable = false;
			x = 22;
			y = -50;
			reload = 12f;
			recoil = 3f;
			inaccuracy = 0;
			shoot = new ShootPattern();
			rotateSpeed = 25f;
			shootSound = NHSounds.gauss;
			bullet = new ShrapnelBulletType(){{
				lifetime = 45f;
				length = 200f;
				damage = 180.0F;
				status = StatusEffects.shocked;
				statusDuration = 60f;
				fromColor = NHColor.lightSkyFront;
				toColor = NHColor.lightSkyBack;
				serrationSpaceOffset = 40f;
				width = 6f;
				shootEffect = NHFx.lightningHitSmall(NHColor.lightSkyBack);
				smokeEffect = new MultiEffect(NHFx.lightSkyCircleSplash, new Effect(lifetime + 10f, b -> {
					Draw.color(fromColor, toColor, b.fin());
					Fill.circle(b.x, b.y, (width / 1.75f) * b.fout());
				}));
			}};
		}};
		
		pointDefenceWeaponC = new PointDefenseWeapon(NewHorizon.name("cannon")){{
			color = NHColor.lightSkyFront;
			mirror = top = alternate = true;
			reload = 6.0F;
			targetInterval = 6.0F;
			targetSwitchInterval = 6.0F;
			bullet = new BulletType() {
				{
					shootEffect = NHFx.shootLineSmall(color);
					hitEffect = NHFx.lightningHitSmall;
					hitColor = color;
					maxRange = 240.0F;
					damage = 150f;
				}
			};
		}};
		
		mainCannon = new Weapon(NewHorizon.name("main-cannon")){{
			top = rotate = true;
			mirror = false;
			alternate = false;
			cooldownTime = 240f;
			recoil = 7f;
			
			inaccuracy = 4f;
			velocityRnd = 0.075f;
			
			shoot = new ShootPattern(){{
				shots = 2;
				firstShotDelay = 280f;
				shotDelay = 20f;
			}};
			
			rotateSpeed = 1f;
			shootSound = NHSounds.flak;
			shootCone = 5f;
			shootY = 15f;
			reload = 300f;
			shake = 7f;
			ejectEffect = Fx.blastsmoke;
			bullet = new BasicBulletType(9.25f, 380f){{
				lifetime = 122f;
				
				trailInterp = NHInterp.artilleryPlus;
				shrinkInterp = NHInterp.artilleryPlus;
				
				shrinkX = 0.75f;
				shrinkY = 0.4f;
				width = 25f;
				height = 55f;
				
				trailWidth = 4.7f;
				trailLength = 140;
				
				//				velocityBegin = 12f;
				//				velocityIncrease = 22f;
				//				accelInterp = Interp.pow3Out;
				//				accelerateBegin = 0f;
				//				accelerateEnd = 0.8f;
				
				maxRange = 740;
				pierce = pierceBuilding = false;
				collideTerrain = collideFloor = collidesGround = collidesTiles = false;
				scaleLife = true;
				
				lightning = 3;
				lightningLength = 4;
				lightningLengthRand = 32;
				
				splashDamageRadius = 76f;
				splashDamage = damage;
				lightningDamage = damage * 0.75f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				
				knockback = 20f;
				
				frontColor = NHColor.lightSkyFront;
				shootEffect = despawnEffect = NHFx.square(backColor, 40f, 4, 40f, 6f);
				smokeEffect = NHFx.hugeSmoke;
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
		}
			@Override
			public void draw(Unit unit, WeaponMount mount){
				super.draw(unit, mount);
				
				if(!unit.isLocal())return;
				
				float
						z = Draw.z(),
						rotation = unit.rotation - 90f,
						weaponRotation  = rotation + (rotate ? mount.rotation : 0),
						fin = Mathf.clamp(1 - (mount.reload - 10f) / reload),
						wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, recoil),
						wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, recoil);
				
				if(fin == 1f)return;
				
				TextureRegion arrowRegion = NHContent.arrowRegion;
				
				Draw.z(Layer.bullet);
				Draw.color(bullet.hitColor);
				
				float railF = Mathf.curve(Interp.pow2Out.apply(fin), 0f, 0.25f) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) * fin;
				
				float length = Math.min(bullet.range, Mathf.dst(wx, wy, unit.aimX, unit.aimY));
				float spacing = 25f;
				for(int i = 0; i <= length / spacing; i++){
					Tmp.v1.trns(weaponRotation + 90f, i * spacing * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) + shootY);
					float f = Interp.pow3Out.apply(Mathf.clamp((fin * length - i * spacing) / spacing)) * (0.6f + railF * 0.4f) * 0.8f;
					Draw.rect(arrowRegion, wx + Tmp.v1.x, wy + Tmp.v1.y, arrowRegion.width * Draw.scl * f, arrowRegion.height * Draw.scl * f, weaponRotation);
				}
				
				Tmp.v1.trns(weaponRotation + 90f, 0f, (2 - railF) * 5f);
				Tmp.v2.trns(weaponRotation + 90f, shootY);
				Lines.stroke(railF * 2f);
				for(int i : Mathf.signs){
					Lines.lineAngle(wx + Tmp.v1.x * i + Tmp.v2.x, wy + Tmp.v1.y * i + Tmp.v2.y, weaponRotation + 90f, length * (0.75f + railF / 4f) * Mathf.curve(Interp.pow5Out.apply(1 - fin) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f), 0f, 0.1f));
				}
				
				Draw.reset();
				Draw.z(z);
			}
		};
		
		multipleLauncher = new Weapon(NewHorizon.name("mult-launcher")){{
			reload = 60f;
			
			shoot = new ShootPattern(){{
				shots = 3;
				shotDelay = 8f;
			}};
			
			shake = 3f;
			
			shootX = 2;
			xRand = 5;
			
			mirror = true;
			rotateSpeed = 2.5f;
			alternate = true;
			shootSound = NHSounds.launch;
			shootCone = 30f;
			shootY = 5f;
			top = true;
			rotate = true;
			bullet = new BasicBulletType(5.25f, 100f, NHBullets.STRIKE){{
				lifetime = 50;
				
				knockback = 12f;
				width = 11f;
				height = 28f;
				
				trailWidth = 2.2f;
				trailLength = 20;
				drawSize = 300f;
				
				
				homingDelay = 5f;
				homingPower = 0.0075f;
				homingRange = 140f;
				
				splashDamageRadius = 16f;
				splashDamage = damage * 0.75f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				
				hitEffect = NHFx.circleSplash(backColor, 40f, 4, 40f, 6f);
				despawnEffect = NHFx.hitSparkLarge;
				shootEffect = NHFx.shootCircleSmall(backColor);
				smokeEffect = Fx.shootBigSmoke2;
				
				trailChance = 0.6f;
				trailEffect = NHFx.trailToGray;
				
				hitShake = 3f;
				hitSound = Sounds.plasmaboom;
			}};
		}};
		
		posLiTurret = new Weapon(NewHorizon.name("pos-li-blaster")){{
			shake = 1f;
			shoot = new ShootPattern();
			predictTarget = rotate = false;
			top = alternate = true;
			reload = 45f;
			shootY = 4f;
			shootSound = Sounds.spark;
			heatColor = NHColor.lightSkyBack;
			bullet = new PosLightningType(20f){{
				lightningColor = NHColor.lightSkyBack;
				maxRange = 140f;
				hitEffect = NHFx.lightningHitSmall(lightningColor);
				lightningLength = 1;
				lightningLengthRand = 4;
			}};
		}};
		
		closeAATurret = new Weapon(NewHorizon.name("anti-air-pulse-laser")){{
			shake = 0f;
			shoot = new ShootPattern();
			rotate = top = true;
			heatColor = NHColor.lightSkyBack;
			shootSound = Sounds.missile;
			shootY = 3f;
			recoil = 2f;
			x = 9.5f;
			y = -7f;
			reload = 10f;
			autoTarget = true;
			controllable = predictTarget = false;
			bullet = NHBullets.basicSkyFrag;
		}};
		
		smallCannon = new Weapon(NewHorizon.name("cannon")){{
			top = mirror = rotate = true;
			reload = 45f;
			
			shoot = new ShootPattern(){{
				shots = 3;
				shotDelay = 8f;
			}};
			controllable = false;
			autoTarget = true;
			shake = 3f;
			inaccuracy = 4f;
			rotateSpeed = 2.5f;
			alternate = true;
			shootSound = NHSounds.scatter;
			shootCone = 15;
			shootY = 5f;
			bullet = new BasicBulletType(5f, 20f){{
				hitColor = trailColor = lightningColor = backColor = lightColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				
				width = 8f;
				height = 20f;
				lifetime = 55f;
				
				hitEffect = NHFx.lightningHitSmall(backColor);
				shootEffect = NHFx.shootLineSmall(backColor);
				smokeEffect = Fx.shootSmallSmoke;
				despawnEffect = NHFx.square(backColor, 15f, 2, 14f, 3);
			}
			};
		}};
	}
	
	
	private static void loadWeapon(){
		basicCannon = new Weapon(NewHorizon.name("basic-weapon")){{
			shoot = new ShootPattern();
			
			shootSound = NHSounds.rapidLaser;
			
			rotateSpeed = 12f;
			reload = 20f;
			shootY = 6f;
			shootX = -1.6f;
			rotate = true;
			mirror = true;
			top = true;
			
			bullet = new BasicBulletType(){{
				width = 7f;
				height = 25f;
				trailWidth = 1f;
				trailLength = 7;
				
				speed = 12f;
				lifetime = 24f;
				drag = 0.015f;
				
				trailColor = hitColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
				frontColor = NHColor.lightSkyFront;
				
				damage = 15f;
				
				smokeEffect = Fx.shootSmallSmoke;
				shootEffect = NHFx.shootCircleSmall(backColor);
				despawnEffect = NHFx.circleSplash(backColor, 40f, 3, 18f, 4f);
				hitEffect = NHFx.lightSkyCircleSplash;
			}};
		}};
		
		laserCannon = new Weapon(NewHorizon.name("laser-cannon")){{
			top = autoTarget = rotate = true;
			mirror = alternate = false;
//			predictTarget = controllable = false;
			x = 22;
			y = -50;
			reload = 70f;
			recoil = 1.75f;
			inaccuracy = 2;
			
			shootY = 6;
			
//			layerOffset = 0.0001f;
			
			shoot = new ShootPattern(){{
				shots = 3;
				shotDelay = 12f;
			}};
			
//			parts.add(new RegionPart("-shooter"){{
//				under = true;
//				progress = PartProgress.recoil;
//				x = 0;
//				y = 0;
//				moveY = -3f;
//			}});
			
			shake = 1f;
			rotateSpeed = 18f;
			shootSound = NHSounds.synchro;
			bullet = new BasicBulletType(3.8f, 50){
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
					UltFire.createChance(b, 12, 0.05f);
				}
			};
		}};
	}
	
	public static void load(){
		loadWeapon();
		
		loadPreviousWeapon();
		
		saviour = new UnitType("saviour"){{
			outlineColor = OColor;
			aiController = SniperAI::new;
			hitSize = 55f;
			armor = 36.0F;
			health = 34000.0F;
			speed = 0.9F;
			rotateSpeed = 0.75f;
			accel = 0.04F;
			drag = 0.035f;
			flying = true;
			engineOffset = 22.0F;
			engineSize = 12;
			buildSpeed = 8.0F;
			drawShields = false;
			lowAltitude = true;
			buildBeamOffset = 43.0F;
			payloadCapacity = (5 * 5) * tilePayload;

			targetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.generator, BlockFlag.battery, null};

			ammoType = new PowerAmmoType();
			
			for(int i : Mathf.signs){
				engines.add(new UnitEngine(i * 30.25f, -28.75f, 5, -90 + i * 45));
				engines.add(new UnitEngine(i * 48f, -13f, 4, -90 + i * 45));
			}
			
			immunities = ObjectSet.with(NHStatusEffects.ultFireBurn, NHStatusEffects.weak, NHStatusEffects.scannerDown, NHStatusEffects.emp1, NHStatusEffects.emp2, NHStatusEffects.emp3, StatusEffects.burning, StatusEffects.melting, NHStatusEffects.scrambler);

			weapons.add(
					new Weapon(){{
						x = shootX = shootY = 0;
						y = 22;

						reload = 180f;
						rotate = true;
						rotationLimit = 160f;
						
						mirror = false;
						shootCone = 15f;

						shake = 5;

						shootSound = Sounds.plasmadrop;

						bullet = new ShieldBreakerType(6, 30, "mine-bullet", 3000){{
							rangeOverride = 400;
							scaleLife = true;
							shootEffect = hitEffect = Fx.hitEmpSpark;
							smokeEffect = Fx.healWave;
							despawnEffect = NHFx.circleSplash(Pal.heal, 75f, 8, 68f, 7f);

							backColor = lightColor = trailColor = lightningColor = hitColor = Pal.heal;
							frontColor = Color.white;

							trailEffect = NHFx.trailSolid;
							trailParam = 4f;
							trailChance = 0.5f;

//							accelerateBegin = 0.15f;
//							accelerateEnd = 0.95f;
//							velocityBegin = 8f;
//							velocityIncrease = -8f;

							lifetime = 100f;

							width = height = 25f;
							shrinkX = shrinkY = 0f;
							spin = 4;

							trailWidth = 3.5f;
							trailLength = 18;

							pierceBuilding = true;
							pierceCap = 8;

							status = NHStatusEffects.scannerDown;
							statusDuration = 180f;
						}};
					}},
					new PointDefenseWeapon("point-defense-mount"){{
						mirror = true;
						x = 52;
						y = -5f;
						reload = 6f;
						targetInterval = 6f;
						targetSwitchInterval = 8f;

						bullet = new BulletType(){{
							color = Pal.heal;
							shootEffect = Fx.hitFlamePlasma;
							hitEffect = Fx.hitMeltHeal;
							maxRange = 240f;
							damage = 150f;
						}};
					}},
					new RepairBeamWeapon("repair-beam-weapon-center-large"){{
						x = 17f;
						y = -21;
						shootY = 6f;
						beamWidth = 0.8f;
						repairSpeed = 4f;

						bullet = new BulletType(){{
							maxRange = 160f;
						}};
					}},
					new Weapon(NewHorizon.name("saviour-cannon-smaller")){{
						top = true;
						rotate = true;
						rotationLimit = 60f;
						rotateSpeed = 1.5f;
						alternate = true;
						mirror = true;
						shake = 2f;
						shootY = 20f;
						x = 32f;
						y = -7;
						reload = 75f;
						recoil = 7f;
						shootSound = Sounds.laser;
						cooldownTime = 40f;

						bullet = NHBullets.saviourBullet;
					}}
			);

			abilities.add(new ForceFieldAbility(160f, 60f, 12000f, 900f), new RepairFieldAbility(1500, 180f, 400f), new HealFieldAbility(Pal.heal, 360f, 0, 22f, 400f, 0.15f){{
				effectRadius = 6f;
				sectors = 6;
				sectorRad = 0.065f;
			}});
		}

			@Override
			public void load(){
				super.load();
				shadowRegion = outlineRegion;
			}

			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
		};
		
		longinus = new UnitType("longinus"){{
			outlineColor = OColor;
			constructor = EntityMapping.map(3);
			lowAltitude = true;
			health = 10000.0F;
			speed = 0.45F;
			outlineRadius = 4;
			strafePenalty = 1f;
			accel = 0.02F;
			drag = 0.025F;
			flying = true;
			circleTarget = false;
			rotateMoveFirst = false;
			hitSize = 50.0F;
			armor = 15.0F;
			engineOffset = 46f;
			engineSize = 12.0F;
			rotateSpeed = 0.65f;
			buildSpeed = 3f;
			ammoType = new ItemAmmoType(NHItems.presstanium);
			
//			aiController = SniperAI::new;
			targetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.turret, BlockFlag.generator, null};
			
			for(int i : Mathf.signs){
				engines.add(new UnitEngine(21.5f * i, -43.5f, 5, -90 + 45 * i));
//				engines.add(new UnitEngine(21.5f * i, -12.5f, 3f, 90 - 45 * i));
			}
			
			weapons.add(
				new Weapon(NewHorizon.name("longinus-weapon")){{
						shootY = 42;
						
						healColor = NHColor.thurmixRed;
						
						shoot = new ShootHelix(){{
							shots = 3;
							shotDelay = 18f;
							mag = 2.2f;
							scl = 2.2f;
						}};
						
						parts.add(new RegionPart("-ejector"){{
							progress = PartProgress.warmup.blend(PartProgress.recoil, 0.15f);
							under = turretShading = true;
							mirror = true;
							x = 15;
							y = 26.5f;
							
							moveX = 6f;
							moveY = -6f;
						}});
					
						parts.add(new RegionPart("-ejector"){{
							progress = PartProgress.warmup.blend(PartProgress.recoil, 0.15f);
							under = turretShading = true;
							mirror = true;
							x = 17;
							y = 14.5f;
							
							moveX = 6f;
							moveY = -6f;
						}});
					
						parts.add(new ShapePart(){{
							progress = PartProgress.smoothReload.inv().delay(0.65f).curve(Interp.pow3Out);
							y = shootY - 6f;
							sides = 4;
							color = NHColor.lightSkyBack;
							colorTo = NHColor.lightSkyMiddle;
							rotateSpeed = 2f;
							hollow = true;
							stroke = 0.0F;
							strokeTo = 1.5F;
							radius = 2.0F;
							radiusTo = 8f;
							moveY = 11f;
							layer = Layer.effect;
						}});
					
						parts.add(new ShapePart(){{
							progress = PartProgress.smoothReload.inv().delay(0.65f).curve(Interp.pow3Out);
							y = shootY - 6f;
							sides = 4;
							color = NHColor.lightSkyBack;
							colorTo = NHColor.lightSkyMiddle;
							rotateSpeed = -1f;
							hollow = true;
							stroke = 0.0F;
							strokeTo = 1.25F;
							radius = 4.0F;
							radiusTo = 10f;
							moveY = 11f;
							layer = Layer.effect;
						}});
					
						for(int s : Mathf.signs){
							parts.add(new HaloPart(){{
								tri = true;
								progress = PartProgress.warmup;
								y = 7f;
								x = 3f * s;
								mirror = false;
								moveX = 7f * s;
								moveY = -2f;
								shapeMoveRot = -45f * s;
								shapes = 1;
								color = NHColor.lightSkyBack;
								colorTo = NHColor.lightSkyMiddle;
								shapeRotation = 90 * s;
								radius = -0.1f;
								radiusTo = 3f;
								triLength = -0.1f;
								triLengthTo = 3;
								layer = Layer.effect;
							}});
							
							parts.add(new HaloPart(){{
								tri = true;
								progress = PartProgress.warmup;
								y = 7f;
								x = 3f * s;
								mirror = false;
								moveX = 7f * s;
								moveY = -2f;
								shapeMoveRot = -45f * s;
								shapes = 1;
								color = NHColor.lightSkyBack;
								colorTo = NHColor.lightSkyMiddle;
								shapeRotation = -90 * s;
								radius = -0.1f;
								radiusTo = 3f;
								triLength = -0.1f;
								triLengthTo = 17;
								layer = Layer.effect;
							}});
						}
						
						
						parts.add(new RegionPart("-panel"){{
							progress = PartProgress.warmup;
							outline = false;
							mirror = true;
							x = 9.25f;
							y = 20.5f;
							
							moveX = -1f;
							moveY = -1f;
						}});
						
						parts.add(new RegionPart("-main-charger"){{
							progress = PartProgress.warmup.blend(PartProgress.recoil.inv().curve(Interp.pow3Out), 0.15f);
							moves.add(new PartMove(PartProgress.recoil, 0, 8, 0));
							under = turretShading = true;
							layerOffset = -0.005f;
							heatLayerOffset = -0.005f;
							heatColor = Pal.sap;
							mirror = true;
							x = 16;
							y = -18;

							moveX = 10.75f;
							moveY = -2;
							moveRot = -45f;
						}});
						
						parts.add(new RegionPart("-charger"){{
							under = turretShading = true;
							heatLayerOffset = -0.005f;
							mirror = true;
							x = 10;
							y = 45.5f;
							
							moveX = -6f;
							moveY = 0;
						}});
						
						x = 0;
						y = -9f;
						recoil = 10;
						reload = 300f;
						cooldownTime = 150f;
						rotationLimit = 10f;
						shake = 12f;
						rotateSpeed = 0.55f;
						rotate = false;
						
						shootCone = 3f;
						
						top = false;
						mirror = false;
						shootSound = NHSounds.railGunBlast;
						soundPitchMax = 1.1f;
						soundPitchMin = 0.9f;
						
						layerOffset = -0.0005f;
						
						bullet = new TrailFadeBulletType(25f, 820f){{
							recoil = 0.095f;
							lifetime = 40f;
							trailLength = 200;
							trailWidth = 2F;
							trailNum = 1;
							keepVelocity = false;
							
							spacing = 10f;
							updateSpacing *= 1.25f;
							
							trailColor = hitColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
							frontColor = NHColor.lightSkyFront;
							width = 10f;
							height = 40f;
							
							hitSound = Sounds.plasmaboom;
							despawnShake = hitShake = 18f;
							
							lightning = 5;
							lightningLength = 6;
							lightningLengthRand = 18;
							lightningDamage = 70;
							
							smokeEffect = NHFx.square(hitColor, 80f, 8, 48f, 6f);
							shootEffect = NHFx.instShoot(backColor, frontColor);
							despawnEffect = NHFx.lightningHitLarge;
							hitEffect = new MultiEffect(NHFx.hitSpark(backColor, 75f, 24, 90f, 2f, 12f), NHFx.square45_6_45, NHFx.lineCircleOut(backColor, 18f, 20, 2), NHFx.sharpBlast(backColor, frontColor, 120f, 40f));
							despawnHit = true;
						}
							
							@Override
							public void update(Bullet b){
								super.update(b);
								b.collided.clear();
							}
							
							@Override
							public void hit(Bullet b, float x, float y){
								super.hit(b, x, y);
								
								UltFire.createChance(x, y, 12, 0.15f, b.team);
							}
						};
						
						shootStatus = StatusEffects.slow;
						shootStatusDuration = bullet.lifetime * 1.5f;
					}}
			);
			
			weapons.add(
				copyAnd(basicCannon, weapon -> {
					weapon.x = 19.5f;
					weapon.y = -28;
					weapon.autoTarget = true;
					weapon.controllable = false;
				}), copyAnd(basicCannon, weapon -> {
					weapon.x = 10;
					weapon.y = -46f;
					weapon.autoTarget = true;
					weapon.controllable = false;
				}),
				copyAndMove(laserCannon, 0, -25.5f)
			);
		}
			@Override
			public void drawSoftShadow(Unit unit){
				float z = Draw.z();
				Draw.z(z - 0.01f);
				super.drawSoftShadow(unit);
				Draw.z(z);
			}
			
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
		};
		
		declining = new UnitType("declining"){{
			outlineColor = OColor;
			weapons.add(copyAndMove(mainCannon, 0, -17));
			weapons.add(copyAndMove(mainCannon, 0, 25));
			weapons.add(copyAndMove(mainCannon, 0, -56));
			
			weapons.add(copyAndMove(pointDefenceWeaponC, 30, -30));
			weapons.add(copyAndMove(pointDefenceWeaponC, 36, -35));
			weapons.add(copyAndMove(pointDefenceWeaponC, 24, -35));
			
			weapons.add(laserCannon);
			
			immunities.addAll(StatusEffects.blasted, StatusEffects.tarred, StatusEffects.burning, StatusEffects.freezing, StatusEffects.melting, NHStatusEffects.ultFireBurn, NHStatusEffects.emp1);
			targetFlags = new BlockFlag[]{BlockFlag.unitAssembler, BlockFlag.turret, BlockFlag.reactor, BlockFlag.generator, null};
			
			health = 30000;
			speed = 0.75f;
			drag = 0.18f;
			hitSize = 60f;
			armor = 30;
			accel = 0.1f;
			rotateSpeed = 0.9f;
			buildSpeed = 6f;
			
			waveTrailX = 20;
			waveTrailY = -49;
			
			trailLength = 70;
			trailScl = 4f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
		};
		
		sin = new UnitType("sin"){{
			outlineColor = OColor;
			abilities.add(new ForceFieldAbility(88.0F, 20F, 5000.0F, 900.0F), new StatusFieldAbility(NHStatusEffects.phased, 245f, 240f, 240f){{
				activeEffect = NHFx.lineSquareOut(NHColor.lightSkyBack, 60f, 240f, 4f, 45);
				applyEffect = NHFx.lineSquareOut(NHColor.lightSkyBack, 30f, 45f, 1f, 45);
			}});
			
			drawShields = false;
			engineOffset = 18.0F;
			engineSize = 9F;
			speed = 0.2f;
			hitSize = 52f;
			health = 80000f;
			buildSpeed = 4f;
			armor = 80f;
			
			ammoType = new ItemAmmoType(NHItems.presstanium);
			
			weapons.add(
					new Weapon(NewHorizon.name("sin-cannon")){{
						top = false;
						rotate = true;
						rotationLimit = 13f;
						rotateSpeed = 1f;
						alternate = true;
						shake = 3.5f;
						shootY = 32f;
						x = 42f;
						y = -2f;
						recoil = 3.4f;
						predictTarget = true;
						shootCone = 30f;
						reload = 60f;
						
						parts.add(new RegionPart("-shooter"){{
							under = turretShading = true;
							outline = true;
							mirror = false;
							moveY = -8f;
							progress = PartProgress.recoil;
						}});
						
						shoot = new ShootPattern(){{
							shots = 3;
							shotDelay = 3.5f;
						}};
						
						velocityRnd = 0.075f;
						inaccuracy = 6.0F;
						ejectEffect = Fx.none;
						bullet = new BasicBulletType(8, 200f, NHBullets.STRIKE){{
							trailColor = lightningColor = backColor = lightColor = NHColor.lightSkyBack;
							frontColor = NHColor.lightSkyFront;
							lightning = 2;
							lightningCone = 360;
							lightningLengthRand = lightningLength = 8;
							homingPower = 0;
							scaleLife = true;
							collides = false;
							
							trailLength = 15;
							trailWidth = 3.5f;
							
							splashDamage = lightningDamage = damage;
							splashDamageRadius = 48f;
							lifetime = 95f;
							
							width = 22f;
							height = 35f;
							
							trailEffect = NHFx.trailToGray;
							trailParam = 3f;
							trailChance = 0.35f;
							
							hitShake = 7f;
							hitSound = Sounds.explosion;
							hitEffect = NHFx.hitSpark(backColor, 75f, 24, 95f, 2.8f, 16);
							
							smokeEffect = new OptionalMultiEffect(NHFx.hugeSmoke, NHFx.circleSplash(backColor, 60f, 8, 60f, 6));
							shootEffect = NHFx.hitSpark(backColor, 30f, 15, 35f, 1.7f, 8);
							
							despawnEffect = NHFx.blast(backColor, 60);
							
							fragBullet = NHBullets.basicSkyFrag;
							fragBullets = 5;
							fragLifeMax = 0.6f;
							fragLifeMin = 0.2f;
							fragVelocityMax = 0.35f;
							fragVelocityMin = 0.074f;
						}
							
							@Override
							public void hit(Bullet b, float x, float y){
								super.hit(b, x, y);
								UltFire.createChance(b, splashDamageRadius, 0.4f);
							}
						};
						
						shootSound = Sounds.artillery;
					}},
					new Weapon(){{
						mirror = false;
						rotate = true;
						rotateSpeed = 25f;
						x = 0;
						y = 12f;
						recoil = 2.7f;
						shootY = 7f;
						shootCone = 40f;
						velocityRnd = 0.075f;
						reload = 150f;
						xRand = 18f;
						
						shoot = new ShootSine(){{
							shots = 12;
							shotDelay = 4f;
						}};
						
						
						inaccuracy = 5.0F;
						ejectEffect = Fx.none;
						bullet = NHBullets.annMissile;
						shootSound = NHSounds.launch;
					}}
			);
			
			weapons.add(copyAndMove(multipleLauncher, 26, -12.5f));
			
			weapons.add(copyAnd(laserCannon, w -> {
				w.mirror = false;
				w.x = 0;
				w.y = 14;
			}));
			
			weapons.add(copyAndMove(pointDefenceWeaponC, 22, 18f));
			weapons.add(copyAndMove(pointDefenceWeaponC, 25, 2));
			
			immunities.addAll(NHStatusEffects.scannerDown, NHStatusEffects.weak, NHStatusEffects.emp1, NHStatusEffects.emp2, NHStatusEffects.emp3, NHStatusEffects.scrambler, StatusEffects.disarmed, StatusEffects.melting, StatusEffects.burning, StatusEffects.wet, StatusEffects.shocked, StatusEffects.tarred, StatusEffects.muddy, StatusEffects.slow, StatusEffects.disarmed);
			
			groundLayer = Layer.legUnit + 0.1f;
			
			rotateSpeed = 1f;
			fallSpeed = 0.03f;
			mechStepParticles = true;
			canDrown = false;
			mechFrontSway = 2.2f;
			mechSideSway = 0.8f;
			canBoost = true;
			boostMultiplier = 2.5f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this); NHPixmap.outlineLegs(packer, this);}
		};
		
		anvil = new UnitType("anvil"){{
			outlineColor = OColor;
			constructor = EntityMapping.map(3);

			EnergyFieldAbility ability = new EnergyFieldAbility(150f, 150f, 300f);
			ability.color = NHColor.thurmixRed;
			ability.y = -9f;
			ability.healEffect = new Effect(11, e -> {
				Draw.color(NHColor.thurmixRed);
				Lines.stroke(e.fout() * 2f);
				Lines.circle(e.x, e.y, 2f + e.finpow() * 7f);
			});
			ability.status = NHStatusEffects.emp1;
			ability.sectors = 4;
			ability.sectorRad = 0.16f;
			ability.healPercent = 1f;
			ability.statusDuration = 120f;
			ability.shootSound = NHSounds.synchro;

			abilities.add(ability);

			immunities.addAll(NHStatusEffects.scannerDown, StatusEffects.slow, StatusEffects.electrified, StatusEffects.muddy, StatusEffects.blasted, StatusEffects.shocked, StatusEffects.sapped, NHStatusEffects.emp1, NHStatusEffects.emp2, NHStatusEffects.weak);

			hitSize = 70f;
			armor = 42;
			health = 64000.0F;
			speed = 1F;
			rotateSpeed = 0.75f;
			accel = 0.06F;
			drag = 0.035f;
			flying = true;
			engineOffset = 65.0F;
			engineSize = 24f;
			buildSpeed = 5.0F;
			drawShields = true;
			lowAltitude = true;
			buildBeamOffset = 63.0F;
			payloadCapacity = (3 * 3) * tilePayload;

			Weapon weapon = new Weapon(NewHorizon.name("anvil-cannon")){{
				mirror = true;
				rotate = false;
				alternate = true;
				top = true;

				shootY = 31;
				recoil = 1f;
				recoilTime = 90f;
				reload = 160f;
				shootCone = 15f;
				
				layerOffset = 0.003f;
				
				parts.add(new RegionPart("-shooter"){{
					moveY = -7f;
					progress = PartProgress.recoil;
					under = turretShading = outline = true;
				}});
				
				parts.add(new RegionPart("-panel"){{
					under = outline = true;
					x = 5;
					y = -0.25f;
					moveRot = -45;
					moveX = 4;
					moveY = -1f;
					layerOffset = -0.001f;
					progress = PartProgress.warmup.add(PartProgress.recoil.mul(0.3f));
				}});
				
				bullet = new ShieldBreakerType(9, 200f, 1200){{
					sprite = NHBullets.MISSILE_LARGE;
					trailColor = lightningColor = backColor = lightColor = NHColor.thurmixRed;
					frontColor = NHColor.thurmixRedLight;
					lightning = 2;
					lightningCone = 360;
					lightningLengthRand = lightningLength = 12;
					homingPower = 0;

					lifetime = 45f;
					trailLength = 15;
					trailWidth = 3.5f;

					splashDamage = lightningDamage = damage * 0.7f;
					splashDamageRadius = 40f;

					width = 22f;
					height = 35f;

					hitShake = despawnShake = 3f;
					hitSound = despawnSound = Sounds.explosion;
					hitEffect = new OptionalMultiEffect(NHFx.blast(backColor, 60f), NHFx.hitSpark(backColor, 75f, 8, 80f, 2f, 12f));

					smokeEffect = NHFx.hugeSmoke;
					shootEffect = NHFx.shootLineSmall(backColor);

					despawnEffect = NHFx.lightningHitLarge(backColor);

					status = NHStatusEffects.weak;
					statusDuration = 180f;
				}};

				shoot = new ShootPattern(){{
					shots = 5;
					shotDelay = 14f;
				}};
				
				inaccuracy = 3f;
				velocityRnd = 0.095f;

				x = 30;
				y = -2;

				shootSound = NHSounds.flak;
				shake = 4f;
			}};
			
			Weapon aaTurret = new Weapon(NewHorizon.name("rapid-laser-cannon")){{
				mirror = true;
				rotate = true;
				alternate = true;
				top = true;

				autoTarget = true;
				rotateSpeed = 30;
				shootY = 10;
				recoil = 2f;
				reload = 12f;

				heatColor = Pal.redderDust;
				cooldownTime = 45f;

				bullet = new ShrapnelBulletType(){{
					length = 520;
					damage = 300.0F;
					status = StatusEffects.slow;
					statusDuration = 60f;
					width = 11f;
					fromColor = NHColor.thurmixRedLight;
					hitColor = lightColor = lightningColor = toColor = NHColor.thurmixRed;
					shootEffect = NHFx.lightningHitSmall(toColor);
					smokeEffect = new OptionalMultiEffect(new Effect(lifetime + 2f, b -> {
						Draw.color(fromColor, toColor, b.fin());
						Fill.circle(b.x, b.y, (width / 2f) * b.fout());
						DrawFunc.tri(b.x, b.y, width / 1.75f * b.fout(Interp.circleIn), 30f, b.rotation + 60);
						DrawFunc.tri(b.x, b.y, width / 1.75f * b.fout(Interp.circleIn), 30f, b.rotation - 60);
					}), NHFx.hitSpark(toColor, 35f, 6, 24f, 1.75f, 8f));
				}};

				shoot = new ShootPattern();

				x = 20;
				y = -34;

				shootSound = NHSounds.synchro;
				shake = 1f;
			}};

			PointDefenseWeapon pointDefenseWeapon = new PointDefenseWeapon(NewHorizon.name("anvil-point-cannon")){{
				color = NHColor.thurmixRed;
				x = 14;
				y = -2f;
				reload = 6f;
				targetInterval = 6f;
				targetSwitchInterval = 8f;
				bullet = new BulletType(){{
					shootEffect = NHFx.shootLineSmall(color);
					hitEffect = NHFx.lightningHitSmall(color);
					maxRange = 280f;
					damage = 120f;
				}};
			}};

			weapons.addAll(
					weapon, copyAndMove(weapon, 50, -24), aaTurret, copyAndMove(aaTurret, 12, -12),
					pointDefenseWeapon, copyAndMove(pointDefenseWeapon, 13, 41)
			);

			range = 500f;

			ammoType = new PowerAmmoType();

			targetFlags = new BlockFlag[]{BlockFlag.factory, BlockFlag.turret, BlockFlag.reactor, BlockFlag.generator, BlockFlag.core, null};

		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHPixmap.createIcons(packer, this);}
		};
		
		guardian = new UnitType("guardian"){{
			clipSize = 260f;
			
			engineLayer = Layer.effect;
			engineOffset = 0;
			
			deathExplosionEffect = Fx.none;
			deathSound = Sounds.plasmaboom;
			trailLength = 40;
			trailScl = 3f;
			
			immunities = ObjectSet.with(NHStatusEffects.scannerDown, NHStatusEffects.weak, StatusEffects.wet, StatusEffects.shocked, StatusEffects.tarred, StatusEffects.burning,
					StatusEffects.melting, StatusEffects.blasted, StatusEffects.corroded, StatusEffects.electrified, StatusEffects.freezing, StatusEffects.muddy,
					NHStatusEffects.emp1, NHStatusEffects.emp2, NHStatusEffects.emp3, NHStatusEffects.quantization
			);
			
			hitSize = 45;
			speed = 1.5f;
			accel = 0.07F;
			drag = 0.075F;
			health = 22000;
			itemCapacity = 0;
			rotateSpeed = 6;
			engineSize = 8f;
			flying = true;
			
			trailLength = -1;
			buildSpeed = 10f;
			crashDamageMultiplier = Mathf.clamp(hitSize / 10f, 1, 10);
			payloadCapacity = Float.MAX_VALUE;
			buildBeamOffset = 0;
			
			weapons.add(new Weapon(){{
				reload = 180f;
				x = y =shootX = shootY = 0;
				shootSound = NHSounds.blaster;
				bullet = NHBullets.guardianBullet;
				shoot = new ShootPattern(){
					public void shoot(int totalShots, BulletHandler handler){
						for(int i = 0; i < shots; i++){
							handler.shoot(0, 0, Mathf.random(360), firstShotDelay + shotDelay * i);
						}
					}
					
					{
						shots = 15;
						shotDelay = 3f;
					}
				};
			}});
			
			aiController = SniperAI::new;
			targetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.generator, BlockFlag.turret, null};
		}
			public Effect slopeEffect = NHFx.boolSelector;
			
			public final float outerEyeScl = 0.25f;
			public final float innerEyeScl = 0.18f;
			
			/*
			 * [0] -> Length
			 * [1] -> Arrow Offset
			 * [2] -> Width
			 * [3] -> Rotate Speed
			 * [4] -> Origin Offset
			 * */
			public final float[][] rotator = {
					{75f, 0, 8.5f, 1.35f, 0.1f},
					{55f, 0, 6.5f, -1.7f, 0.1f},
					{25, 0, 13, 0.75f, 0.3f},
					
					{100f, 33.5f, 11, 0.75f, 0.7f},
					{60f, -20, 6f, -0.5f, 1.25f}
			};
			
			
			public final float bodySize = 24f;
			
			@Override
			public void load(){
				super.load();
				shadowRegion = uiIcon = fullIcon = Core.atlas.find(NewHorizon.name("jump-gate-pointer"));
			}
			
			@Override
			public void init(){
				super.init();
				if(trailLength < 0)trailLength = (int)bodySize * 4;
				if(slopeEffect == NHFx.boolSelector)slopeEffect = new Effect(30, b -> {
					if(!(b.data instanceof Integer))return;
					int i = b.data();
					Draw.color(b.color);
					Angles.randLenVectors(b.id, (int)(b.rotation / 8f), b.rotation / 4f + b.rotation * 2f * b.fin(), (x, y) -> Fill.circle(b.x + x, b.y + y, b.fout() * b.rotation / 2.25f));
					Lines.stroke((i < 0 ? b.fin(Interp.pow2InInverse) : b.fout(Interp.pow2Out)) * 2f);
					Lines.circle(b.x, b.y, (i > 0 ? (b.fin(Interp.pow2InInverse) + 0.5f) : b.fout(Interp.pow2Out)) * b.rotation);
				}).layer(Layer.bullet);
				
				engineSize = bodySize / 4;
				engineSize *= -1;
			}
			
			@Override
			public void draw(Unit unit){
				super.draw(unit);
			}
			
			@Override
			public void drawBody(Unit unit){
				Drawf.light(unit.x,unit.y, unit.hitSize * 4f, unit.team.color, 0.68f);
				Draw.z(Layer.effect + 0.001f);
				float sizeF = 1 + Mathf.absin(4f, 0.1f);
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + Mathf.clamp(unit.hitTime) / 5f * 3f);
				Draw.alpha(0.65f);
				Fill.circle(unit.x, unit.y, bodySize * sizeF * 1.1f);
				Draw.alpha(1f);
				Fill.circle(unit.x, unit.y, bodySize * sizeF);
				
				for(float[] j : rotator){
					for(int i : Mathf.signs){
						float ang = Time.time * j[3] + 90 + 90 * i + Mathf.randomSeed(unit.id, 360);
						Tmp.v1.trns(ang, hitSize * j[4]).add(unit);
						DrawFunc.arrow(Tmp.v1.x, Tmp.v1.y, j[2], j[0], j[1], ang);
					}
				}
				//
				//				if(unit instanceof Trailc){
				//					Trail trail = ((Trailc)unit).trail();
				//					trail.draw(unit.team.color, (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * unit.elevation) * trailScl);
				//				}
				
				Draw.color(Tmp.c1.set(unit.team.color).lerp(Color.white, 0.65f));
				Fill.circle(unit.x, unit.y, bodySize * sizeF * 0.75f * unit.healthf());
				Draw.color(Color.black);
				Fill.circle(unit.x, unit.y, bodySize * sizeF * 0.7f * unit.healthf());
				
				Draw.color(unit.team.color);
				Tmp.v1.set(unit.aimX, unit.aimY).sub(unit).nor().scl(bodySize * 0.15f);
				Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, bodySize * sizeF * outerEyeScl);
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + 0.45f);
				Tmp.v1.setLength(bodySize * sizeF * (outerEyeScl - innerEyeScl));
				Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, bodySize * sizeF * innerEyeScl);
				//				Tmp.v1.setLength(hitSize * 1.5f);
				//				DrawFunc.arrow(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, hitSize / 8, hitSize / 4, hitSize / 8, Tmp.v1.angle());
				Draw.reset();
			}
			
			@Override
			public void update(Unit unit){
				super.update(unit);
				if(Mathf.chanceDelta(0.1))for(int i : Mathf.signs)slopeEffect.at(unit.x + Mathf.range(bodySize), unit.y + Mathf.range(bodySize), bodySize, unit.team.color, i);
			}
			
			@Override
			public void drawCell(Unit unit){
			}
			
			@Override
			public void drawControl(Unit unit){
				Draw.z(Layer.effect + 0.001f);
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) +  Mathf.clamp(unit.hitTime) / 5f);
				for(int i = 0; i < 4; i++){
					float rotation = Time.time * 1.5f + i * 90;
					Tmp.v1.trns(rotation, bodySize * 1.5f).add(unit);
					Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, rotation + 90);
				}
				Draw.reset();
			}
			
			@Override
			public void drawItems(Unit unit){
				super.drawItems(unit);
			}
			
			@Override
			public <T extends Unit & Legsc> void drawLegs(T unit){
			}
			
			@Override
			public void drawLight(Unit unit){
				Drawf.light(unit.x, unit.y, bodySize * 3f, unit.team.color, lightOpacity);
			}
			
			@Override
			public void drawMech(Mechc mech){
			}
			
			@Override
			public void drawOutline(Unit unit){
			}
			
			@Override
			public void drawEngines(Unit unit){
			}
			
			@Override
			public void drawTrail(Unit unit){
			}
			
			@Override
			public <T extends Unit & Payloadc> void drawPayload(T unit){
				super.drawPayload(unit);
			}
			
			@Override
			public void drawShadow(Unit unit){
			}
			
			@Override
			public void drawShield(Unit unit){
				float alpha = unit.shieldAlpha();
				float radius = unit.hitSize() * 1.3f;
				Fill.light(unit.x, unit.y, Lines.circleVertices(radius), radius, Tmp.c1.set(Pal.shield), Tmp.c2.set(unit.team.color).a(0.7f).lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f)).a(Pal.shield.a * alpha));
			}
			
			@Override
			public void drawSoftShadow(Unit unit){
			}
			
			@Override
			public void drawWeapons(Unit unit){
			}
		};
	}
}
