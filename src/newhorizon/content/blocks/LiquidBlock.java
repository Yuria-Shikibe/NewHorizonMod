package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.liquid.AdaptPump;
import newhorizon.expand.block.stream.StreamRedirector;
import newhorizon.expand.block.stream.StreamRepeater;
import newhorizon.expand.block.stream.StreamSource;
import newhorizon.expand.block.stream.StreamSplitter;

import static mindustry.type.ItemStack.with;

public class LiquidBlock {
    public static Block
            streamSource, streamRepeater, streamRedirector, streamSplitter,
            turboPumpSmall, turboPump, standardLiquidStorage, heavyLiquidStorage;

    public static void load() {
        streamSource = new StreamSource("stream-source"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
        }};

        streamRepeater = new StreamRepeater("stream-repeater"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());

            rotateDraw = true;
        }};

        streamRedirector = new StreamRedirector("stream-redirector"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());

            rotateDraw = true;
        }};

        streamSplitter = new StreamSplitter("stream-splitter"){{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());

            rotateDraw = true;
        }};

        turboPumpSmall = new AdaptPump("turbo-pump-small") {{
            requirements(Category.liquid, with(Items.silicon, 10));
            consumePower(0.1f);

            pumpAmount = 0.20f;
            conductivePower = true;
            liquidCapacity = 40f;
            hasPower = true;
            size = 1;

            enableDrawStatus = false;
            placeableLiquid = true;
        }};

        turboPump = new AdaptPump("turbo-pump") {{
            requirements(Category.liquid, with(Items.titanium, 40, Items.tungsten, 30));
            consumePower(0.5f);

            pumpAmount = 0.25f;
            conductivePower = true;
            liquidCapacity = 120f;
            hasPower = true;
            size = 2;
            placeableLiquid = true;
        }};

        standardLiquidStorage = new LiquidRouter("standard-liquid-storage") {{
            requirements(Category.liquid, with(NHItems.metalOxhydrigen, 10, NHItems.presstanium, 15));
            health = 3200;
            size = 2;
            underBullets = true;
            liquidCapacity = 2000;
            armor = 20f;
            placeableLiquid = true;
        }};

        heavyLiquidStorage = new LiquidRouter("heavy-liquid-storage") {{
            requirements(Category.liquid, with(NHItems.metalOxhydrigen, 40, NHItems.presstanium, 60));
            health = 3200;
            size = 3;
            underBullets = true;
            liquidCapacity = 6000;
            armor = 20f;
            placeableLiquid = true;
        }};
    }
}
