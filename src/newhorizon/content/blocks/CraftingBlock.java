package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHLiquids;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.expand.block.production.factory.content.FluxPhaser;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block fluxPhaser;

    public static void load(){
        fluxPhaser = new FluxPhaser();
    }
}
