package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.distribution.item.*;
import newhorizon.expand.block.distribution.liquid.AdaptLiquidBridge;
import newhorizon.expand.block.distribution.liquid.AdaptLiquidDirectionalUnloader;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block conveyor, logisticsJunction, logisticsDirectionalRouter, logisticsDirectionalMerger, logisticsDirectionalGate, logisticsOmniGate, logisticsOmniSorter, logisticsOmniBlocker, conveyorBridge, conveyorUnloader;
    public static Block liquidBridge, liquidUnloader;

    public static void load(){
        conveyor = new AdaptConveyor("hard-light-rail"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 0.115f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;

            buildTime = 1f;

            saveConfig = false;
        }};

        logisticsJunction = new AdaptJunction("logistics-junction"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 3;
            capacity = 1;
        }};

        logisticsDirectionalRouter = new AdaptDirectionalRouter("logistics-directional-router"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        logisticsDirectionalMerger = new AdaptDirectionalMerger("logistics-directional-merger"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        logisticsDirectionalGate = new AdaptDirectionalGate("logistics-directional-gate"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            speed = 4f;
        }};

        logisticsOmniGate = new AdaptGate("logistics-omni-gate"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
        }};

        logisticsOmniSorter = new AdaptSorter("logistics-omni-sorter"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = false;
        }};

        logisticsOmniBlocker = new AdaptSorter("logistics-omni-blocker"){{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = true;
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

        ((AdaptConveyor) conveyor).junctionReplacement = logisticsJunction;
        ((AdaptConveyor) conveyor).bridgeReplacement = conveyorBridge;
    }
}
