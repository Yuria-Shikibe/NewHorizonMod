package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.synth.SynthCore;
import newhorizon.expand.block.synth.SynthConduit;

public class FloodContentBlock {
    public static SynthCore test, testRot;
    public static SynthConduit testWall;

    public static void load(){
        test = new SynthCore("test"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 3;
        }};

        testRot = new SynthCore("test-rot"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            ignoreRotate = false;
            size = 3;
        }};

        testWall = new SynthConduit("synth-conduit-1"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
        }};
    }
}
