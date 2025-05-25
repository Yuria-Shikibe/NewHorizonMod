package newhorizon.content;

import newhorizon.content.blocks.SpecialBlock;
import newhorizon.content.blocks.TurretBlock;
import newhorizon.expand.type.Device;

public class NHDevices {
    public static Device rapidLoaderLight;

    public static void load(){
        rapidLoaderLight = new Device("rapid-loader-light"){{
            installableBlocks.add(SpecialBlock.juniorModuleBeacon);
            installableBlocks.add(SpecialBlock.seniorModuleBeacon);

            compatibleBlocks.add(TurretBlock.synchro, TurretBlock.argmot, TurretBlock.slavio);
        }};
    }
}
