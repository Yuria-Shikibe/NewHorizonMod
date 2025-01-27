package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.AdaptConveyor;
import newhorizon.expand.block.distribution.AdaptItemBridge;
import newhorizon.expand.block.distribution.track.TrackRail;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block compositeReloadConveyor, hyperLinkConveyor, hardLightConveyor;
    public static Block compositeReloadBridge, hyperLinkBridge;

    public static void load(){
        hardLightConveyor = new TrackRail("hard-light-track-rail"){{
            requirements(Category.distribution, with(NHItems.zeta, 3, NHItems.irayrondPanel, 2));
            buildVisibility = BuildVisibility.sandboxOnly;
        }};
    }
}
