package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.special.NexusCore;

import static mindustry.type.ItemStack.with;

public class SpecialBlock {
    public static NexusCore nexusCore;

    public static void load(){
        nexusCore = new NexusCore("nexus-core"){{
            requirements(Category.production, with(Items.copper, 6));

            size = 5;
            unitType = NHUnitTypes.liv;
        }};
    }
}
