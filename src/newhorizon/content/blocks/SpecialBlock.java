package newhorizon.content.blocks;

import newhorizon.expand.block.special.NexusCore;
import newhorizon.expand.block.special.WarpPortalIniter;

public class SpecialBlock {
    public static NexusCore nexusCore;

    public static WarpPortalIniter warpPortalIniter;

    public static void load(){
        nexusCore = new NexusCore();
        warpPortalIniter = new WarpPortalIniter();
    }
}
