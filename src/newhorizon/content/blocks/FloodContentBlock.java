package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.flood.FloodBase;
import newhorizon.expand.block.flood.FloodCore;
import newhorizon.expand.block.flood.FloodWall;

public class FloodContentBlock {
    public static Block dummy11, dummy22, dummy44, dummy88, flood88core;

    public static void load(){
        dummy11 = new FloodBase("dummy11"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            health = 1000;
            size = 1;
        }};
        dummy22 = new FloodWall("dummy22"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            health = 5000;
            size = 2;
        }};
        dummy44 = new FloodWall("dummy44"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            health = 20000;
            size = 4;
        }};
        dummy88 = new FloodWall("dummy88"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            health = 80000;
            size = 8;
        }};
        flood88core = new FloodCore("flood88core"){{
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));
            health = 80000;
            size = 8;
        }};
    }
}
