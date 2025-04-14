package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.payload.ModuleConveyor;
import newhorizon.expand.block.payload.ModuleSource;
import newhorizon.expand.block.payload.ModuleVoid;

import static mindustry.type.ItemStack.with;

public class PayloadBlock {
    public static Block payloadRail, moduleSource, moduleVoid;

    public static void load(){
        moduleSource = new ModuleSource("module-source"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            size = 2;
            alwaysUnlocked = true;
        }};

        moduleVoid = new ModuleVoid("module-void"){{
            requirements(Category.units, BuildVisibility.sandboxOnly, with());
            size = 2;
            alwaysUnlocked = true;
        }};

        payloadRail = new ModuleConveyor("module-rail"){{
            requirements(Category.units, BuildVisibility.shown, ItemStack.with(NHItems.presstanium, 10));
        }};
    }
}
