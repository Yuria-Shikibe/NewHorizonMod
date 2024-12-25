package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.floodv2.SynthCore;
import newhorizon.expand.block.floodv2.SynthConduit;
import newhorizon.expand.block.floodv3.SyntherCore;
import newhorizon.expand.block.floodv3.SyntherVein;

public class FloodContentBlock {
    public static SynthCore test, testRot;
    public static SynthConduit testWall;
    public static Block SyntherCore, SyntherVein;

    public static void load(){
        /*
        test = new SynthCore("test"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 3;
        }};

        testRot = new SynthCore("test-rot"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            ignoreRotate = false;
            size = 3;
        }};

        testWall = new SynthConduit("synth-vein"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
        }};

         */

        SyntherCore = new SyntherCore("synther-core"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 8;
        }};

        SyntherVein = new SyntherVein("synther-vein"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 1;
        }};
    }
}
