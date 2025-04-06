package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import newhorizon.expand.block.cutscene.SubActionBusBlock;
import newhorizon.expand.block.special.IconDisplay;

public class LogicBlock {
    public static Block subActionBusBlock, iconDisplay;

    public static void load(){
        subActionBusBlock = new SubActionBusBlock("sub");

        iconDisplay = new IconDisplay("icon-display"){{
            requirements(Category.logic, ItemStack.with(Items.graphite, 10));
        }};
    }
}
