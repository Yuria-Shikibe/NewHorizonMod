package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.distribution.Conveyor;
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
import newhorizon.expand.block.distribution.platform.FloatArmoredConveyor;
import newhorizon.expand.block.distribution.platform.FloatMultiJunction;
import newhorizon.expand.block.distribution.platform.FloatMultiRouter;

import static mindustry.type.ItemStack.with;

public class DistributionBlock {
    public static Block
            multiRouter, multiJunction, multiArmorConveyor,
            conveyor, logisticsJunction, logisticsDirectionalRouter, logisticsDirectionalMerger,
            logisticsDirectionalGate, logisticsOmniGate, logisticsOmniSorter, logisticsOmniBlocker,
            conveyorBridge, conveyorBridgeExtend, conveyorUnloader, rapidUnloader,
            stackRail, steadyStackRail,lightStackLoader, heavyStackLoader,
            conduit, conduitJunction, conduitRouter, liquidBridge, liquidBridgeExtend, liquidUnloader;

    public static void load() {
        multiRouter = new FloatMultiRouter("multi-router"){{
            size = 1;
            health = 560;
            speed = 2f;

            requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.lead, 5));
        }};

        multiJunction = new FloatMultiJunction("multi-junction"){{
            size = 1;
            health = 560;
            speed = 12f;
            capacity = 12;

            requirements(Category.distribution, with(NHItems.multipleSteel, 5, NHItems.juniorProcessor, 2, Items.copper, 5));
        }};
        multiArmorConveyor = new FloatArmoredConveyor("multi-armor-conveyor"){{
            requirements(Category.distribution,with(NHItems.zeta, 2, NHItems.multipleSteel, 2, Items.thorium, 1));
            speed = 0.12f;
            displayedSpeed = 18f;
            health = 800;
            armor = 5;
            junctionReplacement = multiJunction;
        }};
        conveyor = new AdaptConveyor("hard-light-rail") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 0.115f;
            displayedSpeed = 15f;

            placeableLiquid = true;
            drawTeamOverlay = false;
        }};

        stackRail = new AdaptStackConveyor("stack-rail") {{
            requirements(Category.distribution, with(NHItems.hardLight, 1, NHItems.presstanium, 1, NHItems.juniorProcessor, 1));
            health = 300;
            speed = 6f / 60f;
            canOverdrive = false;
            placeableLiquid = true;
        }};

        steadyStackRail = new AdaptStackConveyor("steady-stack-rail") {{
            requirements(Category.distribution, with(NHItems.hardLight, 10, NHItems.multipleSteel, 1, NHItems.seniorProcessor, 1));
            health = 600;
            speed = 18f / 60f;
            canOverdrive = false;
            placeableLiquid = true;
        }};

        lightStackLoader = new AdaptStackConveyor("light-stack-loader") {{
            requirements(Category.distribution, with(NHItems.hardLight, 5, NHItems.presstanium, 10, NHItems.juniorProcessor, 10));
            health = 300;
            speed = 6f / 60f;
            itemCapacity = 15;
            onlyCarry = false;
            canOverdrive = false;
            placeableLiquid = true;
        }};

        heavyStackLoader = new AdaptStackConveyor("heavy-stack-loader") {{
            requirements(Category.distribution, with(NHItems.hardLight, 10, NHItems.multipleSteel, 10, NHItems.seniorProcessor, 10));
            health = 600;
            speed = 6f / 60f;
            itemCapacity = 60;
            onlyCarry = false;
            canOverdrive = false;
            placeableLiquid = true;
        }};

        conveyorBridge = new AdaptItemBridge("logistics-bridge") {{
            requirements(Category.distribution, with(NHItems.hardLight, 5, Items.silicon, 4));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = false;
            range = 6;
            health = 300;

            placeableLiquid = true;
        }};
        
        conveyorBridgeExtend = new AdaptItemBridge("logistics-extend-bridge") {{
            requirements(Category.distribution, with(NHItems.hardLight, 10, NHItems.multipleSteel, 5));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = false;
            range = 12;
            health = 600;

            placeableLiquid = true;
        }};

        logisticsJunction = new AdaptJunction("logistics-junction") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 3;
            capacity = 1;
            
            placeableLiquid = true;
        }};

        logisticsDirectionalRouter = new AdaptDirectionalRouter("logistics-directional-router") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 4f;

            placeableLiquid = true;
        }};

        logisticsDirectionalMerger = new AdaptDirectionalMerger("logistics-directional-merger") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 4f;

            placeableLiquid = true;
        }};

        logisticsDirectionalGate = new AdaptDirectionalGate("logistics-directional-gate") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 4f;

            placeableLiquid = true;
        }};

        logisticsOmniGate = new AdaptGate("logistics-omni-gate") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            placeableLiquid = true;
            health = 300;
        }};

        logisticsOmniSorter = new AdaptSorter("logistics-omni-sorter") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = false;
            placeableLiquid = true;
            health = 300;
        }};

        logisticsOmniBlocker = new AdaptSorter("logistics-omni-blocker") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 1
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            invert = true;
            placeableLiquid = true;
            health = 300;
        }};

        conveyorUnloader = new AdaptDirectionalUnloader("logistics-unloader") {{
            requirements(Category.distribution, with(
                    NHItems.hardLight, 10
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            health = 300;
            speed = 60f / 16.5f;
            hasPower = true;
            conductivePower = true;
            placeableLiquid = true;
        }};

        rapidUnloader = new AdaptUnloader("rapid-unloader") {{
            requirements(Category.distribution, BuildVisibility.shown, with(
                    NHItems.hardLight, 10, 
                    Items.silicon, 4
            ));

            health = 300;
            speed = 0.5f;
            
            placeableLiquid = true;
        }};

        conduit = new AdaptConduit("conduit") {{
            requirements(Category.liquid, with(NHItems.hardLight, 1, Items.silicon, 1));
            health = 300;
            liquidCapacity = 150f;
            liquidPressure = 1.2f;

            leaks = false;
            placeableLiquid = true;

        }};

        conduitJunction = new LiquidJunction("logistics-liquid-junction") {{
            requirements(Category.liquid, with(NHItems.hardLight, 1, Items.silicon, 4));
            placeableLiquid = true;
            solid = false;
            underBullets = true;
            health = 300;
        }};

        conduitRouter = new LiquidRouter("logistics-liquid-router") {{
            requirements(Category.liquid, with(NHItems.hardLight, 1, Items.silicon, 4));
            placeableLiquid = true;
            solid = false;
            underBullets = true;
            health = 300;
            liquidCapacity = 250f;
        }};

        liquidBridge = new AdaptLiquidBridge("logistics-liquid-bridge") {{
            requirements(Category.liquid, with(NHItems.hardLight, 5, Items.silicon, 4));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            hasPower = false;
            range = 6;
            health = 300;
            liquidCapacity = 250f;

            placeableLiquid = true;
        }};

        liquidBridgeExtend = new AdaptLiquidBridge("logistics-extend-liquid-bridge") {{
            requirements(Category.liquid, with(NHItems.hardLight, 10, NHItems.multipleSteel, 5));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;
            hasPower = false;
            range = 12;
            health = 600;
            liquidCapacity = 250f;

            placeableLiquid = true;
        }};

        ((AdaptConduit) conduit).junctionReplacement = conduitJunction;
        ((AdaptConduit) conduit).bridgeReplacement = liquidBridge;

        liquidUnloader = new AdaptLiquidDirectionalUnloader("logistics-liquid-unloader") {{
            requirements(Category.liquid, with(
                    NHItems.hardLight, 5
            ));
            buildVisibility = BuildVisibility.shown;
            alwaysUnlocked = true;

            hasPower = true;
            conductivePower = true;
            placeableLiquid = true;
            health = 300;
            liquidCapacity = 250f;
        }};

        ((Conveyor) conveyor).junctionReplacement = logisticsJunction;
        ((Conveyor) conveyor).bridgeReplacement = conveyorBridge;
    }
}
