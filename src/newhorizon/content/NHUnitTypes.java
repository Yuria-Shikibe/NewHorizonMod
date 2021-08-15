package newhorizon.content;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ai.types.BuilderAI;
import mindustry.ai.types.MinerAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.ctype.ContentList;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Units;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.abilities.MoveLightningAbility;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.abilities.ShieldRegenFieldAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.units.WeaponMount;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.weapons.PointDefenseWeapon;
import mindustry.type.weapons.RepairBeamWeapon;
import newhorizon.NewHorizon;
import newhorizon.bullets.*;
import newhorizon.feature.PosLightning;
import newhorizon.feature.ScreenHack;
import newhorizon.func.DrawFuncs;
import newhorizon.func.NHSetting;
import newhorizon.func.NHUnitOutline;
import newhorizon.units.BoostAbility;
import newhorizon.units.NHWeapon;
import newhorizon.units.PhaseAbility;
import newhorizon.units.TowardShield;
import newhorizon.vars.EventTriggers;

import static arc.graphics.g2d.Lines.lineAngle;
import static mindustry.Vars.headless;
import static mindustry.Vars.tilesize;

public class NHUnitTypes implements ContentList{
	public static final byte OTHERS = Byte.MIN_VALUE, GROUND_LINE_1 = 0, AIR_LINE_1 = 1, ENERGY_LINE_1 = 3, NAVY_LINE_1 = 6;
	
	public static NHWeapon
			posLiTurret, closeAATurret, collapserCannon, collapserLaser, multipleLauncher, smallCannon,
			mainCannon
			
			;
	
	public static UnitType
			guardian, gather,
			sharp, branch, warper, striker, annihilation, hurricane, collapser,
			origin, thynomo, aliotiat, tarlidor, destruction,
			relay, ghost, zarkov, declining, rhino;
	
	static{
		EntityMapping.nameMap.put(NewHorizon.name("declining"), EntityMapping.idMap[20]);
		EntityMapping.nameMap.put(NewHorizon.name("zarkov"), EntityMapping.idMap[20]);
		EntityMapping.nameMap.put(NewHorizon.name("ghost"), EntityMapping.idMap[20]);
		EntityMapping.nameMap.put(NewHorizon.name("relay"), EntityMapping.idMap[20]);
	}
	
	public void loadWeapon(){
		mainCannon = new NHWeapon("main-cannon"){{
			top = rotate = true;
			mirror = false;
			alternate = false;
			cooldownTime = 240f;
			recoil = 7f;
			shots = 3;
			rotateSpeed = 1f;
			shootSound = NHSounds.flak;
			shootCone = 5f;
			shootY = 15f;
			reload = 300f;
			shake = 7f;
			ejectEffect = Fx.blastsmoke;
			bullet = new SpeedUpBulletType(5.25f, 180f, "large-bomb"){{
				lifetime = 22f;
				
				width = height = 25f;
				
				spin = 4f;
				
				trailWidth = 2.2f;
				trailLength = 20;
				
				velocityBegin = 12f;
				velocityIncrease = 22f;
				func = Interp.pow3Out;
				accelerateBegin = 0f;
				accelerateEnd = 0.8f;
				
				homingDelay = 5f;
				homingPower = 0.0075f;
				homingRange = 140f;
				
				pierce = pierceBuilding = true;
				
				lightning = 2;
				lightningDamage = damage / 4f;
				lightningLength = 4;
				lightningLengthRand = 12;
				
				splashDamageRadius = 16f;
				splashDamage = damage * 0.75f;
				backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.lightSkyBack;
				
				knockback = 20f;
				
				frontColor = NHColor.lightSkyFront;
				shootEffect = despawnEffect = NHFx.square(backColor, 40f, 4, 40f, 6f);
				smokeEffect = NHFx.hugeSmoke;
				trailChance = 0.6f;
				trailEffect = NHFx.trail;
				hitShake = 8f;
				hitSound = Sounds.explosionbig;
				hitEffect = NHFx.instHit(backColor, 3, 85f);
				
				fragBullets = 3;
				fragBullet = NHBullets.skyFrag;
				fragLifeMax = 0.5f;
				fragLifeMin = 0.25f;
				fragVelocityMax = 1.2f;
				fragVelocityMin = 0.75f;
			}};
		}
			@Override
			public void draw(Unit unit, WeaponMount mount){
				super.draw(unit, mount);
				
				if(!unit.isPlayer() && !NHSetting.enableDetails())return;
				
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
				
				float length = bullet.range();
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
		
		multipleLauncher = new NHWeapon("mult-launcher"){{
			reload = 60f;
			shots = 3;
			shake = 3f;
			shotDelay = 8f;
			mirror = true;
			rotateSpeed = 2.5f;
			alternate = true;
			shootSound = NHSounds.launch;
			shootCone = 30f;
			shootY = 5f;
			top = true;
			rotate = true;
			bullet = new SpeedUpBulletType(5.25f, 100f, NHBullets.STRIKE){{
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
				hitEffect = NHFx.square(backColor, 40f, 4, 40f, 6f);
				despawnEffect = NHFx.lightningHitLarge(backColor);
				frontColor = NHColor.lightSkyFront;
				shootEffect = NHFx.shootCircleSmall(backColor);
				smokeEffect = Fx.shootBigSmoke2;
				trailChance = 0.6f;
				trailEffect = NHFx.trail;
				hitShake = 3f;
				hitSound = Sounds.plasmaboom;
			}};
		}};
		
		collapserLaser = new NHWeapon(){{
			reload = 480f;
			rotateSpeed = 1.5f;
			rotate = true;
			continuous = top = true;
			mirror = true;
			shootCone = 90f;
			alternate = false;
			shootSound = Sounds.beam;
			bullet = NHBullets.collapserLaserSmall;
			predictTarget = false;
		}};
		
		collapserCannon = new NHWeapon("collapser-cannon"){{
			top = rotate = alternate = mirror = true;
			reload = 60f;
			shake = 12f;
			heatColor = NHColor.thurmixRed;
			shootSound = NHSounds.coil;
			shootY = 6f;
			inaccuracy = 0;
			shots = 3;
			shotDelay = 6f;
			rotateSpeed = 2f;
			ejectEffect = NHFx.hugeSmoke;
			shootCone = 20f;
			bullet = new AdaptedLaserBulletType(800f){{
				hitColor = NHColor.thurmixRed;
				colors = new Color[]{hitColor.cpy().mul(1f, 1f, 1f, 0.45f), hitColor, NHColor.thurmixRedLight, Color.white};
				length = 600f;
				width = 14f;
				lifetime = PosLightning.lifetime + 5f;
				ammoMultiplier = 4;
				lengthFalloff = 0.8f;
				sideLength = 40f;
				sideWidth = 0.5f;
				sideAngle = 30f;
				largeHit = true;
				hitEffect = NHFx.instHit(hitColor, 2, 36f);
				shootEffect = NHFx.square(hitColor, 15f, 2, 8f, 2f);
			}};
		}};
		
		posLiTurret = new NHWeapon("pos-li-blaster"){{
			shake = 1f;
			shots = 1;
			predictTarget = rotate = false;
			top = alternate = true;
			reload = 30f;
			shootY = 4f;
			shootSound = Sounds.spark;
			heatColor = NHColor.lightSkyBack;
			bullet = new PosLightningType(20f){{
				lightningColor = NHColor.lightSkyBack;
				maxRange = 160f;
				hitEffect = NHFx.lightningHitSmall(lightningColor);
				lightningLength = 1;
				lightningLengthRand = 4;
			}};
		}};
		
		closeAATurret = new NHWeapon("anti-air-pulse-laser"){{
			shake = 0f;
			shots = 1;
			shotDelay = 6f;
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
			bullet = NHBullets.antiAirSap;
		}};
		
		smallCannon = new NHWeapon("cannon"){{
			top = mirror = rotate = true;
			reload = 45f;
			
			shots = 3;
			shotDelay = 8f;
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
	
	@Override
	public void load(){
		loadWeapon();
		
		relay = new UnitType("relay"){{
			armor = 6;
			buildBeamOffset = 6f;
			hitSize = 20f;
			drag = 0.06F;
			itemCapacity = 20;
			speed = 1.2F;
			health = 800.0F;
			accel = 0.12f;
			rotateSpeed = 5f;

			buildSpeed = 1.125f;
			
			trailLength = 70;
			trailX = 4f;
			trailY = -9;
			trailScl = 1.65f;
			
			rotateShooting = false;
			
			weapons.add(new NHWeapon("primary-weapon"){{
				mirror = top = rotate = alternate = true;
				reload = 60f;
				shotDelay = 6f;
				shots = 4;
				x = 5f;
				rotateSpeed = 12f;
				y = -6f;
				shootY = 18f;
				velocityRnd = 0.075f;
				inaccuracy = 5f;
				spacing = 2f;
				shootSound = Sounds.missile;
				bullet = new SpeedUpBulletType(5f, 25f, "missile-large"){{
					velocityBegin = 1f;
					velocityIncrease = 7f;
					accelerateBegin = 0f;
					accelerateEnd = 0.65f;
					lifetime = 65f;
					backColor = hitColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
					frontColor = NHColor.lightSkyFront;
					
					splashDamage = damage / 4f;
					splashDamageRadius = 24f;
					
					hitEffect = NHFx.blast(backColor, splashDamageRadius);
					despawnEffect = NHFx.crossBlast(backColor, 40f);
					shootEffect = NHFx.shootCircleSmall(backColor);
					smokeEffect = Fx.shootBigSmoke;
					
					trailLength = 12;
					trailWidth = 1.75f;
					
					width = 7f;
					height = 30f;
					
					homingDelay = 5f;
					homingPower = 0.02f;
					homingRange = 200f;
				}};
			}});
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		rhino = new UnitType("rhino"){{
			defaultController = BuilderAI::new;
			constructor = EntityMapping.map(3);
			abilities.add(new BoostAbility());
			weapons.add(new RepairBeamWeapon("point-defense-mount"){{
				y = -8.5f;
				x = 0;
				shootY = 4f;
				mirror = false;
				beamWidth = 0.7f;
				repairSpeed = 1f;
				
				bullet = new BulletType(){{
					maxRange = 120f;
				}};
			}});
			armor = 12;
			buildBeamOffset = 6f;
			buildSpeed = 5f;
			hitSize = 20f;
			flying = true;
			drag = 0.06F;
			accel = 0.12F;
			itemCapacity = 200;
			speed = 1F;
			health = 3000.0F;
			engineSize = 3.4F;
			engineOffset = 10.5f;
			isCounted = false;
			lowAltitude = true;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		declining = new UnitType("declining"){{
			weapons.add(mainCannon.copy().setPos(0, -17));
			weapons.add(mainCannon.copy().setPos(0, 25));
			weapons.add(mainCannon.copy().setPos(0, -56));
			
			Weapon w = new PointDefenseWeapon(NewHorizon.name("cannon")){{
				color = NHColor.lightSkyBack;
				beamEffect = Fx.chainLightning;
				mirror = top = alternate = true;
				reload = 15.0F;
				targetInterval = 10.0F;
				targetSwitchInterval = 15.0F;
				bullet = new BulletType() {
					{
						shootEffect = NHFx.lightningHitSmall(color);
						hitEffect = NHFx.square45_4_45;
						maxRange = 120.0F;
						damage = 150f;
					}
				};
			}};
			
			weapons.add(NHWeapon.setPos(w.copy(), 30, -30));
			weapons.add(NHWeapon.setPos(w.copy(), 36, -35));
			weapons.add(NHWeapon.setPos(w.copy(), 24, -35));
			
			weapons.add(new NHWeapon("laser-cannon"){{
				mirror = top = alternate = autoTarget = rotate = true;
				predictTarget = controllable = false;
				x = 22;
				y = -50;
				reload = 12f;
				recoil = 3f;
				inaccuracy = 0;
				shots = 1;
				rotateSpeed = 25f;
				shootSound = NHSounds.gauss;
				bullet = new ShrapnelBulletType(){{
					lifetime = 45f;
					length = 200f;
					damage = 300.0F;
					status = StatusEffects.shocked;
					statusDuration = 60f;
					fromColor = NHColor.lightSkyFront;
					toColor = NHColor.lightSkyBack;
					serrationSpaceOffset = 40f;
					width = 8f;
					shootEffect = NHFx.lightningHitSmall(NHColor.lightSkyBack);
					smokeEffect = new MultiEffect(NHFx.lightSkyCircleSplash, new Effect(lifetime + 10f, b -> {
						Draw.color(fromColor, toColor, b.fin());
						Fill.circle(b.x, b.y, (width / 1.75f) * b.fout());
					}));
				}};
			}});
			
			abilities.add(new TowardShield(160f, 200f, 20000f, 2000f, 120f, 0.025f, 0, 58));
			health = 30000;
			speed = 0.75f;
			drag = 0.18f;
			hitSize = 60f;
			commandRadius = 240f;
			armor = 30;
			commandLimit = 3;
			accel = 0.1f;
			rotateSpeed = 0.9f;
			rotateShooting = false;
			buildSpeed = 6f;
			
			trailLength = 70;
			trailX = 20f;
			trailY = -40f;
			trailScl = 4f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		ghost = new UnitType("ghost"){{
			health = 1200;
			speed = 1.75f;
			drag = 0.18f;
			hitSize = 20f;
			armor = 12;
			accel = 0.1f;
			rotateSpeed = 2f;
			rotateShooting = false;
			buildSpeed = 3f;
			
			weapons.add(
					smallCannon.copy().setPos(12,-7),
					smallCannon.copy().setPos(5,-1),
					new NHWeapon("laser-cannon"){{
						top = rotate = true;
						rotateSpeed = 3f;
						x = 0;
						y = -11;
						
						recoil = 2f;
						mirror = false;
						reload = 60f;
						shootY = 5f;
						shootCone = 12f;
						shake = 8f;
						inaccuracy = 3f;
						shots = 1;
						predictTarget = true;
						
						shootSound = Sounds.laser;
						
						bullet = new BasicBulletType(2f, 90, "mine-bullet"){{
							scaleVelocity = true;
							keepVelocity = false;
							
							trailLength = 22;
							trailWidth = 4f;
							drawSize = 120f;
							recoil = 1.5f;
							
							trailChance = 0.1f;
							trailParam = 4f;
							trailEffect = NHFx.trail;
							
							spin = 3f;
							shrinkX = shrinkY = 0.15f;
							height = width = 25f;
							lifetime = 160f;
							
							status = StatusEffects.blasted;
							
							backColor = trailColor = lightColor = lightningColor = hitColor = NHColor.lightSkyBack;
							frontColor = NHColor.lightSkyFront;
							
							splashDamage = damage / 3;
							splashDamageRadius = 24f;
							
							lightningLength = 2;
							lightningLengthRand = 4;
							lightningDamage = 10;
							
							hitSound = Sounds.explosion;
							hitShake = 8f;
							shootEffect = NHFx.shootCircleSmall(backColor);
							smokeEffect = Fx.shootSmallSmoke;
							despawnEffect = NHFx.lightningHitLarge(backColor);
							hitEffect = NHFx.hugeSmoke;
						}};
					}}
			);
			
			commandLimit = 10;
			
			trailLength = 70;
			trailX = 5f;
			trailY = -13;
			trailScl = 1.65f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		zarkov = new UnitType("zarkov"){{
			weapons.add(multipleLauncher.copy().setPos(8, -22), multipleLauncher.copy().setPos(16, -8), smallCannon.copy().setAutoTarget(true).setPos(8.5f, 5.75f));
			health = 12000;
			speed = 1f;
			drag = 0.18f;
			hitSize = 42f;
			armor = 16f;
			accel = 0.1f;
			rotateSpeed = 1.6f;
			rotateShooting = false;
			buildSpeed = 3f;
			
			trailLength = 70;
			trailX = 7f;
			trailY = -25f;
			trailScl = 2.6f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		collapser = new UnitType("collapser"){{
				rotateShooting = false;
				commandRadius = 240f;
				abilities.add(new ForceFieldAbility(180f, 200, 80000, 900));
				constructor = EntityMapping.map(3);
				rotateShooting = false;
				
				targetAir = targetGround = true;
				weapons.addAll(
					collapserCannon.copy().setPos(60, -50),
					collapserCannon.copy().setPos(40, -20),
					collapserCannon.copy().setPos(32, 60),
					new NHWeapon("collapser-laser"){{
						y = -42;
						x = 0;
						shootY = 25f;
						shots = 3;
						shotDelay = 15;
						spacing = 3f;
						inaccuracy = 3f;
						reload = 150f;
						rotateSpeed = 1.5f;
						rotate = true;
						top = true;
						shootSound = NHSounds.flak;
						mirror = alternate = false;
						bullet = NHBullets.collapserBullet;
					}},
					new NHWeapon(){
						float rangeWeapon = 520f;
						
						@Override
						public void draw(Unit unit, WeaponMount mount){
							float z = Draw.z();
							
							Tmp.v1.trns(unit.rotation, y);
							float f = 1 - mount.reload / reload;
							float rad = 12f;
							
							float f1 = Mathf.curve(f,  0.4f, 1f);
							Draw.z(Layer.bullet);
							Draw.color(heatColor);
							for(int i : Mathf.signs){
								for(int j : Mathf.signs){
									Drawf.tri(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f1 * rad / 1.75f + Mathf.num(j > 0) * 2f * (f1 + 1) / 2, (rad * 3f + Mathf.num(j > 0) * 20f) * f1, j * Time.time + 90 * i);
								}
							}
							
							TextureRegion arrowRegion = NHContent.arrowRegion;
							
							Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
							Tmp.v2.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), rangeWeapon)).add(unit);
							
							for (int l = 0; l < 4; l++) {
								float angle = 45 + 90 * l;
								for (int i = 0; i < 4; i++) {
									Tmp.v3.trns(angle, (i - 4) * tilesize + tilesize).add(Tmp.v2);
									float fS = (100 - (Time.time + 25 * i) % 100) / 100 * f1 / 4;
									Draw.rect(arrowRegion, Tmp.v3.x, Tmp.v3.y, arrowRegion.width * fS, arrowRegion.height * fS, angle + 90);
								}
							}
							
							Lines.stroke((1.5f + Mathf.absin( Time.time + 4, 8f, 1.5f)) * f1, heatColor);
							Lines.square(Tmp.v2.x, Tmp.v2.y, 4 + Mathf.absin(8f, 4f), 45);
							
							Lines.stroke(rad / 2.5f * mount.heat, heatColor);
							Lines.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, rad * 2 * (1 - mount.heat));
							
							Draw.color(heatColor);
							Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad);
							Lines.stroke(f * 1.5f);
							DrawFuncs.circlePercentFlip(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad + 5, Time.time, 20f);
							Draw.color(Color.white);
							Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad * 0.7f);
							
							Draw.z(z);
						}
						
						@Override
						protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float aimX, float aimY, float mountX, float mountY, float rotation, int side){
							shootSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));

							BulletType ammo = bullet;
							float lifeScl = ammo.scaleVelocity ? Mathf.clamp(Mathf.dst(shootX, shootY, aimX, aimY) / ammo.range()) : 1f;
							
							Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
							Tmp.v1.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), rangeWeapon)).add(unit);
							
							Bullet b = bullet.create(unit, unit.team, Tmp.v1.x, Tmp.v1.y, 0);
							b.vel.setZero();
							b.set(Tmp.v1);
							
							ejectEffect.at(mountX, mountY, rotation * side);
							ammo.shootEffect.at(shootX, shootY, rotation);
							ammo.smokeEffect.at(shootX, shootY, rotation);
							unit.apply(shootStatus, shootStatusDuration);
						}
						
						{
							y = 50;
							x = 0;
							shootY = 25f;
							shots = 1;
							reload = 1200f;
							rotateSpeed = 100f;
							rotate = true;
							top = false;
							mirror = alternate = predictTarget = false;
							heatColor = NHColor.thurmixRed;
							shootSound = NHSounds.hugeShoot;
							bullet = new EffectBulletType(480f){
								@Override
								public float range(){
									return rangeWeapon;
								}
								
								@Override
								public void despawned(Bullet b){
									super.despawned(b);
									
									Vec2 vec = new Vec2().set(b);
									
									float damageMulti = b.damageMultiplier();
									Team team = b.team;
									for(int i = 0; i < splashDamageRadius / (tilesize * 2); i++){
										int finalI = i;
										Time.run(i * despawnEffect.lifetime / (splashDamageRadius / (tilesize * 2)), () -> {
											Damage.damage(team, vec.x, vec.y, tilesize * (finalI + 6), splashDamage * damageMulti, true, true);
										});
									}
									
									Units.nearby(team, vec.x, vec.y, splashDamageRadius * 2, u -> {
										u.heal((1 - u.healthf()) / 3 * u.maxHealth());
										u.apply(StatusEffects.overclock, 360f);
									});
									
									Units.nearbyEnemies(team, vec.x, vec.y, splashDamageRadius * 2, u -> {
										if(u.isPlayer())Events.fire(ScreenHack.ScreenHackEvent.class, new ScreenHack.ScreenHackEvent((Player)u.controller(), 360f));
									});
									
									if(!NHSetting.enableDetails())return;
									float rad = 120;
									float spacing = 2.5f;
									
									for(int k = 0; k < (despawnEffect.lifetime - NHFx.chainLightningFadeReversed.lifetime) / spacing; k++){
										Time.run(k * spacing, () -> {
											for(int j : Mathf.signs){
												Vec2 v = Tmp.v6.rnd(rad * 2 + Mathf.random(rad * 4)).add(vec);
												(j > 0 ? NHFx.chainLightningFade : NHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, vec);
											}
										});
									}
								}
								
								@Override
								public void update(Bullet b){
									float rad = 120;
									
									Effect.shake(8 * b.fin(), 6, b);
									
									if(b.timer(1, 12)){
										Seq<Teamc> entites = new Seq<>();
										
										Units.nearbyEnemies(b.team, b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, entites::add);
										
										Units.nearbyBuildings(b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, e -> {
											if(e.team != b.team)entites.add(e);
										});
										
										entites.shuffle();
										entites.truncate(15);
										
										for(Teamc e : entites){
											PosLightning.create(b, b.team, b, e, lightningColor, false, lightningDamage, 5 + Mathf.random(5), PosLightning.WIDTH, 1, p -> {
												NHFx.lightningHitSmall.at(p.getX(), p.getY(), 0, lightningColor);
											});
										}
									}
									
									if(NHSetting.enableDetails() && b.lifetime() - b.time() > NHFx.chainLightningFadeReversed.lifetime)for(int i = 0; i < 2; i++){
										if(Mathf.chanceDelta(0.2 * Mathf.curve(b.fin(), 0, 0.8f))){
											for(int j : Mathf.signs){
												Sounds.spark.at(b.x, b.y, 1f, 0.3f);
												Vec2 v = Tmp.v6.rnd(rad / 2 + Mathf.random(rad * 2) * (1 + Mathf.curve(b.fin(), 0, 0.9f)) / 1.5f).add(b);
												(j > 0 ? NHFx.chainLightningFade : NHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, b);
											}
										}
									}
									
									if(b.fin() > 0.05f && Mathf.chanceDelta(b.fin() * 0.3f + 0.02f)){
										NHSounds.blaster.at(b.x, b.y, 1f, 0.3f);
										Tmp.v1.rnd(rad / 4 * b.fin());
										NHFx.shuttleLerp.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle(), hitColor, Mathf.random(rad, rad * 3f) * (Mathf.curve(b.fin(Interp.pow2In), 0, 0.7f) + 2) / 3);
									}
								}
								
								@Override
								public void draw(Bullet b){
									float fin = Mathf.curve(b.fin(), 0, 0.02f);
									float f = fin * Mathf.curve(b.fout(), 0f, 0.1f);
									float rad = 120;
									
									float circleF = (b.fout(Interp.pow2In) + 1) / 2;
									
									Draw.color(hitColor);
									Lines.stroke(rad / 20 * b.fin());
									Lines.circle(b.x, b.y, rad * b.fout(Interp.pow3In));
									Lines.circle(b.x, b.y, b.fin(Interp.circleOut) * rad * 3f * Mathf.curve(b.fout(), 0, 0.05f));
									
									Rand rand = new Rand(b.id);
									for(int i = 0; i < (int)(rad / 4); i++){
										Tmp.v1.trns(rand.random(360f) + rand.range(1f) * rad / 5 * b.fin(Interp.pow2Out), rad / 4 * circleF + rand.random(rad * (1 + b.fin(Interp.circleOut)) / 2f));
										float angle = Tmp.v1.angle();
										Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 28 + rand.random(0, 8), rad / 16 * b.fout(), angle);
										Drawf.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 12 + rand.random(0, 2), rad / 12 * b.fout(), angle - 180);
									}
									
									Angles.randLenVectors(b.id + 1, (int)(rad / 3), rad / 4 * circleF, rad * (1 + b.fout(Interp.pow3In)) / 3, (x, y) -> {
										float angle = Mathf.angle(x, y);
										Drawf.tri(b.x + x, b.y + y, rad / 6 * (1 + b.fin()) / 2, (b.fin() * 3 + 1) / 3 * 43 + rand.random(4, 12) * (b.fin(Interp.circleIn) + 1) / 2, angle);
										Drawf.tri(b.x + x, b.y + y, rad / 6 * (1 + b.fin()) / 2, (b.fin() * 3 + 1) / 3 * 12 + rand.random(0, 2) * (b.fout() + 1) / 2, angle - 180);
									});
									
									Drawf.light(b.x, b.y, rad * f * (b.fin() + 1) * 2, Draw.getColor(), 0.7f);
									
									Draw.z(Layer.effect + 0.001f);
									Draw.color(hitColor);
									Fill.circle(b.x, b.y, rad * fin * circleF / 2f);
									Draw.color(NHColor.thurmixRedDark);
									Fill.circle(b.x, b.y, rad * fin * circleF * 0.75f / 2f);
								
								}
								
								{
									collides = false;
									collidesTiles = collidesAir = collidesGround = true;
									speed = 100;
									
									keepVelocity = false;
									
									splashDamageRadius = 800f;
									splashDamage = 800f;
									
									lightningDamage = 200f;
									lightning = 22;
									lightningLength = 60;
									lightningLengthRand = 60;
									
									rangeWeapon = 400f;
									hitShake = despawnShake = 40f;
									drawSize = clipSize = 800f;
									hitColor = lightColor = trailColor = lightningColor = NHColor.thurmixRed;
									
									fragBullets = 6;
									fragBullet = NHBullets.collapserBullet;
									hitSound = NHSounds.hugeBlast;
									hitSoundVolume = 4f;
									
									fragLifeMax = 1.1f;
									fragLifeMin = 0.7f;
									fragVelocityMax = 0.6f;
									fragVelocityMin = 0.2f;
									
									shootEffect = NHFx.lightningHitLarge(hitColor);
									
									despawnEffect = new Effect(150f, 1600f, e -> {
										float rad = 150f;
										Rand rand = new Rand(e.id + 1);
										
										Draw.color(Color.white, hitColor, e.fin() + 0.3f);
										float circleRad = rad * e.fout();
										Fill.circle(e.x, e.y, circleRad);
										
										circleRad = e.fin(Interp.circleOut) * rad * 4f;
										Lines.stroke(12 * e.fout());
										Lines.circle(e.x, e.y, circleRad);
										for(int i = 0; i < 16; i++){
											Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
											Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
										}
										
										Lines.stroke(18 * e.fout());
										Lines.circle(e.x, e.y, e.fin(Interp.circleOut) * rad * 1.2f);
										Angles.randLenVectors(e.id, 40, rad / 3, rad * e.fin(Interp.pow2Out), (x, y) -> {
											lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 25 + 10);
										});
										
										Angles.randLenVectors(e.id, (int)(rad / 4), rad / 6, rad * (1 + e.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
											float angle = Mathf.angle(x, y);
											float width = e.foutpowdown() * rand.random(rad / 6, rad / 3);
											float length = rand.random(rad / 2, rad * 5) * e.fout(Interp.circleOut);
											
											Draw.color(hitColor);
											Drawf.tri(e.x + x, e.y + y, width, rad / 3 * e.fout(Interp.circleOut), angle - 180);
											Drawf.tri(e.x + x, e.y + y, width, length, angle);
											
											Draw.color(NHColor.thurmixRedDark);
											
											width *= e.fout();
											
											Drawf.tri(e.x + x, e.y + y, width / 2, rad / 3 * e.fout(Interp.circleOut) * 0.9f * e.fout(), angle - 180);
											Drawf.tri(e.x + x, e.y + y, width / 2, length / 1.5f * e.fout(), angle);
										});
										
										Draw.color(NHColor.thurmixRedDark);
										Fill.circle(e.x, e.y, rad * e.fout() * 0.75f);
										
										Drawf.light(e.x, e.y, rad * e.fslope() * 4f, hitColor, 0.7f);
									}).layer(Layer.effect + 0.001f);
								}
							};
						}
					}
				);
				
				hitSize = 120f;
				
				commandLimit = 3;
				speed = 0.5f;
				health = 100000;
				rotateSpeed = 0.65f;
				engineSize = 30f;
				buildSpeed = 10f;
				engineOffset = 62f;
				itemCapacity = 300;
				armor = 180;
				lowAltitude = true;
				flying = true;
			}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
			
			@Override
			public Unit create(Team team){
				Unit u = super.create(team);
				for(WeaponMount mount : u.mounts){
					mount.reload = mount.weapon.reload;
				}
				Events.fire(EventTriggers.BossGeneratedEvent.class, new EventTriggers.BossGeneratedEvent(u));
				return u;
			}
			
			@Override
			public void drawCell(Unit unit){
				super.drawCell(unit);
				
				Draw.z(Layer.effect + 0.001f);
				
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f));
				Lines.stroke(3f + Mathf.absin(10f, 0.55f));
				DrawFuncs.circlePercent(unit.x, unit.y, unit.hitSize, unit.healthf(), 0);
				
				for(int i = 0; i < 4; i++){
					float rotation = Time.time * 1.5f + i * 90;
					Tmp.v1.trns(rotation, hitSize * 1.1f).add(unit);
					Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, rotation + 90);
				}
				
				Draw.z(Layer.flyingUnitLow);
				Draw.reset();
			}
		};
		
		guardian = new UnitType("guardian"){{
			constructor = EntityMapping.map(3);
			hitSize = 20f;
			speed = 1.5f;
			accel = 0.07F;
			drag = 0.075F;
			health = 22000;
			commandLimit = 4;
			itemCapacity = 0;
			rotateSpeed = 100;
			engineSize = 8f;
			flying = true;
			abilities.add(new PhaseAbility(600f, 320f, 160f));
			weapons.add(new Weapon(){{
				shootCone = 360;
				rotate = false;
				mirror = false;
				alternate = false;
				predictTarget = false;
				top = false;
				shots = 12;
				shotDelay = 6f;
				velocityRnd = 0.15f;
				x = y = shootX = shootY = 0;
				reload = 120f;
				shootSound = NHSounds.blaster;
				bullet = new SpeedUpBulletType(0.25f, 160){
					{
						width = 22f;
						height = 40f;
						
						func = Interp.pow2In;
						
						pierceCap = 3;
						splashDamage = damage / 4;
						splashDamageRadius = 24f;
						
						trailLength = 20;
						trailWidth = 3f;
						
						accelerateBegin = 0.4f;
						accelerateEnd = 0.75f;
						
						velocityBegin = 1.85f;
						velocityIncrease = 12f;
						
						lifetime = 160f;
						
						trailEffect = NHFx.trail;
						trailChance = 0.35f;
						trailParam = 5f;
						
						homingRange = 640F;
						homingPower = 0.125f;
						homingDelay = 30f;
						
						lightning = 3;
						lightningLengthRand = 10;
						lightningLength = 5;
						lightningDamage = damage / 4;
						
						shootEffect = smokeEffect = Fx.none;
						hitEffect = Fx.hitBeam;
						despawnEffect = new Effect(20f, b -> {
							Draw.color(b.color);
							Angles.randLenVectors(b.id, 4, 35 * b.fin() + 5, (x, y) -> Fill.circle(b.x + x, b.y + y, 4 * b.fout(Interp.pow2Out)));
						});
					}
					
					@Override
					public float range(){
						return 400f;
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
						
						for(int i = 0; i < lightning; i++){
							Lightning.create(b, b.team.color, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
						}
						
						if(!(b.owner instanceof Healthc)) return;
						Healthc from = (Healthc)b.owner;
						if(!from.isAdded() || from.healthf() > 0.99f) return;
						PosLightning.createEffect(Tmp.v1.set(x, y), from, b.team.color, 2, PosLightning.WIDTH);
						for(int i = 0; i < 2; i++) NHFx.transport.at(x, y, 3.2f, b.team.color, from);
						from.heal(damage / 8);
					}
					
					@Override
					public void despawned(Bullet b){
						despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
						Effect.shake(despawnShake, despawnShake, b);
						hit(b);
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
						b.vel.rotate(Mathf.range(180));
					}
					
					public void drawTrail(Bullet b){
						if(trailLength > 0 && b.trail != null){
							float z = Draw.z();
							Draw.z(z - 0.0001f);
							b.trail.draw(b.team.color, trailWidth);
							Draw.z(z);
						}
					}
					
					@Override
					public void draw(Bullet b) {
						drawTrail(b);
						
						float height = this.height * ((1f - shrinkY) + shrinkY * b.fout());
						float width = this.width * ((1f - shrinkX) + shrinkX * b.fout());
						float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f);
						
						Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());
						
						Draw.mixcol(mix, mix.a);
						
						Draw.color(b.team.color);
						Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
						Draw.color(Tmp.c1.set(b.team.color).lerp(Color.white, 0.45f));
						Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);
						
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
						
						b.vel.setLength(velocityBegin + func.apply(b.fin()) * velocityIncrease);
						
						if(homingPower > 0.0001f && b.time >= homingDelay){
							Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange, e -> ((e.isGrounded() && collidesGround) || (e.isFlying() && collidesAir)) && !b.collided.contains(e.id), t -> collidesGround);
							if(target != null){
								b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
							}
						}
						
						if(trailChance > 0){
							if(Mathf.chanceDelta(trailChance)){
								trailEffect.at(b.x, b.y, trailParam, b.team.color);
							}
						}
					}
				};
			}});
			
			trailLength = -1;
			buildSpeed = 10f;
			crashDamageMultiplier = Mathf.clamp(hitSize / 10f, 1, 10);
			payloadCapacity = Float.MAX_VALUE;
			buildBeamOffset = 0;
		}
			public Effect slopeEffect = NHFx.boolSelector;
			
			public final float outerEyeScl = 0.25f;
			public final float innerEyeScl = 0.18f;
			
			public final float[][] rotator = {{80f, 12f, 1f}, {60f, 10f, -0.75f}};
		
			@Override
			public void load(){
				super.load();
				shadowRegion = uiIcon = fullIcon = Core.atlas.find(NewHorizon.name("jump-gate-pointer"));
			}
			
			@Override
			public void init(){
				super.init();
				if(trailLength < 0)trailLength = (int)hitSize * 4;
				if(slopeEffect == NHFx.boolSelector)slopeEffect = new Effect(30, b -> {
					if(!(b.data instanceof Integer))return;
					int i = b.data();
					Draw.color(b.color);
					Angles.randLenVectors(b.id, (int)(b.rotation / 8f), b.rotation / 4f + b.rotation * 2f * b.fin(), (x, y) -> Fill.circle(b.x + x, b.y + y, b.fout() * b.rotation / 2.25f));
					Lines.stroke((i < 0 ? b.fin(Interp.pow2InInverse) : b.fout(Interp.pow2Out)) * 2f);
					Lines.circle(b.x, b.y, (i > 0 ? (b.fin(Interp.pow2InInverse) + 0.5f) : b.fout(Interp.pow2Out)) * b.rotation);
				}).layer(Layer.bullet);
				
				engineSize = hitSize / 4;
				engineSize *= -1;
			}
			
			@Override
			public void draw(Unit unit){
				super.draw(unit);
			}
			
			@Override
			public void drawBody(Unit unit){
				Drawf.light(unit.team, unit.x, unit.y, unit.hitSize * 4f, unit.team.color, 0.68f);
				Draw.z(Layer.effect + 0.001f);
				float sizeF = 1 + Mathf.absin(4f, 0.1f);
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + Mathf.clamp(unit.hitTime) / 5f * 3f);
				Draw.alpha(0.65f);
				Fill.circle(unit.x, unit.y, hitSize * sizeF * 1.1f);
				Draw.alpha(1f);
				Fill.circle(unit.x, unit.y, hitSize * sizeF);
				
				for(float[] j : rotator){
					for(int i : Mathf.signs){
						Drawf.tri(unit.x, unit.y, j[1], j[0], Time.time * j[2] + 90 + 90 * i + Mathf.randomSeed(unit.id));
					}
				}
				
				if(unit instanceof Trailc){
					Trail trail = ((Trailc)unit).trail();
					trail.draw(unit.team.color, (engineSize + Mathf.absin(Time.time, 2f, engineSize / 4f) * unit.elevation) * trailScl);
				}
				
				Draw.color(Tmp.c1.set(unit.team.color).lerp(Color.white, 0.65f));
				Fill.circle(unit.x, unit.y, hitSize * sizeF * 0.75f * unit.healthf());
				Draw.color(Color.black);
				Fill.circle(unit.x, unit.y, hitSize * sizeF * 0.7f * unit.healthf());
				
				Draw.color(unit.team.color);
				Tmp.v1.set(unit.aimX, unit.aimY).sub(unit).nor().scl(hitSize * 0.15f);
				Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, hitSize * sizeF * outerEyeScl);
				Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + 0.45f);
				Tmp.v1.setLength(hitSize * sizeF * (outerEyeScl - innerEyeScl));
				Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, hitSize * sizeF * innerEyeScl);
				Draw.reset();
			}
			
			@Override
			public void update(Unit unit){
				super.update(unit);
				if(Mathf.chanceDelta(0.1))for(int i : Mathf.signs)slopeEffect.at(unit.x + Mathf.range(hitSize), unit.y + Mathf.range(hitSize), hitSize, unit.team.color, i);
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
					Tmp.v1.trns(rotation, hitSize * 1.5f).add(unit);
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
				Drawf.light(unit.team, unit.x, unit.y, hitSize * 3f, unit.team.color, lightOpacity);
			}
			
			@Override
			public void drawMech(Mechc mech){
			}
			
			@Override
			public void drawOutline(Unit unit){
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
		
		gather = new UnitType("gather"){{
			defaultController = MinerAI::new;
			constructor = EntityMapping.map(3);
			weapons.add(new RepairBeamWeapon("repair-beam-weapon-center"){{
				y = -6.5f;
				x = 0;
				shootY = 6f;
				mirror = false;
				beamWidth = 0.7f;
				repairSpeed = 0.6f;
				
				bullet = new BulletType(){{
					maxRange = 120f;
				}};
			}});
			armor = 12;
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
			mineTier = 6;
			mineSpeed = 10F;
			lowAltitude = true;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		origin = new UnitType("origin"){{
			constructor = EntityMapping.map(19);
			weapons.add(
				new NHWeapon("primary-weapon"){{
					mirror = true;
					top = false;
					x = 5f;
					y = -1f;
					reload = 15f;
					shots = 3;
					spacing = 4f;
					inaccuracy = 4f;
					velocityRnd = 0.15f;
					shootSound = NHSounds.scatter;
					shake = 0.75f;
					bullet = new BasicBulletType(4f, 7f){{
						width = 5f;
						height = 25f;
						backColor = lightningColor = lightColor = hitColor = NHColor.lightSkyBack;
						frontColor = backColor.cpy().lerp(Color.white, 0.45f);
						shootEffect = NHFx.shootLineSmall(backColor);
						despawnEffect = NHFx.square(hitColor, 16f, 2, 12, 2f);
						hitEffect = NHFx.lightningHitSmall(backColor);
						smokeEffect = Fx.shootBigSmoke2;
						lifetime = 45f;
					}};
				}}
			);
			speed = 0.6F;
			hitSize = 8.0F;
			health = 160.0F;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		thynomo = new UnitType("thynomo"){{
			constructor = EntityMapping.map(19);
			weapons.add(
				new NHWeapon("thynomo-weapon"){{
					mirror = true;
					top = false;
					x = 8f;
					y = 1f;
					shootY = 9.5f;
					reload = 90f;
					shootCone = 25f;
					shootStatus = StatusEffects.slow;
					shootStatusDuration = 90f;
					continuous = true;
					shootSound = Sounds.beam;
					bullet = new ContinuousLaserBulletType(18f){{
						length = 120f;
						width = 2.55f;
						
						incendChance = 0.025F;
						incendSpread = 5.0F;
						incendAmount = 1;
						
						strokes = new float[]{2f, 1.7f, 1.3f, 0.7f};
						tscales = new float[]{1.1f, 0.8f, 0.65f, 0.4f};
						shake = 3;
						colors = new Color[]{NHColor.lightSkyFront.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.lightSkyBack, Color.white};
						oscScl = 0.4f;
						oscMag = 1.5f;
						lifetime = 90f;
						lightColor = hitColor = NHColor.lightSkyBack;
						hitEffect = NHFx.lightSkyCircleSplash;
						shootEffect = NHFx.square(hitColor, 22f, 4, 16, 3f);
						smokeEffect = Fx.shootBigSmoke;
					}};
				}}
			);
			boostMultiplier = 2.0F;
			health = 650.0F;
			buildSpeed = 0.75F;
			rotateSpeed = 2.5f;
			canBoost = true;
			armor = 9.0F;
			landShake = 2.0F;
			riseSpeed = 0.05F;
			mechFrontSway = 0.55F;
			speed = 0.4F;
			hitSize = 15f;
			engineOffset = 7.4F;
			engineSize = 4.25F;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		aliotiat = new UnitType("aliotiat"){{
			constructor = EntityMapping.map(32);
			weapons.add(posLiTurret.copy().setPos(10f, 3f).setDelay(closeAATurret.reload / 2f), posLiTurret.copy().setPos(6f, -2f));
			engineOffset = 10.0F;
			engineSize = 4.5F;
			speed = 0.35f;
			hitSize = 22f;
			health = 1200f;
			buildSpeed = 1.2f;
			armor = 5f;
			rotateSpeed = 2.8f;
			
			singleTarget = false;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.15f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		tarlidor = new UnitType("tarlidor"){{
			constructor = EntityMapping.map(3);
			abilities.add(new ShieldRegenFieldAbility(50.0F, 50F, 600.0F, 800.0F));
			weapons.add(
				new NHWeapon("stiken"){{
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
					bullet = new ShieldBreaker(6.25f, 50, 650f){{
							drawSize = 500f;
							trailLength = 18;
							trailWidth = 3.5f;
							spin = 2.75f;
							hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
							lifetime = 40f;
							pierceCap = 8;
							width = 20f;
							height = 44f;
							backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
							frontColor = Color.white;
							lightning = 3;
							lightningDamage = damage / 4;
							lightningLength = 3;
							lightningLengthRand = 10;
							smokeEffect = Fx.shootBigSmoke2;
							hitShake = 4f;
							hitSound = Sounds.plasmaboom;
							shrinkX = shrinkY = 0f;
					}};
					shootSound = Sounds.laser;
				}}, new NHWeapon("arc-blaster"){{
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
			);
			engineOffset = 13.0F;
			engineSize = 6.5F;
			speed = 0.4f;
			hitSize = 20f;
			health = 9000f;
			buildSpeed = 1.8f;
			armor = 8f;
			rotateSpeed = 3.3f;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.15f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		annihilation = new UnitType("annihilation"){{
			constructor = EntityMapping.map(32);
			weapons.add(
				new NHWeapon("large-launcher"){{
					top = false;
					rotate = false;
					alternate = true;
					shake = 3.5f;
					shootY = 16f;
					x = 20f;
					recoil = 5.4f;
					predictTarget = false;
					shootCone = 30f;
					reload = 20f;
					shots = 4;
					inaccuracy = 4.0F;
					ejectEffect = Fx.none;
					bullet = new ShrapnelBulletType(){{
						length = 280;
						damage = 160.0F;
						status = StatusEffects.shocked;
						statusDuration = 60f;
						fromColor = NHColor.lightSkyFront;
						toColor = NHColor.lightSkyBack;
						shootEffect = NHFx.lightningHitSmall(NHColor.lightSkyBack);
						smokeEffect = new MultiEffect(NHFx.lightSkyCircleSplash, new Effect(lifetime + 10f, b -> {
							Draw.color(fromColor, toColor, b.fin());
							Fill.circle(b.x, b.y, (width / 1.75f) * b.fout());
						}));
					}};
					shootSound = Sounds.shotgun;
				}}, new NHWeapon(){{
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
					shots = 5;
					shotDelay = 8f;
					inaccuracy = 5.0F;
					ejectEffect = Fx.none;
					bullet = NHBullets.annMissile;
					shootSound = NHSounds.launch;
				}}
			);
			abilities.add(new ForceFieldAbility(64.0F, 5F, 5000.0F, 900.0F));
			range = 320f;
			engineOffset = 15.0F;
			engineSize = 6.5F;
			speed = 0.3f;
			hitSize = 29f;
			health = 25000f;
			buildSpeed = 2.8f;
			armor = 11f;
			rotateSpeed = 1.8f;
			singleTarget = false;
			fallSpeed = 0.016f;
			mechStepParticles = true;
			mechStepShake = 0.5f;
			canBoost = true;
			landShake = 6f;
			boostMultiplier = 3.5f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		sharp = new UnitType("sharp"){{
			constructor = EntityMapping.map(3);
			
			itemCapacity = 15;
			commandLimit = 4;
			health = 140;
			armor = 1;
			engineOffset = 10F;
			engineSize = 2.8f;
			speed = 1.5f;
			flying = true;
			accel = 0.08F;
			drag = 0.02f;
			baseRotateSpeed = 1.5f;
			rotateSpeed = 2.5f;
			hitSize = 10f;
			singleTarget = true;
			
			weapons.add(new NHWeapon(){{
				top = false;
				rotate = false;
				alternate = false;
				mirror = false;
				x = 0f;
				y = 0f;
				reload = 30f;
				shots = 6;
				inaccuracy = 5f;
				ejectEffect = Fx.none;
				velocityRnd = 0.125f;
				spacing = 4f;
				shotDelay = 2.5f;
				shake = 2f;
				maxRange = 140f;
				bullet = new BasicBulletType(3.5f, 6f){{
					trailWidth = 1f;
					trailLength = 10;
					drawSize = 200f;
					
					homingPower = 0.1f;
					homingRange = 120f;
					width = 5f;
					height = 25f;
					keepVelocity = true;
					knockback = 0.75f;
					trailColor = backColor = lightColor = lightningColor = hitColor = NHColor.lightSkyBack;
					frontColor = backColor.cpy().lerp(Color.white, 0.45f);
					trailChance = 0.1f;
					trailParam = 1f;
					trailEffect = NHFx.trail;
					despawnEffect = NHFx.square(backColor, 18f, 2, 12f, 2);
					hitEffect = NHFx.lightningHitSmall(backColor);
					shootEffect = NHFx.shootLineSmall(backColor);
					smokeEffect = Fx.shootBigSmoke2;
				}};
				shootSound = NHSounds.thermoShoot;
			}});
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		branch = new UnitType("branch"){{
			constructor = EntityMapping.map(3);
			weapons.add(new Weapon(){{
				top = false;
				rotate = true;
				alternate = true;
				mirror = false;
				shotDelay = 3f;
				shots = 5;
				x = 0f;
				y = -10f;
				reload = 30f;
				inaccuracy = 4f;
				ejectEffect = Fx.none;
				bullet = new FlakBulletType(2.55f, 15){{
					collidesGround = true;
					sprite = NHBullets.CIRCLE_BOLT;
					
					trailLength = 15;
					trailWidth = 3f;
					
					weaveMag = 4f;
					weaveScale = 4f;
					
					splashDamageRadius = 20f;
					explodeRange = splashDamageRadius / 1.5f;
					splashDamage = damage;
					
					homingDelay = 5f;
					homingPower = 0.005f;
					homingRange = 80f;
					
					lifetime = 60f;
					shrinkX = shrinkY = 0;
					backColor = lightningColor = hitColor = lightColor = trailColor = NHColor.lightSkyBack;
					frontColor = backColor.cpy().lerp(Color.white, 0.55f);
					width = height = 8f;
					smokeEffect = Fx.shootBigSmoke;
					shootEffect = NHFx.shootCircleSmall(backColor);
					hitEffect = NHFx.lightningHitSmall(backColor);
					despawnEffect = NHFx.shootCircleSmall(backColor);
				}};
				shootSound = NHSounds.blaster;
			}});
			engineOffset = 9.0F;
			engineSize = 3f;
			speed = 2.4f;
			accel = 0.06F;
			drag = 0.035F;
			hitSize = 14f;
			health = 460f;
			buildSpeed = 0.5f;
			baseRotateSpeed = 1.5f;
			rotateSpeed = 2.5f;
			armor = 3.5f;
			flying = true;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		warper = new UnitType("warper"){{
			constructor = EntityMapping.map(3);
			weapons.add(new Weapon(){{
				top = false;
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
			abilities.add(new MoveLightningAbility(10, 16, 0.2f, 12, 4, 6, NHColor.lightSkyBack));
			targetAir = false;
			maxRange = 200;
			engineOffset = 14.0F;
			engineSize = 4f;
			speed = 5f;
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
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		striker = new UnitType("striker"){{
			weapons.add(new NHWeapon("striker-weapon"){{
				mirror = false;
				rotate = false;
				continuous = true;
				alternate = false;
				shake = 4f;
				heatColor = NHColor.lightSkyBack;
				shootY = 13f;
				reload = 420f;
				shots = 1;
				x = y = 0f;
				predictTarget = false;
				bullet = NHBullets.strikeLaser;
				chargeSound = Sounds.none;
				shootSound = Sounds.none;
				shootStatus = StatusEffects.slow;
				shootStatusDuration = bullet.lifetime + 60f;
			}}, closeAATurret);
			constructor = EntityMapping.map(3);
			lowAltitude = true;
			health = 5500.0F;
			speed = 0.8F;
			accel = 0.02F;
			drag = 0.025F;
			flying = true;
			hitSize = 30.0F;
			armor = 4.0F;
			engineOffset = 28.5F;
			engineSize = 6.0F;
			rotateSpeed = 1.35F;
			buildSpeed = 0.8f;
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		destruction = new UnitType("destruction"){{
			constructor = EntityMapping.map(3);
			weapons.add(
				closeAATurret.copy().setPos(37, -18), closeAATurret.copy().setPos(26, -8), new NHWeapon(){{
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
				new NHWeapon("arc-blaster"){{
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
					predictTarget = false;
					bullet = new ChainBulletType(200){{
						hitColor = NHColor.lightSkyBack;
						hitEffect = NHFx.square(hitColor, 20f, 2, 16f, 3f);
						smokeEffect = Fx.shootBigSmoke;
						shootEffect = NHFx.shootLineSmall(hitColor);
					}};
					//bullet = NHBullets.longLaser;
					shootSound = Sounds.laser;
				}}
			);
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
			abilities.add(new ForceFieldAbility(100.0F, 4.0F, 4000.0F, 360.0F), new RepairFieldAbility(500f, 160f, 240f){{
				healEffect = NHFx.healEffect;
				activeEffect = NHFx.activeEffect;
			}});
		}
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
		
		hurricane = new UnitType("hurricane"){{
				commandRadius = 240f;
				constructor = EntityMapping.map(3);
				weapons.add(
					new NHWeapon(){{
						predictTarget = false;
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
						shootSound = Sounds.beam;
						shootStatus = StatusEffects.slow;
						shootStatusDuration = bullet.lifetime + 40f;
					}},
					new NHWeapon("swepter"){{
						mirror = false;
						top = true;
						rotate = true;
						alternate = false;
						shake = 5f;
						shootY = 17f;
						reload = 300f;
						shots = 1;
						y = -40f;
						x = 0f;
						inaccuracy = 3.0F;
						ejectEffect = Fx.none;
						recoil = 4.4f;
						bullet = NHBullets.hurricaneType;
						shootSound = Sounds.laserblast;
					}},
					new NHWeapon("impulse"){{
						heatColor = NHColor.lightSkyBack;
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
						bullet = new BasicBulletType(7.4f, 60, NHBullets.STRIKE){
							@Override
							public float range(){return 440f;}
							
							{
								drawSize = 200;
								trailLength = 20;
								trailWidth = 3f;
								hitEffect = shootEffect = despawnEffect = NHFx.lightSkyCircleSplash;
								lifetime = 140f;
								pierce = pierceBuilding = true;
								width = 16f;
								height = 50f;
								backColor = lightColor = lightningColor = trailColor = NHColor.lightSkyBack;
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
				abilities.add(new ForceFieldAbility(120.0F, 6F, 20000.0F, 1200.0F), new RepairFieldAbility(800f, 160f, 240f){{
					healEffect = NHFx.healEffect;
					activeEffect = NHFx.activeEffect;
				}});
				commandLimit = 6;
				lowAltitude = true;
				itemCapacity = 500;
				health = 30000.0F;
				speed = 1F;
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
			@Override public void createIcons(MultiPacker packer){super.createIcons(packer); NHUnitOutline.createIcons(packer, this);}
		};
	}
}
