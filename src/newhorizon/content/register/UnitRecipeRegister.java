package newhorizon.content.register;

import arc.func.Cons;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.ItemStack;
import mindustry.type.PayloadStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
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
            recipe.inputItem = ItemStack.list(Items.silicon, 10, Items.titanium, 15);
        });
        unitRecipePlan(UnitBlock.jumpGatePrimary, UnitTypes.poly, 10 * 60f, recipe -> {
            recipe.inputItem = ItemStack.list(Items.titanium, 10);
            recipe.inputPayload = PayloadStack.list(ModuleBlock.wiringKit, 1);
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
