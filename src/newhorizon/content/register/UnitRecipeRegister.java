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
            recipe.inputItem = ItemStack.list(Items.silicon, 180, Items.titanium, 180, NHItems.zetas, 80);
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
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.titanium, 15);
        });
        unitRecipePlan(UnitBlock.jumpGateStandard, NHUnitTypes.naxos, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.titanium, 15);
        });


        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.naxos, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.titanium, 15);
        });
        unitRecipePlan(UnitBlock.jumpGateHyper, NHUnitTypes.naxos, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.titanium, 15);
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
