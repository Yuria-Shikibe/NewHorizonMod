package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.special.AssignedBeacon;
import newhorizon.expand.block.special.NexusCore;

public class SpecialBlock {
    public static NexusCore nexusCore;
    public static Block juniorModuleBeacon, seniorModuleBeacon;

    public static void load(){
        nexusCore = new NexusCore();

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
    }
}
