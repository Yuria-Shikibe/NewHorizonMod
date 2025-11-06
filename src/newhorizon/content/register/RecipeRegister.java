package newhorizon.content.register;

import arc.func.Cons;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.PayloadStack;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.content.blocks.CraftingBlock;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.content.blocks.PowerBlock;
import newhorizon.content.blocks.ProductionBlock;
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.type.Recipe;

public class RecipeRegister {
    public static void load(){
        recipe(ProductionBlock.solidificationShaper, recipe -> {
            recipe.inputItem = ItemStack.list();
            recipe.outputItem = ItemStack.list(NHItems.hardLight, 2);
            recipe.craftTime = 120f;
        });
        
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.scrap, 2);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.copper, 3);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.lead, 3);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.beryllium, 3);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 2);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thorium, 2);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2);
            recipe.outputItem = ItemStack.list(NHItems.sand, 5);
            recipe.craftTime = 30f;
        });
        recipe(ProductionBlock.sandCracker, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicar, 3);
            recipe.outputItem = ItemStack.list(NHItems.graphite, 2, NHItems.silicon, 2);
            recipe.craftTime = 60f;
        });

        recipe(ProductionBlock.tungstenReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.copper, 5);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.tungsten, 2);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.tungstenReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.lead, 5);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.tungsten, 2);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.tungstenReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 6);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.tungsten, 4);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.tungstenReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thorium, 2);
            recipe.outputItem = ItemStack.list(NHItems.zeta, 3);
            recipe.craftTime = 60f;
        });
        
        recipe(ProductionBlock.titaniumReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.beryllium, 3);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.titaniumReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 4);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.titanium, 6);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.titaniumReconstructor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thorium, 2);
            recipe.outputItem = ItemStack.list(NHItems.zeta, 3);
            recipe.craftTime = 60f;
        });

        recipe(ProductionBlock.liquidConvertor, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.water, 36 / 60f);
            recipe.inputItem = ItemStack.list(NHItems.hardLight, 1);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 18 / 60f);
            recipe.craftTime = 90f;
        });
        //recipe(ProductionBlock.liquidConvertor, recipe -> {
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.oil, 15 / 60f);
            //recipe.outputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            //recipe.craftTime = 60f;
        //});
        //recipe(ProductionBlock.liquidConvertor, recipe -> {
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.arkycite, 20 / 60f);
            //recipe.outputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            //recipe.craftTime = 60f;
        //});
        recipe(ProductionBlock.liquidConvertor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.sand, 3);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.oil, 15 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.liquidConvertor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.scrap, 10);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.slag, 40 / 60f);
            recipe.craftTime = 60f;
        });

        recipe(ProductionBlock.xenIterator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 2, NHItems.metalOxhydrigen, 1);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(ProductionBlock.xenIterator, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 1);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.xenFluid, 36 / 60f);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 2, NHItems.graphite, 1);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 3);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 1);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 5);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2, NHItems.beryllium, 3);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2, NHItems.copper, 3);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 1);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 5);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.condenseFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 6);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 36 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.cryofluid, 24 / 60f);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        recipe(CraftingBlock.condenseFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.cryofluid, 48 / 60f);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2, NHItems.graphite, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.ozone, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2, NHItems.pyratite, 1);
            recipe.outputItem = ItemStack.list(NHItems.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.carbide, 5);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.oxide, 1);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.water, 9 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.lead, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.water, 9 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 8 / 60f, NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        //recipe(CraftingBlock.zetaFactory, recipe -> {
            //recipe.inputItem = ItemStack.list(NHItems.hardLight, 4);
           //recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            //recipe.outputItem = ItemStack.list(NHItems.zeta, 2);
            //recipe.craftTime = 60f;
        //});
        //recipe(CraftingBlock.zetaFactory, recipe -> {
            //recipe.inputItem = ItemStack.list(NHItems.hardLight, 2);
            //recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            //recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 1);
            //recipe.outputItem = ItemStack.list(NHItems.zeta, 10);
            //recipe.craftTime = 60f;
            //recipe.priority = 1;
        //});
        recipe(CraftingBlock.zetaFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thorium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.water, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.zeta, 4);
            recipe.craftTime = 60f;
        });
        
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.surgeAlloy, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 3.25f / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 2, NHItems.zeta, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.surgeAlloy, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.phaseFabric, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3.25f / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2, NHItems.zeta, 1);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.phaseFabric, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 2, NHItems.presstanium, 3, NHItems.oxide, 3);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 3);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 3, NHItems.presstanium, 2, NHItems.metaglass, 2);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 3);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 3, NHItems.presstanium, 2, NHItems.metalOxhydrigen, 4);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 3);
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 2, ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 5);
            recipe.priority = 1;
        });

        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 4, NHItems.surgeAlloy, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 3);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 5);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        
        recipe(CraftingBlock.irdryonMixer, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.phaseFabric, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 24 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.irdryonMixer, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 1);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 24 / 60f);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        
        recipe(CraftingBlock.hugeplastaniumFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 6);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.cyanogen, 3 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.plastanium, 9);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.hugeplastaniumFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.oil, 30 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.plastanium, 9);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.hugeplastaniumFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 6);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 1 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.plastanium, 9);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.hugeplastaniumFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 1);
            recipe.outputItem = ItemStack.list(NHItems.plastanium, 18);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.surgeAlloy, 1, NHItems.carbide, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3.25f / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.carbide, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 2 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.carbide, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.setonAlloy, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 3.25f / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.carbide, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 1);
            recipe.outputItem = ItemStack.list(NHItems.setonAlloy, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        
        recipe(CraftingBlock.upgradeSortFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 4, NHItems.seniorProcessor, 4);
            recipe.outputItem = ItemStack.list(NHItems.nodexPlate, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.upgradeSortFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1, ModuleBlock.quantumConductor, 1);
            recipe.outputItem = ItemStack.list(NHItems.nodexPlate, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        recipe(CraftingBlock.upgradeSortFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.tachyonEmitter, 1);
            recipe.outputItem = ItemStack.list(NHItems.hadronicomp, 2);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.ancimembraneConcentrator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 6);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.ancimembrane, 3);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.ancimembraneConcentrator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 1, ModuleBlock.pulseMutator, 1);
            recipe.outputItem = ItemStack.list(NHItems.ancimembrane, 6);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });
        recipe(CraftingBlock.ancimembraneConcentrator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCoreNegative, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 1);
            recipe.outputItem = ItemStack.list(NHItems.hyperProcessor, 2);
            recipe.craftTime = 60f;
        });
       

        recipe(CraftingBlock.factory0, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.tungsten, 4);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.armorCast, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory0, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 4, NHItems.juniorProcessor, 4);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerUnit, 2);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory1, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory1, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 4, NHItems.silicon, 4);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 2);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory2, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.powerCell, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory2, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 5);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 2);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory3, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 2, NHItems.carbide, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory3, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.phaseFabric, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory3, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.surgeAlloy, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory4, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.quantumConductor, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory4, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 8 / 60f);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.resistoArray, 1);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory5, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 1);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.particleModulator, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory5, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 1);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.pulseMutator, 1);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.factory6, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.quantumConductor, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 1);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.factory6, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.nodexPlate, 4);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 2);
            recipe.outputPayload = PayloadStack.list(ModuleBlock.tachyonEmitter, 1);
            recipe.craftTime = 60f;
        });

        recipe(PowerBlock.crystalDecompositionThermalGenerator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 1,NHItems.graphite,1);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid ,12 / 60f);
            recipe.craftTime = 90f;
        });
        recipe(PowerBlock.crystalDecompositionThermalGenerator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicar, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 12 / 60f);
            recipe.craftTime = 120f;
        });
        
        recipe(PowerBlock.nitrogenDissociator, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.slag, 40 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.nitrogen, 12 / 60f);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.nitrogenDissociator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.coal, 4);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.nitrogen, 12 / 60f);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.nitrogenDissociator, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.nitrogen, 24 / 60f);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.nitrogenDissociator, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 6 / 60f);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.nitrogen, 48 / 60f);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(PowerBlock.zetaGenerator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.nitrogen, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.fusionEnergy, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.zetaGenerator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.cryofluid, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.fusionEnergy, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.zetaGenerator, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.zeta, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.fusionEnergy, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.zetaGenerator, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 1);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.fusionEnergy, 5);
            recipe.craftTime = 60f;
            recipe.priority = 1;
        });

        recipe(PowerBlock.anodeFusionReactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 2, NHItems.surgeAlloy, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 8 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.thermoCorePositive, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.anodeFusionReactor, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 1, ModuleBlock.fusionReactor, 1);
            recipe.outputItem = ItemStack.list(NHItems.thermoCorePositive, 5);
            recipe.craftTime = 120f;
            recipe.priority = 1;
        });

        recipe(PowerBlock.cathodeFusionReactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.fusionEnergy, 2, NHItems.phaseFabric, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 8 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.thermoCoreNegative, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.cathodeFusionReactor, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 1, ModuleBlock.fusionReactor, 1);
            recipe.outputItem = ItemStack.list(NHItems.thermoCoreNegative, 5);
            recipe.craftTime = 120f;
            recipe.priority = 1;
        });

        recipe(PowerBlock.thermoReactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.ancimembrane, 2, NHItems.nodexPlate, 2, NHItems.thermoCorePositive, 1, NHItems.thermoCoreNegative, 1);
            recipe.outputItem = ItemStack.list(NHItems.darkEnergy, 2);
            recipe.craftTime = 120f;
        });
        recipe(PowerBlock.thermoReactor, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 2, NHItems.thermoCoreNegative, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.gaussReceptor, 1, ModuleBlock.tachyonEmitter, 1);
            recipe.outputItem = ItemStack.list(NHItems.darkEnergy, 8);
            recipe.craftTime = 120f;
            recipe.priority = 1;
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

}
