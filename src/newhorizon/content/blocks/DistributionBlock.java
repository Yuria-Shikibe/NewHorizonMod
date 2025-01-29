package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.distribution.transport.AdaptConveyor;
import newhorizon.expand.block.distribution.transport.AdaptDirectionalGate;
import newhorizon.expand.block.distribution.transport.AdaptDirectionalRouter;
import newhorizon.expand.block.distribution.transport.AdaptJunction;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block conveyor, conveyorJunction, conveyorRouter, conveyorGate;

    public static void load(){
        conveyor = new AdaptConveyor("hard-light-track-rail"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.sandboxOnly;

            speed = 0.115f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;
        }};

        conveyorJunction = new AdaptJunction("track-rail-junction", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.sandboxOnly;

            speed = 20;
            capacity = 8;
        }};

        conveyorRouter = new AdaptDirectionalRouter("track-rail-router", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.sandboxOnly;

            speed = 4f;
        }};

        conveyorGate = new AdaptDirectionalGate("track-rail-gate", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.sandboxOnly;

            speed = 4f;
        }};

        ((AdaptConveyor) conveyor).junctionReplacement = conveyorJunction;
    }
}
