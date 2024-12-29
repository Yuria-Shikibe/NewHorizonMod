package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;

public class CraftingBlocks {
    public static Block FluxPhaser;

    public static void load(){
        FluxPhaser = new GenericCrafter("flux-phaser"){{
            requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));

            size = 3;
        }};
    }
}
