package newhorizon.content.register;

import arc.func.Cons;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.content.blocks.ModuleBlock;
import newhorizon.content.blocks.UnitBlock;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.type.Recipe;

public class UnitRecipeRegister {
    public static void load(){
        //spawnList = Seq.with(UnitTypes.poly, UnitTypes.cleroi, UnitTypes.quasar, UnitTypes.zenith, UnitTypes.cyerce, NHUnitTypes.ghost, NHUnitTypes.warper, NHUnitTypes.aliotiat, NHUnitTypes.rhino, NHUnitTypes.gather);
        //spawnList = Seq.with(NHUnitTypes.naxos, NHUnitTypes.tarlidor, NHUnitTypes.zarkov, UnitTypes.eclipse, UnitTypes.disrupt, UnitTypes.corvus, UnitTypes.navanax, UnitTypes.collaris);
        //spawnList = Seq.with(NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.annihilation, NHUnitTypes.saviour, NHUnitTypes.declining, NHUnitTypes.hurricane, NHUnitTypes.anvil, NHUnitTypes.sin, NHUnitTypes.collapser);
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.poly, 20 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 20, Items.titanium, 25));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cleroi, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.titanium, 75, Items.tungsten, 50));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.tungsten, 180, NHItems.zeta, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, NHItems.zeta, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 150, Items.titanium, 150, NHItems.zeta, 60));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 240, Items.tungsten, 120, NHItems.zeta, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 200, NHItems.presstanium, 100, NHItems.juniorProcessor, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.warper, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 200, NHItems.presstanium, 50, NHItems.juniorProcessor, 150));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 150, NHItems.presstanium, 150, NHItems.juniorProcessor, 50));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 120, NHItems.juniorProcessor, 60, Items.carbide, 80, NHItems.metalOxhydrigen, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 150, NHItems.juniorProcessor, 100, Items.carbide, 100, NHItems.metalOxhydrigen, 100, Items.surgeAlloy, 20));
        
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 600, NHItems.presstanium, 300, NHItems.metalOxhydrigen, 600, NHItems.seniorProcessor, 100, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 300, Items.surgeAlloy, 300, NHItems.juniorProcessor, 500, NHItems.metalOxhydrigen, 600, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 300, Items.surgeAlloy, 600, NHItems.metalOxhydrigen, 1000, NHItems.multipleSteel, 400));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 300, NHItems.presstanium, 200, NHItems.juniorProcessor, 200, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 300, Items.carbide, 150, NHItems.zeta, 200, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 200, Items.surgeAlloy, 200, NHItems.juniorProcessor, 200, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.surgeAlloy, 600, NHItems.presstanium, 200, NHItems.juniorProcessor, 200, NHItems.metalOxhydrigen, 600, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.phaseFabric, 400, Items.surgeAlloy, 400, Items.carbide, 300, NHItems.presstanium, 400, NHItems.juniorProcessor, 400, NHItems.seniorProcessor, 100));

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 350, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 300, NHItems.fusionEnergy, 250));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 250));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.annihilation, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 250, NHItems.multipleSteel, 400, NHItems.seniorProcessor, 400, NHItems.fusionEnergy, 400));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 250, Items.surgeAlloy, 400, NHItems.seniorProcessor, 350, NHItems.thermoCoreNegative, 150, NHItems.zeta, 500));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.nodexPlate, 500, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 300, NHItems.thermoCoreNegative, 300, Items.tungsten, 1200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 800, NHItems.nodexPlate, 900, NHItems.seniorProcessor, 1200, NHItems.thermoCoreNegative, 500));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 1000, NHItems.setonAlloy, 800, NHItems.nodexPlate, 600, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 750));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 600, NHItems.nodexPlate, 750, NHItems.seniorProcessor, 300, NHItems.thermoCorePositive, 500, NHItems.presstanium, 1500));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 600 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.nodexPlate, 4500, NHItems.thermoCorePositive, 2000, NHItems.thermoCoreNegative, 2000));
    }

    public static void unitRecipePlan(Block block, UnitType unitType, float craftTime, Cons<Recipe> recipe) {
        if (block instanceof JumpGate gate) {
            Recipe r = new Recipe();
            recipe.get(r);
            gate.addUnitRecipe(unitType, craftTime, r);
        }
    }
}
