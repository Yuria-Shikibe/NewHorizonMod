package newhorizon.content.register;

import arc.func.Cons;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.content.blocks.UnitBlock;
import newhorizon.content.units.GroundUnitTypes;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.type.Recipe;

public class UnitRecipeRegister {
    public static void load() {
        //spawnList = Seq.with(UnitTypes.poly, UnitTypes.cleroi, UnitTypes.quasar, UnitTypes.zenith, UnitTypes.cyerce, NHUnitTypes.ghost, NHUnitTypes.warper, NHUnitTypes.aliotiat, NHUnitTypes.rhino, NHUnitTypes.gather);
        //spawnList = Seq.with(NHUnitTypes.naxos, NHUnitTypes.tarlidor, NHUnitTypes.zarkov, UnitTypes.eclipse, UnitTypes.disrupt, UnitTypes.corvus, UnitTypes.navanax, UnitTypes.collaris);
        //spawnList = Seq.with(NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.annihilation, NHUnitTypes.saviour, NHUnitTypes.declining, NHUnitTypes.hurricane, NHUnitTypes.anvil, NHUnitTypes.sin, NHUnitTypes.collapser);

        unitRecipePlan(UnitBlock.jumpGateBasic, GroundUnitTypes.origin, 20 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.silicar, 20));
        unitRecipePlan(UnitBlock.jumpGateBasic, GroundUnitTypes.thynomo, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.graphite, 60,Items.titanium,80,Items.tungsten,80));
        unitRecipePlan(UnitBlock.jumpGateBasic, NHUnitTypes.sharp, 20 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.silicar, 20,Items.titanium,20));
        unitRecipePlan(UnitBlock.jumpGateBasic, NHUnitTypes.branch, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 80, Items.graphite, 80,Items.titanium,60,Items.tungsten,80));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.poly, 20 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 20, Items.titanium, 25));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.horizon, 20 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.graphite, 60));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.minke, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.graphite, 60,Items.titanium,30));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.oxynoe, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.graphite, 60,Items.tungsten,30));
        unitRecipePlan(UnitBlock.jumpGateBasic, NHUnitTypes.relay, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.graphite, 60,Items.tungsten,80,NHItems.titanium,80));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.locus, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.tungsten, 60,Items.titanium,60));
        unitRecipePlan(UnitBlock.jumpGateBasic, UnitTypes.cleroi, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.titanium, 75, Items.tungsten, 50));

        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.assaulter, 30 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 40, NHItems.silicar, 200, Items.thorium, 120));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.tungsten, 180, Items.thorium, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, Items.thorium, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 150, Items.titanium, 150, Items.thorium, 60));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 240, Items.tungsten, 120, Items.thorium, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 200, NHItems.presstanium, 100, NHItems.juniorProcessor, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.warper, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 200, NHItems.presstanium, 50, NHItems.juniorProcessor, 150));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.thorium, 150, NHItems.presstanium, 150, NHItems.juniorProcessor, 50));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 120, NHItems.juniorProcessor, 60, Items.carbide, 80, NHItems.metalOxhydrigen, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 150, Items.carbide, 100, NHItems.metalOxhydrigen, 100, Items.surgeAlloy, 20));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.obviate, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 80, Items.tungsten, 100, NHItems.metalOxhydrigen, 80, Items.phaseFabric, 10));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.precept, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 80, Items.tungsten, 100, NHItems.thorium, 120, Items.carbide, 10));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.anthicus, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 80, Items.tungsten, 100, NHItems.carbide, 40, NHItems.multipleSteel, 10));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.scepter, 120 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 160, NHItems.metalOxhydrigen, 200, NHItems.carbide, 160, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quad, 120 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 120, NHItems.presstanium, 200, NHItems.carbide, 120, NHItems.multipleSteel, 180));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.tecta, 160 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 160, NHItems.surgeAlloy, 100, NHItems.phaseFabric, 100, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.sei, 160 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 200, NHItems.surgeAlloy, 80, NHItems.zeta, 40, NHItems.multipleSteel, 240));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.aegires, 160 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 160, NHItems.carbide, 120, NHItems.phaseFabric, 100, NHItems.metalOxhydrigen, 200));

        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 600, Items.surgeAlloy, 300, NHItems.seniorProcessor, 100, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.striker, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 400, Items.carbide, 400, NHItems.seniorProcessor, 120, NHItems.multipleSteel, 400));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 500, NHItems.metalOxhydrigen, 600, Items.surgeAlloy, 300, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 300, NHItems.metalOxhydrigen, 800, Items.surgeAlloy, 800, NHItems.multipleSteel, 400));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 200, NHItems.juniorProcessor, 200, Items.phaseFabric, 300, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.omura, 200 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 200, NHItems.juniorProcessor, 200, Items.phaseFabric, 300, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 200, Items.carbide, 150, Items.phaseFabric, 300, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 200, Items.surgeAlloy, 200, Items.phaseFabric, 200, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 200, Items.surgeAlloy, 600, NHItems.metalOxhydrigen, 600, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.carbide, 300, Items.surgeAlloy, 400, Items.phaseFabric, 400, NHItems.seniorProcessor, 100));

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 400, NHItems.setonAlloy, 200, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 100, NHItems.irayrondPanel, 100, NHItems.setonAlloy, 200, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, GroundUnitTypes.annihilation, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 100, NHItems.setonAlloy, 400, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 1000, Items.surgeAlloy, 500, NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 600, NHItems.thermoCorePositive, 100));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 1000, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 800, NHItems.thermoCoreNegative, 250, NHItems.nodexPlate, 300));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 600, NHItems.thermoCoreNegative, 300, NHItems.nodexPlate, 600, NHItems.ancimembrane, 300));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 500, Items.carbide, 400, NHItems.multipleSteel, 800, NHItems.thermoCorePositive, 200, NHItems.nodexPlate, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 1000, NHItems.seniorProcessor, 400, NHItems.thermoCorePositive, 300, NHItems.nodexPlate, 600, NHItems.ancimembrane, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 600 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 2500, NHItems.thermoCoreNegative, 2500, NHItems.nodexPlate, 3000, NHItems.ancimembrane, 2000));
        //ancient
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.restrictionEnzyme, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.carbide, 100, NHItems.metalOxhydrigen, 80, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.macrophage, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.phaseFabric, 200, Items.plastanium, 400, NHItems.seniorProcessor, 120, NHItems.multipleSteel, 200,NHItems.irayrondPanel,100));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.laugra, 420 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 400, Items.carbide, 400, NHItems.irayrondPanel, 400, NHItems.thermoCorePositive, 200, NHItems.nodexPlate, 300));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.guardian, 300 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.darkEnergy, 2400));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.pester, 600 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.hadronicomp, 2800, NHItems.hyperProcessor, 1600, NHItems.thermoCorePositive, 1000, NHItems.thermoCoreNegative, 1000));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.nucleoid, 1200 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.hadronicomp, 10000, NHItems.hyperProcessor, 10000, NHItems.darkEnergy, 20000));
//        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.ancientProbe, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.hadronicomp, 10, NHItems.hyperProcessor, 10));
    }


    public static void unitRecipePlan(Block block, UnitType unitType, float craftTime, Cons<Recipe> recipe) {
        if (block instanceof JumpGate gate) {
            Recipe r = new Recipe();
            recipe.get(r);
            gate.addUnitRecipe(unitType, craftTime, r);
        }
    }
}
