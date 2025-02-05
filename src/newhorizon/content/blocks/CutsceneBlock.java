package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.cutscene.SubActionBusBlock;
import newhorizon.expand.block.cutscene.WorldEventController;

public class CutsceneBlock {
    public static Block subActionBusBlock, worldEventBlock;

    public static void load(){
        subActionBusBlock = new SubActionBusBlock("sub");
        worldEventBlock = new WorldEventController("world-event");
    }
}
