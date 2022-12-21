package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.UnitSorts;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBridge;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.ItemSource;
import mindustry.world.blocks.sandbox.LiquidSource;
import mindustry.world.blocks.sandbox.PowerVoid;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import newhorizon.NewHorizon;
import newhorizon.expand.block.adapt.AdaptUnloader;
import newhorizon.expand.block.adapt.AssignOverdrive;
import newhorizon.expand.block.adapt.LaserBeamDrill;
import newhorizon.expand.block.adapt.MultiCrafter;
import newhorizon.expand.block.commandable.AirRaider;
import newhorizon.expand.block.defence.*;
import newhorizon.expand.block.drawer.ArcCharge;
import newhorizon.expand.block.drawer.DrawFactories;
import newhorizon.expand.block.drawer.DrawPrinter;
import newhorizon.expand.block.drawer.DrawRotator;
import newhorizon.expand.block.special.HyperGenerator;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.expand.block.special.UnitSpawner;
import newhorizon.expand.block.turrets.MultTractorBeamTurret;
import newhorizon.expand.block.turrets.ShootMatchTurret;
import newhorizon.expand.block.turrets.SpeedupTurret;
import newhorizon.expand.block.turrets.Webber;
import newhorizon.expand.bullets.PosLightningType;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;

public class NHBlocks{

	//Load Mod Factories

	public static Block
		reinForcedItemSource, reinForcedLiquidSource,
		//delivery,
		zetaOre, xenMelter, hyperGenerator, fusionCollapser,
		chargeWall, chargeWallLarge, eoeUpgrader, jumpGate, jumpGateJunior, jumpGatePrimary,
		multiplePresstaniumFactory, presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
		zetaFactoryLarge, zetaFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, irayrondPanelFactorySmall,
		setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory, metalOxhydrigenFactoryLarge,
		sandCracker,
		thermoCorePositiveFactory, thermoCoreNegativeFactory, thermoCoreFactory, irdryonVault,
	
		//Turrets
		shockWaveTurret, usualUpgrader, bloodStar, pulseShotgun, beamLaserTurret,
		blaster, endOfEra, thurmix, argmot, thermoTurret, railGun, divlusion,
		blastTurret, empTurret, gravity, multipleLauncher, pulseLaserTurret, multipleArtillery,
		antiMatterTurret, atomSeparator, eternity, synchro,

		//Liquids
		irdryonTank,
		//Liquids factories
		irdryonFluidFactory, xenBetaFactory, xenGammaFactory, zetaFluidFactory, oilRefiner, waterInstancer,
		//walls
		insulatedWall, setonWall, setonWallLarge, heavyDefenceWall, heavyDefenceWallLarge, heavyDefenceDoor, heavyDefenceDoorLarge, laserWall, ancientLaserWall,
		//Distributions
		towardGate, rapidUnloader, liquidAndItemBridge, remoteRouter, multiArmorConveyor, multiConveyor, multiEfficientConveyor,
		multiJunction, multiRouter, multiConduit, multiSteelItemBridge, multiSteelLiquidBridge,
		//Drills
		largeWaterExtractor, beamDrill,
		//Powers
		armorPowerNode, armorBatteryLarge, radiationGenerator, zetaGenerator, hugeBattery, heavyPowerNode,
		//Defence
		largeMendProjector, shapedWall, assignOverdrive, antiBulletTurret, largeShieldGenerator, fireExtinguisher, webber,
		//Special
		playerJumpGate, gravityTrap, hyperspaceWarper, bombLauncher, scrambler, airRaider, configurer, shieldProjector, unitIniter, remoteStorage,
		disposePowerVoid, gravityTrapSmall,
	
		//Env
		quantumField, quantumFieldDeep, quantumFieldDisturbing, metalUnit, metalTower, metalGround, metalGroundQuantum,
		metalGroundHeat, conglomerateRock, conglomerateWall
		;
	
	private static void loadEnv(){
		conglomerateWall = new StaticWall("conglomerate-wall"){{
			variants = 4;
			mapColor = Color.valueOf("858585");
		}};
		
		conglomerateRock = new Floor("conglomerate-rock", 3){{
			mapColor = Color.valueOf("565557");
			blendGroup = Blocks.stone;
		}};
		
		metalGroundHeat = new Floor("metal-ground-heat", 3){{
			mapColor = Pal.darkerGray.cpy().lerp(NHColor.darkEnr, 0.5f);
			wall = metalUnit;
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.heat, 1.25f);
			attributes.set(Attribute.light, 1f);
			attributes.set(Attribute.spores, -1f);
			walkSound = NHSounds.metalWalk;
			walkSoundVolume = 0.05f;
			speedMultiplier = 1.25f;
			
			liquidMultiplier = 0.8f;
			liquidDrop = NHLiquids.quantumLiquid;
			lightColor = NHColor.darkEnrColor;
			emitLight = true;
			lightRadius = 35f;
		}};
		
		quantumField = new Floor("quantum-field", 8){{
			status = NHStatusEffects.quantization;
			statusDuration = 60f;
			speedMultiplier = 1.15f;
			liquidDrop = NHLiquids.quantumLiquid;
			isLiquid = true;
			cacheLayer = CacheLayer.water;
			attributes.set(Attribute.light, 2f);
			emitLight = true;
			lightRadius = 32f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.1f);
			blendGroup = this;
			
			attributes.set(Attribute.heat, 1.25f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
//			cacheLayer = NHContent.quantum;
		}};
		
		quantumFieldDeep = new Floor("quantum-field-deep", 0){{
			drownTime = 180f;
			status = NHStatusEffects.quantization;
			statusDuration = 240f;
			speedMultiplier = 1.3f;
			liquidDrop = NHLiquids.quantumLiquid;
			isLiquid = true;
			cacheLayer = CacheLayer.water;
			attributes.set(Attribute.light, 3f);
			emitLight = true;
			lightRadius = 40f;
			liquidMultiplier = 2f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f);
			blendGroup = this;
			
			attributes.set(Attribute.heat, 1.5f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
//			cacheLayer = NHContent.quantum;
		}};
		
		quantumFieldDisturbing = new Floor("quantum-field-disturbing", 0){{
			drownTime = 180f;
			status = NHStatusEffects.quantization;
			statusDuration = 240f;
			speedMultiplier = 1.3f;
			liquidDrop = NHLiquids.quantumLiquid;
			isLiquid = true;
			attributes.set(Attribute.light, 3f);
			emitLight = true;
			lightRadius = 40f;
			liquidMultiplier = 2f;
			lightColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.2f);
			blendGroup = this;
			
			attributes.set(Attribute.heat, 1.5f);
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.spores, -1f);
			
			cacheLayer = NHContent.quantum;
			
			details = "Has unique shader.";
		}
			
			@Override
			public void load(){
				super.load();
				
				editorIcon = fullIcon = uiIcon = region = Core.atlas.find(NewHorizon.name("quantum-field-disturbing-icon"));
			}
		};
		
		metalUnit = new StaticWall("metal-unit"){{
			variants = 6;
		}};
		
		metalTower = new StaticWall("metal-tower"){{
			variants = 3;
		}};
		
		metalGround = new Floor("metal-ground", 6){{
			mapColor = Pal.darkerGray;
			wall = metalUnit;
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.heat, 0);
			attributes.set(Attribute.light, 0);
			attributes.set(Attribute.spores, -1f);
			walkSound = NHSounds.metalWalk;
			walkSoundVolume = 0.05f;
			speedMultiplier = 1.25f;
		}};
		
		metalGroundQuantum = new Floor("metal-ground-quantum", 2){{
			mapColor = Pal.darkerMetal;
			wall = metalUnit;
			blendGroup = metalGround;
			attributes.set(Attribute.water, -1f);
			attributes.set(Attribute.oil, -1f);
			attributes.set(Attribute.heat, 0.2f);
			attributes.set(Attribute.light, 0);
			attributes.set(Attribute.spores, -1f);
			walkSound = NHSounds.metalWalk;
			walkSoundVolume = 0.05f;
			speedMultiplier = 1.25f;
			
			emitLight = true;
			lightColor = NHColor.darkEnrColor;
			lightRadius = 4.4f;
		}
			@Override
			public void load(){
				super.load();
				region = Core.atlas.find(NewHorizon.name("metal-ground1"));
			}
		};
	}
	
	private static void loadExperiments(){

	}
	
	private static void loadTurrets(){
		webber = new Webber("webber"){{
			size = 3;
			
			moveInterp = Interp.pow3In;
			status = NHStatusEffects.scrambler;
			shootLength = 22f;
			laserColor = NHColor.thermoPst;
			requirements(Category.turret, ItemStack.with(Items.plastanium, 85, NHItems.juniorProcessor, 55, NHItems.presstanium, 80));
			hasPower = true;
			cal = d -> 4 * Interp.pow4Out.apply(d) - 3;
			scaledForce = 0;
			force = 40f;
			range = 280.0F;
			damage = 0.3F;
			scaledHealth = 160.0F;
			rotateSpeed = 10.0F;
			consumePower(6.0F);
		}};
		
		gravity = new MultTractorBeamTurret("gravity"){{
			size = 3;
			requirements(Category.turret, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
			health = 1020;
			maxAttract = 8;
			shootCone = 60f;
			range = 300f;
			hasPower = true;
			force = 40.0F;
			scaledForce = 8.0F;
			shootLength = size * tilesize / 2f - 3;
			damage = 0.15F;
			rotateSpeed = 6f;
			consumePowerCond(6.0F, (MultTractorBeamBuild e) -> e.target != null);
		}};
		
		argmot = new SpeedupTurret("argmot"){{
			shoot = new ShootAlternate(){{
				spread = 7f;
			}};
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart(){{
					drawRegion = false;
					mirror = true;
					moveY = -2.75f;
					progress = PartProgress.recoil;
					children.add(new RegionPart("-shooter"){{
						heatLayerOffset = 0.001f;
						heatColor = NHColor.thurmixRed.cpy().a(0.85f);
						progress = PartProgress.warmup;
						mirror = outline = true;
						moveX = 2f;
						moveY = 2f;
						moveRot = 11.25f;
					}});
				}}, new RegionPart("-up"){{
					layerOffset = 0.3f;
					
					turretHeatLayer += layerOffset + 0.1f;
					
					heatColor = NHColor.thurmixRed.cpy().a(0.85f);
					outline = false;
				}});
			}};
			
			warmupMaintainTime = 120f;
			
			health = 960;
			requirements(Category.turret, with(NHItems.multipleSteel, 120, NHItems.juniorProcessor, 80, Items.plastanium, 120));
			maxSpeedupScl = 9f;
			speedupPerShoot = 0.3f;
			hasLiquids = true;
			coolant = new ConsumeCoolant(0.15f);
			consumePowerCond(8f, TurretBuild::isActive);
			size = 3;
			range = 200;
			reload = 60f;
			shootCone = 24f;
			shootSound = NHSounds.laser3;
			shootType = new PosLightningType(75f){{
				lightningColor = hitColor = NHColor.lightSkyBack;
				maxRange = rangeOverride = 250f;
				hitEffect = NHFx.hitSpark;
				smokeEffect = Fx.shootBigSmoke2;
			}};
		}};
		
		eternity = new ItemTurret("eternity"){{
			armor = 30;
			size = 16;
			outlineRadius = 7;
			range = 1200;
			heatColor = NHColor.darkEnrColor;
			unitSort = UnitSorts.strongest;
			
			coolant = consumeCoolant(0.8F);
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-side"){{
					under = turretShading = mirror = true;
					moveX = 6f;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					mirror = true;
					layerOffset = -0.001f;
					moveX = 10f;
					moveY = 45f;
					y = 10f;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					mirror = true;
					layerOffset = -0.0035f;
					moveX = -9f;
					moveY = 7f;
					y = -2f;
					x = 8;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}}, new RegionPart("-side-down"){{
					under = mirror = true;
					layerOffset = -0.002f;
					moveY = -33f;
					y = -33f;
					x = 14;
					progress = PartProgress.smoothReload.inv().curve(Interp.pow3Out);
				}});
				
				parts.add(new ArcCharge(){{
					progress = PartProgress.smoothReload.inv().curve(Interp.pow5Out);
					color = NHColor.darkEnrColor;
					chargeY = t -> -35f;
					shootY = t -> 90 * curve.apply(1 - t.smoothReload);
				}});
				
//				parts.add(new FlarePart(){{
//					layer = Layer.effect;
//					rotMove = 360;
//					rotation = 45f;
//					followRotation = true;
//					y = 70f;
//					stroke = 7;
//					radius = 0;
//					radiusTo = 68;
//					color1 = NHColor.darkEnrColor;
//					color2 = NHColor.darkEnrFront;
//					progress = DrawPart.PartProgress.smoothReload.inv().curve(Interp.pow10In).delay(0.2f);
//				}
//					@Override
//					public void draw(PartParams params){
//						float z = Draw.z();
//						if(layer > 0) Draw.z(layer);
//
//						float prog = progress.getClamp(params);
//						int i = params.sideOverride == -1 ? 0 : params.sideOverride;
//
//						float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
//						Tmp.v1.set(x * sign, y).rotate(params.rotation - 90);
//
//						float
//								rx = params.x + Tmp.v1.x,
//								ry = params.y + Tmp.v1.y,
//								rot = (followRotation ? params.rotation : 0f) + rotMove * prog + rotation + Time.time * 0.86f,
//								rad = radiusTo < 0 ? radius : Mathf.lerp(radius, radiusTo, prog);
//
//						Draw.color(color1);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke, rad, j * 360f / sides + rot);
//						}
//
//						Draw.color(color2);
//						for(int j = 0; j < sides; j++){
//							Drawf.tri(rx, ry, stroke * innerScl, rad * innerRadScl, j * 360f / sides + rot);
//						}
//
//						Draw.color();
//						Draw.z(z);
//					}
//				});
			}};
			shoot = new ShootPattern();
			inaccuracy = 0;
			
			ammoPerShot = 10;
			coolantMultiplier = 0.8f;
			canOverdrive = false;
			rotateSpeed = 0.25f;
			
			float chargeCircleFrontRad = 12f;
			
			shootEffect = new Effect(120f, 2000f, e -> {
				float scl = 1f;
				if(e.data instanceof Float)scl *= (float)e.data;
				Draw.color(heatColor, Color.white, e.fout() * 0.25f);
				
				float rand = Mathf.randomSeed(e.id, 60f);
				float extend = Mathf.curve(e.fin(Interp.pow10Out), 0.075f, 1f) * scl;
				float rot = e.fout(Interp.pow10In);
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl,200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i - 45);
				}
				
				for(int i : Mathf.signs){
					DrawFunc.tri(e.x, e.y, chargeCircleFrontRad * 1.2f * e.foutpowdown() * scl,200 + 500 * extend, e.rotation + (90 + rand) * rot + 90 * i + 45);
				}
			});
			
			smokeEffect = new Effect(50, e -> {
				Draw.color(heatColor);
				Lines.stroke(e.fout() * 5f);
				Lines.circle(e.x, e.y, e.fin() * 300);
				Lines.stroke(e.fout() * 3f);
				Lines.circle(e.x, e.y, e.fin() * 180);
				Lines.stroke(e.fout() * 3.2f);
				Angles.randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
					lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
				});
				Draw.color(Color.white);
				Drawf.light(e.x, e.y, e.fout() * 120, heatColor, 0.7f);
			});
			
			recoil = 18f;
			shake = 80f;
			shootSound = Sounds.laserblast;
			health = 800000;
			shootCone = 5f;
			maxAmmo = 50;
			consumePowerCond(800f, TurretBuild::isActive);
			reload = 1800f;
			
			ammo(NHItems.darkEnergy, NHBullets.eternity);
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 5000, NHItems.darkEnergy, 2000));
		}};
		
		synchro = new ItemTurret("synchro"){{
			size = 3;
			health = 1420;
			reload = 12f;
			inaccuracy = 0.75f;
			
			recoil = 0.5f;
			
			coolant = consumeCoolant(0.2F);
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-shooter"){{
					under = true;
					outline = true;
					moveY = -3f;
					progress = PartProgress.recoil;
				}});
			}};
			
			coolantMultiplier = 2f;
			
			velocityRnd = 0.075f;
			unitSort = UnitSorts.weakest;
			
			range = 360f;
			
			shootSound = NHSounds.synchro;
			
			shoot = new ShootMulti(new ShootPattern(), new ShootBarrel(){{
				barrels = new float[]{-6.5f, 3f, 0f};
			}}, new ShootBarrel(){{
				barrels = new float[]{6.5f, 3f, 0f};
			}});
			
			ammo(
					NHItems.zeta, NHBullets.synchroZeta,
					NHItems.fusionEnergy, NHBullets.synchroFusion,
					NHItems.thermoCorePositive, NHBullets.synchroThermoPst,
					Items.phaseFabric, NHBullets.synchroPhase
			);
			
			ammoPerShot = 1;
			maxAmmo = 80;
			
			requirements(Category.turret, BuildVisibility.shown, with(Items.phaseFabric, 20, NHItems.metalOxhydrigen, 90, NHItems.juniorProcessor, 60, NHItems.zeta, 120, Items.plastanium, 80));
		}};
		
		antiBulletTurret = new PointDefenseTurret("anti-bullet-turret"){{
			health = 1080;
			size = 3;
			
			coolant = consumeCoolant(0.1F);
			
			color = lightColor = NHColor.lightSkyBack;
			beamEffect = Fx.chainLightning;
			hitEffect = NHFx.square45_4_45;
			shootEffect = NHFx.shootLineSmall(color);
			shootSound = NHSounds.gauss;
			
			range = 280f;
			
			hasPower = true;
			consumePowerCond(8f, (PointDefenseBuild b) -> b.target != null);
			
			shootLength = 5f;
			bulletDamage = 80f;
			reload = 6f;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 90, NHItems.juniorProcessor, 60, NHItems.presstanium, 120, NHItems.zeta, 120, Items.graphite, 80));
		}};
		
		pulseShotgun = new ItemTurret("pulse-shotgun"){{
			health = 960;
			range = 200;
			smokeEffect = Fx.shootBigSmoke;
			
			coolant = consumeCoolant(0.1F);
			
			shoot = new ShootSpread(){{
				shots = 12;
				shotDelay = 2f;
				spread = 1.25f;
			}};

			reload = 90f;
			recoil = 3f;
			shootCone = 30f;
			inaccuracy = 4f;
			size = 2;
			shootSound = Sounds.shootSnap;
			shake = 3f;
			ammo(
					Items.titanium, new BasicBulletType(5, 24){{
						width = 8f;
						height = 25f;
						hitColor = backColor = lightColor = trailColor = Items.titanium.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 12f, 2f);
						ammoMultiplier = 8;
						pierceArmor = true;
					}},
					
					Items.plastanium, new BasicBulletType(5, 26){{
						width = 8f;
						height = 25f;
						fragBullets = 4;
						fragBullet = new BasicBulletType(2, 26){{
							width = 3f;
							lifetime = 10f;
							height = 12f;
							ammoMultiplier = 12;
							hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
							frontColor = backColor.cpy().lerp(Color.white, 0.35f);
							hitEffect = NHFx.lightningHitSmall(backColor);
							shootEffect = despawnEffect = NHFx.square45_4_45;
						}};
						fragAngle = 130f;
						fragVelocityMax = 1.1f;
						fragVelocityMin = 0.5f;
						fragLifeMax = 1.25f;
						fragLifeMin = 0.25f;
						ammoMultiplier = 12;
						hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					NHItems.zeta, new BasicBulletType(5, 18){{
						width = 8f;
						height = 25f;
						lightning = 2;
						lightningLength = 2;
						lightningLengthRand = 6;
						lightningDamage = damage;
						status = StatusEffects.shocked;
						statusDuration = 15f;
						ammoMultiplier = 12;
						lightningColor = hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					Items.pyratite, new BasicBulletType(5, 18){{
						width = 8f;
						height = 25f;
						incendAmount = 4;
						incendChance = 0.25f;
						incendSpread = 12f;
						status = StatusEffects.burning;
						statusDuration = 15f;
						ammoMultiplier = 12;
						hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						despawnEffect = Fx.blastExplosion;
						shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}},
					
					Items.blastCompound, new BasicBulletType(5, 22){{
						width = 8f;
						height = 25f;
						status = StatusEffects.blasted;
						statusDuration = 15f;
						splashDamageRadius = 12f;
						splashDamage = damage;
						ammoMultiplier = 8;
						hitColor = backColor = lightColor = trailColor = Items.blastCompound.color.cpy().lerp(Color.white, 0.1f);
						frontColor = backColor.cpy().lerp(Color.white, 0.35f);
						hitEffect = NHFx.crossBlast(hitColor, height + width);
						despawnEffect = Fx.blastExplosion;
						shootEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
					}}
			);
			
			limitRange();
			maxAmmo = 120;
			ammoPerShot = 1;
			
			requirements(Category.turret, with(Items.copper, 30, Items.graphite, 40, NHItems.presstanium, 50, Items.lead, 60));
		}};
		
		atomSeparator = new LaserTurret("atom-separator"){{
			health = 12000;
			range = 360f;
			shootEffect = NHFx.hugeSmokeGray;
			shootCone = 20.0F;
			recoil = 6.0F;
			size = 5;
			shake = 4.0F;
			reload = 60.0F;
			
			accurateDelay = false;
			rotateSpeed = 3f;
			firingMoveFract = 0.15F;
			shootDuration = 200.0F;
			shootSound = Sounds.laserbig;
			loopSound = Sounds.beam;
			loopSoundVolume = 2.0F;
			shootType = NHBullets.atomSeparator;
			
			coolant = consumeCoolant(0.3F);
			consumePower(30.0F);
			unitSort = (u, x, y) -> u.speed();
			requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 200, NHItems.zeta, 150, NHItems.presstanium, 250, NHItems.metalOxhydrigen, 150));
		}};
		
		bloodStar = new ItemTurret("blood-star"){{
			size = 5;
			coolant = consumeCoolant(0.1F);
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 230, NHItems.zeta, 300, NHItems.seniorProcessor, 200, NHItems.presstanium, 300, Items.thorium, 600));
			recoil = 5f;
			reload = 120f;
			range = 520f;
			unitSort = (u, x, y) -> -u.hitSize();
			shootSound = Sounds.laserblast;
			inaccuracy = 0f;
			shootCone = 15f;
			heatColor = Items.surgeAlloy.color.cpy().lerp(Color.white, 0.2f);
			consumePowerCond(12f, TurretBuild::isActive);
			
			ammo(NHItems.thermoCorePositive,
					new BasicBulletType(4, 500, "large-bomb"){{
						lightning = 6;
						lightningCone = 360;
						lightningLengthRand = lightningLength = 12;
						splashDamageRadius = 60f;
						splashDamage = lightningDamage = 0.5f * damage;
						
						trailColor = backColor = lightColor = lightningColor = heatColor;
						frontColor = Color.white;
						
						status = NHStatusEffects.emp1;
						statusDuration = 30f;
						
						spin = 3f;
						trailLength = 40;
						trailWidth = 2.5f;
						lifetime = 140f;
						shrinkX = shrinkY = 0;
						hitSound = Sounds.explosionbig;
						drawSize = 60f;
						hitShake = despawnShake = 6f;
						shootEffect = NHFx.instShoot(backColor, frontColor);
						smokeEffect = Fx.shootBigSmoke2;
						hitEffect = new Effect(50, e -> {
							Draw.color(backColor);
							Fill.circle(e.x, e.y, e.fout() * height / 1.5f);
							Lines.stroke(e.fout() * 3f);
							Lines.circle(e.x, e.y, e.fin() * 80);
							Lines.stroke(e.fout() * 2f);
							Lines.circle(e.x, e.y, e.fin() * 50);
							Angles.randLenVectors(e.id, 35, 18 + 100 * e.fin(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 12 + 4));
							
							Draw.color(frontColor);
							Fill.circle(e.x, e.y, e.fout() * height / 2f);
						});
						despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(backColor, 120f), NHFx.instHit(backColor, 3, 80f));
						height = width = 40;
					}
						
						@Override
						public void draw(Bullet b){
							super.draw(b);
							
							float f = Mathf.curve(b.fout(), 0, 0.05f);
							float f2 = Mathf.curve(b.fin(), 0, 0.1f);
							Draw.color(backColor);
							
							float fi = Mathf.randomSeed(b.id, 360f);
							
							for(int i : Mathf.signs){
								DrawFunc.tri(b.x, b.y, 6 * f2 * f, 80 * f2 * f, fi + (i + 1) * 90 + Time.time * 2);
								DrawFunc.tri(b.x, b.y, 6 * f2 * f, 65 * f2 * f, fi + (i + 1) * 90 - Time.time * 2 + 90);
							}
						}
					}
			);
		}};
		
		multipleLauncher = new ItemTurret("multiple-launcher"){{
			size = 3;
			health = 1250;
			coolant = consumeCoolant(0.2F);
			requirements(Category.turret, ItemStack.with(Items.plastanium, 60, NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30));
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-shooter"){{
					mirror = true;
					progress = PartProgress.warmup.compress(0, 0.75f);
					moveX = 0.75f;
					moveY = -1.5f;
				}});
			}};
			
			ammo(
					Items.titanium, NHBullets.missileTitanium,
					Items.thorium, NHBullets.missileThorium,
					NHItems.zeta, NHBullets.missileZeta,
					Items.graphite, NHBullets.missileNormal,
					NHItems.presstanium, NHBullets.missileStrike
			);
			smokeEffect = Fx.shootSmallFlame;
			shootEffect = Fx.shootBig2;
			recoil = 3f;
			range = 280f;
			reload = 90f;
			
			shoot = new ShootBarrel(){{
				barrels = new float[]{
					-4, -2, 0,
					0, -3, 0,
					4, -2, 0
				};
				
				shotDelay = 2f;
				shots = 15;
			}};
			
			maxAmmo = 160;
			ammoPerShot = 1;
			ammoEjectBack = 6f;
			inaccuracy = 9f;
			
			xRand = tilesize * size / 6.5f;
			shootSound = Sounds.missile;
			coolantMultiplier = 0.85f;
		}};
		
		thermoTurret = new PowerTurret("thermo-turret"){{
			size = 1;
			health = 320;
			
			coolant = consumeCoolant(0.05F);
			requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
			shootType = new BasicBulletType(6.5f, 18f){{
				hitEffect = new Effect(12.0F, (e) -> {
					Draw.color(Pal.lancerLaser, Color.white, e.fout() * 0.75f);
					Lines.stroke(e.fout() * 1.5F);
					Angles.randLenVectors(e.id, 3, e.finpow() * 17.0F, e.rotation, 360.0F, (x, y) -> {
						float ang = Mathf.angle(x, y);
						Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4.0F + 1.0F);
					});
				});
				trailWidth = 1.25f;
				trailLength = 15;
				
				knockback = 0.5f;
				trailColor = backColor = hitColor = Pal.lancerLaser;
				frontColor = Color.white;
				lifetime = 50f;
				homingDelay = 1f;
				homingPower = 0.2f;
				homingRange = 120f;
				status = StatusEffects.shocked;
				collidesGround = false;
				statusDuration = 30f;
				width = 5f;
				drawSize = 120f;
				height = 22f;
			}};
			consumePower(3.5f);
			
			shoot = new ShootPattern(){{
				shots = 5;
				shotDelay = 6f;
			}};
			
			inaccuracy = 3f;
			reload = 75f;
			shootCone = 50.0F;
			rotateSpeed = 8.0F;
			targetGround = false;
			range = 200.0F;
			shootEffect = Fx.lightningShoot;
			smokeEffect = Fx.shootSmallSmoke;
			heatColor = Color.red;
			recoil = 1.0F;
			shootSound = NHSounds.thermoShoot;
		}};
		
		endOfEra = new ShootMatchTurret("end-of-era"){{
			recoil = 5f;
			armor = 15;
			
			shootCone = 15f;
			
			unitSort = UnitSorts.strongest;
			
			warmupMaintainTime = 30f;
			coolant = consumeCoolant(0.5F);
			moveWhileCharging = false;
			
			shootWarmupSpeed = 0.035f;
			
			drawer = new DrawTurret(){{
				parts.add(new RegionPart("-charger"){{
					under = mirror = true;
					layerOffset = -0.002f;
					moveX = 14f;
					moveY = -9f;
					moveRot = -45f;
					y = -4f;
					x = 16f;
					
					progress = PartProgress.warmup;
				}}, new RegionPart(){{
					drawRegion = false;
					mirror = true;
					heatColor = Color.clear;
					progress = PartProgress.recoil.min(PartProgress.warmup);
					moveY = -10f;
					children.add(new RegionPart("-wing"){{
						under = mirror = true;
						moveRot = 12.5f;
						moveY = 14f;
						moveX = 4f;
						heatColor = NHColor.darkEnrColor;
						progress = PartProgress.warmup;
					}});
				}}, new RegionPart("-shooter"){{
					outline = true;
					layerOffset = 0.001f;
					moveY = -12f;
					heatColor = NHColor.darkEnrColor;
					progress = PartProgress.warmup.blend(PartProgress.recoil, 0.5f);
				}});
			}};
			
			shoot = new ShootPattern(){{
				firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
			}};
			
			chargeSound = NHSounds.railGunCharge;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 2000));
			ammo(NHItems.darkEnergy, NHBullets.arc_9000, NHItems.upgradeSort, new BulletType(){{
				hitColor = lightColor = trailColor = NHColor.darkEnrFront;
				spawnUnit = NHBullets.airRaidMissile;
				chargeEffect = NHFx.railShoot(NHColor.darkEnrColor, 800f, 18, NHFx.darkEnergyChargeBegin.lifetime, 25);
				shootEffect = NHFx.instShoot(NHColor.darkEnrColor, NHColor.darkEnrFront);
				smokeEffect = new Effect(180f, 300f, b -> {
					float intensity = 2f;
					
					Rand rand = Fx.rand;
					
					color(b.color, 0.7f);
					for(int i = 0; i < 4; i++){
						rand.setSeed(b.id*2 + i);
						float lenScl = rand.random(0.5f, 1f);
						int fi = i;
						b.scaled(b.lifetime * lenScl, e -> {
							randLenVectors(e.id + fi - 1, e.fin(Interp.pow4Out), (int)(2 * intensity), 35f * intensity, e.rotation, 20, (x, y, in, out) -> {
								float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
								float rad = fout * ((2f + intensity) * 1.75f);
								
								Fill.circle(e.x + x, e.y + y, rad);
								Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
							});
						});
					}
				});
			}});
			shooter(NHItems.upgradeSort, new ShootBarrel(){{
				barrels = new float[]{
					22, -12, 25,
					-22, -12, -25,
					0, -22, 0,
				};
				firstShotDelay = NHFx.darkEnergyChargeBegin.lifetime;
				shots = 3;
				shotDelay = 12f;
			}});
			
			shootCone = 20f;
			rotateSpeed = 0.75f;
			ammoPerShot = 4;
			maxAmmo = 20;
			size = 8;
			health = 15000;
			hasItems = true;
			heatColor = NHColor.darkEnrColor;
			consumePower(30f);
			reload = 300f;
			range = 800f;
			inaccuracy = 1f;
			shootCone = 45f;
			shootSound = Sounds.laserbig;
		}};
	}
	
	private static void loadFactories(){
		sandCracker = new MultiCrafter("sand-cracker"){{
			size = 2;
			requirements(Category.crafting, ItemStack.with(Items.lead, 40, Items.copper, 60, Items.graphite, 45));
//			NHTechTree.add(Blocks.pulverizer, this);
			health = 320;
			craftTime = 45f;
			itemCapacity = 20;
			hasPower = hasItems = true;
			craftEffect = NHFx.hugeSmokeGray;
			updateEffect = new Effect(80f, e -> {
				Fx.rand.setSeed(e.id);
				Draw.color(Color.lightGray, Color.gray, e.fin());
				Angles.randLenVectors(e.id, 4, 2.0F + 12.0F * e.fin(Interp.pow3Out), (x, y) -> {
					Fill.circle(e.x + x, e.y + y, e.fout() * Fx.rand.random(1, 2.5f));
				});
			}).layer(Layer.blockOver + 1);
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawFrames(), new DrawArcSmelt(), new DrawDefault());
			consumePower(1.5f);
			setExchangeMap(Items.copper, 1, 1, Items.lead, 1, 1, Items.titanium, 1, 2, Items.thorium, 1, 3, Items.scrap, 2, 5);
			setOutput(Items.sand);
		}};
		
		multiplePresstaniumFactory = new GenericCrafter("multiple-presstanium-factory"){{
			size = 3;
			health = 540;
			requirements(Category.crafting, ItemStack.with(NHItems.presstanium, 80, NHItems.juniorProcessor, 60, Items.thorium, 80));
			craftTime = 60f;
			consumePower(5);
			consumeItems(with(Items.titanium, 6, Items.graphite, 2));
			consumeLiquid(NHLiquids.zetaFluid, 0.125f);
			outputItems = with(NHItems.presstanium, 8, Items.scrap, 1);
			
			itemCapacity = 30;
			liquidCapacity = 30;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.zetaFluid), new DrawRotator(), new DrawRegion("-middle"), new DrawCrucibleFlame(){{
				alpha = 0.375f;
				particles = 20;
				particleSize = 2f;
				particleRad = 6f;
				flameColor = NHLiquids.zetaFluid.color;
				midColor = NHLiquids.zetaFluid.color.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
		}};
		
		oilRefiner = new GenericCrafter("oil-refiner"){{
			size = 2;
			requirements(Category.production, ItemStack.with(Items.metaglass, 30, NHItems.juniorProcessor, 20, Items.copper, 60, NHItems.metalOxhydrigen, 45));
			//NHTechTree.add(Blocks.oilExtractor, this);
			health = 200;
			craftTime = 90f;
			liquidCapacity = 60f;
			itemCapacity = 20;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.oil), new DrawDefault(), new DrawRegion("-top"));
			consumePower(5f);
			consumeItems(new ItemStack(Items.sand, 5));
			outputLiquid = new LiquidStack(Liquids.oil, 15f / 60f);
		}};
		
		waterInstancer = new GenericCrafter("water-instancer"){{
			size = 1;
			updateEffect = Fx.smeltsmoke;
			consumePower(0.5f);
			consumeLiquid(NHLiquids.quantumLiquid, 0.1f);
			outputLiquid = new LiquidStack(Liquids.water, 12f / 60f);
			craftTime = 30f;
			requirements(Category.crafting, BuildVisibility.shown, with(Items.metaglass, 15, Items.copper, 30, NHItems.presstanium, 20));
			//NHTechTree.add(Blocks.mechanicalPump, this);
		}};
		
		xenMelter = new GenericCrafter("xen-melter"){{
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			itemCapacity = 12;
			liquidCapacity = 24;
			craftTime = 60f;
			drawer = new DrawMulti(new DrawDefault(), new DrawLiquidTile(NHLiquids.xenAlpha), new DrawRegion("-top"));
			
			craftEffect = NHFx.lightSkyCircleSplash;
			updateEffect = Fx.smeltsmoke;
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.juniorProcessor, 35, NHItems.metalOxhydrigen, 50, Items.thorium, 30, NHItems.presstanium, 25));
			consumePower(3f);
			consumeItems(new ItemStack(NHItems.metalOxhydrigen, 4), new ItemStack(NHItems.zeta, 4));
			outputLiquid = new LiquidStack(NHLiquids.xenAlpha, 12f / 60f);
		}};
		
		zetaOre = new OreBlock("ore-zeta"){{
			oreDefault = true;
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop = NHItems.zeta;
			localizedName = itemDrop.localizedName;
			mapColor.set(itemDrop.color);
			useColor = true;
		}};
		
		
		presstaniumFactory = new GenericCrafter("presstanium-factory") {
			{
				Color color = NHColor.lightSkyBack.cpy().lerp(Color.lightGray, 0.3f);
				
				drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
					midColor = flameColor = color;
					flameRad /= 1.585f;
					particleStroke /= 1.35f;
					particleLen /= 1.25f;
				}}, new DrawDefault());
				
				requirements(Category.crafting, with(Items.silicon, 45, Items.lead, 115, Items.graphite, 25, Items.titanium, 100));
				hasItems = hasPower = true;
				craftTime = 60f;
				outputItem = new ItemStack(NHItems.presstanium, 2);
				size = 2;
				health = 320;
				updateEffect = Fx.smeltsmoke;
				craftEffect = NHFx.square(color, 32f, 5, 22f, 4);
				
				consumePower(3f);
				consumeItems(new ItemStack(Items.titanium, 2), new ItemStack(Items.graphite, 1));
			}
		};
		
		zetaFactorySmall = new GenericCrafter("small-zeta-crystal-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 15, Items.lead, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 75f;
				outputItem = new ItemStack(NHItems.zeta, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.smeltsmoke;
				updateEffect = Fx.smoke;
				
				consumePower(1.5f);
				consumeItem(Items.thorium, 2);
			}
		};
		
		thermoCorePositiveFactory = new GenericCrafter("thermo-core-positive-factory") {
			{
				requirements(Category.crafting, with(NHItems.seniorProcessor, 15, NHItems.presstanium, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 120f;
				outputItem = new ItemStack(NHItems.thermoCorePositive, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.formsmoke;
				consumePower(3f);
				consumeItem(NHItems.thermoCoreNegative, 1);
			}
		};
		
		thermoCoreNegativeFactory = new GenericCrafter("thermo-core-negative-factory") {
			{
				requirements(Category.crafting, with(NHItems.seniorProcessor, 15, NHItems.presstanium, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 120f;
				outputItem = new ItemStack(NHItems.thermoCoreNegative, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.formsmoke;
				consumePower(3f);
				consumeItem(NHItems.thermoCorePositive, 1);
			}
		};
		
		darkEnergyFactory = new GenericCrafter("dark-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 60, NHItems.setonAlloy, 30, NHItems.seniorProcessor, 60));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.darkEnergy, 2);
				craftTime = 90f;
				size = 2;
				hasPower = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.darkEnergy.color));
				
				consumeItems(new ItemStack(NHItems.thermoCoreNegative, 1), new ItemStack(NHItems.thermoCorePositive, 1));
				consumePower(20f);
			}
		};
		
		fusionEnergyFactory = new GenericCrafter("fusion-core-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 50, Items.thorium, 60, Items.graphite, 30));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.fusionEnergy, 3);
				craftTime = 90f;
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.fusionEnergy.color));
				consumeLiquid(Liquids.water, 0.3f);
				consumeItems(new ItemStack(NHItems.presstanium, 2), new ItemStack(NHItems.zeta, 6));
				consumePower(6f);
			}
		};
		
		irayrondPanelFactory = new GenericCrafter("irayrond-panel-factory") {
			{
				requirements(Category.crafting, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 50, Items.plastanium, 60, Items.surgeAlloy, 75, Items.graphite, 30));
				craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 7, 4f + e.fin() * 18f, (x, y) -> {
					Draw.color(NHItems.irayrondPanel.color);
					Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
				}));
				outputItem = new ItemStack(NHItems.irayrondPanel, 4);
				craftTime = 120f;
				health = 800;
				size = 4;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.irayrondPanel.color));
				consumeLiquid(NHLiquids.xenAlpha, 0.1f);
				consumeItems(new ItemStack(NHItems.presstanium, 4), new ItemStack(Items.surgeAlloy, 2));
				consumePower(2f);
			}
		};
		
		juniorProcessorFactory = new GenericCrafter("processor-junior-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 40, NHItems.presstanium, 30, Items.copper, 25, Items.lead, 25));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.juniorProcessor, 3);
				craftTime = 120f;
				size = 2;
				hasPower = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.fusionEnergy.color));
				consumeItems(new ItemStack(Items.silicon, 2), new ItemStack(Items.copper, 4));
				consumePower(2f);
			}
		};
		
		seniorProcessorFactory = new GenericCrafter("processor-senior-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25, Items.thorium, 25));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.seniorProcessor, 4);
				craftTime = 120f;
				size = 2;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHItems.fusionEnergy.color));
				consumeItems(new ItemStack(Items.surgeAlloy, 2), new ItemStack(NHItems.juniorProcessor, 4));
				consumePower(4f);
			}
		};
		
		irdryonFluidFactory = new GenericCrafter("irdryon-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 20, NHItems.seniorProcessor, 50, NHItems.presstanium, 80, NHItems.irayrondPanel, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 8f / 60f);
				craftTime = 60;
				size = 2;
				drawer = new DrawMulti(new DrawDefault(), new DrawLiquidRegion(NHLiquids.irdryonFluid), new DrawFrames(){{
					frames = 5;
					sine = true;
				}}, new DrawRegion("-top"));
				itemCapacity = 20;
				hasPower = hasLiquids = hasItems = true;
				consumeLiquid(NHLiquids.xenBeta, 0.075f);
				consumeItems(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.metalOxhydrigen, 4));
				consumePower(4f);
			}
		};
		
		zetaFluidFactory = new GenericCrafter("zeta-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 50, NHItems.juniorProcessor, 35, NHItems.presstanium, 80, Items.graphite, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.zetaFluid, 15f / 60f);
				craftTime = 60f;
				health = 550;
				drawer = new DrawMulti(new DrawDefault(), new DrawFlame(NHLiquids.zetaFluid.color));
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				consumeLiquid(Liquids.water, 0.1f);
				consumeItem(NHItems.zeta, 2);
				consumePower(8f);
			}
		};
		
		metalOxhydrigenFactory = new GenericCrafter("metal-oxhydrigen-factory") {
			{
				requirements(Category.crafting, with(Items.copper, 60, NHItems.juniorProcessor, 30, NHItems.presstanium, 25, Items.thorium, 25));
				craftEffect = NHFx.square(NHColor.lightSkyFront, 38, 3, 24, 3.2f);
				outputItem = new ItemStack(NHItems.metalOxhydrigen, 4);
				craftTime = 120f;
				size = 2;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = Liquids.water.color;
						drawRotator = 1.5f;
						drawTop = true;
					}
				};
				consumeLiquid(Liquids.water, 0.1f);
				consumeItem(Items.lead, 2);
				consumePower(2f);
			}
		};
		
		metalOxhydrigenFactoryLarge = new GenericCrafter("metal-oxhydrigen-factory-large"){{
			health = 450;
			size = 3;
			
			requirements(Category.crafting, with(NHItems.multipleSteel, 50, NHItems.juniorProcessor, 60, NHItems.presstanium, 55, NHItems.zeta, 85));
			
			itemCapacity = 40;
			liquidCapacity = 80;
			
			updateEffect = new Effect(40f, 80f, e -> {
				Draw.color(NHColor.lightSkyFront, NHColor.lightSkyBack, e.fin() * 0.8f);
				Lines.stroke(2f * e.fout());
				Lines.spikes(e.x, e.y, 12 * e.finpow(), 1.5f * e.fout() + 4 * e.fslope(), 4, 45);
			});
			craftEffect = NHFx.square(NHColor.lightSkyFront, 38, 5, 34, 5);
			outputItem = new ItemStack(NHItems.metalOxhydrigen, 8);
			craftTime = 60f;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
				flameRad = 0.45f;
				circleStroke = 0.8f;
				flameRadiusMag = 0.15f;
				flameColor = NHColor.lightSkyFront;
				midColor = NHColor.lightSkyBack;
			}}, new DrawDefault(), new DrawRegion("-top"));
			consumeLiquid(Liquids.water, 0.25f);
			consumeItem(Items.lead, 3);
			consumePower(5f);
		}};
		
		thermoCoreFactory = new GenericCrafter("thermo-core-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 150, NHItems.seniorProcessor, 80, NHItems.presstanium, 250, Items.plastanium, 80));
				craftEffect = Fx.plasticExplosionFlak;
				outputItem = new ItemStack(NHItems.thermoCorePositive, 4);
				craftTime = 90f;
				itemCapacity = 30;
				health = 1500;
				size = 5;
				hasPower = hasLiquids = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = NHLiquids.zetaFluid.color;
						drawRotator = 1f;
						drawTop = false;
						pressorSet = new float[] {(craftTime / 6f), -4.2f, 45, 0};
					}
				};
				consumeLiquid(NHLiquids.zetaFluid, 0.2f);
				consumeItems(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.fusionEnergy, 4), new ItemStack(NHItems.metalOxhydrigen, 2));
				consumePower(5f);
			}
		};
		
		upgradeSortFactory = new GenericCrafter("upgradeSort-factory") {{
			requirements(Category.crafting, with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.thorium, 200));
			updateEffect = NHStatusEffects.quantization.effect;
			craftEffect = new Effect(25f, e -> {
				Draw.color(NHColor.darkEnrColor);
				Angles.randLenVectors(e.id, 4, 24 * e.fout() * e.fout(), (x, y) -> {
					Lines.stroke(e.fout() * 1.7f);
					Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
				});
				
			});
			outputItem = new ItemStack(NHItems.upgradeSort, 3);
			craftTime = 150f;
			size = 3;
			hasPower = hasItems = true;
			drawer = new DrawPrinter(outputItem.item) {{
				printColor = NHColor.darkEnrColor;
				lightColor = Color.valueOf("#E1BAFF");
				moveLength = 4.2f;
				time = 25f;
			}};
			clipSize = size * tilesize * 2f;
			consumeItems(new ItemStack(NHItems.setonAlloy, 4), new ItemStack(NHItems.seniorProcessor, 4));
			consumePower(10f);
		}};
		
		zetaFactoryLarge = new GenericCrafter("large-zeta-factory") {{
			requirements(Category.crafting, with(Items.plastanium, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25));
			outputItem = new ItemStack(NHItems.zeta, 3);
			craftTime = 30f;
			size = 2;
			craftEffect = Fx.formsmoke;
			updateEffect = NHFx.trailToGray;
			hasPower = hasItems = hasLiquids = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCultivator(){{
				bottomColor = NHLiquids.zetaFluid.color.cpy().lerp(Color.gray, 0.1f);
				plantColor = NHLiquids.zetaFluid.color;
				plantColorLight = NHLiquids.zetaFluid.color.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
			
			consumeItem(Items.thorium, 3);
			consumePower(7f);
			consumeLiquid(Liquids.water, 0.075f);
		}};
		
		multipleSteelFactory = new GenericCrafter("multiple-steel-factory") {
			{
				requirements(Category.crafting, with(Items.graphite, 65, NHItems.juniorProcessor, 65, NHItems.presstanium, 100, Items.metaglass, 30));
				updateEffect = Fx.smeltsmoke;
				craftEffect = Fx.shockwave;
				outputItem = new ItemStack(NHItems.multipleSteel, 4);
				craftTime = 40f;
				itemCapacity = 20;
				health = 600;
				size = 3;
				hasPower = hasItems = true;
				drawer = new DrawFactories() {
					{
						liquidColor = NHLiquids.xenAlpha.color;
						drawTop = false;
						pressorSet = new float[] {(craftTime / 4f), 3.8f, 0, 90};
					}
				};
				consumeLiquid(NHLiquids.xenAlpha, 0.2f);
				consumeItems(new ItemStack(Items.metaglass, 4), new ItemStack(Items.titanium, 2));
				consumePower(3f);
			}
		};
		
		irayrondPanelFactorySmall = new GenericCrafter("small-irayrond-panel-factory"){{
			requirements(Category.crafting, with(NHItems.multipleSteel, 55, NHItems.juniorProcessor, 35, NHItems.presstanium, 60, Items.plastanium, 40, NHItems.zeta, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenBeta.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputItem = new ItemStack(NHItems.irayrondPanel, 2);
			craftTime = 180f;
			itemCapacity = 24;
			liquidCapacity = 20f;
			health = 500;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawFactories() {
				{
					liquidColor = NHLiquids.xenAlpha.color;
					drawTop = false;
					pressorSet = new float[] {(craftTime / 8f), 3.8f, 0, 0};
				}
			};
			consumeLiquid(NHLiquids.xenBeta, 0.1f);
			consumeItems(new ItemStack(NHItems.presstanium, 5), new ItemStack(NHItems.metalOxhydrigen, 3));
			consumePower(10f);
		}};
		
		multipleSurgeAlloyFactory = new GenericCrafter("multiple-surge-alloy-factory"){{
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.irayrondPanel, 80, NHItems.seniorProcessor, 60, Items.plastanium, 40, NHItems.presstanium, 100, Items.surgeAlloy, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(Items.surgeAlloy.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
			}));
			outputItem = new ItemStack(Items.surgeAlloy, 5);
			craftTime = 90f;
			itemCapacity = 30;
			liquidCapacity = 20f;
			health = 500;
			size = 3;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawDefault(), new DrawLiquidRegion(NHLiquids.zetaFluid), new DrawRegion("-top"));
			consumeLiquid(NHLiquids.zetaFluid, 0.1f);
			consumeItems(new ItemStack(NHItems.metalOxhydrigen, 6), new ItemStack(Items.thorium, 6), new ItemStack(NHItems.fusionEnergy, 1));
			consumePower(20f);
		}};
		
		setonAlloyFactory = new GenericCrafter("seton-alloy-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 80, NHItems.seniorProcessor, 60, NHItems.presstanium, 100, Items.surgeAlloy, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 4f + e.fin() * 12f, (x, y) -> {
				Draw.color(NHLiquids.irdryonFluid.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 3f);
			}));
			outputItem = new ItemStack(NHItems.setonAlloy, 2);
			craftTime = 60f;
			itemCapacity = 24;
			liquidCapacity = 20f;
			health = 500;
			size = 3;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(NHLiquids.irdryonFluid), new DrawRotator(1, 45), new DrawRotator(-1.5f, "-rotator1"), new DrawDefault());
		
			consumeLiquid(NHLiquids.irdryonFluid, 0.12f);
			consumeItems(new ItemStack(Items.plastanium, 4), new ItemStack(Items.graphite, 6));
			consumePower(12f);
		}};
		
		xenBetaFactory = new GenericCrafter("xen-beta-factory"){{
			requirements(Category.crafting, with(NHItems.metalOxhydrigen, 35, NHItems.juniorProcessor, 60, Items.plastanium, 20, NHItems.presstanium, 80, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenBeta.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenBeta, 6f / 60f);
			craftTime = 60f;
			itemCapacity = 12;
			liquidCapacity = 20f;
			health = 260;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawFactories() {{
				liquidColor = NHLiquids.xenBeta.color;
				drawTop = true;
			}};
			consumeLiquid(NHLiquids.xenAlpha, 0.1f);
			consumeItem(NHItems.zeta, 2);
			consumePower(3f);
		}};
		
		xenGammaFactory = new GenericCrafter("xen-gamma-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 70, NHItems.seniorProcessor, 60, Items.surgeAlloy, 20, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenGamma.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenGamma, 12f / 60f);
			craftTime = 60f;
			itemCapacity = 12;
			liquidCapacity = 20f;
			health = 260;
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawFactories() {
				{
					liquidColor = NHLiquids.xenBeta.color;
				}
			};
			consumeLiquid(NHLiquids.xenBeta, 0.2f);
			consumeItems(new ItemStack(Items.phaseFabric, 2));
			consumePower(8f);
		}};
	}
	
	public static void load() {
		final int healthMult2 = 4, healthMult3 = 9;
		
		blaster = new ShockwaveGenerator("blaster"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.multipleSteel, 100, NHItems.juniorProcessor, 120));
			
			size = 3;
			chargerOffset = 5.65f;
			rotateOffset = -45f;
			damage = 150;
			lightningDamage = 200;
			generateLiNum = 3;
			generateLiLen = 12;
			generateLenRand = 20;
			gettingBoltNum = 1;
			lightningColor = NHColor.darkEnrColor;
			generateEffect = NHFx.blastgenerate;
			acceptEffect = NHFx.blastAccept;
			blastSound = Sounds.explosionbig;
			status = NHStatusEffects.emp2;
			range = 240;
			health = 1200;
			knockback = 10f;
			consumePower(8f);
			itemCapacity = 30;
			consumeItem(NHItems.zeta, 3);
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
				midColor = flameColor = NHColor.darkEnrColor;
			}}, new DrawDefault());
		}};
		
		reinForcedLiquidSource = new LiquidSource("reinforced-liquid-source"){{
			size = 1;
			health = 800;
			armor = 10;
			buildVisibility = BuildVisibility.sandboxOnly;
			
			category = Category.liquid;
			
			buildType = () -> new LiquidSourceBuild(){
				@Override
				public boolean canPickup(){
					return false;
				}
				
				@Override
				public void write(Writes write){
					write.str(source == null ? "null-liquid" : source.name);
				}
				
				@Override
				public void read(Reads read, byte revision){
					source = content.liquid(read.str());
				}
			};
		}};
		
		reinForcedItemSource = new ItemSource("reinforced-item-source"){{
			size = 1;
			health = 800;
			armor = 10;
			buildVisibility = BuildVisibility.sandboxOnly;
			
			buildType = () -> new ItemSourceBuild(){
				@Override
				public boolean canPickup(){
					return false;
				}
				
				@Override
				public void draw(){
					if (this.block.variants != 0 && this.block.variantRegions != null) {
						Draw.rect(this.block.variantRegions[Mathf.randomSeed((long)this.tile.pos(), 0, Math.max(0, this.block.variantRegions.length - 1))], this.x, this.y, this.drawrot());
					} else {
						Draw.rect(this.block.region, this.x, this.y, this.drawrot());
					}
					
					this.drawTeamTop();
					
					if(outputItem == null){
						Draw.rect(NHContent.crossRegion, x, y);
					}else{
						Draw.color(outputItem.color);
						Draw.rect(NHContent.sourceCenter, x, y);
						Draw.color();
					}
				}
				
				@Override
				public void write(Writes write){
					write.str(outputItem == null ? "null-item" : outputItem.name);
				}
				
				@Override
				public void read(Reads read, byte revision){
					outputItem = content.item(read.str());
				}
			};
		}
			@Override
			public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
				drawPlanConfigCenter(plan, plan.config, NewHorizon.name("source-center"), true);
			}
		};
		
		ancientLaserWall = new LaserWallBlock("ancient-laser-wall"){{
			size = 2;
			consumePowerCond(80f, LaserWallBuild::canActivate);
			health = 8000;
			range = 800;
			
			armor = 20f;
			
			generateType = new Shooter(450){{
				Color c = Items.surgeAlloy.color;
				colors = new Color[]{c.cpy().mul(0.9f, 0.9f, 0.9f, 0.3f), c.cpy().mul(1f, 1f, 1f, 0.6f), c, Color.white};
				hitColor = lightColor = lightningColor = c;
				width = 4.5f;
				oscMag = 0.5f;
				
				lightningDamage = 200;
			}
				
				@Override
				public void hit(Bullet b, float x, float y){
					super.hit(b, x, y);
					
					for(int i = 0; i < 2; i++){
						Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, x, y, Mathf.range(180), lightningLength + Mathf.random(lightningLengthRand));
					}
				}
			};
			
			requirements(Category.defense, with(NHItems.seniorProcessor, 250, NHItems.upgradeSort, 600, NHItems.zeta, 800));
		}};
		
		laserWall = new LaserWallBlock("laser-wall"){{
			size = 3;
			consumePowerCond(30f, LaserWallBuild::canActivate);
			health = 4000;
			
			armor = 10f;
			
			requirements(Category.defense, with(NHItems.juniorProcessor, 120, Items.copper, 350, NHItems.multipleSteel, 80, NHItems.zeta, 180, Items.graphite, 80));
//			NHTechTree.add(Blocks.forceProjector, this);
		}};
		
		multiSteelItemBridge = new BufferedItemBridge("multi-steel-item-bridge"){{
			health = 480;
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.zeta, 5, Items.graphite, 10));
			
			fadeIn = moveArrows = true;
			hasPower = false;
			range = 8;
			speed = 40;
			arrowSpacing = 8f;
			bufferCapacity = 20;
		}};
		
		multiSteelLiquidBridge = new LiquidBridge("multi-steel-liquid-bridge"){{
			health = 480;
			requirements(Category.liquid, with(NHItems.multipleSteel, 5, NHItems.zeta, 5, Items.metaglass, 10));

			fadeIn = moveArrows = true;
			arrowSpacing = 8;
			range = 8;
			hasPower = false;
		}};
		
		fireExtinguisher = new FireExtinguisher("fire-extinguisher"){{
			size = 3;
			health = 920;
			intensity = 1600;
			
			consumeItem(NHItems.metalOxhydrigen, 2);
			consumePowerCond(3f, FireExtinguisherBuild::isActive);
			
			requirements(Category.defense, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 120, Items.copper, 80, Items.graphite, 60));
//			//NHTechTree.add(Blocks.tsunami, this);
		}};
		
		shapedWall = new ShapedWall("shaped-wall"){{
			health = 6000;
			insulated = absorbLasers = true;
			
			requirements(Category.defense, with(NHItems.upgradeSort, 5, NHItems.juniorProcessor, 2, NHItems.setonAlloy, 10));
		}};
		
		multiConduit = new Conduit("multi-conduit"){{
			size = 1;
			health = 420;
			liquidCapacity = 20.0F;
			liquidPressure = 1.1f;
			leaks = false;
			
			requirements(Category.liquid, with(NHItems.multipleSteel, 1, Items.copper, 2, Items.metaglass, 1));
//			//NHTechTree.add(Blocks.pulseConduit, this);
		}};
		
		multiRouter = new Router("multi-router"){{
			size = 1;
			health = 420;
			speed = 2f;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.lead, 5));
//			//NHTechTree.add(Blocks.router, this);
		}};
		
		multiJunction = new Junction("multi-junction"){{
			size = 1;
			health = 420;
			speed = 12f;
			capacity = 12;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.copper, 5));
//			//NHTechTree.add(Blocks.junction, this);
		}};
		
		beamDrill = new LaserBeamDrill("beam-drill"){{
			size = 4;
			health = 960;
			tier = 6;
			drillTime = 150f;
			liquidBoostIntensity = 1.65f;
			warmupSpeed = 0.001f;
			consumePower(6);
			consumeLiquid(Liquids.water, 0.1f).optional(true, true);
			requirements(Category.production, BuildVisibility.shown, with(NHItems.juniorProcessor, 60, NHItems.multipleSteel, 45, NHItems.zeta, 60, NHItems.presstanium, 40, Items.lead, 80));
//			//NHTechTree.add(Blocks.blastDrill, this);
		}};
		
		airRaider = new AirRaider("air-raider"){{
			requirements(Category.effect, with(NHItems.upgradeSort, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 120, NHItems.juniorProcessor, 100, Items.phaseFabric, 150));
			
			shoot = new ShootSummon(0, 0, 120, 0){{
				shots = 4;
				shotDelay = 8f;
			}};
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(){{
				alpha = 0.375f;
				particles = 20;
				particleSize = 2.6f;
				particleRad = 7f;
				flameColor = NHColor.darkEnrColor;
				midColor = NHColor.darkEnrColor.cpy().lerp(Color.white, 0.1f);
			}}, new DrawDefault());
			
			size = 3;
			consumePowerCond(6f, AirRaiderBuild::isCharging);
			consumeItem(NHItems.darkEnergy, 4);
			itemCapacity = 16;
			health = 4500;
			
			triggeredEffect = new Effect(45f, e -> {
				Draw.color(NHColor.darkEnrColor);
				Lines.stroke(e.fout() * 2f);
				Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
			});
			
			bullet = NHBullets.airRaidBomb;
		}};
		
		
		remoteStorage = new RemoteCoreStorage("remote-vault"){{
			size = 3;
			health = 960;
			consumePower(10);
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 150, NHItems.multipleSteel, 120));
			//NHTechTree.add(Blocks.coreShard, this);
		}};
		
		unitIniter = new UnitSpawner("unit-initer");
		
/*		shieldProjector = new ShieldProjector("shield-projector"){{
			consumePower(1f);
			consumePowerCond(8f, ShieldProjectorBuild::isCharging);
			size = 3;
			itemCapacity = 20;
			consumeItem(NHItems.fusionEnergy, 5);
			requirements(Category.defense, BuildVisibility.shown, with(Items.copper, 300, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.plastanium, 75, NHItems.multipleSteel, 120));
			//NHTechTree.add(Blocks.forceProjector, this);
		}};
		
		scrambler = new AirRaider("scrambler"){{
			requirements(Category.effect, with(NHItems.multipleSteel, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 100, Items.plastanium, 100, Items.phaseFabric, 150));
			
			range =  720f;
			
			size = 3;
			consumePowerCond(8f, AirRaiderBuild::isCharging);
			consumeItems(with(NHItems.juniorProcessor, 2, Items.phaseFabric, 1, NHItems.metalOxhydrigen, 1));
			itemCapacity = 12;
			burstSpacing = 30f;
			salvos = 2;
			health = 2500;
			
			lightColor = NHColor.thermoPst;
			
			triggeredEffect = new Effect(45f, e -> {
				Draw.color(lightColor);
				Lines.stroke(e.fout() * 2f);
				Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
			});
			
			bulletHitter = new EmpBulletType(){{
				speed = 6f;
				damage = 100f;
				sprite = "missile-large";
				
				status = NHStatusEffects.scrambler;
				statusDuration = 480f;
				
				trailLength = 14;
				trailWidth = 1.5f;
				
				hitColor = trailColor = backColor = lightColor = lightningColor = NHColor.thermoPst;
				frontColor = Color.white;
				
				homingRange = 300f;
				homingDelay = 6f;
				homingPower = 0f; //Custome Homing
				
				hitSound = Sounds.explosionbig;
				trailChance = 0.075f;
				trailEffect = NHFx.polyTrail;
				drawSize = 120f;
				
				hitPowerEffect = applyEffect = NHFx.lightningHitSmall(hitColor);
				
				collides = false;
				scaleVelocity = true;
				hitShake = despawnShake = 16f;
				lightning = 3;
				lightningCone = 360;
				lightningLengthRand = lightningLength = 20;
				shootEffect = NHFx.instShoot(backColor);
				smokeEffect = NHFx.square(backColor, 50f, 3, 80f, 5f);
				shrinkX = shrinkY = 0;
				radius = 100f;
				splashDamageRadius = 60f;
				splashDamage = lightningDamage = damage;
				height = 22f;
				width = 8f;
				lifetime = 120f;
				
				despawnEffect = new Effect(60f, 150f, e -> {
					Draw.color(Color.white, hitColor, 0.3f + e.fin());
					Lines.stroke(2f * e.fout());
					
					float rad = radius * 1.25f * e.fin(Interp.pow4Out);
					Lines.circle(e.x, e.y, rad);
					
					Fill.circle(e.x, e.y, radius / 5f * e.fout());
					Drawf.light(e.x, e.y, rad * 1.2f, hitColor, 0.7f);
				});
				
				hitEffect = new OptionalMultiEffect(NHFx.square(hitColor, 100f, 3, 80f, 8f), NHFx.blast(hitColor, radius));
			}
				
				@Override
				public void update(Bullet b){
					super.update(b);
					
					Teamc target = Units.closestEnemy(b.team, b.x, b.y, homingRange * 2f, Unitc::isPlayer);
					
					if(target != null){
						if(b.within(target, radius / 4))b.time(b.lifetime());
						b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), Time.delta * 7));
					}
				}
				
				@Override
				public void despawned(Bullet b){
					super.despawned(b);
					
					Units.nearbyEnemies(b.team, b.x, b.y, radius, u -> {
						if(u.isLocal()) ScreenInterferencer.generate(600);
					});
					
					for(int i = 0; i < 6; i++){
						Vec2 v = Tmp.v6.rnd(radius + Mathf.random(radius)).add(b).cpy();
						NHFx.chainLightningFade.at(b.x, b.y, 12f, hitColor, v);
						Time.run(NHFx.chainLightningFade.lifetime * NHFx.lightningAlign, () -> {
							NHFx.lightningHitSmall.at(v.x, v.y, 20f, hitColor);
						});
					}
				}
			};
		}};
		

		bombLauncher = new BombLauncher("bomb-launcher"){{
			requirements(Category.effect, with(Items.phaseFabric, 100, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.thorium, 100, Items.surgeAlloy, 75));
			//NHTechTree.add(Blocks.massDriver, this);
			size = 3;
			bulletHitter = new EffectBulletType(75f){{
				trailChance = 0.25f;
				trailEffect = NHFx.trailToGray;
				trailParam = 1.5f;
				
				smokeEffect = NHFx.hugeSmoke;
				shootEffect = NHFx.boolSelector;
				
				collidesTiles = collidesGround = collides = true;
				splashDamage = 500f;
				lightningDamage = 200f;
				hitColor = NHColor.thurmixRed;
				lightning = 3;
				lightningLength = 8;
				lightningLengthRand = 16;
				splashDamageRadius = 120f;
				hitShake = despawnShake = 20f;
				hitSound = despawnSound = Sounds.explosionbig;
				hitEffect = despawnEffect = new OptionalMultiEffect(NHFx.crossBlast(hitColor, splashDamageRadius * 1.25f), NHFx.blast(hitColor, splashDamageRadius * 1.5f));
			}};
			consumePowerCond(6f, BombLauncherBuild::isCharging);
			consumeItem(NHItems.fusionEnergy, 2);
			itemCapacity = 16;
			health = 900;
		}};*/
		
		hyperspaceWarper = new HyperSpaceWarper("hyper-space-warper"){{
			size = 4;
			health = 2250;
			
			completeEffect = NHFx.square45_4_45;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawPlasma(){{
				plasma1 = NHColor.lightSkyBack;
				plasma2 = NHColor.thermoPst;
			}}, new DrawDefault());
			
			hasPower = hasItems = true;
			itemCapacity = 20;
			consumeItem(NHItems.fusionEnergy, 5);
			consumePower(12f);
			
			requirements(Category.units, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 450, NHItems.zeta, 200));
		}};
		
		gravityTrapSmall = new GravityTrap("gravity-trap-small"){{
			size = 2;
			health = 640;
			range = 16;
			
			consumePower(5f);
			requirements(Category.units, BuildVisibility.shown, with(Items.titanium, 60, NHItems.metalOxhydrigen, 80, NHItems.juniorProcessor, 50, Items.copper, 200, NHItems.zeta, 50));
			//NHTechTree.add(hyperspaceWarper, this);
		}};
		
		gravityTrap = new GravityTrap("gravity-gully"){{
			size = 3;
			health = 1250;
			
			consumePower(8f);
			requirements(Category.units, BuildVisibility.shown, with(Items.plastanium, 80, NHItems.multipleSteel, 80, NHItems.juniorProcessor, 80, Items.copper, 200));
			//NHTechTree.add(gravityTrapSmall, this);
		}};
		
		irdryonTank = new LiquidRouter("irdryon-tank"){{
			requirements(Category.liquid, with(NHItems.metalOxhydrigen, 25, NHItems.multipleSteel, 40, Items.metaglass, 25));
//			//NHTechTree.add(Blocks.liquidTank, this);
			size = 3;
			underBullets = true;
			liquidCapacity = 2500.0F;
			health = 2500;
		}};
		
		assignOverdrive = new AssignOverdrive("assign-overdrive"){{
			requirements(Category.effect, with(NHItems.irayrondPanel, 120, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.plastanium, 80, Items.surgeAlloy, 75));
			//NHTechTree.add(Blocks.overdriveProjector, this);
			consumePower(14.0F);
			size = 3;
			range = 240.0F;
			speedBoost = 4f;
			useTime = 300.0F;
			hasBoost = true;
			consumeItem(Items.phaseFabric).boost();
			consumeLiquid(NHLiquids.xenBeta, 0.1f);
		}};
		
		largeMendProjector = new MendProjector("large-mend-projector"){{
			size = 3;
			reload = 180f;
			useTime = 600f;
			healPercent = 15;
			requirements(Category.effect, with(NHItems.presstanium, 60, NHItems.juniorProcessor, 50, Items.plastanium, 40, Items.thorium, 80));
//			//NHTechTree.add(Blocks.mendProjector, this);
			consumePower(2F);
			range = 160.0F;
			phaseBoost = 12f;
			phaseRangeBoost = 60.0F;
			health = 980;
			consumeItem(NHItems.juniorProcessor).boost();
		}};

		zetaGenerator = new ConsumeGenerator("zeta-generator"){{
			requirements(Category.power,ItemStack.with(NHItems.metalOxhydrigen, 120, NHItems.juniorProcessor, 80, Items.plastanium, 80, NHItems.zeta,100, Items.copper, 150));
//			//NHTechTree.add(Blocks.thoriumReactor,this);
			size = 3;
			powerProduction = 60f;
			ambientSound = Sounds.hum;
			ambientSoundVolume = 0.24F;
			itemCapacity = 30;
			liquidCapacity = 30;
			itemDuration = 150f;
			consumeItem(NHItems.zeta, 3);
			consumeLiquid(Liquids.cryofluid,0.1f);
			
			hasLiquids = true;
			
			drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.cryofluid), new DrawDefault(), new DrawGlowRegion(){{
				color = NHItems.zeta.color;
			}});
			
			lightColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
			generateEffect = NHFx.square(lightColor, 30f, 5, 20f, 4);
//			explodeEffect = NHFx.lightningHitLarge(heatColor);
		}};
		
		hugeBattery = new Battery("huge-battery"){{
			size = 5;
			health = 1600;
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(Items.phaseFabric, 40, NHItems.juniorProcessor, 20, NHItems.zeta, 80, NHItems.presstanium, 35, Items.graphite, 50));
//			//NHTechTree.add(Blocks.batteryLarge, this);
			consumePowerBuffered(750_000);
		}};
		
		armorPowerNode = new PowerNode("armor-power-node"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 25, NHItems.juniorProcessor, 5, Items.lead, 25));
//			//NHTechTree.add(Blocks.powerNodeLarge, this);
			size = 2;
			maxNodes = 12;
			laserRange = 8.5F;
			health = 1650;
		}};
		
		armorBatteryLarge = new Battery("large-armor-battery"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 10, Items.lead, 40));
			size = 3;
			health = 3000;
			consumePowerBuffered(45000.0F);
		}};
		
		heavyPowerNode = new PowerNode("heavy-power-node"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.multipleSteel, 25, NHItems.juniorProcessor, 15, NHItems.zeta, 45, NHItems.presstanium, 40));
//			//NHTechTree.add(Blocks.powerNodeLarge, this);
			size = 3;
			maxNodes = 30;
			laserRange = 20F;
			health = 1050;
		}};
		
		largeWaterExtractor = new SolidPump("large-water-extractor"){{
			size = 3;
			pumpAmount = 0.3f;
			requirements(Category.production, ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 45, Items.thorium, 60, Items.metaglass, 30));
//			//NHTechTree.add(Blocks.waterExtractor, this);
			result = Liquids.water;
			liquidCapacity = 60.0F;
			rotateSpeed = 1.4F;
			attribute = Attribute.water;
			consumePower(4f);
		}};
		
		rapidUnloader = new AdaptUnloader("rapid-unloader"){{
			speed = 0.5f;
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.presstanium, 20, Items.lead, 15, NHItems.juniorProcessor, 25));
//			//NHTechTree.add(Blocks.unloader, this);
		}};
		
		multiEfficientConveyor = new Conveyor("multi-efficient-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.multipleSteel, 2));
//			//NHTechTree.add(Blocks.titaniumConveyor, this);
			speed = 0.16f;
			displayedSpeed = 18f;
			health = 120;
			junctionReplacement = multiJunction;
		}};
		
		multiArmorConveyor = new ArmoredConveyor("multi-armor-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2, NHItems.multipleSteel, 2, Items.thorium, 1));
//			//NHTechTree.add(Blocks.armoredConveyor, this);
			speed = 0.16f;
			displayedSpeed = 18f;
			health =  320;
			junctionReplacement = multiJunction;
		}};

		multiConveyor = new StackConveyor("multi-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.irayrondPanel, 2, NHItems.juniorProcessor, 1));
//			//NHTechTree.add(Blocks.plastaniumConveyor, this);
			speed = 0.125f;
			health = 320;
			itemCapacity = 20;
			recharge = 1f;
			
			loadEffect = unloadEffect = new Effect(30f, e -> {
				Lines.stroke(1.5f * e.fout(Interp.pow2Out), NHItems.multipleSteel.color);
				Lines.square(e.x, e.y, tilesize / 8f * Mathf.sqrt2 * (e.fin(Interp.pow2Out) * 3 + 1f), 45f);
			});
		}};
		
		hyperGenerator = new HyperGenerator("hyper-generator"){{
			size = 8;
			health = 12500;
			powerProduction = 1750f;
			updateLightning = updateLightningRand = 3;
			effectColor = NHColor.thermoPst;
			itemCapacity = 40;
			itemDuration = 180f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.1F;
			consumePower(50.0F);
			consumeItems(new ItemStack(NHItems.metalOxhydrigen, 8), new ItemStack(NHItems.thermoCorePositive, 4));
			consumeLiquid(NHLiquids.zetaFluid, 0.25F);
			requirements(Category.power, BuildVisibility.shown, with(NHItems.upgradeSort, 1000, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250, Items.metaglass, 250));
//			//NHTechTree.add(fusionCollapser, this);
		}};
		
		insulatedWall = new Wall("insulated-wall"){{
			size = 1;
			health = 300;
			requirements(Category.defense, with(Items.titanium, 10, Items.copper, 5));
			insulated = true;
			absorbLasers = true;
		}};
		
		setonWall = new Wall("seton-wall"){{
			armor = 15f;
			
			insulated = true;
			
			size = 1;
			health = 1250;
			chanceDeflect = 15.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5, NHItems.irayrondPanel, 10, Items.silicon, 15, NHItems.presstanium, 15));
		}};
		
		setonWallLarge = new Wall("seton-wall-large"){{
			armor = 15f;
			
			insulated = true;
			
			size = 2;
			health = 1250 * healthMult2;
			chanceDeflect = 15.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5 * healthMult2, NHItems.irayrondPanel, 10 * healthMult2, Items.silicon, 15 * healthMult2, NHItems.presstanium, 15 * healthMult2));
		}};
		
		heavyDefenceWall = new ShieldWall("heavy-defence-wall"){{
			shieldHealth = 1200f;
			breakCooldown = 30f * 10f;
			regenSpeed = 5f;
			glowColor = NHColor.darkEnrColor.cpy().lerp(NHColor.lightSkyFront, 0.3f).a(0.5f);
			consumePower(8f / 60f);
			
			outputsPower = false;
			hasPower = true;
			consumesPower = true;
			conductivePower = true;
			
			armor = 30f;
			
			size = 1;
			health = 1750;
			absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20));
			
			buildType = () -> new ShieldWallBuild(){
				@Override
				public void draw(){
					Draw.rect(block.region, x, y);
					
					if(shieldRadius > 0){
						float radius = shieldRadius * tilesize / 2;
						
						Draw.z(Layer.shields);
						
						Draw.color(team.color, Color.white, Mathf.clamp(hit));
						
						if(renderer.animateShields){
							Fill.square(x, y, radius);
						}else{
							Lines.stroke(1.5f);
							Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
							Fill.square(x, y, radius);
							Draw.alpha(1f);
							Lines.poly(x, y, 4, radius, 45f);
							Draw.reset();
						}
						
						Draw.reset();
						
						Drawf.additive(glowRegion, glowColor, (1f - glowMag + Mathf.absin(glowScl, glowMag)) * shieldRadius, x, y, 0f, Layer.blockAdditive);
					}
				}
			};
		}};
		
		heavyDefenceWallLarge = new ShieldWall("heavy-defence-wall-large"){{
			shieldHealth = 1200f * healthMult2;
			breakCooldown = 30f * 10f;
			regenSpeed = 5f * healthMult2;
			glowColor = NHColor.darkEnrColor.cpy().lerp(NHColor.lightSkyFront, 0.3f).a(0.5f);
			consumePower(8f / 60f * healthMult2);
			
			armor = 30f;
			
			outputsPower = false;
			hasPower = true;
			consumesPower = true;
			conductivePower = true;
			
			size = 2;
			health = 1750 * healthMult2;
			absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult2, NHItems.presstanium, 20 * healthMult2));
		}};
		
		heavyDefenceDoor = new Door("heavy-defence-door"){{
			size = 1;
			health = 1750;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20, NHItems.juniorProcessor, 5));
		}};
		
		heavyDefenceDoorLarge = new Door("heavy-defence-door-large"){{
			size = 2;
			health = 1750 * healthMult2;
			openfx = Fx.dooropenlarge;
			closefx = Fx.doorcloselarge;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult2, NHItems.presstanium, 20 * healthMult2, NHItems.juniorProcessor, 5 * healthMult2));
		}};
		
		largeShieldGenerator = new ForceProjector("large-shield-generator") {{
			size = 4;
			radius = 220f;
			shieldHealth = 20000f;
			cooldownNormal = 18f;
			cooldownLiquid = 6f;
			cooldownBrokenBase = 15f;
			consumeItem(NHItems.fusionEnergy).boost();
			phaseUseTime = 180.0F;
			phaseRadiusBoost = 100.0F;
			phaseShieldBoost = 12000.0F;
			consumePower(12F);
			requirements(Category.effect, with(NHItems.seniorProcessor, 120, Items.lead, 250, Items.graphite, 180, NHItems.presstanium, 150, NHItems.fusionEnergy, 80, NHItems.multipleSteel, 50));
		}};
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.defense, with(NHItems.irayrondPanel, 10, NHItems.seniorProcessor, 5, NHItems.upgradeSort, 15));
			size = 1;
			absorbLasers = true;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSkyBack;
			
			armor = 10f;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.defense, ItemStack.mult(chargeWall.requirements, healthMult2));
			size = 2;
			absorbLasers = true;
			range = 200;
            health = 1350 * healthMult2;
            effectColor = NHColor.lightSkyBack;
			
			armor = 10f;
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
		
		jumpGatePrimary = new JumpGate("jump-gate-primary"){{
			size = 3;
			atlasSizeScl = 0.55f;
			squareStroke = 1.75f;
			health = 1800;
			spawnDelay = 90f;
			spawnReloadTime = 750f;
			range = 160f;
			
			armor = 5f;
			
			itemCapacity = 500;
			
			consumePowerCond(8, JumpGateBuild::isCalling);
			
			requirements(Category.units, BuildVisibility.shown, with(
					Items.copper, 250,
					Items.lead, 200,
					Items.titanium, 80,
					Items.silicon, 80
			));
			
			addSets(
				new UnitSet(UnitTypes.poly, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
						with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
				),
				new UnitSet(NHUnitTypes.assaulter, new byte[]{NHUnitTypes.AIR_LINE_2, 1}, 15 * 60f,
					with(Items.silicon, 16, Items.copper, 30, NHItems.zeta, 20)
				),
				new UnitSet(NHUnitTypes.sharp, new byte[]{NHUnitTypes.AIR_LINE_1, 1}, 15 * 60f,
					with(Items.titanium, 30, Items.silicon, 15)
				),
				new UnitSet(NHUnitTypes.branch, new byte[]{NHUnitTypes.AIR_LINE_1, 2}, 30 * 60f,
					with(Items.titanium, 60, Items.silicon, 45, Items.graphite, 30)
				),
				new UnitSet(NHUnitTypes.origin, new byte[]{NHUnitTypes.GROUND_LINE_1, 1}, 20 * 60f,
					with(Items.lead, 15, Items.silicon, 10 ,Items.copper, 10)
				),
				new UnitSet(NHUnitTypes.thynomo, new byte[]{NHUnitTypes.GROUND_LINE_1, 2}, 35 * 60f,
					with(Items.lead, 30, Items.titanium, 60, Items.graphite, 45, Items.silicon, 30)
				)/*,
				new UnitSet(NHUnitTypes.relay, new byte[]{NHUnitTypes.NAVY_LINE_1, 2}, 30 * 60f,
						with(Items.metaglass, 30, Items.titanium, 60, Items.graphite, 30, Items.silicon, 50)
				)*/
			);
		}};
		
		jumpGateJunior = new JumpGate("jump-gate-junior"){{
			size = 5;
			atlasSizeScl = 0.75f;
			squareStroke = 2f;
			health = 6000;
			spawnDelay = 60f;
			spawnReloadTime = 600f;
			range = 300f;
			
			adaptBase = jumpGatePrimary;
			adaptable = true;
			consumePowerCond(30, JumpGateBuild::isCalling);
			
			requirements(Category.units, BuildVisibility.shown, with(
					NHItems.presstanium, 800,
					NHItems.metalOxhydrigen, 300,
					NHItems.juniorProcessor, 600,
					Items.plastanium, 350,
					Items.metaglass, 300,
					Items.thorium, 1000
			));
			
			armor = 10f;
			
			itemCapacity = 1200;
			
			addSets(
				new UnitSet(NHUnitTypes.naxos, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 120 * 60f,
					with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, Items.surgeAlloy, 50, NHItems.metalOxhydrigen, 120)
				),
				new UnitSet(NHUnitTypes.rhino, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
					with(Items.lead, 80, Items.graphite, 60, NHItems.presstanium, 60, NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 60)
				),
				new UnitSet(UnitTypes.mega, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
					with(Items.copper, 80, Items.metaglass, 30, NHItems.presstanium, 40, Items.graphite, 40, NHItems.juniorProcessor, 35)
				),
				new UnitSet(NHUnitTypes.gather, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
					with(Items.thorium, 80, Items.metaglass, 30, NHItems.presstanium, 80, NHItems.zeta, 120, NHItems.juniorProcessor, 80)
				),
				new UnitSet(NHUnitTypes.aliotiat, new byte[]{NHUnitTypes.GROUND_LINE_1, 3}, 55 * 60f,
					with(Items.copper, 120, NHItems.multipleSteel, 50, NHItems.presstanium, 60, NHItems.juniorProcessor, 45)
				),
				new UnitSet(NHUnitTypes.tarlidor, new byte[]{NHUnitTypes.GROUND_LINE_1, 4}, 130 * 60f,
						ItemStack.with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, NHItems.zeta, 250)
				),
				new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
						ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
					with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.zarkov, new byte[]{NHUnitTypes.NAVY_LINE_1, 4}, 140 * 60f,
						ItemStack.with(NHItems.multipleSteel, 500, NHItems.juniorProcessor, 300, NHItems.presstanium, 400, NHItems.metalOxhydrigen, 200)
				)/*,
				new UnitSet(NHUnitTypes.striker, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 150 * 60f,
						ItemStack.with(Items.phaseFabric, 200, NHItems.juniorProcessor, 300, NHItems.presstanium, 350, NHItems.seniorProcessor, 75)
				)*/
			);
		}};
		
		jumpGate = new JumpGate("jump-gate"){{
			consumePowerCond(60, JumpGateBuild::isCalling);
			health = 80000;
			spawnDelay = 30f;
			spawnReloadTime = 300f;
			range = 600f;
			squareStroke = 2.35f;
			size = 8;
			adaptable = false;
			adaptBase = jumpGateJunior;
			
			armor = 20f;
			
			itemCapacity = 3000;
			
			requirements(Category.units, BuildVisibility.shown, with(
				NHItems.presstanium, 1800,
				NHItems.metalOxhydrigen, 800,
				NHItems.seniorProcessor, 800,
				NHItems.multipleSteel, 1000,
				Items.thorium, 2000,
				Items.titanium, 1500,
				Items.phaseFabric, 600,
				NHItems.irayrondPanel, 400
			));
			
			addSets(
				new UnitSet(NHUnitTypes.longinus, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 400 * 60f,
					with(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 250)
				),
				new UnitSet(NHUnitTypes.saviour, new byte[]{NHUnitTypes.OTHERS, 5}, 300 * 60f,
					with(NHItems.setonAlloy, 450, Items.surgeAlloy, 400, NHItems.seniorProcessor, 350, NHItems.thermoCoreNegative, 150, Items.plastanium, 400, NHItems.zeta, 500)
				),
				new UnitSet(NHUnitTypes.declining, new byte[]{NHUnitTypes.NAVY_LINE_1, 5}, 420 * 60f,
					with(NHItems.setonAlloy, 800, NHItems.irayrondPanel, 600, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 300)
				),
				new UnitSet(NHUnitTypes.guardian, new byte[]{NHUnitTypes.OTHERS, 5}, 9600f,
						new ItemStack(NHItems.darkEnergy, 1500)
				),
				new UnitSet(NHUnitTypes.sin, new byte[]{NHUnitTypes.GROUND_LINE_1, 6}, 480 * 60f,
					with(NHItems.setonAlloy, 600, NHItems.upgradeSort, 750, NHItems.seniorProcessor, 300, NHItems.thermoCorePositive, 500, NHItems.presstanium, 1500)
				),
				new UnitSet(NHUnitTypes.anvil, new byte[]{NHUnitTypes.AIR_LINE_2, 6}, 600 * 60f,
					with(NHItems.multipleSteel, 1000, NHItems.setonAlloy, 800, NHItems.upgradeSort, 600, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 750)
				),
				new UnitSet(NHUnitTypes.hurricane, new byte[]{NHUnitTypes.AIR_LINE_1, 6}, 480 * 60f,
					with(NHItems.setonAlloy, 800, NHItems.upgradeSort, 900, NHItems.seniorProcessor, 1200, NHItems.thermoCoreNegative, 500)
				),
				new UnitSet(NHUnitTypes.annihilation, new byte[]{NHUnitTypes.GROUND_LINE_1, 5}, 320 * 60f,
					with(NHItems.setonAlloy, 600, NHItems.irayrondPanel, 900, NHItems.seniorProcessor, 800, NHItems.fusionEnergy, 500)
				)/*,
				new UnitSet(UnitTypes.quell, new byte[]{NHUnitTypes.AIR_LINE_2, 4}, 30 * 60f,
					new ItemStack(NHItems.darkEnergy, 500),
					new ItemStack(NHItems.upgradeSort, 500)
				),
				
				new UnitSet(NHUnitTypes.destruction, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 360 * 60f,
					with(NHItems.setonAlloy, 300, NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 500, NHItems.fusionEnergy, 150)
				)*/
			);
		}};
		
		loadFactories();
		loadTurrets();
		loadEnv();
		loadExperiments();
		
		disposePowerVoid = new PowerVoid("dispose-power-void"){{
			size = 1;
			rebuildable = false;
			requirements(Category.power, BuildVisibility.sandboxOnly, with());
			alwaysUnlocked = true;
		}};
	}
}
