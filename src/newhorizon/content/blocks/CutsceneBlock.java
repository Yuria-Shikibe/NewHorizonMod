package newhorizon.content.blocks;

import mindustry.world.Block;
import newhorizon.expand.block.cutscene.SubActionBusBlock;

public class CutsceneBlock {
    public static Block subActionBusBlock;

    public static void load(){
        subActionBusBlock = new SubActionBusBlock("sub");
    }
}
