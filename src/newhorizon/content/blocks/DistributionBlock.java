package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.distribution.transport.*;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block conveyor, conveyorJunction, conveyorRouter, conveyorMerger, conveyorGate;

    public static void load(){
        conveyor = new AdaptConveyor("hard-light-rail"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            buildCost = 2f;
            speed = 0.115f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;
        }};

        conveyorJunction = new AdaptJunction("logistics-junction", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 3;
            capacity = 1;
        }};

        conveyorRouter = new AdaptDirectionalRouter("logistics-router", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 4f;
        }};

        conveyorMerger = new AdaptDirectionMerger("logistics-merger", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 4f;
        }};

        conveyorGate = new AdaptDirectionalGate("logistics-gate", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 4f;
        }};

        ((AdaptConveyor) conveyor).junctionReplacement = conveyorJunction;
    }
}
