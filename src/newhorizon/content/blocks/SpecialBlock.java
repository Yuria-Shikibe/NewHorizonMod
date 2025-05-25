package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.special.AssignedBeacon;
import newhorizon.expand.block.special.DeviceBase;
import newhorizon.expand.block.special.NexusCore;

import static mindustry.type.ItemStack.with;

public class SpecialBlock {
    public static Block
            standardStorage, heavyStorage, nexusCore, juniorModuleBeacon, seniorModuleBeacon, deviceTest;

    public static void load(){
        nexusCore = new NexusCore();

        standardStorage = new StorageBlock("standard-storage"){{
            requirements(Category.effect, with(NHItems.presstanium, 80, NHItems.juniorProcessor, 40));
            size = 2;
            health = 1200;
            itemCapacity = 1000;
        }};

        heavyStorage = new StorageBlock("heavy-storage"){{
            requirements(Category.effect, with(NHItems.presstanium, 150, NHItems.juniorProcessor, 120, NHItems.metalOxhydrigen, 100, Items.carbide, 100));
            size = 3;
            health = 4000;
            itemCapacity = 2500;
        }};

        juniorModuleBeacon = new AssignedBeacon("junior-module-beacon"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with(NHItems.juniorProcessor, 120, NHItems.presstanium, 160, Items.carbide, 80));
            maxLink = 4;
            maxSlot = 2;
            range = 60f;
            powerCons = 600 / 60f;
        }};

        seniorModuleBeacon = new AssignedBeacon("senior-module-beacon"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with(NHItems.multipleSteel, 100, Items.surgeAlloy, 200, Items.phaseFabric, 150));
            maxLink = 10;
            maxSlot = 4;
            range = 80f;
            powerCons = 1000 / 60f;
        }};

        deviceTest = new DeviceBase("device-test"){{
            requirements(Category.effect, BuildVisibility.shown, ItemStack.with(NHItems.multipleSteel, 100, Items.surgeAlloy, 200, Items.phaseFabric, 150));
        }};
    }
}
