package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.AdaptConveyor;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block hyperLinkConveyor, hardLightConveyor;

    public static void load(){
        hyperLinkConveyor = new AdaptConveyor("hyper-link-conveyor"){{
            requirements(Category.distribution, with(NHItems.presstanium, 1));
            itemCapacity = 15;
        }};
        hardLightConveyor = new AdaptConveyor("hard-light-conveyor"){{
            requirements(Category.distribution, with(NHItems.zeta, 2, NHItems.irayrondPanel, 1));
            itemCapacity = 75;
            drawPulse = true;
        }};
    }
}
