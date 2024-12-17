package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.synth.SynthCore;

public class FloodContentBlock {
    public static SynthCore test;

    public static void load(){
        test = new SynthCore("test"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 3;
        }};
    }
}
