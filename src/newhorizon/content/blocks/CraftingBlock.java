package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block FluxPhaser;

    public static void load(){
        FluxPhaser = new AdaptCrafter("flux-phaser"){{
            requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.with(NHItems.zeta, 5));

            size = 3;

            addLink(2, -1, 1,  /**/ 2, 0, 1, /**/2, 1, 1, /**/
                    -2, -1, 1, /**/-2, 0, 1, /**/-2, 1, 1/**/);

            craftTime = 40f;
            consumePower(5);
            consumeItems(with(Items.silicon, 4, NHItems.zeta, 2));
            outputItems = with(Items.phaseFabric, 20);

            itemCapacity = 30;
        }};
    }
}
