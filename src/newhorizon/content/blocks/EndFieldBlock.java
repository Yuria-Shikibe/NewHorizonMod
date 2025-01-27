package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;

import static mindustry.type.ItemStack.with;

public class EndFieldBlock {
    public static Block smelter, planet, factory;

    public static void load(){
        smelter = new AdaptCrafter("smelter"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(NHItems.zeta, 100));

            size = 3;

            craftTime = 60f;
            consumePower(1080 / 60f);
            consumeItems(with(Items.silicon, 1));
            outputItems = with(Items.phaseFabric, 1);

            squareSprite = false;
        }};

        planet = new AdaptCrafter("planet"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(NHItems.zeta, 100));

            size = 5;

            craftTime = 60f;
            consumePower(1080 / 60f);
            consumeItems(with(Items.silicon, 1));
            outputItems = with(Items.phaseFabric, 1);

            squareSprite = false;
        }};

        factory = new AdaptCrafter("factory"){{
            requirements(Category.crafting, BuildVisibility.shown, ItemStack.with(NHItems.zeta, 100));

            size = 4;

            addLink(-2, -1, 1, -2, 0, 1, -2, 1, 1, -2, 2, 1,
                    3, -1, 1, 3, 0, 1, 3, 1, 1, 3, 2, 1);

            craftTime = 60f;
            consumePower(1080 / 60f);
            consumeItems(with(Items.silicon, 1));
            outputItems = with(Items.phaseFabric, 1);

            squareSprite = false;
        }};
    }
}
