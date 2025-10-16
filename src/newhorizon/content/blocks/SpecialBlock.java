package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.special.AdaptOverdriveProjector;
import newhorizon.expand.block.special.AssignedBeacon;
import newhorizon.expand.block.special.NexusCore;
import newhorizon.expand.block.special.RemoteCoreStorage;

import static mindustry.type.ItemStack.with;

public class SpecialBlock {
    public static Block
            standardStorage, heavyStorage, remoteStorage, nexusCore, juniorModuleBeacon, seniorModuleBeacon, deviceTest;

    public static void load() {
        nexusCore = new NexusCore();

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
