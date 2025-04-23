package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.liquid.LiquidRouter;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.liquid.AdaptPump;

import static mindustry.type.ItemStack.with;

public class LiquidBlock {
    public static Block turboPump, standardLiquidStorage, heavyLiquidStorage;

    public static void load(){
        turboPump = new AdaptPump("turbo-pump"){{
            requirements(Category.liquid, with(Items.titanium, 40, Items.tungsten, 30));
            consumePower(0.5f);

            pumpAmount = 0.25f;
            conductivePower = true;
            liquidCapacity = 120f;
            hasPower = true;
            size = 2;
        }};

        standardLiquidStorage = new LiquidRouter("standard-liquid-storage"){{
            requirements(Category.liquid, with(NHItems.metalOxhydrigen, 10, NHItems.presstanium, 15));
            health = 3200;
            size = 2;
            underBullets = true;
            liquidCapacity = 2000;
            armor = 20f;
        }};

        heavyLiquidStorage = new LiquidRouter("heavy-liquid-storage"){{
            requirements(Category.liquid, with(NHItems.metalOxhydrigen, 40, NHItems.presstanium, 60));
            health = 3200;
            size = 3;
            underBullets = true;
            liquidCapacity = 6000;
            armor = 20f;
        }};
    }
}
