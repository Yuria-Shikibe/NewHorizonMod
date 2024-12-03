package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.flood.FloodBlock;

public class FloodContentBlock {
    public static FloodBlock dummy11, dummy22, dummy44, dummy88, turret22, turret44, unit22, unit44;

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
        dummy88 = new FloodBlock("dummy88"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 8;
        }};
        turret22 = new FloodBlock("dummy22turret"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 2;
        }};
        turret44 = new FloodBlock("dummy44turret"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 4;
        }};
        unit22 = new FloodBlock("dummy22unit"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 2;
        }};
        unit44 = new FloodBlock("dummy44unit"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            size = 4;
        }};
    }
}
