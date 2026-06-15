package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.units.CoreUnitTypes;
import newhorizon.expand.block.special.AdaptOverdriveProjector;
import newhorizon.expand.block.special.AssignedBeacon;
import newhorizon.expand.block.special.RemoteCoreStorage;

import static mindustry.type.ItemStack.with;

public class SpecialBlock {
    public static Block
            coreConflux, coreArray, coreNexus, coreCluster,
            standardStorage, heavyStorage, remoteStorage, nexusCore, juniorModuleBeacon, seniorModuleBeacon;

    public static void load() {
        coreConflux = new CoreBlock("core-conflux") {{
            requirements(Category.effect, with(
                    NHItems.titanium, 1000,
                    NHItems.graphite, 500,
                    NHItems.silicon, 500
            ));

            alwaysUnlocked = true;

            size = 3;
            armor = 10f;
            health = 30000;
            itemCapacity = 6000;

            unitCapModifier = 8;
            buildCostMultiplier = 2f;

            unitType = CoreUnitTypes.scalar;

            drawTeamOverlay = false;
            requiresCoreZone = false;
            incinerateNonBuildable = false;
        }};

        coreArray = new CoreBlock("core-array") {{
            requirements(Category.effect, with(
                    NHItems.juniorProcessor, 2000,
                    NHItems.metalOxhydrigen, 1000,
                    NHItems.multipleSteel, 1000,
                    NHItems.carbide, 1000
            ));

            alwaysUnlocked = true;

            size = 4;
            armor = 15f;
            health = 80000;
            itemCapacity = 12000;

            unitCapModifier = 12;
            buildCostMultiplier = 2f;

            unitType = CoreUnitTypes.vector;

            drawTeamOverlay = false;
            requiresCoreZone = false;
            incinerateNonBuildable = false;
        }};

        coreNexus = new CoreBlock("core-nexus") {{
            requirements(Category.effect, with(
                    NHItems.multipleSteel, 4000,
                    NHItems.carbide, 4000,
                    NHItems.zeta, 2000,
                    NHItems.phaseFabric, 3000,
                    NHItems.surgeAlloy, 3000
            ));

            alwaysUnlocked = true;

            size = 5;
            armor = 25f;
            health = 200000;
            itemCapacity = 25000;

            unitCapModifier = 18;
            buildCostMultiplier = 2f;

            unitType = CoreUnitTypes.martix;

            drawTeamOverlay = false;
            requiresCoreZone = false;
            incinerateNonBuildable = false;
        }};

        coreCluster = new CoreBlock("core-cluster") {{
            requirements(Category.effect, with(
                    NHItems.zeta, 5000,
                    NHItems.seniorProcessor, 3000,
                    NHItems.irayrondPanel, 5000,
                    NHItems.setonAlloy, 5000
            ));

            alwaysUnlocked = true;

            size = 6;
            armor = 40f;
            health = 500000;
            itemCapacity = 50000;

            unitCapModifier = 25;
            buildCostMultiplier = 2f;

            unitType = CoreUnitTypes.tensor;

            drawTeamOverlay = false;
            requiresCoreZone = false;
            incinerateNonBuildable = false;
        }};

        standardStorage = new StorageBlock("standard-storage") {{
            requirements(Category.effect, with(NHItems.presstanium, 40, NHItems.juniorProcessor, 20));
            size = 2;
            health = 1200;
            itemCapacity = 1000;
        }};

        heavyStorage = new StorageBlock("heavy-storage") {{
            requirements(Category.effect, with(NHItems.presstanium, 80, NHItems.juniorProcessor, 40, Items.carbide, 40));
            size = 3;
            health = 4000;
            itemCapacity = 4000;
        }};

        remoteStorage = new RemoteCoreStorage("remote-storage") {{
            requirements(Category.effect, BuildVisibility.shown, with(
                    NHItems.irayrondPanel, 200,
                    NHItems.seniorProcessor, 200,
                    NHItems.multipleSteel, 120
            ));

            size = 3;
            health = 1500;
        }};

        juniorModuleBeacon = new AdaptOverdriveProjector("junior-module-beacon") {{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with(
                    NHItems.juniorProcessor, 120,
                    NHItems.presstanium, 160,
                    NHItems.carbide, 80,
                    NHItems.zeta, 120
            ));
            size = 3;
            range = 320f;
            speedBoost = 2f;
            speedBoostPhase = 1f;
            phaseRangeBoost = 0f;
            consumePower(600 / 60f);
            consumeItem(NHItems.phaseFabric).boost();
        }};

        seniorModuleBeacon = new AssignedBeacon("senior-module-beacon") {{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with(
                    NHItems.carbide, 80,
                    NHItems.zeta, 160,
                    NHItems.phaseFabric, 120,
                    NHItems.surgeAlloy, 200
            ));
            size = 3;
            maxLink = 8;
            range = 200f;
            speedBoost = 0.25f;
            speedBoostPhase = 0.25f;
            phaseRangeBoost = 0f;
            consumePower(1200 / 60f);
            consumeItem(NHItems.surgeAlloy).boost();
        }};
    }
}
