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
import newhorizon.expand.block.production.factory.RecipeGenericCrafter;
import newhorizon.expand.type.Recipe;

public class RecipeRegister {
    public static void load(){
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 3);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 3, NHItems.graphite, 2);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 3);
            recipe.craftTime = 90f;
        });
        recipe(CraftingBlock.stampingFacility, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.titanium, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 1);
            recipe.outputItem = ItemStack.list(NHItems.presstanium, 5);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.quantumLiquid, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.silicon, 2, NHItems.graphite, 2);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorPrinter, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 1);
            recipe.outputItem = ItemStack.list(NHItems.juniorProcessor, 5);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.outputItem = ItemStack.list(Items.carbide, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crucibleFoundry, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(Items.carbide, 5);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 8 / 60f, NHLiquids.quantumLiquid, 6 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 8 / 60f, NHLiquids.water, 12 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.crystallizer, recipe -> {
            recipe.inputLiquid = LiquidStack.list(NHLiquids.xenFluid, 12 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(NHItems.metalOxhydrigen, 6);
            recipe.craftTime = 60f;
        });
        
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 4 / 60f);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 3 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.surgeRefactor, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 2 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(Items.surgeAlloy, 5);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(Items.phaseFabric, 2);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3 / 60f);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.fabricSynthesizer, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 2);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 2 / 60f);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 1);
            recipe.outputItem = ItemStack.list(Items.phaseFabric, 6);
            recipe.craftTime = 90f;
        });

        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.tungsten, 3, NHItems.presstanium, 2, NHItems.metalOxhydrigen, 4);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 3);
        });
        recipe(CraftingBlock.multipleSteelFactory, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.presstanium, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 1, ModuleBlock.coolingUnit, 1);
            recipe.outputItem = ItemStack.list(NHItems.multipleSteel, 3);
        });

        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 4, NHItems.surgeAlloy, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 2);
            recipe.craftTime = 60f;
        });
        recipe(CraftingBlock.processorEncoder, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 2);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 1);
            recipe.outputItem = ItemStack.list(NHItems.seniorProcessor, 5);
            recipe.craftTime = 60f;
        });

        recipe(CraftingBlock.irayrondFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.surgeAlloy, 2, Items.plastanium, 3);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.zetaFluidNegative, 4 / 60f);
            recipe.outputItem = ItemStack.list(NHItems.irayrondPanel, 4);
            recipe.outputLiquid = LiquidStack.list(NHLiquids.zetaFluidPositive, 3 / 60f);
            recipe.craftTime = 120f;
        });


        recipe(CraftingBlock.setonFactory, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 4);
            recipe.inputLiquid = LiquidStack.list(NHLiquids.irdryonFluid, 4 / 60f, NHLiquids.zetaFluidPositive, 4 / 60f);
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
