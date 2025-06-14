package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import newhorizon.expand.block.special.IconDisplay;

public class LogicBlock {
    public static Block iconDisplay;

    public static void load() {
        iconDisplay = new IconDisplay("icon-display") {{
            requirements(Category.logic, ItemStack.with(Items.graphite, 10));
        }};
    }
}
