package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.cutscene.SubActionBusBlock;
import newhorizon.expand.block.cutscene.WorldEventController;

public class CutsceneBlock {
    public static Block subActionBusBlock;

    public static void load(){
        subActionBusBlock = new SubActionBusBlock("sub");
    }
}
