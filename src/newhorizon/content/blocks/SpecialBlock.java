package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.special.NexusCore;

import static mindustry.type.ItemStack.with;

public class SpecialBlock {
    public static NexusCore nexusCore;

    public static void load(){
        nexusCore = new NexusCore();
    }
}
