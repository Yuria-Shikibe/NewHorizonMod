package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.distribution.transport.item.*;
import newhorizon.expand.block.distribution.transport.liquid.AdaptLiquidBridge;
import newhorizon.expand.block.distribution.transport.liquid.AdaptLiquidDirectionalUnloader;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block conveyor, conveyorJunction, conveyorRouter, conveyorMerger, conveyorGate, conveyorBridge, conveyorUnloader;
    public static Block liquidBridge, liquidUnloader;

    public static void load(){
        conveyor = new AdaptConveyor("hard-light-rail"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            buildCost = 2f;
            speed = 0.115f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;
        }};

        conveyorJunction = new AdaptJunction("logistics-junction", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 3;
            capacity = 1;
        }};

        conveyorRouter = new AdaptDirectionalRouter("logistics-router", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        conveyorMerger = new AdaptDirectionalMerger("logistics-merger", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        conveyorGate = new AdaptDirectionalGate("logistics-gate", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        conveyorBridge = new AdaptItemBridge("logistics-bridge", (AdaptConveyor) conveyor){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = false;
            range = 6;
            speed = 40;
            bufferCapacity = 20;
        }};

        conveyorUnloader = new AdaptDirectionalUnloader("logistics-unloader"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 60f/16.5f;
            hasPower = true;
            conductivePower = true;
        }};

        liquidBridge = new AdaptLiquidBridge("logistics-liquid-bridge", (AdaptConveyor) conveyor){{
            requirements(Category.liquid, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            hasPower = false;
        }};

        liquidUnloader = new AdaptLiquidDirectionalUnloader("logistics-liquid-unloader"){{
            requirements(Category.liquid, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = true;
            conductivePower = true;
        }};

        ((AdaptConveyor) conveyor).junctionReplacement = conveyorJunction;
        ((AdaptConveyor) conveyor).bridgeReplacement = conveyorBridge;
    }
}
