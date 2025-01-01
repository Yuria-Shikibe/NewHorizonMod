package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.production.factory.content.FluxPhaser;

public class CraftingBlock {
    public static Block fluxPhaser;

    public static void load(){
        fluxPhaser = new FluxPhaser();
    }
}
