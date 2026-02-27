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
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.poly, 20 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 3, ModuleBlock.powerUnit, 4));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cleroi, 40 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.titanium, 75, Items.tungsten, 50));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cleroi, 40 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 12, ModuleBlock.powerUnit, 16));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.tungsten, 180, NHItems.zeta, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 40, ModuleBlock.crystalDiode, 25, ModuleBlock.supraGel, 12));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, NHItems.zeta, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 30, ModuleBlock.crystalDiode, 30, ModuleBlock.supraGel, 12));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 150, Items.titanium, 150, NHItems.zeta, 60));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 30, ModuleBlock.crystalDiode, 15, ModuleBlock.powerUnit, 15));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 240, Items.tungsten, 120, NHItems.zeta, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 40, ModuleBlock.supraGel, 12, ModuleBlock.powerUnit, 30));        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.silicon, 200, NHItems.presstanium, 100, NHItems.juniorProcessor, 100));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 10, ModuleBlock.supraGel, 12, ModuleBlock.powerUnit, 30));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.warper, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 200, NHItems.presstanium, 50, NHItems.juniorProcessor, 150));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.warper, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 20, ModuleBlock.supraGel, 12, ModuleBlock.powerUnit, 20));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 60 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 150, NHItems.presstanium, 150, NHItems.juniorProcessor, 50));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 60 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 30, ModuleBlock.supraGel, 12, ModuleBlock.powerUnit, 10));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 120, NHItems.juniorProcessor, 60, Items.carbide, 80, NHItems.metalOxhydrigen, 80));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 90 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.armorCast, 50, ModuleBlock.supraGel, 36, ModuleBlock.powerUnit, 30));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 90 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 150, Items.carbide, 100, NHItems.metalOxhydrigen, 100, Items.surgeAlloy, 20));
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 90 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.crystalDiode, 30, ModuleBlock.supraGel, 36, ModuleBlock.coolingUnit, 15));
        
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 600, Items.surgeAlloy, 300, NHItems.seniorProcessor, 100, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 180 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 60, ModuleBlock.bionicsProcessor, 105, ModuleBlock.fusionReactor, 30));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 500, NHItems.metalOxhydrigen, 600, Items.surgeAlloy, 300, NHItems.multipleSteel, 300));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 135 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 45, ModuleBlock.signalCirculator, 105, ModuleBlock.fusionReactor, 45));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 300, NHItems.metalOxhydrigen, 800, Items.surgeAlloy, 800, NHItems.multipleSteel, 400));
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 135 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.coolingUnit, 420, ModuleBlock.powerCell, 60, ModuleBlock.fusionReactor, 30));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.presstanium, 200, NHItems.juniorProcessor, 200, Items.phaseFabric, 300, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 135 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 30, ModuleBlock.bionicsProcessor, 45, ModuleBlock.signalCirculator, 45));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 200, Items.carbide, 150, Items.phaseFabric, 300, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 135 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.supraGel, 120, ModuleBlock.powerCell, 30, ModuleBlock.signalCirculator, 60));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 200, Items.surgeAlloy, 200, Items.phaseFabric, 200, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 180 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 60, ModuleBlock.fusionReactor, 30));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 200, Items.surgeAlloy, 600, NHItems.metalOxhydrigen, 600, NHItems.multipleSteel, 200));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 135 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 45, ModuleBlock.fusionReactor, 90));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.carbide, 300, Items.surgeAlloy, 400, Items.phaseFabric, 400, NHItems.seniorProcessor, 100));
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 180 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.powerCell, 90, ModuleBlock.bionicsProcessor, 30, ModuleBlock.signalCirculator, 30));

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 400, NHItems.setonAlloy, 200, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 240 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 60, ModuleBlock.fusionReactor, 45, ModuleBlock.resistoArray, 60));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 180 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 100, NHItems.irayrondPanel, 100, NHItems.setonAlloy, 200, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 120 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.signalCirculator, 45, ModuleBlock.quantumConductor, 60));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.annihilation, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.irayrondPanel, 100, NHItems.setonAlloy, 400, NHItems.thermoCoreNegative, 50));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.annihilation, 160 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 40, ModuleBlock.resistoArray, 40, ModuleBlock.quantumConductor, 40));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.zeta, 1000, Items.surgeAlloy, 500, NHItems.seniorProcessor, 200, NHItems.irayrondPanel, 600, NHItems.thermoCorePositive, 100));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 240 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 60, ModuleBlock.quantumConductor, 60, ModuleBlock.particleModulator, 120));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 360 * 60f, recipe -> recipe.inputItem = ItemStack.list(Items.tungsten, 1000, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 800, NHItems.thermoCoreNegative, 250, NHItems.nodexPlate, 300));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 240 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 180, ModuleBlock.resistoArray, 180, ModuleBlock.pulseMutator, 120));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.seniorProcessor, 600, NHItems.thermoCoreNegative, 300, NHItems.nodexPlate, 600, NHItems.ancimembrane, 300));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 320 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 200, ModuleBlock.quantumConductor, 200, ModuleBlock.particleModulator, 400));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 240 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.juniorProcessor, 500, Items.carbide, 400, NHItems.multipleSteel, 800, NHItems.thermoCorePositive, 200, NHItems.nodexPlate, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 160 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.pulseMutator, 200, ModuleBlock.particleModulator, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 480 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.metalOxhydrigen, 1000, NHItems.seniorProcessor, 400, NHItems.thermoCorePositive, 300, NHItems.nodexPlate, 600, NHItems.ancimembrane, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 320 * 60f, recipe -> recipe.inputPayload = PayloadStack.list(ModuleBlock.fusionReactor, 200, ModuleBlock.resistoArray, 320, ModuleBlock.pulseMutator, 200));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 600 * 60f, recipe -> recipe.inputItem = ItemStack.list(NHItems.thermoCorePositive, 2500, NHItems.thermoCoreNegative, 2500, NHItems.nodexPlate, 3000, NHItems.ancimembrane, 2000));
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 400 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.darkEnergy, 800);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.tachyonEmitter, 400, ModuleBlock.gaussReceptor, 400);
        });
    }


    public static void unitRecipePlan(Block block, UnitType unitType, float craftTime, Cons<Recipe> recipe) {
        if (block instanceof JumpGate gate) {
            Recipe r = new Recipe();
            recipe.get(r);
            gate.addUnitRecipe(unitType, craftTime, r);
        }
    }
}
