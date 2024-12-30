package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.content.Recipes;
import newhorizon.expand.block.energy.XenConduit;
import newhorizon.expand.block.env.TiledFloor;
import newhorizon.expand.block.production.factory.Assembler;

import static mindustry.type.ItemStack.with;

public class TestBlock {
    public static Block xenConduit, xenCrafter, irayroudRefinery, xenFactory, orePatch;
    public static TiledFloor ancientEnergyFloor;
    //steepCliff,
    //highAltitude;


    public static void load(){
        xenConduit = new XenConduit("xen-conduit"){{
            requirements(Category.defense, with(Items.copper, 6));
        }};

        xenCrafter = new Assembler("xen-crafter"){{
            requirements(Category.production, with(Items.copper, 6));

            size = 3;
            xenArea = 20f;
            recipeSeq.add(Recipes.xenAlphaCraft, Recipes.xenBetaCraft, Recipes.xenGammaCraft);
        }};

        xenFactory = new Assembler("xen-factory"){{
            requirements(Category.production, with(Items.copper, 6));

            size = 4;
            xenArea = 20f;
            recipeSeq.add(Recipes.xenAlphaCraft, Recipes.xenBetaCraft, Recipes.xenGammaCraft);
        }};

        irayroudRefinery = new Assembler("irayroud-refinery"){{
            requirements(Category.production, with(Items.copper, 6));

            size = 4;
            xenArea = 20f;
            recipeSeq.add(Recipes.xenAlphaCraft, Recipes.xenBetaCraft, Recipes.xenGammaCraft);
        }};
        //steepCliff = new SteepCliff("steep-cliff");
        //highAltitude = new HighAltitudeMarker("high-altitude-marker");
    }
}
