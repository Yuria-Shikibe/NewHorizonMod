package newhorizon.contents.blocks;

import arc.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.ctype.*;
import mindustry.content.*;

import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.experimental.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import newhorizon.contents.items.*;
import newhorizon.contents.colors.*;
import newhorizon.contents.blocks.drawers.*;

import static mindustry.type.ItemStack.*;
import static mindustry.Vars.*;

public class NHFactories implements ContentList {

	//Load Mod Factories

	public static Block
	presstaniumFactory, seniorProcessorFactory, juniorProcessorFactory, multipleSurgeAlloyFactory,
						zateFactoryLarge, zateFactorySmall, fusionEnergyFactory, multipleSteelFactory, irayrondPanelFactory, irayrondPanelFactorySmall,
						setonAlloyFactory, darkEnergyFactory, upgradeSortFactory, metalOxhydrigenFactory,
						thermoCorePositiveFactory, thermoCoreNegativeFactory, thermoCoreFactory,
						//Liquids factories
						irdryonFluidFactory, xenBetaFactory, xenGammaFactory, zateFluidFactory;

	@Override
	public void load() {
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

		zateFactorySmall = new GenericCrafter("small-zate-ctystal-factory") {
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
				outputItem = new ItemStack(NHItems.fusionEnergy, 2);
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
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 4f + e.fin() * 12f, (x, y) -> {
						Draw.color(NHLiquids.irdryonFluid.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 2.5f, 45);
					});
				});
				outputItem = new ItemStack(NHItems.irayrondPanel, 3);
				craftTime = 120f;
				health = 800;
				size = 4;
				flameColor = NHItems.irayrondPanel.color;
				hasPower = hasLiquids = hasItems = true;
				flameColor = NHItems.fusionEnergy.color;
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
				consumes.items(new ItemStack(Items.silicon, 4), new ItemStack(Items.copper, 2));
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
				consumes.items(new ItemStack(NHItems.irayrondPanel, 2), new ItemStack(NHItems.fusionEnergy, 10));
				consumes.power(5f);
			}
		};

		upgradeSortFactory = new GenericCrafter("upgradeSort-factory") {
			{
				requirements(Category.crafting, with(NHItems.setonAlloy, 160, NHItems.seniorProcessor, 80, NHItems.presstanium, 150, Items.thorium, 200));
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
						Draw.color(NHColor.darkEnrColor, Pal.gray, e.fin());
						Fill.square(e.x + x, e.y + y, 0.3f + e.fout() * 2.6f, 45);
					});
				});
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

		zateFactoryLarge = new Cultivator("large-zate-factory") {
			{
				requirements(Category.crafting, with(Items.plastanium, 25, NHItems.juniorProcessor, 50, NHItems.presstanium, 25));
				outputItem = new ItemStack(NHItems.zate, 2);
				craftTime = 70f;
				size = 2;
				hasPower = hasItems = true;
				plantColor = plantColorLight = NHItems.zate.color;
				bottomColor = Color.valueOf("ff846b");
				attribute = Attribute.water;
				consumes.item(Items.thorium, 3);
				consumes.power(6f);
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
				hasPower = hasLiquids = hasItems = true;
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
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
						Draw.color(NHLiquids.xenBeta.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
					});
				});
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
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
						Draw.color(Items.surgeAlloy.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 3f, 45);
					});
				});
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
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 4f + e.fin() * 12f, (x, y) -> {
						Draw.color(NHLiquids.irdryonFluid.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 3f);
					});
				});
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
				consumes.items(new ItemStack(NHItems.irayrondPanel, 3), new ItemStack(NHItems.metalOxhydrigen, 2));
				consumes.power(12f);
		}};
		
		xenBetaFactory = new GenericCrafter("xen-beta-factory"){{
			requirements(Category.crafting, with(NHItems.metalOxhydrigen, 35, NHItems.juniorProcessor, 60, Items.plastanium, 20, NHItems.presstanium, 80, Items.metaglass, 40));
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
						Draw.color(NHLiquids.xenBeta.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
					});
				});
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
				craftEffect = new Effect(30f, e -> {
					Angles.randLenVectors(e.id, 6, 3f + e.fin() * 7f, (x, y) -> {
						Draw.color(NHLiquids.xenGamma.color);
						Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45);
					});
				});
				outputLiquid = new LiquidStack(NHLiquids.xenGamma, 6f);
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
				consumes.item(NHItems.fusionEnergy, 1);
				consumes.power(6f);
		}};
	}
}





