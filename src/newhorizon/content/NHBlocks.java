package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.*;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.EmpBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.gen.Unitc;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.MendProjector;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LaserTurret;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.DecayGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.power.SingleTypeGenerator;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.sandbox.PowerVoid;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.consumers.ConsumeType;
import mindustry.world.draw.DrawArcSmelter;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawMixer;
import mindustry.world.draw.DrawSmelter;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.expand.block.adapt.*;
import newhorizon.expand.block.defence.*;
import newhorizon.expand.block.distribution.RemoteRouter;
import newhorizon.expand.block.distribution.TowardGate;
import newhorizon.expand.block.drawer.DrawFactories;
import newhorizon.expand.block.drawer.DrawHoldLiquid;
import newhorizon.expand.block.drawer.DrawPrinter;
import newhorizon.expand.block.drawer.NHDrawAnimation;
import newhorizon.expand.block.special.*;
import newhorizon.expand.block.turrets.FinalTurret;
import newhorizon.expand.block.turrets.MultTractorBeamTurret;
import newhorizon.expand.block.turrets.ScalableTurret;
import newhorizon.expand.block.turrets.SpeedupTurret;
import newhorizon.expand.bullets.AdaptedContinuousLaserBulletType;
import newhorizon.expand.bullets.AdaptedLaserBulletType;
import newhorizon.expand.bullets.EffectBulletType;
import newhorizon.util.feature.ScreenInterferencer;
import newhorizon.util.func.DrawFunc;

import static arc.graphics.g2d.Lines.lineAngle;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class NHBlocks implements ContentList {

	//Load Mod Factories

	public static Block
		//delivery,
		zetaOre, xenMelter, hyperGenerator, fusionCollapser,
		chargeWall, chargeWallLarge, eoeUpgrader, jumpGate, jumpGateJunior, jumpGatePrimary,
		multiplePresstaniumFactory, presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
		zetaFactoryLarge, zetaFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, irayrondPanelFactorySmall,
		setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory,
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
		multiJunction, multiRouter, multiConduit,
		//Drills
		largeWaterExtractor, beamDrill,
		//Powers
		armorPowerNode, armorBatteryLarge, disposableBattery, radiationGenerator, zetaGenerator,
		//Defence
		largeMendProjector, shapedWall, assignOverdrive, antiBulletTurret, largeShieldGenerator,
		//Special
		playerJumpGate, debuger, payloadEntrance, gravityTrap, hyperspaceWarper, bombLauncher, scrambler, airRaider, configurer, shieldProjector, unitIniter, remoteStorage,
		disposePowerVoid, disposePowerNode, temporaryPowerSource,
	
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
		
//		payloadEntrance = new PayloadEntrance("payload-entrance"){{
//			requirements(Category.effect, with(Items.lead, 200, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.plastanium, 80, Items.surgeAlloy, 75));
//			size = 7;
//		}};
//
//		liquidAndItemBridge = new LiquidAndItemBridge("debug"){{
//			requirements(Category.effect, with(Items.lead, 200, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.plastanium, 80, Items.surgeAlloy, 75));
//			range = 12;
//		}};
//

//		configurer = new Configurer("configurer"){{
//			size = 1;
//			requirements(Category.effect, BuildVisibility.shown, with(Items.lead, 30, NHItems.juniorProcessor, 15, NHItems.presstanium, 15));
//			NHTechTree.add(Blocks.logicProcessor, this);
//		}};
	}
	
	private static void loadTurrets(){
		synchro = new ItemTurret("synchro"){{
			size = 3;
			health = 1420;
			spread = 5f;
			reloadTime = 12f;
			inaccuracy = 0.75f;
			
			recoilAmount = 5f;
			
			coolantMultiplier = 2f;
			
			velocityInaccuracy = 0.075f;
			unitSort = UnitSorts.weakest;
			
			range = 360f;
			
			shootSound = NHSounds.synchro;
			
			shootLength = 16f;
			
			ammo(
				NHItems.zeta, NHBullets.synchroZeta,
				NHItems.fusionEnergy, NHBullets.synchroFusion,
				NHItems.thermoCorePositive, NHBullets.synchroThermoPst,
				Items.phaseFabric, NHBullets.synchroPhase
			);
			
			ammoPerShot = 1;
			maxAmmo = 80;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.metalOxhydrigen, 90, NHItems.juniorProcessor, 60, NHItems.zeta, 120, Items.plastanium, 80));
			
			buildType = () -> new ItemTurretBuild(){
				@Override
				protected void shoot(BulletType type){
					for(int j : Mathf.signs){
						tr.trns(rotation, shootLength, Mathf.range(xRand) + spread * j);
						
						for(int i = 0; i < shots; i++){
							bullet(type, rotation + Mathf.range(inaccuracy + type.inaccuracy));
						}
						
						effects();
					}
					
					shotCounter++;
					
					recoil = recoilAmount;
					heat = 1f;
					
					useAmmo();
				}
			};
		}};
		
		eternity = new FinalTurret("eternity"){{
			size = 16;
			outlineRadius = 7;
			range = 800;
			heatColor = NHColor.darkEnrColor;
			shootLength = 9 * tilesize;
			unitSort = UnitSorts.strongest;
			
			shots = 1;
			burstSpacing = 0;
			velocityInaccuracy = 0;
			inaccuracy = 0;
			
			ammoPerShot = 10;
			coolantMultiplier = 0.8f;
			canOverdrive = false;
			rotateSpeed = 0.25f;
			restitution = 0.0125f;
			
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
			
			recoilAmount = 18f;
			shootShake = 80f;
			shootSound = Sounds.laserblast;
			health = 800000;
			shootCone = 5f;
			maxAmmo = 50;
			consumes.powerCond(800f, FinalTurretBuild::isActive);
			reloadTime = 1800f;
			
			ammo(NHItems.darkEnergy, NHBullets.eternity);
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 5000, NHItems.darkEnergy, 2000));
		}};
		
		antiBulletTurret = new PointDefenseTurret("anti-bullet-turret"){{
			health = 1080;
			size = 3;
			
			color = lightColor = NHColor.lightSkyBack;
			beamEffect = Fx.chainLightning;
			hitEffect = NHFx.square45_4_45;
			shootEffect = NHFx.shootLineSmall(color);
			shootSound = NHSounds.gauss;
			
			range = 280f;
			
			hasPower = true;
			consumes.powerCond(8f, (PointDefenseBuild b) -> b.target != null);
			
			shootLength = 5f;
			bulletDamage = 80f;
			reloadTime = 6f;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 90, NHItems.juniorProcessor, 60, NHItems.presstanium, 120, NHItems.zeta, 120, Items.graphite, 80));
			NHTechTree.add(Blocks.segment, this);
		}};
		
		beamLaserTurret = new ItemTurret("beam-laser-turret"){{
			size = 2;
			requirements(Category.turret, BuildVisibility.shown, with(Items.copper, 60, NHItems.juniorProcessor, 60, NHItems.presstanium, 60));
			NHTechTree.add(Blocks.lancer, this);
			recoilAmount = 2f;
			reloadTime = 30f;
			alternate = true;
			spread = 8f;
			shootSound = Sounds.laser;
			range = 220f;
			shootCone = 30f;
			inaccuracy = 6f;
			maxAmmo = 40;
			health = 600;
			smokeEffect = Fx.shootBigSmoke2;
			consumes.powerCond(3f, TurretBuild::isActive);
			ammo(
				Items.silicon, new AdaptedLaserBulletType(140){{
					colors = new Color[]{Pal.bulletYellowBack.cpy().mul(1f, 1f, 1f, 0.45f), Pal.bulletYellowBack, Color.white};
					hitColor = Pal.bulletYellow;
					length = range + 10f;
					width = 14f;
					ammoMultiplier = 4;
					lengthFalloff = 0.8f;
					sideLength = 25f;
					sideWidth = 0.7f;
					sideAngle = 30f;
					largeHit = false;
					shootEffect = NHFx.square(hitColor, 15f, 2, 8f, 2f);
				}}
			);
		}};
		
		pulseShotgun = new ItemTurret("pulse-shotgun"){{
			health = 960;
			range = 200;
			smokeEffect = Fx.shootBigSmoke;
			shots = 6;
			burstSpacing = 8f;
			reloadTime = 90f;
			recoilAmount = 3f;
			shootCone = 30f;
			inaccuracy = 4f;
			size = 2;
			shootSound = Sounds.shootSnap;
			shootShake = 3f;
			ammo(
				Items.titanium, new BasicBulletType(5, 24){{
					width = 8f;
					height = 25f;
					hitColor = backColor = lightColor = trailColor = Items.titanium.color.cpy().lerp(Color.white, 0.1f);
					frontColor = backColor.cpy().lerp(Color.white, 0.35f);
					hitEffect = NHFx.crossBlast(hitColor, height + width);
					shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 12f, 2f);
					ammoMultiplier = 4;
				}},
				
				Items.plastanium, new BasicBulletType(5, 26){{
					width = 8f;
					height = 25f;
					fragBullets = 4;
					fragBullet = Bullets.fragPlasticFrag;
					ammoMultiplier = 8;
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
					ammoMultiplier = 8;
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
					ammoMultiplier = 8;
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
			maxAmmo = 60;
			ammoPerShot = 6;
			
			requirements(Category.turret, with(Items.copper, 30, Items.graphite, 40, NHItems.presstanium, 50, Items.lead, 60));
			NHTechTree.add(Blocks.salvo, this);
		}};
		
		atomSeparator = new LaserTurret("atom-separator"){{
			health = 12000;
			range = 360f;
			shootEffect = NHFx.hugeSmoke;
			shootCone = 20.0F;
			recoilAmount = 6.0F;
			size = 5;
			shootShake = 4.0F;
			reloadTime = 120.0F;
			
			rotateSpeed = 3f;
			firingMoveFract = 0.15F;
			shootDuration = 200.0F;
			shootSound = Sounds.laserbig;
			loopSound = Sounds.beam;
			loopSoundVolume = 2.0F;
			shootType = new AdaptedContinuousLaserBulletType(300){{
				strokes = new float[]{1f, 0.9f, 0.75f, 0.55f};
				tscales = new float[]{1f, 0.9f, 0.75f, 0.5f};
				shake = 3;
				hitColor = NHColor.lightSkyBack;
				colors = new Color[]{NHColor.lightSkyBack.cpy().mul(0.75f, 0.85f, 1f, 0.65f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.65f), NHColor.lightSkyBack, Color.white};
				width = 16f;
				length = range + 20f;
				oscScl = 0.4f;
				oscMag = 1.5f;
				lifetime = 35f;
				lightning = 2;
				lightningLength = 2;
				lightningLengthRand = 8;
				lightColor = lightningColor =  NHColor.lightSkyBack;
				hitEffect = NHFx.shootCircleSmall(NHColor.lightSkyBack);
				shootEffect = NHFx.lightningHitLarge(NHColor.lightSkyBack);
				lightningDamage = damage / 6f;
			}};
			consumes.add(
				new ConsumeLiquidFilter((liquid) -> liquid.temperature <= 0.5F && liquid.flammability < 0.1F, 0.5F)
			).update(false);
			powerUse = 30f;
			unitSort = (u, x, y) -> u.speed();
			requirements(Category.turret, with(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 200, NHItems.zeta, 150, NHItems.presstanium, 250, NHItems.metalOxhydrigen, 150));
			NHTechTree.add(Blocks.meltdown, this);
		}};
		
		bloodStar = new ItemTurret("blood-star"){{
			size = 5;
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.irayrondPanel, 230, NHItems.zeta, 300, NHItems.seniorProcessor, 200, NHItems.presstanium, 300, Items.thorium, 600));
			NHTechTree.add(Blocks.spectre, this);
			recoilAmount = 5f;
			reloadTime = 120f;
			range = 520f;
			unitSort = (u, x, y) -> -u.hitSize();
			shootSound = Sounds.laserblast;
			inaccuracy = 0f;
			cooldown = 0.01f;
			shootCone = 15f;
			heatColor = Items.surgeAlloy.color.cpy().lerp(Color.white, 0.2f);
			consumes.powerCond(12f, TurretBuild::isActive);
			
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
						shootEffect = NHFx.instShoot(backColor);
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
						despawnEffect = new MultiEffect(NHFx.crossBlast(backColor, 120f), NHFx.instHit(backColor, 3, 80f));
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
		
		shockWaveTurret = new ScalableTurret("shock-wave"){{
			defaultData = NHUpgradeDatas.longRangeShoot;
			recoilAmount = 5f;
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.metalOxhydrigen, 180, NHItems.multipleSteel, 200, NHItems.seniorProcessor, 150, NHItems.presstanium, 120));
			NHTechTree.add(Blocks.foreshadow, this);
			consumes.items(new ItemStack(NHItems.fusionEnergy, 2));
			size = 4;
			health = 3650;
			hasItems = true;
			heatColor = NHColor.lightSkyBack;
			baseColor = NHColor.lightSkyBack.cpy().lerp(Color.white, 0.35f);
			powerUse = 8;
			reloadTime = 300f;
			range = 460f;
			inaccuracy = 0f;
			cooldown = 0.01f;
			shootCone = 15f;
			shootSound = Sounds.laser;
			unitSort = (u, x, y) -> u.speed();
		}};
		
		multipleArtillery = new ItemTurret("multiple-artillery"){{
			size = 4;
			health = 4000;
			range = 360f;
			targetAir = false;
			inaccuracy = 5f;
			spread = 3f;
			velocityInaccuracy = 0.15f;
			minRange = 160f;
			shots = 8;
			reloadTime = 90f;
			coolantMultiplier = 0.95f;
			recoilAmount = 5f;
			shootShake = 6f;
			shootSound = NHSounds.flak;
			ammo(
					NHItems.multipleSteel, NHBullets.artilleryIrd,
					NHItems.fusionEnergy, NHBullets.artilleryFusion,
					NHItems.thermoCorePositive, NHBullets.artilleryThermo,
					Items.plastanium, NHBullets.artilleryPlast,
					Items.phaseFabric, NHBullets.artilleryPhase,
					NHItems.juniorProcessor, NHBullets.artilleryMissile
			);
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.multipleSteel, 180, NHItems.juniorProcessor, 150, Items.plastanium, 200, Items.phaseFabric, 150));
			NHTechTree.add(Blocks.ripple, this);
		}
			@Override
			public void setStats(){
				super.setStats();
				stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
			}
		};
		
		pulseLaserTurret = new SpeedupTurret("pulse-laser-turret"){{
			size = 3;
			health = 1350;
			requirements(Category.turret, ItemStack.with(Items.titanium, 60, NHItems.presstanium, 45, NHItems.zeta, 90, NHItems.juniorProcessor, 40));
			
			powerUse = 7.5f;
			shootType = new BasicBulletType(7f, 50f, NewHorizon.name("circle-bolt")){{
				drag = 0.01f;
				trailColor = backColor = lightColor = lightningColor = NHColor.lightSkyBack;
				frontColor = Color.white;
				lightning = 3;
				lightningLengthRand = 8;
				lightningLength = 2;
				lightningDamage = damage / 2;
				splashDamage = damage / 4;
				splashDamageRadius = 12f;
				hitEffect = NHFx.lightningHitLarge(backColor);
				despawnEffect = NHFx.instHit(backColor, 3, 22f);
				trailChance = 0.35f;
				trailEffect = NHFx.trail;
				trailParam = 4f;
				height = 35f;
				width = 10f;
				knockback = 3f;
				lifetime = 57f;
				shootEffect = NHFx.shootLineSmall(backColor);
				smokeEffect = Fx.shootBigSmoke;
			}};
			inaccuracy = 3f;
			inaccuracyUp = 5f;
			shots = 1;
			shootShake = 2f;
			shootSound = NHSounds.rapidLaser;
			heatColor = Pal.place;
			recoilAmount = 4f;
			reloadTime = 30f;
			slowDownReloadTime = 120f;
			maxSpeedupScl = 4f;
			speedupPerShoot = 0.25f;
			chargeEffect = NHFx.genericCharge(NHColor.lightSkyBack, 4, 120, 28f);
			chargeEffects = 3;
			chargeBeginEffect = NHFx.genericChargeBegin(NHColor.lightSkyBack, 5f, 60f);
			chargeTime = chargeBeginEffect.lifetime;
			range = 240f;
		}};
		
		multipleLauncher = new ItemTurret("multiple-launcher"){{
			size = 3;
			health = 1250;
			requirements(Category.turret, ItemStack.with(Items.plastanium, 60, NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30));
			NHTechTree.add(Blocks.swarmer, this);
			ammo(
					Items.titanium, NHBullets.missileTitanium,
					Items.thorium, NHBullets.missileThorium,
					NHItems.zeta, NHBullets.missileZeta,
					Items.graphite, NHBullets.missile,
					NHItems.presstanium, NHBullets.missileStrike
			);
			smokeEffect = Fx.shootSmallFlame;
			shootEffect = Fx.shootBig2;
			recoilAmount = 3f;
			range = 280f;
			reloadTime = 90f;
			shots = 16;
			maxAmmo = 160;
			ammoPerShot = 1;
			ammoEjectBack = 6f;
			burstSpacing = 2f;
			inaccuracy = 13f;
			xRand = tilesize * size / 3.5f;
			shootSound = Sounds.missile;
			coolantMultiplier = 0.85f;
		}};
		
		gravity = new MultTractorBeamTurret("gravity"){{
			size = 3;
			requirements(Category.turret, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
			NHTechTree.add(Blocks.parallax, this);
			health = 1020;
			maxAttract = 8;
			shootCone = 60f;
			range = 300f;
			hasPower = true;
			force = 18.0F;
			scaledForce = 8.0F;
			shootLength = size * tilesize / 2f - 3;
			damage = 0.15F;
			rotateSpeed = 6f;
			consumes.powerCond(6.0F, (MultTractorBeamBuild e) -> e.target != null);
		}};
		
		railGun = new ItemTurret("rail-gun"){{
			unitSort = (u, x, y) -> -u.speed();
			ammo(
					NHItems.irayrondPanel, NHBullets.railGun1,
					NHItems.setonAlloy, NHBullets.railGun2
			);
			consumes.powerCond(12f, TurretBuild::isActive);
			
			size = 4;
			health = 4550;
			reloadTime = 200f;
			recoilAmount = 6f;
			shootShake = 5f;
			range = 620.0F;
			minRange = 120f;
			rotateSpeed = 1.5f;
			shootCone = 8f;
			
			shootSound = NHSounds.railGunBlast;
			heatColor = NHItems.irayrondPanel.color;
			
			chargeEffects = 2;
			chargeBeginEffect = NHFx.railShoot(heatColor, range, 18, 90, 12);
			chargeEffect = NHFx.genericCharge(heatColor, 13, 90, 90);
			chargeTime = 90;
			
			accurateDelay = true;
			
			buildType = () -> new ItemTurretBuild(){
//				public BulletType useAmmo(){
//					if(cheating()) return peekAmmo();
//
//					AmmoEntry entry = ammo.peek();
//					entry.amount -= ammoPerShot;
//					if(entry.amount <= 0) ammo.pop();
//					totalAmmo -= ammoPerShot;
//					totalAmmo = Math.max(totalAmmo, 0);
//					ejectEffects();
//					return entry.type();
//				}
//
				@Override
				public BulletType peekAmmo(){
					return ammo.any() ? ammo.peek().type() : NHBullets.railGun1;
				}
			};
			
			coolantMultiplier = 0.55f;
			restitution = 0.009f;
			cooldown = 0.009f;
			
			maxAmmo = 16;
			ammoPerShot = 2;
			
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.setonAlloy, 150, NHItems.irayrondPanel, 400, Items.plastanium, 250, NHItems.seniorProcessor, 250, NHItems.multipleSteel, 300, NHItems.zeta, 500, Items.phaseFabric, 175));
			NHTechTree.add(Blocks.foreshadow, this);
		}
			@Override
			public void setStats(){
				super.setStats();
				stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
			}
		};
		
		empTurret = new ItemTurret("emp-turret"){{
			size = 3;
			health = 1060;
			maxAmmo = 30;
			shots = 3;
			spread = 24f;
			burstSpacing = 6f;
			inaccuracy = 6f;
			shootCone = 50f;
			reloadTime = 100f;
			shootSound = Sounds.plasmadrop;
			
			ammo(NHItems.juniorProcessor, NHBullets.empBlot2, NHItems.seniorProcessor, NHBullets.empBlot3);
			requirements(Category.turret, BuildVisibility.shown, with(Items.surgeAlloy, 35, NHItems.multipleSteel, 80, Items.plastanium, 65, NHItems.seniorProcessor, 50, NHItems.zeta, 120));
			NHTechTree.add(Blocks.cyclone, this);
			range = 280f;
		}};
		
		blastTurret = new ItemTurret("blast-turret"){{
			size = 6;
			health = 10200;
			requirements(Category.turret, BuildVisibility.shown, with(Items.surgeAlloy, 250, NHItems.irayrondPanel, 650, Items.plastanium, 375, NHItems.seniorProcessor, 150, NHItems.setonAlloy, 400));
			ammo(
					NHItems.thermoCorePositive, NHBullets.blastEnergyPst, NHItems.thermoCoreNegative, NHBullets.blastEnergyNgt
			);
			spread = 0;
			shots = 8;
			burstSpacing = 4f;
			maxAmmo = 80;
			ammoPerShot = 8;
			xRand = tilesize * (size - 2.225f) / 2;
			reloadTime = 120f;
			shootCone = 50.0F;
			rotateSpeed = 1.5F;
			range = 440.0F;
			inaccuracy = 1;
			heatColor = NHBullets.blastEnergyPst.lightColor;
			recoilAmount = 4.0F;
			shootSound = NHSounds.thermoShoot;
		}};
		
		thermoTurret = new PowerTurret("thermo-turret"){{
			size = 1;
			health = 320;
			
			requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
			NHTechTree.add(Blocks.arc, this);
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
				drawSize = 180f;
				
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
			powerUse = 3.5f;
			shots = 5;
			inaccuracy = 3f;
			burstSpacing = 6f;
			reloadTime = 75f;
			shootCone = 50.0F;
			rotateSpeed = 8.0F;
			targetGround = false;
			range = 200.0F;
			shootEffect = Fx.lightningShoot;
			smokeEffect = Fx.shootSmallSmoke;
			heatColor = Color.red;
			recoilAmount = 1.0F;
			shootSound = NHSounds.thermoShoot;
		}};
		
		divlusion = new PowerTurret("divlusion"){{
			shots = 2;
			burstSpacing = 8f;
			health = 960;
			range = 240;
			shootCone = 30f;
			shootSound = Sounds.laser;
			size = 3;
			reloadTime = 90f;
			recoilAmount = 4f;
			shootType = NHBullets.tear;
			powerUse = 8f;
			requirements(Category.turret, with(NHItems.juniorProcessor, 80, Items.plastanium, 120, Items.thorium, 150, NHItems.presstanium, 50, NHItems.metalOxhydrigen, 20));
		}};
		
		argmot = new SpeedupTurret("argmot"){{
			alternate = true;
			spread = 6f;
			shots = 2;
			health = 960;
			requirements(Category.turret, with(NHItems.multipleSteel, 120, NHItems.juniorProcessor, 80, Items.plastanium, 120));
			maxSpeedupScl = 9f;
			speedupPerShoot = 0.4f;
			powerUse = 8f;
			size = 3;
			range = 240;
			reloadTime = 70f;
			shootCone = 24f;
			shootSound = Sounds.laser;
			shootType = NHBullets.supSky;
		}};
		
		endOfEra = new ScalableTurret("end-of-era"){{
			defaultData = NHUpgradeDatas.posLightning;
			recoilAmount = 7f;
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.upgradeSort, 2000));
			consumes.items(new ItemStack(NHItems.darkEnergy, 4));
			size = 8;
			health = 15000;
			hasItems = true;
			heatColor = baseColor = NHColor.darkEnrColor;
			powerUse = 30;
			reloadTime = 240f;
			range = 800f;
			inaccuracy = 1f;
			cooldown = 0.01f;
			shootCone = 45f;
			shootSound = Sounds.laserbig;
		}};
		
		thurmix = new ItemTurret("thurmix"){{
			requirements(Category.turret, with(Items.phaseFabric, 170, Items.graphite, 355, Items.titanium, 560, NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 300));
			ammo(
					NHItems.fusionEnergy, NHBullets.curveBomb, NHItems.thermoCorePositive, NHBullets.strikeMissile
			);
			targetAir = false;
			size = 5;
			range = 360;
			reloadTime = 75f;
			restitution = 0.03f;
			ammoEjectBack = 3f;
			inaccuracy = 13f;
			cooldown = 0.03f;
			recoilAmount = 3f;
			shootShake = 1f;
			burstSpacing = 3f;
			shots = 4;
			health = 300 * size * size;
			shootSound = Sounds.laser;
			rotateSpeed = 2f;
		}};
		
		loadUpgraders();
	}
	
	private static void loadUpgraders(){
		usualUpgrader = new UpgradeBlock("upgrader-usual"){{
			requirements(Category.effect, BuildVisibility.shown, with(Items.graphite, 180, Items.thorium, 200, NHItems.seniorProcessor, 150, NHItems.presstanium, 120));
			NHTechTree.add(Blocks.commandCenter, this);
			
			linkTarget.add(shockWaveTurret);
			addUpgrades(
					NHUpgradeDatas.longRangeShoot,
					NHUpgradeDatas.longRangeShootRapid,
					NHUpgradeDatas.longRangeShootSplash,
					NHUpgradeDatas.mineShoot
			);
			
			baseColor = NHColor.lightSkyBack.cpy().lerp(Color.white, 0.35f);
			size = 3;
			range = 120f;
			health = 1250;
		}};
		eoeUpgrader = new UpgradeBlock("end-of-era-upgrader"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 3;
			linkTarget.add(endOfEra);
			health = 2350;
			baseColor = NHColor.darkEnrColor;
			addUpgrades(
				NHUpgradeDatas.posLightning,
				NHUpgradeDatas.darkEnrlaser,
				NHUpgradeDatas.arc9000,
				NHUpgradeDatas.curveBomb,
				NHUpgradeDatas.airRaid,
				NHUpgradeDatas.decayLaser,
				NHUpgradeDatas.strikeRocket,
				NHUpgradeDatas.bombStorm
			);
		}};
	}
	
	private static void loadFactories(){
		multiplePresstaniumFactory = new GenericCrafter("multiple-presstanium-factory"){{
			size = 3;
			health = 540;
			requirements(Category.crafting, ItemStack.with(NHItems.presstanium, 80, NHItems.juniorProcessor, 60, Items.thorium, 80));
			craftTime = 60f;
			consumes.power(5);
			consumes.items(with(Items.titanium, 6, Items.graphite, 2));
			consumes.liquid(NHLiquids.zetaFluid, 0.125f);
			outputItems = with(NHItems.presstanium, 8, Items.scrap, 1);
			
			itemCapacity = 30;
			liquidCapacity = 30;
			
			drawer = new DrawFactories(){{
				drawRotator = 3f;
				liquidColor = consumes.<ConsumeLiquid>get(ConsumeType.liquid).liquid.color;
			}
				
				@Override
				public void draw(GenericCrafterBuild entity){
					super.draw(entity);
					if(entity.warmup > 0f){
						Color flameColor = Color.valueOf("ffc999");
						float lightRadius = 60f, lightAlpha = 0.65f, lightSinScl = 10f, lightSinMag = 5;
						float flameRadius = 3f, flameRadiusIn = 1.9f, flameRadiusScl = 5f, flameRadiusMag = 2f, flameRadiusInMag = 1f;
						
						float g = 0.3f;
						float r = 0.06f;
						float cr = Mathf.random(0.1f);
						
						Draw.z(Layer.block + 0.01f);
						
						Draw.alpha(((1f - g) + Mathf.absin(Time.time, 8f, g) + Mathf.random(r) - r) * entity.warmup);
						
						Draw.tint(flameColor);
						Fill.circle(entity.x, entity.y, flameRadius + Mathf.absin(Time.time, flameRadiusScl, flameRadiusMag) + cr);
						Draw.color(1f, 1f, 1f, entity.warmup);
						Fill.circle(entity.x, entity.y, flameRadiusIn + Mathf.absin(Time.time, flameRadiusScl, flameRadiusInMag) + cr);
						
						Draw.color();
					}
				}
			};
		}};
		
		sandCracker = new MultiCrafter("sand-cracker"){{
			size = 2;
			requirements(Category.crafting, ItemStack.with(Items.lead, 40, Items.copper, 60, Items.graphite, 45));
			NHTechTree.add(Blocks.pulverizer, this);
			health = 320;
			craftTime = 45f;
			itemCapacity = 20;
			hasPower = hasItems = true;
			drawer = new DrawArcSmelter();
			consumes.power(1.5f);
			setExchangeMap(Items.copper, 1, 1, Items.lead, 1, 1, Items.titanium, 1, 2, Items.thorium, 1, 3, Items.scrap, 2, 5);
			setOutput(Items.sand);
		}};
		
		oilRefiner = new GenericCrafter("oil-refiner"){{
			size = 2;
			requirements(Category.production, ItemStack.with(Items.metaglass, 30, NHItems.juniorProcessor, 20, Items.copper, 60, NHItems.metalOxhydrigen, 45));
			NHTechTree.add(Blocks.oilExtractor, this);
			health = 200;
			craftTime = 90f;
			liquidCapacity = 60f;
			itemCapacity = 20;
			hasPower = hasLiquids = hasItems = true;
			drawer = new DrawMixer();
			consumes.power(5f);
			consumes.items(new ItemStack(Items.sand, 5));
			outputLiquid = new LiquidStack(Liquids.oil, 15f);
		}};
		
		waterInstancer = new GenericCrafter("water-instancer"){{
			size = 1;
			updateEffect = Fx.smeltsmoke;
			consumes.power(0.5f);
			consumes.liquid(NHLiquids.quantumLiquid, 0.1f);
			outputLiquid = new LiquidStack(Liquids.water, 12f);
			craftTime = 30f;
			requirements(Category.crafting, BuildVisibility.shown, with(Items.metaglass, 15, Items.copper, 30, NHItems.presstanium, 20));
			NHTechTree.add(Blocks.mechanicalPump, this);
		}};
		
		xenMelter = new GenericCrafter("xen-melter"){{
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			itemCapacity = 12;
			liquidCapacity = 24;
			craftTime = 60f;
			drawer = new DrawMixer();
			
			craftEffect = NHFx.lightSkyCircleSplash;
			updateEffect = Fx.smeltsmoke;
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.juniorProcessor, 35, NHItems.metalOxhydrigen, 50, Items.thorium, 30, NHItems.presstanium, 25));
			consumes.power(3f);
			consumes.items(new ItemStack(NHItems.metalOxhydrigen, 4), new ItemStack(NHItems.zeta, 4));
			outputLiquid = new LiquidStack(NHLiquids.xenAlpha, 12f);
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
				requirements(Category.crafting, with(Items.silicon, 45, Items.lead, 115, Items.graphite, 25, Items.titanium, 100));
				hasItems = hasPower = true;
				craftTime = 60f;
				outputItem = new ItemStack(NHItems.presstanium, 2);
				size = 2;
				health = 320;
				craftEffect = Fx.smeltsmoke;
				drawer = new DrawBlock();
				consumes.power(3f);
				consumes.items(new ItemStack(Items.titanium, 2), new ItemStack(Items.graphite, 1));
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
				drawer = new DrawBlock();
				consumes.power(1.5f);
				consumes.item(Items.thorium, 2);
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
				drawer = new DrawBlock();
				consumes.power(3f);
				consumes.item(NHItems.thermoCoreNegative, 1);
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
				drawer = new DrawBlock();
				consumes.power(3f);
				consumes.item(NHItems.thermoCorePositive, 1);
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
				drawer = new DrawSmelter(NHItems.darkEnergy.color);
				
				consumes.items(new ItemStack(NHItems.thermoCoreNegative, 1), new ItemStack(NHItems.thermoCorePositive, 1));
				consumes.power(20f);
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
				drawer = new DrawSmelter(NHItems.fusionEnergy.color);
				consumes.liquid(Liquids.water, 0.3f);
				consumes.items(new ItemStack(NHItems.presstanium, 2), new ItemStack(NHItems.zeta, 6));
				consumes.power(6f);
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
				drawer = new DrawSmelter(NHItems.irayrondPanel.color);
				consumes.liquid(NHLiquids.xenAlpha, 0.1f);
				consumes.items(new ItemStack(NHItems.presstanium, 4), new ItemStack(Items.surgeAlloy, 2));
				consumes.power(2f);
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
				drawer = new DrawSmelter(NHItems.fusionEnergy.color);
				consumes.items(new ItemStack(Items.silicon, 2), new ItemStack(Items.copper, 4));
				consumes.power(2f);
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
				drawer = new DrawSmelter(NHItems.fusionEnergy.color);
				consumes.items(new ItemStack(Items.surgeAlloy, 2), new ItemStack(NHItems.juniorProcessor, 4));
				consumes.power(4f);
			}
		};
		
		irdryonFluidFactory = new GenericCrafter("irdryon-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 20, NHItems.seniorProcessor, 50, NHItems.presstanium, 80, NHItems.irayrondPanel, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 8f);
				craftTime = 60;
				size = 2;
				drawer = new NHDrawAnimation() {
					{
						frameCount = 5;
						frameSpeed = 5f;
						sine = true;
						liquidColor = NHLiquids.irdryonFluid.color;
					}
				};
				itemCapacity = 20;
				hasPower = hasLiquids = hasItems = true;
				consumes.liquid(NHLiquids.xenBeta, 0.075f);
				consumes.items(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.metalOxhydrigen, 4));
				consumes.power(4f);
			}
		};
		
		zetaFluidFactory = new GenericCrafter("zeta-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 50, NHItems.juniorProcessor, 35, NHItems.presstanium, 80, Items.graphite, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.zetaFluid, 15f);
				craftTime = 60f;
				health = 550;
				drawer = new DrawSmelter(NHLiquids.zetaFluid.color);
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				consumes.liquid(Liquids.water, 0.1f);
				consumes.item(NHItems.zeta, 2);
				consumes.power(8f);
			}
		};
		
		metalOxhydrigenFactory = new GenericCrafter("metal-oxhydrigen-factory") {
			{
				requirements(Category.crafting, with(Items.copper, 60, NHItems.juniorProcessor, 30, NHItems.presstanium, 25, Items.thorium, 25));
				craftEffect = Fx.none;
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
				consumes.liquid(Liquids.water, 0.1f);
				consumes.item(Items.lead, 2);
				consumes.power(2f);
			}
		};
		
		thermoCoreFactory = new GenericCrafter("thermo-core-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 150, NHItems.seniorProcessor, 80, NHItems.presstanium, 250, Items.plastanium, 80));
				craftEffect = Fx.plasticburn;
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
				consumes.liquid(NHLiquids.zetaFluid, 0.2f);
				consumes.items(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.fusionEnergy, 4), new ItemStack(NHItems.metalOxhydrigen, 2));
				consumes.power(5f);
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
			consumes.items(new ItemStack(NHItems.setonAlloy, 4), new ItemStack(NHItems.seniorProcessor, 4));
			consumes.power(10f);
		}};
		
		zetaFactoryLarge = new GenericCrafter("large-zeta-factory") {{
			requirements(Category.crafting, with(Items.plastanium, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25));
			outputItem = new ItemStack(NHItems.zeta, 3);
			craftTime = 30f;
			size = 2;
			craftEffect = Fx.formsmoke;
			updateEffect = NHFx.trail;
			hasPower = hasItems = hasLiquids = true;
			drawer = new DrawHoldLiquid();
			consumes.item(Items.thorium, 3);
			consumes.power(7f);
			consumes.liquid(Liquids.water, 0.075f);
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
				consumes.liquid(NHLiquids.xenAlpha, 0.2f);
				consumes.items(new ItemStack(Items.metaglass, 4), new ItemStack(Items.titanium, 2));
				consumes.power(3f);
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
			consumes.liquid(NHLiquids.xenBeta, 0.1f);
			consumes.items(new ItemStack(NHItems.presstanium, 8), new ItemStack(NHItems.metalOxhydrigen, 2));
			consumes.power(12f);
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
			drawer = new DrawHoldLiquid();
			consumes.liquid(NHLiquids.xenAlpha, 0.1f);
			consumes.items(new ItemStack(NHItems.metalOxhydrigen, 6), new ItemStack(Items.thorium, 6), new ItemStack(NHItems.fusionEnergy, 1));
			consumes.power(20f);
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
			drawer = new DrawFactories() {
				{
					liquidColor = NHLiquids.irdryonFluid.color;
					drawTop = true;
					drawRotator = 1f;
					drawRotator2 = -1.5f;
				}
			};
			consumes.liquid(NHLiquids.irdryonFluid, 0.12f);
			consumes.items(new ItemStack(Items.plastanium, 4), new ItemStack(Items.graphite, 6));
			consumes.power(12f);
		}};
		
		xenBetaFactory = new GenericCrafter("xen-beta-factory"){{
			requirements(Category.crafting, with(NHItems.metalOxhydrigen, 35, NHItems.juniorProcessor, 60, Items.plastanium, 20, NHItems.presstanium, 80, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenBeta.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenBeta, 6f);
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
			consumes.liquid(NHLiquids.xenAlpha, 0.1f);
			consumes.item(NHItems.zeta, 2);
			consumes.power(3f);
		}};
		
		xenGammaFactory = new GenericCrafter("xen-gamma-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 70, NHItems.seniorProcessor, 60, Items.surgeAlloy, 20, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenGamma.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenGamma, 12f);
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
			consumes.liquid(NHLiquids.xenBeta, 0.2f);
			consumes.items(new ItemStack(Items.phaseFabric, 2));
			consumes.power(8f);
		}};
	}
	
	
	@Override
	public void load() {
		final int healthMult2 = 4, healthMult3 = 9;
		
		ancientLaserWall = new LaserWallBlock("ancient-laser-wall"){{
			size = 2;
			consumes.powerCond(80f, LaserWallBuild::canActivate);
			health = 8000;
			range = 800;
			
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
			
			requirements(Category.defense, with(NHItems.seniorProcessor, 150, NHItems.upgradeSort, 100, NHItems.zeta, 300));
		}};
		
		laserWall = new LaserWallBlock("laser-wall"){{
			size = 3;
			consumes.powerCond(30f, LaserWallBuild::canActivate);
			health = 4000;
			
			requirements(Category.defense, with(NHItems.juniorProcessor, 120, Items.copper, 350, NHItems.multipleSteel, 80, NHItems.zeta, 180, Items.graphite, 80));
			NHTechTree.add(Blocks.forceProjector, this);
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
			NHTechTree.add(Blocks.pulseConduit, this);
		}};
		
		multiRouter = new Router("multi-router"){{
			size = 1;
			health = 420;
			speed = 2f;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.lead, 5));
			NHTechTree.add(Blocks.router, this);
		}};
		
		multiJunction = new Junction("multi-junction"){{
			size = 1;
			health = 420;
			speed = 12f;
			capacity = 12;
			
			requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.copper, 5));
			NHTechTree.add(Blocks.junction, this);
		}};
		
		remoteRouter = new RemoteRouter("remote-router"){{
			size = 3;
			consumes.power(10);
			requirements(Category.distribution, BuildVisibility.shown, with(NHItems.seniorProcessor, 80, NHItems.multipleSteel, 45, NHItems.zeta, 60, NHItems.presstanium, 40, Items.surgeAlloy, 80));
			health = 450;
			NHTechTree.add(Blocks.router, this);
		}};
		
		beamDrill = new BeamDrill("beam-drill"){{
			size = 4;
			health = 960;
			tier = 6;
			drillTime = 150f;
			liquidBoostIntensity = 1.65f;
			warmupSpeed = 0.001f;
			consumes.power(6);
			consumes.liquid(Liquids.water, 0.1f).optional(true, true);
			requirements(Category.production, BuildVisibility.shown, with(NHItems.juniorProcessor, 60, NHItems.multipleSteel, 45, NHItems.zeta, 60, NHItems.presstanium, 40, Items.lead, 80));
			NHTechTree.add(Blocks.blastDrill, this);
		}};
		
		remoteStorage = new RemoteCoreStorage("remote-vault"){{
			size = 3;
			health = 960;
			consumes.power(10);
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 150, NHItems.multipleSteel, 120));
			NHTechTree.add(Blocks.coreShard, this);
		}};
		
		unitIniter = new UnitSpawner("unit-initer");
		
		shieldProjector = new ShieldProjector("shield-projector"){{
			consumes.power(1f);
			consumes.powerCond(8f, ShieldProjectorBuild::isCharging);
			size = 3;
			itemCapacity = 20;
			consumes.item(NHItems.fusionEnergy, 5);
			requirements(Category.defense, BuildVisibility.shown, with(Items.copper, 300, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.plastanium, 75, NHItems.multipleSteel, 120));
			NHTechTree.add(Blocks.forceProjector, this);
		}};
		
		scrambler = new AirRaider("scrambler"){{
			requirements(Category.effect, with(NHItems.multipleSteel, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 100, Items.plastanium, 100, Items.phaseFabric, 150));
			
			range =  720f;
			
			size = 3;
			consumes.powerCond(8f, AirRaiderBuild::isCharging);
			consumes.items(with(NHItems.juniorProcessor, 2, Items.phaseFabric, 1, NHItems.metalOxhydrigen, 1));
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
				
				hitEffect = new MultiEffect(NHFx.blast(hitColor, radius), NHFx.square(hitColor, 100f, 3, 80f, 8f));
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
						if(u.isPlayer()) ScreenInterferencer.generate(600);
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
		
		airRaider = new AirRaider("air-raider"){{
			requirements(Category.effect, with(NHItems.upgradeSort, 160, NHItems.presstanium, 260, NHItems.seniorProcessor, 120, NHItems.juniorProcessor, 100, Items.phaseFabric, 150));
			
			size = 3;
			consumes.powerCond(6f, AirRaiderBuild::isCharging);
			consumes.item(NHItems.darkEnergy, 2);
			itemCapacity = 16;
			burstSpacing = 15f;
			salvos = 4;
			health = 4500;
			
			triggeredEffect = new Effect(45f, e -> {
				Draw.color(NHColor.darkEnrColor);
				Lines.stroke(e.fout() * 2f);
				Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
			});
			
			bulletHitter = NHBullets.airRaidMissile;
		}};
		
		bombLauncher = new BombLauncher("bomb-launcher"){{
			requirements(Category.effect, with(Items.phaseFabric, 100, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.thorium, 100, Items.surgeAlloy, 75));
			NHTechTree.add(Blocks.massDriver, this);
			size = 3;
			bulletHitter = new EffectBulletType(75f){{
				trailChance = 0.25f;
				trailEffect = NHFx.trail;
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
				hitEffect = despawnEffect = new MultiEffect(NHFx.blast(hitColor, splashDamageRadius * 1.5f), NHFx.crossBlast(hitColor, splashDamageRadius * 1.25f));
			}};
			consumes.powerCond(6f, BombLauncherBuild::isCharging);
			consumes.item(NHItems.fusionEnergy, 2);
			itemCapacity = 16;
			health = 900;
		}};
		
		hyperspaceWarper = new HyperSpaceWarper("hyper-space-warper"){{
			size = 4;
			health = 2250;
			
			hasPower = hasItems = true;
			itemCapacity = 20;
			consumes.item(NHItems.fusionEnergy, 5);
			consumes.power(12f);
			
			requirements(Category.units, BuildVisibility.shown, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 200, NHItems.presstanium, 450, NHItems.zeta, 200));
		}};
		
		gravityTrap = new GravityTrap("gravity-gully"){{
			size = 3;
			health = 1250;
			
			consumes.power(8f);
			requirements(Category.units, BuildVisibility.shown, with(Items.plastanium, 80, NHItems.multipleSteel, 80, NHItems.juniorProcessor, 80, Items.copper, 200));
			NHTechTree.add(hyperspaceWarper, this);
		}};
		
		irdryonTank = new LiquidRouter("irdryon-tank"){{
			requirements(Category.liquid, with(NHItems.metalOxhydrigen, 25, NHItems.multipleSteel, 40, Items.metaglass, 25));
			NHTechTree.add(Blocks.liquidTank, this);
			size = 3;
			liquidCapacity = 2500.0F;
			health = 2500;
		}};
		
		assignOverdrive = new AssignOverdrive("assign-overdrive"){{
			requirements(Category.effect, with(NHItems.irayrondPanel, 120, NHItems.presstanium, 160, NHItems.juniorProcessor, 100, Items.plastanium, 80, Items.surgeAlloy, 75));
			NHTechTree.add(Blocks.overdriveProjector, this);
			consumes.power(14.0F);
			size = 3;
			range = 240.0F;
			speedBoost = 4f;
			useTime = 300.0F;
			hasBoost = true;
			consumes.item(Items.phaseFabric).boost();
			consumes.liquid(NHLiquids.xenBeta, 0.1f);
		}};
		
		largeMendProjector = new MendProjector("large-mend-projector"){{
			size = 3;
			reload = 180f;
			useTime = 600f;
			healPercent = 15;
			requirements(Category.effect, with(NHItems.presstanium, 60, NHItems.juniorProcessor, 50, Items.plastanium, 40, Items.thorium, 80));
			NHTechTree.add(Blocks.mendProjector, this);
			consumes.power(2F);
			range = 160.0F;
			phaseBoost = 12f;
			phaseRangeBoost = 60.0F;
			health = 980;
			consumes.item(NHItems.juniorProcessor).boost();
		}};
		
		playerJumpGate = new PlayerJumpGate("player-jump-gate"){{
			requirements(Category.effect, ItemStack.with(Items.titanium, 60, NHItems.presstanium, 45, NHItems.zeta, 120, NHItems.juniorProcessor, 50));
			NHTechTree.add(Blocks.massDriver, this);
			size = 3;
			consumes.power(5f);
		}};
		
		radiationGenerator = new DecayGenerator("radiation-generator"){{
			requirements(Category.power, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
			NHTechTree.add(Blocks.rtgGenerator, this);
			size = 2;
			powerProduction = 2.5F;
			heatColor = NHColor.lightSkyBack.mul(1.1f);
			itemDuration = 480.0F;
		}};

		zetaGenerator = new SingleTypeGenerator("zeta-generator"){{
			requirements(Category.power,ItemStack.with(NHItems.metalOxhydrigen, 120, NHItems.juniorProcessor, 80, Items.plastanium, 80, NHItems.zeta,100, Items.copper, 150));
			NHTechTree.add(Blocks.thoriumReactor,this);
			size = 3;
			powerProduction = 60f;
			ambientSound = Sounds.hum;
			ambientSoundVolume = 0.24F;
			itemCapacity = 30;
			liquidCapacity = 30;
			itemDuration = 150f;
			consumes.item(NHItems.zeta, 3);
			consumes.liquid(Liquids.cryofluid,0.1f);
			
			lightColor = heatColor = NHItems.zeta.color.cpy().lerp(Color.white, 0.125f);
			generateEffect = NHFx.square(heatColor, 30f, 5, 20f, 4);
			explodeEffect = NHFx.lightningHitLarge(heatColor);
		}};
		
		disposableBattery = new DisposableBattery("disposable-battery"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.fusionEnergy, 15, NHItems.juniorProcessor, 10, NHItems.presstanium, 40));
			NHTechTree.add(Blocks.battery, this);
			size = 2;
			consumption = 8;
			consumes.powerBuffered(45000f);
		}};
		
		armorBatteryLarge = new Battery("large-armor-battery"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 40, NHItems.juniorProcessor, 10, Items.lead, 40));
			size = 3;
			health = 3000;
			consumes.powerBuffered(45000.0F);
		}};
		
		armorPowerNode = new PowerNode("armor-power-node"){{
			requirements(Category.power, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 25, NHItems.juniorProcessor, 5, Items.lead, 25));
			NHTechTree.add(Blocks.powerNodeLarge, this);
			size = 2;
			maxNodes = 12;
			laserRange = 8.5F;
			health = 1650;
		}};
		
		largeWaterExtractor = new SolidPump("large-water-extractor"){{
			size = 3;
			pumpAmount = 0.3f;
			requirements(Category.production, ItemStack.with(NHItems.presstanium, 50, NHItems.juniorProcessor, 45, Items.thorium, 60, Items.metaglass, 30));
			NHTechTree.add(Blocks.waterExtractor, this);
			result = Liquids.water;
			liquidCapacity = 60.0F;
			rotateSpeed = 1.4F;
			attribute = Attribute.water;
			consumes.power(4f);
		}};
		
		rapidUnloader = new AdaptUnloader("rapid-unloader"){{
			speed = 0.5f;
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.presstanium, 20, Items.lead, 15, NHItems.juniorProcessor, 25));
			NHTechTree.add(Blocks.unloader, this);
		}};
		
		towardGate = new TowardGate("toward-gate"){{
			speed = 80;
			requirements(Category.distribution, BuildVisibility.shown, with(Items.titanium, 5, Items.copper, 10, Items.silicon, 5));
			NHTechTree.add(Blocks.sorter, this);
		}};
		
		multiEfficientConveyor = new Conveyor("multi-efficient-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.multipleSteel, 2));
			NHTechTree.add(Blocks.titaniumConveyor, this);
			speed = 0.16f;
			displayedSpeed = 18f;
			health = 120;
			junctionReplacement = multiJunction;
		}};
		
		multiArmorConveyor = new ArmoredConveyor("multi-armor-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2, NHItems.multipleSteel, 2, Items.thorium, 1));
			NHTechTree.add(Blocks.armoredConveyor, this);
			speed = 0.16f;
			displayedSpeed = 18f;
			health =  320;
			junctionReplacement = multiJunction;
		}};

		multiConveyor = new StackConveyor("multi-conveyor"){{
			requirements(Category.distribution,with(NHItems.zeta, 2,NHItems.irayrondPanel, 2, NHItems.juniorProcessor, 1));
			NHTechTree.add(Blocks.plastaniumConveyor, this);
			speed = 0.125f;
			health = 320;
			itemCapacity = 20;
			recharge = 1f;
			
			loadEffect = unloadEffect = new Effect(30f, e -> {
				Lines.stroke(1.5f * e.fout(Interp.pow2Out), NHItems.multipleSteel.color);
				Lines.square(e.x, e.y, tilesize / 8f * Mathf.sqrt2 * (e.fin(Interp.pow2Out) * 3 + 1f), 45f);
			});
		}};
		
		fusionCollapser = new AdaptImpactReactor("fusion-collapser"){{
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.09F;
			size = 5;
			itemCapacity = 20;
			liquidCapacity = 40;
			health = 2400;
			powerProduction = 250f;
			itemDuration = 90f;
			
			consumes.power(30.0F);
			consumes.items(new ItemStack(NHItems.fusionEnergy, 2));
			consumes.liquid(NHLiquids.xenBeta, 0.1f);
			requirements(Category.power, BuildVisibility.shown, with(Items.thorium, 600, NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 150, NHItems.presstanium, 250, Items.graphite, 450, Items.metaglass, 200));
			NHTechTree.add(Blocks.impactReactor, this);
		}};
		
		hyperGenerator = new HyperGenerator("hyper-generator"){{
			size = 8;
			health = 12500;
			powerProduction = 1250f;
			updateLightning = updateLightningRand = 3;
			effectColor = NHColor.thermoPst;
			itemCapacity = 40;
			itemDuration = 180f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.1F;
			consumes.power(50.0F);
			consumes.items(new ItemStack(NHItems.metalOxhydrigen, 8), new ItemStack(NHItems.thermoCorePositive, 4));
			consumes.liquid(NHLiquids.zetaFluid, 0.25F);
			requirements(Category.power, BuildVisibility.shown, with(NHItems.upgradeSort, 1000, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250, Items.metaglass, 250));
			NHTechTree.add(fusionCollapser, this);
		}};
		
		insulatedWall = new Wall("insulated-wall"){{
			size = 1;
			health = 300;
			requirements(Category.defense, with(Items.titanium, 10, Items.copper, 5));
			insulated = true;
			absorbLasers = true;
		}};
		
		setonWall = new Wall("seton-wall"){{
			size = 1;
			health = 1250;
			chanceDeflect = 10.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5, NHItems.irayrondPanel, 10, Items.silicon, 15, NHItems.presstanium, 15));
		}};
		
		setonWallLarge = new Wall("seton-wall-large"){{
			size = 2;
			health = 1250 * healthMult2;
			chanceDeflect = 10.0F;
			flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5 * healthMult2, NHItems.irayrondPanel, 10 * healthMult2, Items.silicon, 15 * healthMult2, NHItems.presstanium, 15 * healthMult2));
		}};
		
		heavyDefenceWall = new Wall("heavy-defence-wall"){{
			size = 1;
			health = 1750;
			absorbLasers = insulated = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20));
		}};
		
		heavyDefenceWallLarge = new Wall("heavy-defence-wall-large"){{
			size = 2;
			health = 1750 * healthMult2;
			absorbLasers = insulated = true;
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
			shieldHealth = 16000f;
			cooldownNormal = 5f;
			cooldownLiquid = 4f;
			cooldownBrokenBase = 2.7f;
			consumes.item(NHItems.fusionEnergy).boost();
			phaseUseTime = 180.0F;
			phaseRadiusBoost = 100.0F;
			phaseShieldBoost = 8000.0F;
			consumes.power(12F);
			requirements(Category.effect, with(NHItems.seniorProcessor, 150, Items.lead, 250, Items.graphite, 180, NHItems.presstanium, 150, NHItems.fusionEnergy, 80, NHItems.irayrondPanel, 50));
		}};
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.defense, with(NHItems.irayrondPanel, 10, NHItems.seniorProcessor, 5, NHItems.upgradeSort, 15));
			size = 1;
			absorbLasers = true;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSkyBack;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.defense, ItemStack.mult(chargeWall.requirements, healthMult2));
			size = 2;
			absorbLasers = true;
			range = 200;
            health = 1350 * healthMult2;
            effectColor = NHColor.lightSkyBack;
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
        
        blaster = new StaticChargeBlaster("blaster"){{
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
            lightningColor = heatColor = NHColor.darkEnrColor;
            generateEffect = NHFx.blastgenerate;
            acceptEffect = NHFx.blastAccept;
            blastSound = Sounds.explosionbig;
            status = NHStatusEffects.emp2;
            range = 240;
            health = 1200;
            knockback = 10f;
            consumes.power(8f);
            itemCapacity = 30;
            consumes.item(NHItems.zeta, 3);
        }};
		
		jumpGatePrimary = new JumpGate("jump-gate-primary"){{
			size = 3;
			atlasSizeScl = 0.55f;
			squareStroke = 1.75f;
			health = 1800;
			spawnDelay = 90f;
			spawnReloadTime = 750f;
			range = 160f;
			
			consumes.power(8f);
			
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
				),
				new UnitSet(NHUnitTypes.relay, new byte[]{NHUnitTypes.NAVY_LINE_1, 2}, 30 * 60f,
						with(Items.metaglass, 30, Items.titanium, 60, Items.graphite, 30, Items.silicon, 50)
				)
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
			consumes.power(25f);
			
			requirements(Category.units, BuildVisibility.shown, with(
					NHItems.presstanium, 800,
					NHItems.metalOxhydrigen, 300,
					NHItems.juniorProcessor, 600,
					Items.plastanium, 350,
					Items.metaglass, 300,
					Items.thorium, 1000
			));
			
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
				new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
					with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
				),
				new UnitSet(NHUnitTypes.zarkov, new byte[]{NHUnitTypes.NAVY_LINE_1, 4}, 140 * 60f,
						ItemStack.with(NHItems.multipleSteel, 500, NHItems.juniorProcessor, 300, NHItems.presstanium, 400, NHItems.metalOxhydrigen, 200)
				),
				new UnitSet(NHUnitTypes.tarlidor, new byte[]{NHUnitTypes.GROUND_LINE_1, 4}, 130 * 60f,
						ItemStack.with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, NHItems.zeta, 250)
				),
				new UnitSet(NHUnitTypes.striker, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 150 * 60f,
						ItemStack.with(Items.phaseFabric, 200, NHItems.juniorProcessor, 300, NHItems.presstanium, 350, NHItems.seniorProcessor, 75)
				),
				new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
						ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
				)
			);
		}};
		
		jumpGate = new JumpGate("jump-gate"){{
			consumes.power(60f);
			health = 50000;
			spawnDelay = 30f;
			spawnReloadTime = 300f;
			range = 600f;
			squareStroke = 2.35f;
			size = 8;
			adaptable = true;
			adaptBase = jumpGateJunior;
			
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
				new UnitSet(NHUnitTypes.saviour, new byte[]{NHUnitTypes.OTHERS, 5}, 400 * 60f,
					with(NHItems.setonAlloy, 250, Items.surgeAlloy, 200, NHItems.seniorProcessor, 150, NHItems.thermoCoreNegative, 100, Items.plastanium, 200, NHItems.zeta, 500)
				),
				new UnitSet(NHUnitTypes.collapser, new byte[]{NHUnitTypes.AIR_LINE_2, 7}, 600 * 60f,
					new ItemStack(NHItems.darkEnergy, 2500),
					new ItemStack(NHItems.upgradeSort, 2500)
				),
				new UnitSet(NHUnitTypes.anvil, new byte[]{NHUnitTypes.AIR_LINE_2, 6}, 600 * 60f,
					with(NHItems.zeta, 1000, NHItems.setonAlloy, 500, NHItems.upgradeSort, 200, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 400)
				),
				new UnitSet(NHUnitTypes.guardian, new byte[]{NHUnitTypes.OTHERS, 5}, 9600f,
					new ItemStack(NHItems.darkEnergy, 1200)
				),
				new UnitSet(NHUnitTypes.annihilation, new byte[]{NHUnitTypes.GROUND_LINE_1, 5}, 320 * 60f,
					with(NHItems.setonAlloy, 200, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 400, NHItems.fusionEnergy, 100)
				),
				new UnitSet(NHUnitTypes.longinus, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 400 * 60f,
					with(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 200)
				),
				new UnitSet(NHUnitTypes.hurricane, new byte[]{NHUnitTypes.AIR_LINE_1, 6}, 480 * 60f,
					with(NHItems.setonAlloy, 800, NHItems.upgradeSort, 300, NHItems.seniorProcessor, 800, NHItems.thermoCoreNegative, 500)
				),
				new UnitSet(NHUnitTypes.sin, new byte[]{NHUnitTypes.GROUND_LINE_1, 6}, 480 * 60f,
						with(NHItems.setonAlloy, 600, NHItems.upgradeSort, 200, NHItems.seniorProcessor, 500, NHItems.thermoCorePositive, 500, NHItems.irayrondPanel, 300)
				),
				new UnitSet(NHUnitTypes.declining, new byte[]{NHUnitTypes.NAVY_LINE_1, 5}, 420 * 60f,
					with(NHItems.setonAlloy, 500, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 300, NHItems.thermoCoreNegative, 300)
				),
				new UnitSet(NHUnitTypes.destruction, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 360 * 60f,
					with(NHItems.setonAlloy, 300, NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 500, NHItems.fusionEnergy, 150)
				)
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
		
		disposePowerNode = new DisposePowerNode("dispose-power-node"){{
			size = 1;
			rebuildable = false;
			requirements(Category.power, BuildVisibility.sandboxOnly, with());
			alwaysUnlocked = true;
			laserRange = 800f;
			clipSize = 1700f;
			maxNodes = 5;
			laserColor1 = NHColor.lightSkyFront;
			laserColor2 = NHColor.lightSkyBack;
		}};
		
		temporaryPowerSource = new TemporaryPowerSource("temporary-power-source"){{
			requirements(Category.power, BuildVisibility.sandboxOnly, with());
			size = 1;
			alwaysUnlocked = true;
		}};
	}
}
