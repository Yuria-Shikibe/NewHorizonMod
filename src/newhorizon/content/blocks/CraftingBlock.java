package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.factory.AdaptCrafter;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

import static mindustry.type.ItemStack.with;

public class CraftingBlock {
    public static Block fluxPhaser, hyperZetaFactory, factory;

    public static void load(){
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();

    }
}
