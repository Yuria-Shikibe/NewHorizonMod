package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.ctype.ContentList;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.StatusEffect;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Door;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.GenericSmelter;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawMixer;
import mindustry.world.meta.BuildVisibility;

import newhorizon.block.adapt.AdaptImpactReactor;
import newhorizon.block.drawer.DrawFactories;

import newhorizon.block.drawer.DrawHoldLiquid;
import newhorizon.block.drawer.DrawPrinter;
import newhorizon.block.drawer.NHDrawAnimation;
import newhorizon.block.special.*;
import newhorizon.block.turrets.ScalableTurret;
import newhorizon.block.turrets.SpeedupTurret;
import newhorizon.bullets.NHTrailBulletType;

import static mindustry.type.ItemStack.with;

public class NHBlocks implements ContentList {

	//Load Mod Factories

	public static Block
		delivery, zateOre, xenMelter, hyperGenerator, fusionCollapser, blastTurret,
		largeShieldGenerator, divlusion,
		chargeWall, chargeWallLarge, nemesisUpgrader, jumpGate,
		irdryonVault, blaster, nemesis, thurmix, argmot, thermoTurret,
		presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
		zateFactoryLarge, zateFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, irayrondPanelFactorySmall,
		setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory,
		thermoCorePositiveFactory, thermoCoreNegativeFactory, thermoCoreFactory,
		//Liquids factories
		irdryonFluidFactory, xenBetaFactory, xenGammaFactory, zateFluidFactory,
		//walls
		insulatedWall, setonWall, setonWallLarge, heavyDefenceWall, heavyDefenceWallLarge, heavyDefenceDoor, heavyDefenceDoorLarge;

	@Override
	public void load() {
		final int healthMult = 4;
		fusionCollapser = new AdaptImpactReactor("fusion-collapser"){{
			this.ambientSound = Sounds.pulse;
			this.ambientSoundVolume = 0.09F;
			size = 5;
			itemCapacity = 20;
			liquidCapacity = 40;
			health = 2400;
			powerProduction = 250f;
			itemDuration = 90f;
			NHTechTree.add(Blocks.impactReactor, this);
			this.consumes.power(30.0F);
			this.consumes.items(new ItemStack(NHItems.fusionEnergy, 2));
			this.consumes.liquid(NHLiquids.xenBeta, 0.1F);
			this.requirements(Category.power, BuildVisibility.shown, with(Items.thorium, 600, NHItems.irayrondPanel, 350, NHItems.seniorProcessor, 200, NHItems.presstanium, 850, Items.surgeAlloy, 250, Items.metaglass, 250));
		}};
		
		hyperGenerator = new HyperGenerator("hyper-generator"){{
			size = 8;
			health = 12500;
			powerProduction = 1250f;
			updateLightning = updateLightningRand = 3;
			effectColor = NHItems.thermoCorePositive.color;
			itemCapacity = 40;
			itemDuration = 180f;
			this.ambientSound = Sounds.pulse;
			this.ambientSoundVolume = 0.1F;
			this.consumes.power(50.0F);
			this.consumes.items(new ItemStack(NHItems.metalOxhydrigen, 8), new ItemStack(NHItems.thermoCorePositive, 4));
			this.consumes.liquid(NHLiquids.xenGamma, 0.15F);
			NHTechTree.add(fusionCollapser, this);
			this.requirements(Category.power, BuildVisibility.shown, with(NHItems.upgradeSort, 1000, NHItems.setonAlloy, 600, NHItems.irayrondPanel, 400, NHItems.presstanium, 1500, Items.surgeAlloy, 250, Items.metaglass, 250));
		}};
		
		blastTurret = new ItemTurret("blast-turret"){{
			size = 6;
			health = 10200;
			this.requirements(Category.turret, BuildVisibility.shown, with(Items.surgeAlloy, 250, NHItems.irayrondPanel, 650, Items.plastanium, 375, NHItems.seniorProcessor, 150, NHItems.setonAlloy, 400));
			ammo(
					NHItems.thermoCorePositive, NHBullets.blastEnergyPst, NHItems.thermoCoreNegative, NHBullets.blastEnergyNgt
			);
			shots = 8;
			burstSpacing = 4f;
			maxAmmo = 80;
			ammoPerShot = 8;
			xRand = Vars.tilesize * (size - 2.225f) / 2;
			this.reloadTime = 120f;
			this.shootCone = 50.0F;
			this.rotateSpeed = 1.5F;
			this.range = 600.0F;
			this.heatColor = NHBullets.blastEnergyPst.lightColor;
			this.recoilAmount = 4.0F;
			this.shootSound = NHSounds.rapidLaser;
		}};
		
		thermoTurret = new PowerTurret("thermo-turret"){{
			NHTechTree.add(Blocks.arc, this);
			size = 1;
			health = 320;
			this.requirements(Category.turret, BuildVisibility.shown, with(Items.titanium, 50, Items.copper, 50, Items.silicon, 25));
			shootType = new NHTrailBulletType(6.5f, 15f){{
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
				lifetime = 130f;
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
			powerUse = 4;
			shots = 5;
			inaccuracy = 3f;
			burstSpacing = 6f;
			this.reloadTime = 75f;
			this.shootCone = 50.0F;
			this.rotateSpeed = 8.0F;
			this.targetGround = false;
			this.range = 200.0F;
			this.shootEffect = Fx.lightningShoot;
			this.smokeEffect = Fx.shootSmallSmoke;
			this.heatColor = Color.red;
			this.recoilAmount = 1.0F;
			this.shootSound = Sounds.laser;
		}};
		
		insulatedWall = new Wall("insulated-wall"){{
			size = 1;
			health = 550;
			requirements(Category.defense, with(Items.titanium, 10, Items.copper, 5));
			this.insulated = true;
			this.absorbLasers = true;
		}};
		
		setonWall = new Wall("seton-wall"){{
			size = 1;
			health = 1250;
			this.chanceDeflect = 10.0F;
			this.flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5, NHItems.irayrondPanel, 10, Items.silicon, 15, NHItems.presstanium, 15));
		}};
		
		setonWallLarge = new Wall("seton-wall-large"){{
			size = 2;
			health = 1250 * healthMult;
			this.chanceDeflect = 10.0F;
			this.flashHit = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 5 * healthMult, NHItems.irayrondPanel, 10 * healthMult, Items.silicon, 15 * healthMult, NHItems.presstanium, 15 * healthMult));
		}};
		
		heavyDefenceWall = new Wall("heavy-defence-wall"){{
			size = 1;
			health = 1750;
			this.absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20));
		}};
		
		heavyDefenceWallLarge = new Wall("heavy-defence-wall-large"){{
			size = 2;
			health = 1750 * healthMult;
			this.absorbLasers = true;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult, NHItems.presstanium, 20 * healthMult));
		}};
		
		heavyDefenceDoor = new Door("heavy-defence-door"){{
			size = 1;
			health = 1750;
			requirements(Category.defense, with(NHItems.setonAlloy, 10, NHItems.presstanium, 20, NHItems.juniorProcessor, 5));
		}};
		
		heavyDefenceDoorLarge = new Door("heavy-defence-door-large"){{
			size = 2;
			health = 1750 * healthMult;
			openfx = Fx.dooropenlarge;
			closefx = Fx.doorcloselarge;
			requirements(Category.defense, with(NHItems.setonAlloy, 10 * healthMult, NHItems.presstanium, 20 * healthMult, NHItems.juniorProcessor, 5 * healthMult));
		}};
		
		
		
		xenMelter = new GenericCrafter("xen-melter"){{
			size = 2;
			hasPower = hasLiquids = hasItems = true;
			itemCapacity = 12;
			liquidCapacity = 24;
			craftTime = 15f;
			drawer = new DrawMixer();
			
			craftEffect = NHFx.lightSkyCircleSplash;
			updateEffect = Fx.smeltsmoke;
			requirements(Category.crafting, BuildVisibility.shown, with(NHItems.juniorProcessor, 35, NHItems.metalOxhydrigen, 50, Items.thorium, 30, NHItems.presstanium, 25));
			consumes.power(3f);
			consumes.items(new ItemStack(NHItems.metalOxhydrigen, 2), new ItemStack(NHItems.zate, 1));
			outputLiquid = new LiquidStack(NHLiquids.xenAlpha, 3f);
		}};
		
		zateOre = new OreBlock("ore-zate"){{
			this.oreDefault = true;
			Log.info(this.name);
			variants = 3;
			this.oreThreshold = 0.95F;
			this.oreScale = 20.380953F;
			this.itemDrop = NHItems.zate;
			this.localizedName = itemDrop.localizedName;
			this.mapColor.set(itemDrop.color);
			this.useColor = true;
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

		zateFactorySmall = new GenericCrafter("small-zate-crystal-factory") {
			{
				requirements(Category.crafting, with(Items.silicon, 15, Items.lead, 30, Items.titanium, 40));
				hasItems = hasPower = true;
				craftTime = 45f;
				outputItem = new ItemStack(NHItems.zate, 1);
				size = 1;
				health = 60;
				craftEffect = Fx.smeltsmoke;
				updateEffect = Fx.shockwave;
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
				consumes.power(8f);
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
				consumes.items(new ItemStack(NHItems.presstanium, 2), new ItemStack(NHItems.zate, 6));
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
				outputItem = new ItemStack(NHItems.irayrondPanel, 3);
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
				outputLiquid = new LiquidStack(NHLiquids.irdryonFluid, 12f);
				craftTime = 120f;
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
				consumes.liquid(NHLiquids.xenBeta, 0.1f);
				consumes.items(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.metalOxhydrigen, 8));
				consumes.power(4f);
			}
		};

		zateFluidFactory = new GenericSmelter("zate-fluid-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 50, NHItems.juniorProcessor, 35, NHItems.presstanium, 80, Items.graphite, 65));
				craftEffect = Fx.smeltsmoke;
				outputLiquid = new LiquidStack(NHLiquids.zateFluid, 15f);
				craftTime = 60f;
				health = 550;
				flameColor = NHLiquids.zateFluid.color;
				size = 3;
				itemCapacity = 20;
				liquidCapacity = 60f;
				hasPower = hasLiquids = hasItems = true;
				consumes.liquid(Liquids.water, 0.1f);
				consumes.item(NHItems.zate, 2);
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
				consumes.item(Items.titanium, 2);
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
						liquidColor = NHLiquids.zateFluid.color;
						drawRotator = 1f;
						drawTop = false;
						pressorSet = new float[] {(craftTime / 6f), -4.2f, 45, 0};
					}
				};
				consumes.liquid(NHLiquids.zateFluid, 0.2f);
				consumes.items(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.fusionEnergy, 6));
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
				drawer = new DrawPrinter() {
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

		zateFactoryLarge = new GenericCrafter("large-zate-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25));
				outputItem = new ItemStack(NHItems.zate, 2);
				craftTime = 70f;
				size = 2;
				craftEffect = Fx.formsmoke;
				updateEffect = NHFx.trail;
				hasPower = hasItems = hasLiquids = true;
				drawer = new DrawHoldLiquid();
				consumes.item(Items.thorium, 3);
				consumes.power(5f);
				consumes.liquid(Liquids.water, 0.1f);
			}
		};

		multipleSteelFactory = new GenericCrafter("multiple-steel-factory") {
			{
				requirements(Category.crafting, with(Items.graphite, 65, NHItems.juniorProcessor, 65, NHItems.presstanium, 100, Items.metaglass, 30));
				updateEffect = Fx.smeltsmoke;
				craftEffect = Fx.shockwave;
				outputItem = new ItemStack(NHItems.multipleSteel, 2);
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
				consumes.items(new ItemStack(Items.metaglass, 2), new ItemStack(Items.titanium, 5));
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
			consumes.liquid(NHLiquids.xenBeta, 0.12f);
			consumes.items(new ItemStack(NHItems.presstanium, 8), new ItemStack(NHItems.metalOxhydrigen, 2));
			consumes.power(12f);
		}};

		multipleSurgeAlloyFactory = new GenericCrafter("multiple-surge-alloy-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 80, NHItems.seniorProcessor, 60, Items.plastanium, 40, NHItems.presstanium, 100, Items.surgeAlloy, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(Items.surgeAlloy.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
			}));
			outputItem = new ItemStack(Items.surgeAlloy, 4);
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
				}
			};
			consumes.liquid(NHLiquids.irdryonFluid, 0.12f);
			consumes.items(new ItemStack(NHItems.presstanium, 12), new ItemStack(NHItems.fusionEnergy, 1));
			consumes.power(12f);
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
			consumes.item(NHItems.zate, 2);
			consumes.power(3f);
		}};

		xenGammaFactory = new GenericCrafter("xen-gamma-factory"){{
			requirements(Category.crafting, with(NHItems.irayrondPanel, 70, NHItems.seniorProcessor, 60, Items.surgeAlloy, 20, Items.metaglass, 40));
			craftEffect = new Effect(30f, e -> Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
				Draw.color(NHLiquids.xenGamma.color);
				Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
			}));
			outputLiquid = new LiquidStack(NHLiquids.xenGamma, 8f);
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
			consumes.liquid(NHLiquids.xenBeta, 0.1f);
			consumes.items(new ItemStack(NHItems.juniorProcessor, 1), new ItemStack(Items.titanium, 3));
			consumes.power(6f);
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


		nemesis = new ScalableTurret("end-of-era") {
			{
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
			}
		};

		nemesisUpgrader = new UpgradeBlock("end-of-era-upgrader"){{
			requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 3;
			linkTarget = nemesis;
			health = 2350;
			baseColor = NHColor.darkEnrColor;
			maxLevel = 10;
			addUpgrades(
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
			requirements(Category.defense, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
			size = 1;
			absorbLasers = true;
			range = 120;
            health = 1350;
            effectColor = NHColor.lightSky;
		}};
		
		chargeWallLarge = new ChargeWall("charge-wall-large"){{
			requirements(Category.defense, with(NHItems.presstanium, 600, NHItems.metalOxhydrigen, 200, NHItems.irayrondPanel, 300));
			size = 2;
			absorbLasers = true;
			range = 200;
            health = 1350 * size * size;
            effectColor = NHColor.lightSky;
		}};
		
		irdryonVault = new StorageBlock("irdryon-vault"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
            size = 3;
            health = 3500;
            itemCapacity = 2500;
        }};
        
        blaster = new StaticChargeBlaster("blaster"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.metalOxhydrigen, 50, NHItems.irayrondPanel, 75));
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
            status = new StatusEffect("emp"){{
            	reloadMultiplier = 0.01f;
            	damage = 12f;
          	  effect = NHFx.emped;
				speedMultiplier = 0.24f;
            }};
            range = 280;
            health = 5000;
            knockback = 80f;         
            consumes.power(10f);
        }};

		jumpGate = new JumpGate("jump-gate"){{
			consumes.power(100f);
			health = 50000;
			spawnDelay = 20f;
			spawnReloadTime = 300f;
			spawnRange = 220f;
			range = 600f;
			squareStroke = 2.35f;

			requirements(Category.units, with(
				NHItems.presstanium, 3000,
				NHItems.metalOxhydrigen, 5000,
				NHItems.seniorProcessor, 2000,
				NHItems.darkEnergy, 1000,
				NHItems.irayrondPanel, 3000,
				NHItems.upgradeSort, 1300,
				Items.thorium, 3500,
				Items.titanium, 6000,
				Items.surgeAlloy, 1000,
				Items.phaseFabric, 800
			));
			size = 8;
			addSets(
				new UnitSet(9, NHUnits.annihilation, 15000f, 4,
						new ItemStack(NHItems.upgradeSort, 1400),
						new ItemStack(NHItems.seniorProcessor, 800),
						new ItemStack(NHItems.darkEnergy, 800)
				),
				new UnitSet(7, NHUnits.tarlidor, 12000f,4,
						new ItemStack(NHItems.irayrondPanel, 400),
						new ItemStack(NHItems.setonAlloy, 1200),
						new ItemStack(NHItems.seniorProcessor, 800),
						new ItemStack(NHItems.thermoCoreNegative, 400)
				),
				new UnitSet(10, NHUnits.hurricane, 20000f,4,
					new ItemStack(NHItems.irayrondPanel, 800),
					new ItemStack(NHItems.setonAlloy, 1200),
					new ItemStack(NHItems.seniorProcessor, 1000),
					new ItemStack(NHItems.upgradeSort, 1500),
					new ItemStack(NHItems.darkEnergy, 800)
				),
				new UnitSet(6, NHUnits.striker, 9000f,9,
					new ItemStack(NHItems.irayrondPanel, 600),
					new ItemStack(NHItems.seniorProcessor, 8000),
					new ItemStack(NHItems.presstanium, 1500),
					new ItemStack(NHItems.zate, 800),
					new ItemStack(Items.plastanium, 550)
				),
				new UnitSet(8, UnitTypes.eclipse,  9000f,6,
					new ItemStack(Items.lead, 375),
					new ItemStack(Items.plastanium, 750),
					new ItemStack(Items.copper, 500),
					new ItemStack(NHItems.presstanium, 550)
				),
				new UnitSet(6, UnitTypes.quad, 6000f,8,
					new ItemStack(NHItems.irayrondPanel, 550),
					new ItemStack(Items.plastanium, 550),
					new ItemStack(NHItems.fusionEnergy, 320),
					new ItemStack(Items.titanium, 450)
				)
			);
		}};
	}
}












