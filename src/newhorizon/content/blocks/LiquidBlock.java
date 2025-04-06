package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.expand.block.liquid.AdaptPump;

import static mindustry.type.ItemStack.with;

public class LiquidBlock {
    public static Block hydroPump, turboPump;

    public static void load(){
        hydroPump = new AdaptPump("hydro-pump"){{
            requirements(Category.liquid, with(Items.copper, 70, Items.metaglass, 50, Items.silicon, 20, Items.titanium, 35));
            pumpAmount = 0.2f;
            consumePower(0.3f);
            liquidCapacity = 120f;
            hasPower = true;
            size = 2;
        }};

        turboPump = new AdaptPump("turbo-pump"){{
            requirements(Category.liquid, with(Items.copper, 70, Items.metaglass, 50, Items.silicon, 20, Items.titanium, 35));
            pumpAmount = 0.2f;
            consumePower(0.3f);
            liquidCapacity = 120f;
            hasPower = true;
            size = 4;
        }};
    }
}
