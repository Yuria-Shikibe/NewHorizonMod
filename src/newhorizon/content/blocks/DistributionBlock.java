package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.graph.GraphBlock;

public class DistributionBlock {
    public static Block hardLightConveyor;

    public static void load(){
        hardLightConveyor = new GraphBlock("hard-light-conveyor");
    }
}
