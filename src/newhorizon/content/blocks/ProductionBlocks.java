package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.drill.*;

import static mindustry.type.ItemStack.with;

public class ProductionBlocks {
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
