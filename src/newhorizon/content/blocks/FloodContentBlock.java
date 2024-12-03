package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.flood.FloodBlock;

public class FloodContentBlock {
    public static FloodBlock dummy11, dummy22, dummy44;

    public static void load(){
        dummy11 = new FloodBlock("dummy11"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 1;
        }};
        dummy22 = new FloodBlock("dummy22"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 2;
        }};
        dummy44 = new FloodBlock("dummy44"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 4;
        }};
    }
}
