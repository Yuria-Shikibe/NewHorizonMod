package newhorizon.content.blocks;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.content.Recipes;
import newhorizon.expand.block.NHBlock;
import newhorizon.expand.block.energy.XenConduit;
import newhorizon.expand.block.env.HighAltitudeMarker;
import newhorizon.expand.block.env.OrePatch;
import newhorizon.expand.block.env.SteepCliff;
import newhorizon.expand.block.env.TiledFloor;
import newhorizon.expand.block.production.Assembler;
import newhorizon.expand.block.special.NexusCore;
import newhorizon.expand.recipe.Recipe;

import static mindustry.type.ItemStack.with;

public class TestBlock {
    public static Block xenConduit, xenCrafter, irayroudRefinery, xenFactory, orePatch, nexusCore;
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

        nexusCore = new NexusCore("nexus-core"){{
            requirements(Category.production, with(Items.copper, 6));

            size = 5;
        }};
        //steepCliff = new SteepCliff("steep-cliff");
        //highAltitude = new HighAltitudeMarker("high-altitude-marker");
    }
}
