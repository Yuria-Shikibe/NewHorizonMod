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
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.poly, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 20, Items.titanium, 25);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.poly, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 1, ModuleBlock.armorCast, 1);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cleroi, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 60, Items.titanium, 75);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cleroi, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 2, ModuleBlock.armorCast, 3);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 160, Items.titanium, 160, NHItems.zeta, 60);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.mega, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 3, ModuleBlock.armorCast, 4, ModuleBlock.powerCell, 1);
        });
        
         unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, NHItems.zeta, 80);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.quasar, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 4, ModuleBlock.armorCast, 4, ModuleBlock.powerCell, 2);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 160, Items.titanium, 160, NHItems.zeta, 60);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.zenith, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 3, ModuleBlock.armorCast, 3, ModuleBlock.powerCell, 1);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, NHItems.zeta, 80);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.cyerce, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 4, ModuleBlock.armorCast, 4, ModuleBlock.powerCell, 2);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 120, NHItems.juniorProcessor, 200, NHItems.zeta, 120, NHItems.metalOxhydrigen, 80);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 3, ModuleBlock.heatDetector, 4, ModuleBlock.powerCell, 2, ModuleBlock.supraGel, 1);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 120, NHItems.juniorProcessor, 200, NHItems.zeta, 120, NHItems.metalOxhydrigen, 80);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.ghost, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 3, ModuleBlock.heatDetector, 4, ModuleBlock.powerCell, 2, ModuleBlock.supraGel, 1);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 120, NHItems.juniorProcessor, 200, NHItems.zeta, 120, NHItems.metalOxhydrigen, 80);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.aliotiat, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 3, ModuleBlock.heatDetector, 4, ModuleBlock.powerCell, 2, ModuleBlock.supraGel, 1);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 200, NHItems.juniorProcessor, 200, NHItems.metalOxhydrigen, 100, Items.surgeAlloy, 180);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.rhino, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 4, ModuleBlock.heatDetector, 4, ModuleBlock.fissionCell, 2, ModuleBlock.supraGel, 2);
        });
        
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.carbide, 200, NHItems.juniorProcessor, 200, NHItems.metalOxhydrigen, 100, Items.surgeAlloy, 180);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, NHUnitTypes.gather, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.powerUnit, 4, ModuleBlock.heatDetector, 4, ModuleBlock.fissionCell, 2, ModuleBlock.supraGel, 2);
        });

        
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.tarlidor, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.zarkov, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.eclipse, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.disrupt, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.corvus, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.navanax, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.phaseFabric, 150, Items.surgeAlloy, 150, NHItems.metalOxhydrigen, 300, NHItems.seniorProcessor, 100);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, UnitTypes.collaris, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.gaussReceptor, 4, ModuleBlock.chargeCompensator, 2, ModuleBlock.coolingUnit, 2);
        });

      
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 350, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 300, NHItems.fusionEnergy, 250);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.destruction, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.hadronBuffers, 4,  ModuleBlock.echoCanceller, 3, ModuleBlock.fusionReactor, 3, ModuleBlock.particleModulator, 3);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 250);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.longinus, 10 * 60f, recipe -> {
            recipe.inputPayload =PayloadStack.list(ModuleBlock.memoryRecalibrator, 3, ModuleBlock.hadronBuffers, 4,  ModuleBlock.echoCanceller, 3, ModuleBlock.chargeCompensator, 4, ModuleBlock.particleModulator, 3);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.annihilation, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 250, NHItems.multipleSteel, 400, NHItems.seniorProcessor, 400, NHItems.fusionEnergy, 400);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.annihilation, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.bionicsProcessor, 4, ModuleBlock.tachyonEmitter, 3,  ModuleBlock.gaussReceptor, 4, ModuleBlock.fusionReactor, 3, ModuleBlock.particleModulator, 3);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 250, Items.surgeAlloy, 400, NHItems.seniorProcessor, 350, NHItems.thermoCoreNegative, 150, Items.plastanium, 400, NHItems.zeta, 500);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.saviour, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.memoryRecalibrator, 3, ModuleBlock.tachyonEmitter, 3,  ModuleBlock.echoCanceller, 3, ModuleBlock.fusionReactor, 3, ModuleBlock.particleModulator, 3);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 500, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 300, NHItems.thermoCoreNegative, 300, Items.tungsten, 1200);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.declining, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 2, ModuleBlock.tachyonEmitter, 4,  ModuleBlock.pulseMutator, 2, ModuleBlock.multiphasePropellant, 4, ModuleBlock.particleModulator, 4);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 800, NHItems.upgradeSort, 900, NHItems.seniorProcessor, 1200, NHItems.thermoCoreNegative, 500);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.hurricane, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.memoryRecalibrator, 4, ModuleBlock.neutronMembrane, 2,  ModuleBlock.echoCanceller, 4, ModuleBlock.multiphasePropellant, 2, ModuleBlock.quantumConductor, 2);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.multipleSteel, 1000, NHItems.setonAlloy, 800, NHItems.upgradeSort, 600, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 750);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.anvil, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 2, ModuleBlock.neutronMembrane, 3,  ModuleBlock.pulseMutator, 2, ModuleBlock.fusionReactor, 4, ModuleBlock.particleModulator, 4);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.setonAlloy, 600, NHItems.upgradeSort, 750, NHItems.seniorProcessor, 300, NHItems.thermoCorePositive, 500, NHItems.presstanium, 1500);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.sin, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.memoryRecalibrator, 4, ModuleBlock.neutronMembrane, 2,  ModuleBlock.pulseMutator, 2, ModuleBlock.multiphasePropellant, 2, ModuleBlock.particleModulator, 4);
        });

        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(NHItems.upgradeSort, 4500, NHItems.thermoCorePositive, 2000, NHItems.thermoCoreNegative, 2000);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.collapser, 10 * 60f, recipe -> {
            recipe.inputPayload = PayloadStack.list(ModuleBlock.resistoArray, 6, ModuleBlock.neutronMembrane, 6,  ModuleBlock.pulseMutator, 6, ModuleBlock.multiphasePropellant, 6, ModuleBlock.quantumConductor, 6);
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
