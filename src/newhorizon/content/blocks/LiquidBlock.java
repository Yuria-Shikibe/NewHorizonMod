package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.expand.block.liquid.AdaptPump;

import static mindustry.type.ItemStack.with;

public class LiquidBlock {
    public static Block turboPump;

    public static void load(){
        turboPump = new AdaptPump("turbo-pump"){{
            requirements(Category.liquid, with(Items.copper, 70, Items.metaglass, 50, Items.silicon, 20, Items.titanium, 35));
            consumePower(0.5f);

            pumpAmount = 0.25f;
            conductivePower = true;
            liquidCapacity = 120f;
            hasPower = true;
            size = 2;
        }};
    }
}
