package newhorizon.content.register;

import arc.func.Cons;
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

public class RecipeRegister {
    public static void load(){
        //tier1
        input(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.heatDetector, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
        });
        input(CraftingBlock.stampingFacility, recipe -> recipe.inputItem = ItemStack.list(Items.titanium, 3, Items.graphite, 1.5));

        input(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 4 / 60f);
        });
        input(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3, Items.copper, 3);
        });
        input(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3, Items.beryllium, 3);
        });

        input(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
        });
        input(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2, Items.pyratite, 1);
        });
        input(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2, Items.graphite, 3);
            recipe.inputLiquid = LiquidStack.list(Liquids.ozone, 6 / 60f);
        });

        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 0.8);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 4 / 60f, NHLiquids.quantumLiquid, 6 / 60f);
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.metaglass, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 6 / 60f);
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.lead, 3);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 9 / 60f);
        });
        input(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.oxide, 1);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 9 / 60f);
        });
        
        //tier2
        input(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 4 / 60f);
        });

        input(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
        });

        input(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.echoCanceller, 1);
            recipe.craftScl = 8f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 1);
            recipe.craftScl = 4f;
            recipe.boostScl = 0.5f;
        });
        input(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 3, NHItems.metalOxhydrigen, 4));
        input(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 2, Items.metaglass, 2));
        input(CraftingBlock.multipleSteelFactory, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 2, NHItems.presstanium, 3, Items.oxide, 3));
        

        
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 8, Items.tungsten, 8));
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.graphite, 8, Items.metaglass, 8));
        input(CraftingBlock.electronicFacilityBasic, recipe -> recipe.inputItem = ItemStack.list(Items.beryllium, 8, Items.oxide, 8));
        output(CraftingBlock.electronicFacilityBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.wiringKit, 2);
        });

        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 12, NHItems.metalOxhydrige, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });  
        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 8, Items.plastanium, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        input(CraftingBlock.electronicFacilityRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.thorium, 8, Items.carbide, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });  
        output(CraftingBlock.electronicFacilityRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.powerUnit, 2);
        });

        input(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 12, NHItems.multipleSteel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        input(CraftingBlock.electronicFacilityUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8, Items.phaseFabric, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2);
        });
        output(CraftingBlock.electronicFacilityUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.bionicsProcessor, 2);
        });

        input(CraftingBlock.electronicFacilityEpic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 8, NHItems.seniorProcessor, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
        });
        output(CraftingBlock.electronicFacilityEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.memoryRecalibrator, 2);
        });

        input(CraftingBlock.electronicFacilityLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 2, ModuleBlock.memoryRecalibrator, 2);
        });
        output(CraftingBlock.electronicFacilityLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.resistoArray, 2);
        });

      
        input(CraftingBlock.particleProcessorBasic, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 8, NHItems.juniorProcessor, 12));
        output(CraftingBlock.particleProcessorBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.crystalDiode, 2);
        });

        input(CraftingBlock.particleProcessorRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 8, NHItems.juniorProcessor, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
        });
        output(CraftingBlock.particleProcessorRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.protonCapacitor, 2);
        });

        input(CraftingBlock.particleProcessorUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8, NHItems.seniorProcessor, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
        });
        output(CraftingBlock.particleProcessorUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.hadronBuffers, 2);
        });

        input(CraftingBlock.particleProcessorEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 8, NHItems.irayrondPanel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.protonCapacitor, 2);
        });
        output(CraftingBlock.particleProcessorEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.tachyonEmitter, 2);
        });

        input(CraftingBlock.particleProcessorLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.hadronBuffers, 2, ModuleBlock.tachyonEmitter, 2);
        });
        output(CraftingBlock.particleProcessorLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.neutronMembrane, 2);
        });


        input(CraftingBlock.foundryBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 8, NHItems.presstanium, 8));
        output(CraftingBlock.foundryBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.armorCast, 2);
        });

        input(CraftingBlock.foundryRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
        });
        output(CraftingBlock.foundryRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.heatDetector, 2);
        });

        input(CraftingBlock.foundryUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
        });
        output(CraftingBlock.foundryUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.gaussReceptor, 2);
        });

        input(CraftingBlock.foundryEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.heatDetector, 2);
        });
        output(CraftingBlock.foundryEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.echoCanceller, 2);
        });

        input(CraftingBlock.foundryLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 2, ModuleBlock.echoCanceller, 2);
        });
        output(CraftingBlock.foundryLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.pulseMutator, 2);
        });


        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 8, NHItems.zeta, 8));
        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.titanium, 8, Items.thorium, 8));
        input(CraftingBlock.powerBasic, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 8, Items.thorium, 8));
        output(CraftingBlock.powerBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.powerCell, 2);
        });

        input(CraftingBlock.powerRare, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        output(CraftingBlock.powerRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.fissionCell, 2);
        });

        input(CraftingBlock.powerUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 2);
        });
        output(CraftingBlock.powerUncommon, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.chargeCompensator, 2);
        });

        input(CraftingBlock.powerEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 8);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.fissionCell, 2);
        });
        output(CraftingBlock.powerEpic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.fusionReactor, 2);
        });

        input(CraftingBlock.powerLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 6, NHItems.thermoCoreNegative, 6);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.chargeCompensator, 2, ModuleBlock.fusionReactor, 2);
        });
        output(CraftingBlock.powerLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.multiphasePropellant, 2);
        });


        input(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 12 / 60f);
        });
        input(CraftingBlock.componentBasic, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 8);
            recipe.inputLiquid = LiquidStack.list(Liquids.water, 12 / 60f);
        });  
        output(CraftingBlock.componentBasic, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.supraGel, 2);
        });

        input(CraftingBlock.componentRare, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
        });
        output(CraftingBlock.componentRare, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.coolingUnit, 2);
        });

        input(CraftingBlock.componentUncommon, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 2);
        });
        output(CraftingBlock.componentUncommon, block -> {
            block.outputLiquids =  LiquidStack.with(NHLiquids.zetaFluidNegative, 6 / 60f);
            block.outputPayloads = PayloadStack.with(ModuleBlock.signalCirculator, 2);
        });

        input(CraftingBlock.componentEpic, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 8);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 2);
        });
        output(CraftingBlock.componentEpic, block -> {
            block.outputLiquids = LiquidStack.with(NHLiquids.zetaFluidPositive, 6 / 60f);
            block.outputPayloads = PayloadStack.with(ModuleBlock.particleModulator, 2);
        });

        input(CraftingBlock.componentLegendary, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 2, ModuleBlock.particleModulator, 2);
        });
        output(CraftingBlock.componentLegendary, block -> {
            block.outputPayloads = PayloadStack.with(ModuleBlock.quantumConductor, 2);
        });
}

    public static void input(Block block, Cons<Recipe> recipe) {
        if (block instanceof RecipeGenericCrafter crafter) {
            Recipe r = new Recipe();
            recipe.get(r);
            crafter.recipes.add(r);
        }
    }

    public static void output(Block block, Cons<AdaptCrafter> output) {
        if (block instanceof AdaptCrafter crafter) {
            output.get(crafter);
        }
    }
}
