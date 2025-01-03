package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block fluxPhaser, hyperZetaFactory, factory;

    public static void load(){
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
        factory = new AdaptCrafter("factory"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(NHItems.zeta, 5));

            size = 4;

            addLink(-2, 0, 1, /**/-2, 1, 1, /**/-2, 2, 1, /**/
                    -1, 3, 1, /**/0, 3, 1,  /**/1, 3, 1,  /**/
                    3, 1, 1,  /**/3, 0, 1,  /**/3, -1, 1, /**/
                    0, -2, 1, /**/1, -2, 1, /**/2, -2, 1, /**/
                    3, -2, 1  /**/
            );

            craftTime = 120f;
            consumePower(6);
            consumeItems(with(Items.thorium, 4));
            consumeLiquid(Liquids.cryofluid, 0.1f);
            outputItems = with(NHItems.zeta, 8);

            itemCapacity = 20;
        }};
    }
}
