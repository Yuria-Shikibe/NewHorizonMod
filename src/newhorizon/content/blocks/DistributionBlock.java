package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.AdaptConveyor;
import newhorizon.expand.block.distribution.track.TrackRail;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block hardLightConveyor;

    public static void load(){
        hardLightConveyor = new AdaptConveyor("hard-light-track-rail"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.sandboxOnly;

            speed = 0.11f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;
        }};
    }
}
