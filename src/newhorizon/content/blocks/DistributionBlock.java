package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.AdaptConveyor;
import newhorizon.expand.block.distribution.AdaptItemBridge;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block hyperLinkConveyor, compositeReloadConveyor, hardLightConveyor, compositeReloadBridge;

    public static void load(){

        compositeReloadConveyor = new AdaptConveyor("composite-reload-track-rail"){{
            requirements(Category.distribution, with(Items.titanium, 2, Items.copper, 2));
            itemCapacity = 8;
        }};

        hyperLinkConveyor = new AdaptConveyor("hyper-link-track-rail"){{
            requirements(Category.distribution, with(NHItems.multipleSteel, 1, NHItems.presstanium, 2));
            itemCapacity = 30;
        }};

        hardLightConveyor = new AdaptConveyor("hard-light-track-rail"){{
            requirements(Category.distribution, with(NHItems.zeta, 3, NHItems.irayrondPanel, 2));
            itemCapacity = 120;
            drawPulse = true;
        }};

        compositeReloadBridge = new AdaptItemBridge("composite-reload-bridge"){{
            requirements(Category.distribution, with(Items.titanium, 20, Items.copper, 20));
            conveyor = (AdaptConveyor) compositeReloadConveyor;
        }};
    }
}
