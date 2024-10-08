package newhorizon.content.blocks;

import newhorizon.expand.block.special.NexusCore;

public class SpecialBlock {
    public static NexusCore nexusCore;

    public static void load(){
        nexusCore = new NexusCore();
    }
}
