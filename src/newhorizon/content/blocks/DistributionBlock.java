package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.AdaptConveyor;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block hyperLinkConveyor, compositeReloadConveyor, hardLightConveyor;

    public static void load(){
        hyperLinkConveyor = new AdaptConveyor("hyper-link-track-rail"){{
            requirements(Category.distribution, with(NHItems.presstanium, 1));
            itemCapacity = 20;
        }};

        compositeReloadConveyor = new AdaptConveyor("composite-reload-track-rail"){{
            requirements(Category.distribution, with(NHItems.multipleSteel, 1));
            itemCapacity = 50;
        }};

        hardLightConveyor = new AdaptConveyor("hard-light-track-rail"){{
            requirements(Category.distribution, with(NHItems.zeta, 2, NHItems.irayrondPanel, 1));
            itemCapacity = 120;
            drawPulse = true;
        }};
    }
}
