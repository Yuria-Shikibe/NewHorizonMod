package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.flood.FloodBase;
import newhorizon.expand.block.flood.FloodCore;
import newhorizon.expand.block.flood.FloodMargin;
import newhorizon.expand.block.flood.FloodWall;

public class FloodContentBlock {
    public static Block dummy11, dummy22, dummy44, dummy88, flood88core;

    public static void load(){
        dummy11 = new FloodMargin("dummy11"){{
            requirements(Category.defense, BuildVisibility.editorOnly, ItemStack.with(NHItems.zeta, 5));
            health = 1000;
            armor = 8;
            size = 1;
        }};
        dummy22 = new FloodWall("dummy22"){{
            requirements(Category.defense, BuildVisibility.editorOnly, ItemStack.with(NHItems.zeta, 5));
            health = 4000;
            armor = 12;
            size = 2;

            nextBlock = dummy11;
        }};
        dummy44 = new FloodWall("dummy44"){{
            requirements(Category.defense, BuildVisibility.editorOnly, ItemStack.with(NHItems.zeta, 5));
            health = 16000;
            armor = 15;
            size = 4;

            nextBlock = dummy22;
        }};
        dummy88 = new FloodWall("dummy88"){{
            requirements(Category.defense, BuildVisibility.editorOnly, ItemStack.with(NHItems.zeta, 5));
            health = 64000;
            armor = 18;
            size = 8;

            nextBlock = dummy44;
        }};
        flood88core = new FloodCore("flood88core"){{
            requirements(Category.defense, BuildVisibility.editorOnly, ItemStack.with(NHItems.zeta, 5));
            health = 150000;
            armor = 25;
            size = 8;
        }};
    }
}
