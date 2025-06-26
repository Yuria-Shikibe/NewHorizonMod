package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.liquid.LiquidJunction;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.distribution.item.AdaptConveyor;
import newhorizon.expand.block.distribution.item.AdaptStackConveyor;
import newhorizon.expand.block.distribution.item.logistics.*;
import newhorizon.expand.block.distribution.liquid.AdaptConduit;
import newhorizon.expand.block.distribution.liquid.AdaptLiquidBridge;
import newhorizon.expand.block.distribution.liquid.AdaptLiquidDirectionalUnloader;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block
            conveyor, logisticsJunction, logisticsDirectionalRouter, logisticsDirectionalMerger,
            logisticsDirectionalGate, logisticsOmniGate, logisticsOmniSorter, logisticsOmniBlocker,
            conveyorBridge, conveyorBridgeExtend, conveyorUnloader, rapidUnloader,

    stackRail, steadystackRail,lightStackLoader, heavyStackLoader,

    conduit, conduitJunction, conduitRouter, liquidBridge, liquidBridgeExtend, liquidUnloader;

    public static void load() {
        conveyor = new AdaptConveyor("hard-light-rail") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 0.115f;
            displayedSpeed = 15f;
            framePeriod = 9.2f;

            buildTime = 1f;

            saveConfig = false;
            canOverdrive = false;
            placeableLiquid = true;

        }};

        stackRail = new AdaptStackConveyor("stack-rail") {{
            requirements(Category.distribution, with(NHItems.presstanium, 1, NHItems.juniorProcessor, 1));
            health = 320;
            speed = 6f / 60f;
            canOverdrive = false;
            placeableLiquid = true;

        }};

        steadystackRail = new AdaptStackConveyor("steady-stack-rail") {{
            requirements(Category.distribution, with(NHItems.multipleSteel, 1, NHItems.seniorProcessor, 1));
            health = 640;
            speed = 18f / 60f;
            canOverdrive = false;
            placeableLiquid = true;

        }};

        lightStackLoader = new AdaptStackConveyor("light-stack-loader") {{
            requirements(Category.distribution, with(NHItems.presstanium, 10, NHItems.juniorProcessor, 10));
            health = 320;
            speed = 6f / 60f;
            itemCapacity = 15;
            onlyCarry = false;
            canOverdrive = false;
            placeableLiquid = true;

        }};

        heavyStackLoader = new AdaptStackConveyor("heavy-stack-loader") {{
            requirements(Category.distribution, with(NHItems.multipleSteel, 10, NHItems.seniorProcessor, 10));
            health = 640;
            speed = 6f / 60f;
            itemCapacity = 60;
            onlyCarry = false;
            canOverdrive = false;
            placeableLiquid = true;

        }};

        conveyorBridge = new AdaptItemBridge("logistics-bridge") {{
            requirements(Category.distribution, with(Items.silicon, 4));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            itemOnce = 1;
            itemCapacity = 20;
            hasPower = false;
            range = 6;
            health = 320;

            placeableLiquid = true;

        }};

        conveyorBridgeExtend = new AdaptItemBridge("logistics-extend-bridge") {{
            requirements(Category.distribution, with(NHItems.multipleSteel, 5));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            itemOnce = 2;
            itemCapacity = 40;
            hasPower = false;
            range = 12;
            health = 640;

            placeableLiquid = true;

        }};

        logisticsJunction = new AdaptJunction("logistics-junction") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 3;
            capacity = 1;

            placeableLiquid = true;

        }};

        logisticsDirectionalRouter = new AdaptDirectionalRouter("logistics-directional-router") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 4f;

            placeableLiquid = true;

        }};

        logisticsDirectionalMerger = new AdaptDirectionalMerger("logistics-directional-merger") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 4f;

            placeableLiquid = true;

        }};

        logisticsDirectionalGate = new AdaptDirectionalGate("logistics-directional-gate") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 4f;

            placeableLiquid = true;

        }};

        logisticsOmniGate = new AdaptGate("logistics-omni-gate") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            placeableLiquid = true;
            health = 320;

        }};

        logisticsOmniSorter = new AdaptSorter("logistics-omni-sorter") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = false;
            placeableLiquid = true;
            health = 320;

        }};

        logisticsOmniBlocker = new AdaptSorter("logistics-omni-blocker") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = true;
            placeableLiquid = true;
            health = 320;

        }};

        conveyorUnloader = new AdaptDirectionalUnloader("logistics-unloader") {{
            requirements(Category.distribution, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 320;
            speed = 60f / 16.5f;
            hasPower = true;
            conductivePower = true;
            placeableLiquid = true;

        }};

        rapidUnloader = new AdaptUnloader("rapid-unloader") {{
            health = 320;
            speed = 0.5f;
            requirements(Category.distribution, BuildVisibility.shown, with(Items.silicon, 4));

            placeableLiquid = true;

        }};

        conduit = new AdaptConduit("conduit") {{
            requirements(Category.liquid, with(Items.silicon, 1));
            health = 320;
            liquidCapacity = 100f;
            liquidPressure = 1.2f;

            leaks = false;
            placeableLiquid = true;

        }};

        conduitJunction = new LiquidJunction("logistics-liquid-junction") {{
            requirements(Category.liquid, with(Items.silicon, 4));
            placeableLiquid = true;
            solid = false;
            underBullets = true;
            health = 320;
        }};

        conduitRouter = new LiquidRouter("logistics-liquid-router") {{
            requirements(Category.liquid, with(Items.silicon, 4));
            placeableLiquid = true;
            solid = false;
            underBullets = true;
            health = 320;
            liquidCapacity = 200f;
        }};

        liquidBridge = new AdaptLiquidBridge("logistics-liquid-bridge") {{
            requirements(Category.liquid, with(Items.silicon, 4));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            hasPower = false;
            range = 6;
            health = 320;
            liquidCapacity = 200f;

            placeableLiquid = true;

        }};

        liquidBridgeExtend = new AdaptLiquidBridge("logistics-extend-liquid-bridge") {{
            requirements(Category.liquid, with(NHItems.multipleSteel, 5));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            hasPower = false;
            range = 12;
            health = 640;
            liquidCapacity = 200f;

            placeableLiquid = true;

        }};

        ((AdaptConduit) conduit).junctionReplacement = conduitJunction;
        ((AdaptConduit) conduit).bridgeReplacement = liquidBridge;

        liquidUnloader = new AdaptLiquidDirectionalUnloader("logistics-liquid-unloader") {{
            requirements(Category.liquid, with());
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = true;
            conductivePower = true;
            placeableLiquid = true;
            health = 320;
            liquidCapacity = 200f;

        }};

        ((AdaptConveyor) conveyor).junctionReplacement = logisticsJunction;
        ((AdaptConveyor) conveyor).bridgeReplacement = conveyorBridge;
    }
}
