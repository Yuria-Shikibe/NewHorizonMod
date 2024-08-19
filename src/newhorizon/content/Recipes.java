package newhorizon.content;

import mindustry.content.Items;
import mindustry.type.ItemStack;
import newhorizon.expand.recipe.Recipe;

public class Recipes {
    public static Recipe xenAlphaCraft, xenBetaCraft, xenGammaCraft;

    public static void load(){
        xenAlphaCraft = new Recipe("xen-alpha"){{
            inputItems = ItemStack.with(NHItems.zeta, 2);
            xenAmount = 200;
            xenThreshold = 200;
        }};

        xenBetaCraft = new Recipe("xen-beta"){{
            inputItems = ItemStack.with(Items.phaseFabric, 2);
            xenAmount = 500;
            xenThreshold = 350;
        }};

        xenGammaCraft = new Recipe("xen-gamma"){{
            inputItems = ItemStack.with(NHItems.ancimembrane, 2);
            xenAmount = 1200;
            xenThreshold = 500;
        }};
    }
}
