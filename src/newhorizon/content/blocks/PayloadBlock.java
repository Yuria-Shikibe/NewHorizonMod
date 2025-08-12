package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.payload.ModuleConveyor;

public class PayloadBlock {
    public static Block payloadRail;

    public static void load() {
        payloadRail = new ModuleConveyor("module-rail") {{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 10));
        }};
    }
}
