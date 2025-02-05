package newhorizon.content.blocks;

import newhorizon.expand.block.production.drill.*;

public class ProductionBlock {
    public static AdaptDrill resonanceMiningFacility, beamMiningFacility, implosionMiningFacility;

    public static DrillModule speedModule, refineModule, deliveryModule;

    public static void load(){
        resonanceMiningFacility = new ResonanceDrill();
        beamMiningFacility = new BeamDrill();
        implosionMiningFacility = new ImplosionDrill();

        speedModule = new SpeedModule();
        refineModule = new RefineModule();
        deliveryModule = new DeliveryModule();
    }
}
