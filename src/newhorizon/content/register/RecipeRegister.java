package newhorizon.content.register;

import arc.func.Cons;
import arc.util.Log;
import arc.util.Structs;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.blocks.CraftingBlock;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.type.Recipe;

import static mindustry.Vars.content;

public class RecipeRegister {
    public static void load(){
        //tier1
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 3);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3, NHItems.graphite, 2);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 3);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.heatDetector, 1);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 9);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 1);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 6);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 3);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 15);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 10);
            recipe.craftTime = 120f;
        });
        /*
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 4 / 60f);
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3, Items.copper, 3);
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3, Items.beryllium, 3);
        });
        */
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 9);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 1);
            recipe.outputItem = ItemStack.list(Items.carbide, 24);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 1);
            recipe.outputItem = ItemStack.list(Items.carbide, 16);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            recipe.outputItem = ItemStack.list(Items.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2, Items.pyratite, 1);
            recipe.outputItem = ItemStack.list(Items.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2, Items.graphite, 3);
            recipe.inputLiquid = LiquidStack.list(Liquids.ozone, 6 / 60f);
            recipe.outputItem = ItemStack.list(Items.carbide, 2);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 24 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 24);
            recipe.craftTime = 240f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 48 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 12);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 12);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 8);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 48 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 4);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 18 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 6);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 4 / 60f, NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 4);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 4);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.lead, 3);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 9 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 4);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 9 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 4);
            recipe.craftTime = 120f;
        });
        
        //tier2
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 15);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 9);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 6);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 4 / 60f);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 3 / 60f);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 15);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.outputItem = ItemStack.list(Items.phaseFabric, 15);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 9);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.outputItem = ItemStack.list(Items.phaseFabric, 6);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(Items.phaseFabric, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 3 / 60f);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 12, NHItems.presstanium, 12);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.echoCanceller, 1);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 12);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.xenFluid, 24 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 6, NHItems.presstanium, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 1);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 4);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 3, NHItems.metalOxhydrigen, 4));
        recipe(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 2, Items.metaglass, 2));
        recipe(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 2, NHItems.presstanium, 3, Items.oxide, 3));

        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.memoryRecalibrator, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 10);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 4);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorEncoder, recipe -> recipe.inputItem = ItemStack.list(Items.surgeAlloy, 2, NHItems.juniorProcessor, 4));

        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 12);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 9);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 4);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 2, Items.carbide, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 4);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3 / 60f);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 2, Items.plastanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 4);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3 / 60f);
            recipe.craftTime = 120f;
        });

        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 12);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.outputItem = ItemStack.list(NHItems.setonAlloy, 10);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.outputItem = ItemStack.list(NHItems.setonAlloy, 6);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f);
        });
        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.plastanium, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f);
        });
        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 2, NHItems.presstanium, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f);
        });

        //tier3
        recipe(CraftingBlock.processorCompactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 30);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 1);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 80, NHItems.seniorProcessor, 60);
            recipe.craftTime = 150f;
        });
        recipe(CraftingBlock.processorCompactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 15, NHItems.metalOxhydrigen, 10);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);;
        });
        recipe(CraftingBlock.processorCompactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.plastanium, 10);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);;
        });

        recipe(CraftingBlock.multipleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 60);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.neutronMembrane, 1);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 40, NHItems.presstanium, 80);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.multipleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 20, NHItems.fusionEnergy, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.neutronMembrane, 1);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 40, NHItems.presstanium, 80);
            recipe.craftTime = 120f;
        });
        recipe(CraftingBlock.multipleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 12);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 8 / 60f, NHLiquids.irdryonFluid, 12 / 60f);;
        });
            recipe(CraftingBlock.multipleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 6, NHItems.fusionEnergy, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 8 / 60f);
        });

      
        recipe(CraftingBlock.electronicFacilityBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 8, Items.tungsten, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        recipe(CraftingBlock.electronicFacilityBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.graphite, 8, Items.metaglass, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        recipe(CraftingBlock.electronicFacilityBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.beryllium, 8, Items.oxide, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });

        recipe(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 12, NHItems.metalOxhydrigen, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
        });  
        recipe(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 8, Items.plastanium, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
        });
        recipe(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 8, Items.carbide, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
        });

        recipe(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 12, NHItems.multipleSteel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 2);
        });
        recipe(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8, Items.phaseFabric, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 2);
        });

        recipe(CraftingBlock.electronicFacilityEpic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 8, NHItems.seniorProcessor, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.memoryRecalibrator, 2);
        });

        recipe(CraftingBlock.electronicFacilityLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 2, ModuleBlock.memoryRecalibrator, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.resistoArray, 2);
        });
      
        recipe(CraftingBlock.particleProcessorBasic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 8, NHItems.juniorProcessor, 12);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
        });

        recipe(CraftingBlock.particleProcessorRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 8, NHItems.juniorProcessor, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 2);
        });

        recipe(CraftingBlock.particleProcessorUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8, NHItems.seniorProcessor, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.hadronBuffers, 2);
        });

        recipe(CraftingBlock.particleProcessorEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 8, NHItems.irayrondPanel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.tachyonEmitter, 2);
        });

        recipe(CraftingBlock.particleProcessorLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.hadronBuffers, 2, ModuleBlock.tachyonEmitter, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.neutronMembrane, 2);
        });


        recipe(CraftingBlock.foundryBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 8, NHItems.presstanium, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
        });

        recipe(CraftingBlock.foundryRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.heatDetector, 2);
        });

        recipe(CraftingBlock.foundryUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 2);
        });

        recipe(CraftingBlock.foundryEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.heatDetector, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.echoCanceller, 2);
        });

        recipe(CraftingBlock.foundryLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 2, ModuleBlock.echoCanceller, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.pulseMutator, 2);
        });


        recipe(CraftingBlock.powerBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 8, NHItems.zeta, 4);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        recipe(CraftingBlock.powerBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 8, Items.thorium, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        recipe(CraftingBlock.powerBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 6, Items.thorium, 8);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });

        recipe(CraftingBlock.powerRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.fissionCell, 2);
        });

        recipe(CraftingBlock.powerUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.chargeCompensator, 2);
        });

        recipe(CraftingBlock.powerEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.fissionCell, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 2);
        });

        recipe(CraftingBlock.powerLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 6, NHItems.thermoCoreNegative, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.chargeCompensator, 2, ModuleBlock.fusionReactor, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.multiphasePropellant, 2);
        });


        recipe(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 12 / 60f);
        });
        recipe(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 8);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 12 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
        });

        recipe(CraftingBlock.componentRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 2);
        });

        recipe(CraftingBlock.componentUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
            recipe.outputLiquid =  LiquidStack.list(NHLiquids.zetaFluidNegative, 6 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 2);
        });

        recipe(CraftingBlock.componentEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 6 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.particleModulator, 2);
        });

        recipe(CraftingBlock.componentLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 2, ModuleBlock.particleModulator, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.quantumConductor, 2);
        });

    }

    public static void recipe(Block block, Cons<Recipe> recipe) {
        if (block instanceof RecipeGenericCrafter crafter) {
            Recipe r = new Recipe();
            recipe.get(r);
            //auto ignore zeta fluid
            crafter.recipes.add(r);
        }
    }

    public static void recipePostProcess() {}
}
