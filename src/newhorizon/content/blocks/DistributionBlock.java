package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHBlocks;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.FloatItemBridge;
import newhorizon.expand.block.distribution.transport.*;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block conveyor, conveyorJunction, conveyorRouter, conveyorMerger, conveyorGate, conveyorBridge;

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

        conveyorMerger = new AdaptDirectionalMerger("logistics-merger", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 4f;
        }};

        conveyorGate = new AdaptDirectionalGate("logistics-gate", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            speed = 4f;
        }};

        conveyorBridge = new AdaptItemBridge("logistics-bridge", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;

            hasPower = false;
            range = 6;
            speed = 40;
            bufferCapacity = 20;
        }};

        ((AdaptConveyor) conveyor).junctionReplacement = conveyorJunction;
        ((AdaptConveyor) conveyor).bridgeReplacement = conveyorBridge;
    }
}
