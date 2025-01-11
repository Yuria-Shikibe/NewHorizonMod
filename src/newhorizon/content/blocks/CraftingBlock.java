package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.production.factory.content.FluxPhaser;
import newhorizon.expand.block.production.factory.content.GlassQuantifier;
import newhorizon.expand.block.production.factory.content.HyperZetaFactory;

public class CraftingBlock {
    public static Block fluxPhaser, hyperZetaFactory, glassQuantifier;

    public static void load(){
        fluxPhaser = new FluxPhaser();
        hyperZetaFactory = new HyperZetaFactory();
        glassQuantifier = new GlassQuantifier();
    }
}
