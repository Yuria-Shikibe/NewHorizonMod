package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.flood.FloodFluidBlock;

public class FloodBlock {
    public static Block floodPipe;

    public static void load() {
        floodPipe = new FloodFluidBlock("flood-pipe") {{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with());
        }};
    }
}
