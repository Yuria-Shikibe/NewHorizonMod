package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.content.NHItems;
import newhorizon.expand.block.production.drill.AdaptDrill;
import newhorizon.expand.block.production.drill.DrillModule;
import newhorizon.expand.block.production.drill.BeamDrill;
import newhorizon.expand.block.production.drill.ResonanceDrill;

import static mindustry.type.ItemStack.with;

public class ProductionBlocks {
    public static AdaptDrill resonanceMiningFacility, beamMiningFacility, implosionMiningFacility;

    public static DrillModule speedModule, refineModule, deliveryModule;

    public static void load(){
        resonanceMiningFacility = new ResonanceDrill();

        beamMiningFacility = new BeamDrill();

        implosionMiningFacility = new AdaptDrill("implosion-mining-facility"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten, NHItems.zeta});
            size = 4;

            mineSpeed = 10f;
            mineCount = 5;

            consumePower(900/60f);

        }};

        speedModule = new DrillModule("speed-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            boostSpeed = 1f;
            powerMul = 1.2f;
            powerExtra = 180f;
        }};

        refineModule = new DrillModule("refine-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            boostFinalMul = -0.25f;
            powerMul = 1.8f;
            powerExtra = 300f;
            convertList.add(new Item[]{Items.sand, Items.silicon}, new Item[]{Items.coal, Items.graphite}, new Item[]{Items.beryllium, Items.oxide});
        }};

        deliveryModule = new DrillModule("delivery-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            powerMul = 2f;
            powerExtra = 600f;
            coreSend = true;
        }};
    }
}
