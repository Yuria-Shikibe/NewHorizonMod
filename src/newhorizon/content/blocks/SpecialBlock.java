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
        nexusCore = new NexusCore("nexus-core"){{
            requirements(Category.effect, with(NHItems.zeta, 1500, NHItems.presstanium, 1000, NHItems.juniorProcessor, 1000, NHItems.metalOxhydrigen, 1800, NHItems.multipleSteel, 600));

            alwaysUnlocked = true;
            isFirstTier = true;

            unitType = NHUnitTypes.liv;
            health = 30000;
            itemCapacity = 10000;
            size = 5;
            armor = 20f;
            incinerateNonBuildable = false;
            buildCostMultiplier = 2f;
            requiresCoreZone = true;

            unitCapModifier = 10;
        }};
    }
}
