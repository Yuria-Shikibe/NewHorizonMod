package newhorizon.content;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.ShapePart;
import mindustry.entities.pattern.ShootHelix;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Bullet;
import mindustry.gen.EntityMapping;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.meta.BlockFlag;
import newhorizon.NewHorizon;
import newhorizon.expand.bullets.TrailFadeBulletType;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHPixmap;

public class NHUnitTypes{
	private static final Color OColor = Color.valueOf("565666");
	
	public static final byte OTHERS = Byte.MIN_VALUE, GROUND_LINE_1 = 0, AIR_LINE_1 = 1, AIR_LINE_2 = 2, ENERGY_LINE_1 = 3, NAVY_LINE_1 = 6;
	
	public static Weapon
		basicCannon;
	
	public static UnitType
			longinus; //Navy
	
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
	}
	
	public static void load(){
		loadWeapon();
		
		longinus = new UnitType("longinus"){{
			outlineColor = OColor;
			constructor = EntityMapping.map(3);
			lowAltitude = true;
			health = 10000.0F;
			speed = 0.5F;
			accel = 0.02F;
			drag = 0.025F;
			flying = true;
			circleTarget = false;
			hitSize = 50.0F;
			armor = 15.0F;
			engineOffset = 46f;
			engineSize = 12.0F;
			rotateSpeed = 0.65f;
			buildSpeed = 3f;
			
//			aiController = SniperAI::new;
			targetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.turret, BlockFlag.generator, null};
			
			for(int i : Mathf.signs){
				engines.add(new UnitEngine(21.5f * i, -43.5f, 5, -90 + 45 * i));
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
							progress = PartProgress.recoil;
							under = turretShading = true;
							mirror = true;
							x = 15;
							y = 26.5f;
							
							moveX = 5.5f;
							moveY = -5.5f;
						}});
					
						parts.add(new RegionPart("-ejector"){{
							progress = PartProgress.recoil;
							under = turretShading = true;
							mirror = true;
							x = 17;
							y = 14.5f;
							
							moveX = 5.5f;
							moveY = -5.5f;
						}});
					
						parts.add(new ShapePart(){{
							progress = PartProgress.smoothReload.inv().delay(0.65f).curve(Interp.pow3Out);
							y = shootY - 6f;
							sides = 4;
							color = NHColor.lightSkyBack;
							colorTo = NHColor.lightSkyFront;
							rotateSpeed = 1f;
							hollow = true;
							stroke = 0.0F;
							strokeTo = 2.0F;
							radius = 2.0F;
							radiusTo = 8f;
							moveY = 11f;
							layer = Layer.effect;
						}});
						
						parts.add(new RegionPart("-panel"){{
							progress = PartProgress.warmup;
							layerOffset = 0.005f;
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

							moveX = 10;
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
						rotate = true;
						
						top = false;
						mirror = false;
						shootSound = NHSounds.railGunBlast;
						soundPitchMax = 1.1f;
						soundPitchMin = 0.9f;
						
						layerOffset = -0.0005f;
						
						bullet = new TrailFadeBulletType(20f, 320f){{
							recoil = 0.095f;
							lifetime = 40f;
							trailLength = 200;
							trailWidth = 2F;
							trailNum = 1;
							keepVelocity = false;
							
							trailColor = hitColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
							frontColor = NHColor.lightSkyFront;
							width = 10f;
							height = 40f;
							
							hitSound = Sounds.plasmaboom;
							despawnShake = hitShake = 18f;
							
							lightning = 5;
							lightningLength = 6;
							lightningLengthRand = 8;
							lightningDamage = 70;
							
							smokeEffect = NHFx.square(hitColor, 80f, 8, 48f, 6f);
							shootEffect = NHFx.instShoot(backColor, frontColor);
							despawnEffect = NHFx.lightningHitLarge;
							hitEffect = new MultiEffect(NHFx.hitSpark(backColor, 75f, 24, 90f, 2f, 12f), NHFx.square45_6_45, NHFx.lineCircleOut(backColor, 18f, 20, 2));
							despawnHit = true;
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
				})
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
	}
}
