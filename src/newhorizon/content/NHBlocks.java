package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import mindustry.content.*;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
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
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.power.DecayGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericSmelter;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawMixer;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import newhorizon.NewHorizon;
import newhorizon.block.adapt.AdaptImpactReactor;
import newhorizon.block.adapt.AdaptUnloader;
import newhorizon.block.adapt.DisposableBattery;
import newhorizon.block.drawer.DrawFactories;
import newhorizon.block.drawer.DrawHoldLiquid;
import newhorizon.block.drawer.DrawPrinter;
import newhorizon.block.drawer.NHDrawAnimation;
import newhorizon.block.special.*;
import newhorizon.block.turrets.MultTractorBeamTurret;
import newhorizon.block.turrets.ScalableTurret;
import newhorizon.block.turrets.SpeedupTurret;
import newhorizon.bullets.NHTrailBulletType;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class NHBlocks implements ContentList {

	//Load Mod Factories

	public static Block
		delivery, zetaOre, xenMelter, hyperGenerator, fusionCollapser,
		largeShieldGenerator,
		chargeWall, chargeWallLarge, eoeUpgrader, jumpGate, jumpGateJunior,
		//Turrets
		blaster, endOfEra, thurmix, argmot, thermoTurret, railGun, divlusion, blastTurret, empTurret, gravity, multipleLauncher, pulseLaserTurret, multipleArtillery,
		presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
		zetaFactoryLarge, zetaFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, irayrondPanelFactorySmall,
		setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory,
		thermoCorePositiveFactory, thermoCoreNegativeFactory, thermoCoreFactory, irdryonVault,
		//Liquids factories
		irdryonFluidFactory, xenBetaFactory, xenGammaFactory, zetaFluidFactory, oilRefiner,
		//walls
		insulatedWall, setonWall, setonWallLarge, heavyDefenceWall, heavyDefenceWallLarge, heavyDefenceDoor, heavyDefenceDoorLarge,
		//Distributions
		towardGate, rapidUnloader,
		//Drills
		largeWaterExtractor,
		//Powers
		armorPowerNode, armorBatteryLarge, disposableBattery, radiationGenerator,
		//Defence
		largeMendProjector, shapedWall,
		//Special
		playerJumpGate, debuger
		;

	@Override
	public void load() {
		final int healthMult2 = 4, healthMult3 = 9;
		shapedWall = new ShapedWall("debug"){{
			requirements(Category.effect, with(NHItems.presstanium, 60, NHItems.juniorProcessor, 50, Items.plastanium, 40, Items.thorium, 80));
			
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
		
		
		multipleArtillery = new ItemTurret("multiple-artillery"){{
			size = 4;
			health = 4000;
			range = 400f;
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
			shootSound = Sounds.explosionbig;
			ammo(
				NHItems.irayrondPanel, NHBullets.artilleryIrd,
				NHItems.fusionEnergy, NHBullets.artilleryFusion,
				NHItems.thermoCorePositive, NHBullets.artilleryThermo,
				Items.plastanium, NHBullets.artilleryPlast,
				Items.phaseFabric, NHBullets.artilleryPhase,
				NHItems.juniorProcessor, NHBullets.artilleryMissile
			);
			//consumes.powerCond(8f, TurretBuild::isActive);
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.metalOxhydrigen, 250, Items.thorium, 400, NHItems.seniorProcessor, 150, Items.plastanium, 300, Items.phaseFabric, 150));
			NHTechTree.add(Blocks.ripple, this);
		}
			@Override
			public void setStats(){
				super.setStats();
				stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
			}
		};
		
		playerJumpGate = new PlayerJumpGate("player-jump-gate"){{
			requirements(Category.effect, ItemStack.with(Items.titanium, 60, NHItems.presstanium, 45, NHItems.zeta, 120, NHItems.juniorProcessor, 50));
			NHTechTree.add(Blocks.massDriver, this);
			size = 3;
			consumes.power(5f);
		}};
		
		pulseLaserTurret = new SpeedupTurret("pulse-laser-turret"){{
			size = 3;
			health = 1350;
			requirements(Category.turret, ItemStack.with(Items.titanium, 60, NHItems.presstanium, 45, NHItems.zeta, 90, NHItems.juniorProcessor, 40));
			NHTechTree.add(Blocks.lancer, this);
			powerUse = 7.5f;
			shootType = new BasicBulletType(7f, 50f, NewHorizon.configName("circle-bolt")){{
				drag = 0.01f;
				trailColor = backColor = lightColor = lightningColor = NHColor.lightSky;
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
			shootSound = Sounds.laser;
			heatColor = Pal.place;
			recoilAmount = 4f;
			reloadTime = 30f;
			slowDownReloadTime = 120f;
			maxSpeedupScl = 4f;
			speedupPerShoot = 0.25f;
			chargeEffect = NHFx.genericCharge(NHColor.lightSky, 4, 120, 28f);
			chargeEffects = 3;
			chargeBeginEffect = NHFx.genericChargeBegin(NHColor.lightSky, 5f, 60f);
			chargeTime = chargeBeginEffect.lifetime;
			range = 280f;
		}};
		
		multipleLauncher = new ItemTurret("multiple-launcher"){{
			size = 3;
			health = 1250;
			requirements(Category.turret, ItemStack.with(Items.plastanium, 60, NHItems.presstanium, 45, NHItems.metalOxhydrigen, 45, NHItems.juniorProcessor, 30, Items.phaseFabric, 50));
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
			consumes.items(new ItemStack(Items.sand, 6));
			outputLiquid = new LiquidStack(Liquids.oil, 15f);
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
		
		radiationGenerator = new DecayGenerator("radiation-generator"){{
			requirements(Category.power, ItemStack.with(Items.metaglass, 35, NHItems.juniorProcessor, 15, Items.lead, 80, NHItems.presstanium, 45));
			NHTechTree.add(Blocks.rtgGenerator, this);
			size = 2;
			powerProduction = 2.65F;
			heatColor = NHColor.lightSky.mul(1.1f);
			itemDuration = 480.0F;
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
			attribute = null;
			consumes.power(4f);
		}};
		
		railGun = new ItemTurret("rail-gun"){{
			unitSort = (u, x, y) -> -u.maxHealth;
			size = 4;
			health = 4550;
			reloadTime = 200f;
			recoilAmount = 6f;
			shootShake = 6f;
			range = 620.0F;
			shootSound = NHSounds.railGunBlast;
			chargeSound = NHSounds.railGunCharge;
			chargeEffects = 1;
			heatColor = NHItems.irayrondPanel.color;
			chargeEffect = NHFx.chargeEffectSmall(heatColor, 132f);
			chargeBeginEffect = NHFx.chargeBeginEffect(heatColor, 10, chargeEffect.lifetime);
			chargeTime = chargeEffect.lifetime;
			ammo(
				NHItems.irayrondPanel, NHBullets.railGun1,
				NHItems.setonAlloy, NHBullets.railGun2
			);
			minRange = 120f;
			rotateSpeed = 1f;
			shootCone = 8f;
			coolantMultiplier = 0.55f;
			restitution = 0.009f;
			cooldown = 0.009f;
			ammoUseEffect = Fx.casing3Double;
			consumes.powerCond(12f, TurretBuild::isActive);
			requirements(Category.turret, BuildVisibility.shown, with(NHItems.setonAlloy, 150, NHItems.irayrondPanel, 400, Items.plastanium, 250, NHItems.seniorProcessor, 250, NHItems.multipleSteel, 300, NHItems.zeta, 500, Items.phaseFabric, 175));
			NHTechTree.add(Blocks.foreshadow, this);
		}
			@Override
			public void setStats(){
				super.setStats();
				stats.add(Stat.shootRange, minRange / tilesize, StatUnit.blocks);
			}
		};
		
		rapidUnloader = new AdaptUnloader("rapid-unloader"){{
			speed = 5f;
			requirements(Category.effect, BuildVisibility.shown, with(NHItems.presstanium, 20, Items.lead, 15, NHItems.juniorProcessor, 25));
			NHTechTree.add(Blocks.unloader, this);
		}};
		
		towardGate = new TowardGate("toward-gate"){{
			speed = 80;
			requirements(Category.distribution, BuildVisibility.shown, with(Items.titanium, 5, Items.copper, 10, Items.silicon, 5));
			NHTechTree.add(Blocks.sorter, this);
		}};
		
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
			requirements(Category.turret, BuildVisibility.shown, with(Items.surgeAlloy, 75, NHItems.irayrondPanel, 100, Items.plastanium, 175, NHItems.seniorProcessor, 150, NHItems.zeta, 250));
			NHTechTree.add(Blocks.cyclone, this);
			range = 280f;
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
			consumes.liquid(NHLiquids.xenBeta, 0.1f * 5 / 6);
			requirements(Category.power, BuildVisibility.shown, with(Items.thorium, 600, NHItems.irayrondPanel, 350, NHItems.seniorProcessor, 200, NHItems.presstanium, 850, Items.surgeAlloy, 250, Items.metaglass, 250));
			NHTechTree.add(Blocks.impactReactor, this);
		}};
		
		hyperGenerator = new HyperGenerator("hyper-generator"){{
			size = 8;
			health = 12500;
			powerProduction = 1250f;
			updateLightning = updateLightningRand = 3;
			effectColor = NHItems.thermoCorePositive.color;
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
		
		blastTurret = new ItemTurret("blast-turret"){{
			size = 6;
			health = 10200;
			requirements(Category.turret, BuildVisibility.shown, with(Items.surgeAlloy, 250, NHItems.irayrondPanel, 650, Items.plastanium, 375, NHItems.seniorProcessor, 150, NHItems.setonAlloy, 400));
			ammo(
					NHItems.thermoCorePositive, NHBullets.blastEnergyPst, NHItems.thermoCoreNegative, NHBullets.blastEnergyNgt
			);
			shots = 8;
			burstSpacing = 4f;
			maxAmmo = 80;
			ammoPerShot = 8;
			xRand = tilesize * (size - 2.225f) / 2;
			reloadTime = 120f;
			shootCone = 50.0F;
			rotateSpeed = 1.5F;
			range = 440.0F;
			heatColor = NHBullets.blastEnergyPst.lightColor;
			recoilAmount = 4.0F;
			shootSound = NHSounds.rapidLaser;
		}};
		
		thermoTurret = new PowerTurret("thermo-turret"){{
			size = 1;
			health = 320;
			requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
			NHTechTree.add(Blocks.arc, this);
			shootType = new NHTrailBulletType(6.5f, 18f){{
				hitEffect = new Effect(12.0F, (e) -> {
					Draw.color(Pal.lancerLaser, Color.white, e.fout() * 0.75f);
					Lines.stroke(e.fout() * 1.5F);
					Angles.randLenVectors(e.id, 3, e.finpow() * 17.0F, e.rotation, 360.0F, (x, y) -> {
						float ang = Mathf.angle(x, y);
						Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4.0F + 1.0F);
					});
				});
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
			shootSound = Sounds.laser;
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
			absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20));
		}};
		
		heavyDefenceWallLarge = new Wall("heavy-defence-wall-large"){{
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
		
		delivery = new Delivery("mass-deliver"){{
			size = 3;
			shake = 3f;
			itemCapacity = 300;
			consumes.power(5f);
			requirements(Category.distribution, with(NHItems.seniorProcessor, 80, Items.plastanium, 120, Items.thorium, 150, NHItems.presstanium, 50, NHItems.metalOxhydrigen, 120));
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

		largeShieldGenerator = new ForceProjector("large-shield-generator") {{
			size = 4;
			radius = 220f;
			shieldHealth = 16000f;
			cooldownNormal = 5f;
			cooldownLiquid = 4f;
			cooldownBrokenBase = 2.7f;
			basePowerDraw = 2f;
			consumes.item(NHItems.fusionEnergy).boost();
			phaseUseTime = 180.0F;
			phaseRadiusBoost = 100.0F;
			phaseShieldBoost = 8000.0F;
			consumes.power(25F);
			requirements(Category.effect, with(NHItems.seniorProcessor, 150, Items.lead, 250, Items.graphite, 180, NHItems.presstanium, 150, NHItems.fusionEnergy, 80, NHItems.irayrondPanel, 50));
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

		//Smelters

		darkEnergyFactory = new GenericSmelter("dark-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.irayrondPanel, 60, NHItems.setonAlloy, 30, NHItems.seniorProcessor, 60));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.darkEnergy, 2);
				craftTime = 90f;
				size = 2;
				hasPower = hasItems = true;
				flameColor = NHItems.darkEnergy.color;

				consumes.items(new ItemStack(NHItems.upgradeSort, 2), new ItemStack(NHItems.thermoCoreNegative, 1), new ItemStack(NHItems.thermoCorePositive, 1));
				consumes.power(15f);
			}
		};

		fusionEnergyFactory = new GenericSmelter("fusion-core-energy-factory") {
			{
				requirements(Category.crafting, with(NHItems.juniorProcessor, 60, NHItems.presstanium, 50, Items.thorium, 60, Items.graphite, 30));
				craftEffect = Fx.smeltsmoke;
				outputItem = new ItemStack(NHItems.fusionEnergy, 3);
				craftTime = 90f;
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				flameColor = NHItems.fusionEnergy.color;
				consumes.liquid(Liquids.water, 0.3f);
				consumes.items(new ItemStack(NHItems.presstanium, 2), new ItemStack(NHItems.zeta, 6));
				consumes.power(6f);
			}
		};

		irayrondPanelFactory = new GenericSmelter("irayrond-panel-factory") {
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
				flameColor = NHItems.irayrondPanel.color;
				consumes.liquid(NHLiquids.xenAlpha, 0.1f);
				consumes.items(new ItemStack(NHItems.presstanium, 4), new ItemStack(Items.surgeAlloy, 2));
				consumes.power(2f);
			}
		};

		juniorProcessorFactory = new GenericSmelter("processor-junior-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 40, NHItems.presstanium, 30, Items.copper, 25, Items.lead, 25));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.juniorProcessor, 3);
				craftTime = 120f;
				size = 2;
				hasPower = hasLiquids = hasItems = true;
				flameColor = NHItems.fusionEnergy.color;
				consumes.items(new ItemStack(Items.silicon, 2), new ItemStack(Items.copper, 4));
				consumes.power(2f);
			}
		};

		seniorProcessorFactory = new GenericSmelter("processor-senior-factory") {
			{
				requirements(Category.crafting, with(Items.surgeAlloy, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25, Items.thorium, 25));
				craftEffect = Fx.none;
				outputItem = new ItemStack(NHItems.seniorProcessor, 4);
				craftTime = 120f;
				size = 2;
				hasPower = hasLiquids = hasItems = true;
				flameColor = NHItems.fusionEnergy.color;
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

		zetaFluidFactory = new GenericSmelter("zeta-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 50, NHItems.juniorProcessor, 35, NHItems.presstanium, 80, Items.graphite, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.zetaFluid, 15f);
				craftTime = 60f;
				health = 550;
				flameColor = NHLiquids.zetaFluid.color;
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

		upgradeSortFactory = new GenericCrafter("upgradeSort-factory") {
			{
				requirements(Category.crafting, with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.thorium, 200));
				craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
					Draw.color(NHColor.darkEnrColor, Pal.gray, e.fin());
					Fill.square(e.x + x, e.y + y, 0.3f + e.fout() * 2.6f, 45);
				}));
				updateEffect = new Effect(25f, e -> {
					Draw.color(NHColor.darkEnrColor);
					Angles.randLenVectors(e.id, 2, 24 * e.fout() * e.fout(), (x, y) -> {
						Lines.stroke(e.fout() * 1.7f);
						Lines.square(e.x + x, e.y + y, 2f + e.fout() * 6f);
					});

				});
				outputItem = new ItemStack(NHItems.upgradeSort, 3);
				craftTime = 150f;
				size = 3;
				hasPower = hasItems = true;
				drawer = new DrawPrinter(outputItem.item) {
					{
						printColor = NHColor.darkEnrColor;
						lightColor = Color.valueOf("#E1BAFF");
						moveLength = 4.2f;
						time = 25f;
						//toPrint = NHItems.upgradeSort;
					}
				};
				consumes.items(new ItemStack(NHItems.setonAlloy, 4), new ItemStack(NHItems.seniorProcessor, 4));
				consumes.power(10f);
			}
		};

		zetaFactoryLarge = new GenericCrafter("large-zeta-factory") {
			{
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
			}
		};

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
			requirements(Category.crafting, with(NHItems.irayrondPanel, 55, NHItems.seniorProcessor, 35, NHItems.presstanium, 100, Items.plastanium, 40));
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
			consumes.items(new ItemStack(NHItems.presstanium, 6), new ItemStack(NHItems.metalOxhydrigen, 2));
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
			drawer = new DrawFactories() {
				{
					liquidColor = NHLiquids.xenBeta.color;
					drawTop = true;
				}
			};
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

		argmot = new SpeedupTurret("argmot") {
			{
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
			}
		};


		endOfEra = new ScalableTurret("end-of-era") {
			{
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
			}
		};

		thurmix = new ItemTurret("thurmix") {
			{
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
			}
		};

		eoeUpgrader = new UpgradeBlock("end-of-era-upgrader"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 3;
			linkTarget = endOfEra;
			health = 2350;
			baseColor = NHColor.darkEnrColor;
			maxLevel = 10;
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
		
		chargeWall = new ChargeWall("charge-wall"){{
			requirements(Category.defense, with(NHItems.irayrondPanel, 10, NHItems.seniorProcessor, 5, NHItems.upgradeSort, 15));
			size = 1;
			absorbLasers = true;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSky;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.defense, ItemStack.mult(chargeWall.requirements, healthMult2));
			size = 2;
			absorbLasers = true;
			range = 200;
            health = 1350 * healthMult2;
            effectColor = NHColor.lightSky;
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
        
        blaster = new StaticChargeBlaster("blaster"){{
            requirements(Category.effect, BuildVisibility.hidden, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            chargerOffset = 5.65f;
            rotateOffset = -45f;
            damage = 40;
            lightningDamage = 150;
            generateLiLen = 12;
            generateLiRand = 4;
            gettingBoltNum = 8;
            lightningColor = heatColor = NHColor.darkEnrColor;
            generateEffect = NHFx.blastgenerate;
            acceptEffect = NHFx.blastAccept;
            status = NHStatusEffects.emp2;
            range = 280;
            health = 5000;
            knockback = 80f;         
            consumes.power(10f);
        }};

		jumpGate = new JumpGate("jump-gate"){{
			consumes.power(60f);
			health = 50000;
			spawnDelay = 20f;
			spawnReloadTime = 300f;
			spawnRange = 220f;
			range = 600f;
			squareStroke = 2.35f;
			size = 8;
			adaptable = true;
			
			requirements(Category.units, BuildVisibility.shown, with(
				NHItems.presstanium, 2500,
				NHItems.metalOxhydrigen, 2000,
				NHItems.seniorProcessor, 2000,
				NHItems.multipleSteel, 1000,
				Items.thorium, 3500,
				Items.titanium, 6000,
				Items.surgeAlloy, 1000,
				Items.phaseFabric, 1800,
				NHItems.irayrondPanel, 800
			));
			addSets(
				new UnitSet(5.5f, NHUnits.annihilation, 6600f, 4,
					new ItemStack(NHItems.setonAlloy, 800),
					new ItemStack(NHItems.seniorProcessor, 800),
					new ItemStack(NHItems.thermoCoreNegative, 800)
				),
				new UnitSet(4.5f, NHUnits.tarlidor, 5400f,4,
					new ItemStack(NHItems.irayrondPanel, 800),
					new ItemStack(NHItems.multipleSteel, 1600),
					new ItemStack(NHItems.seniorProcessor, 1000),
					new ItemStack(NHItems.thermoCoreNegative, 600)
				),
				new UnitSet(6, NHUnits.hurricane, 10800f,4,
					new ItemStack(NHItems.seniorProcessor, 3000),
					new ItemStack(NHItems.upgradeSort, 1500),
					new ItemStack(NHItems.darkEnergy, 1000)
				),
				new UnitSet(4.5f, NHUnits.striker, 3600f,3,
					new ItemStack(NHItems.irayrondPanel, 600),
					new ItemStack(NHItems.seniorProcessor, 900),
					new ItemStack(NHItems.presstanium, 1500),
					new ItemStack(NHItems.zeta, 1800),
					new ItemStack(Items.plastanium, 1200)
				),
				new UnitSet(5.5f, NHUnits.destruction, 6200f,3,
					new ItemStack(NHItems.setonAlloy, 600),
					new ItemStack(NHItems.seniorProcessor, 600),
					new ItemStack(NHItems.multipleSteel, 500),
					new ItemStack(Items.phaseFabric, 600),
					new ItemStack(Items.graphite, 450)
				)
			);
		}};
		
		jumpGateJunior = new JumpGate("jump-gate-junior"){{
			primary = true;
			size = 5;
			atlasSizeScl = 0.75f;
			squareStroke = 2f;
			health = 6000;
			spawnDelay = 50f;
			spawnReloadTime = 600f;
			spawnRange = 160f;
			range = 300f;
			
			consumes.power(25f);
			
			requirements(Category.units, BuildVisibility.shown, with(
					NHItems.presstanium, 800,
					NHItems.metalOxhydrigen, 300,
					NHItems.juniorProcessor, 600,
					Items.plastanium, 350,
					Items.metaglass, 300,
					Items.thorium, 1000
			));
			NHTechTree.add(Blocks.commandCenter, this);
			addSets(
				new UnitSet(3, UnitTypes.quasar, 3000f, 4,
						new ItemStack(Items.titanium, 300),
						new ItemStack(Items.thorium, 300),
						new ItemStack(NHItems.multipleSteel, 200),
						new ItemStack(NHItems.juniorProcessor, 150)
				),
				new UnitSet(4, UnitTypes.scepter, 5800f, 4,
						new ItemStack(Items.plastanium, 400),
						new ItemStack(NHItems.presstanium, 600),
						new ItemStack(NHItems.multipleSteel, 300),
						new ItemStack(NHItems.juniorProcessor, 350)
				),
				new UnitSet(3, NHUnits.gather, 4200f, 3,
						new ItemStack(Items.thorium, 300),
						new ItemStack(NHItems.presstanium, 180),
						new ItemStack(NHItems.zeta, 210),
						new ItemStack(NHItems.juniorProcessor, 150)
				),
				new UnitSet(3.5f, NHUnits.aliotiat, 5200f, 6,
						new ItemStack(Items.titanium, 350),
						new ItemStack(NHItems.multipleSteel, 200),
						new ItemStack(NHItems.presstanium, 250),
						new ItemStack(NHItems.juniorProcessor, 150)
				),
				new UnitSet(3.5f, NHUnits.warper, 6600f, 9,
						new ItemStack(Items.thorium, 1500),
						new ItemStack(Items.graphite, 500),
						new ItemStack(NHItems.presstanium, 400),
						new ItemStack(NHItems.multipleSteel, 400),
						new ItemStack(NHItems.juniorProcessor, 300)
				)
			);
		}};
	}
}












