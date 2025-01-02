package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

public class CraftingBlock {
    public static Block fluxPhaser, hyperZetaFactory;

    public static void load(){
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
    }
}