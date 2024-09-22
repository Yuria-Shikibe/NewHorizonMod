package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.Item;
import newhorizon.expand.block.production.AdaptDrill;
import newhorizon.expand.block.production.DrillModule;

import static mindustry.type.ItemStack.with;

public class ProductionBlocks {
    public static AdaptDrill resonanceMiningFacility, beamMiningFacility, implosionMiningFacility;

    public static DrillModule speedModule, refineModule, deliveryModule;

    public static void load(){
        resonanceMiningFacility = new AdaptDrill("resonance-mining-facility"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium});
            size = 4;

            mineSpeed = 5;
            mineCount = 2;
        }};

        beamMiningFacility = new AdaptDrill("beam-mining-facility"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten});
            size = 4;

            mineSpeed = 7.5f;
            mineCount = 3;

            consumePower(300/60f);
        }};

        implosionMiningFacility = new AdaptDrill("implosion-mining-facility"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20));
            mineOres.add(new Item[]{Items.sand, Items.scrap, Items.copper, Items.lead, Items.coal, Items.titanium, Items.beryllium, Items.thorium, Items.tungsten});
            size = 4;

            mineSpeed = 10f;
            mineCount = 5;

            consumePower(900/60f);

        }};

        speedModule = new DrillModule("speed-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            boostSpeed = 1f;
        }};

        refineModule = new DrillModule("refine-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            boostSpeed = -0.25f;
            convertList.add(new Item[]{Items.sand, Items.silicon}, new Item[]{Items.coal, Items.graphite});
        }};

        deliveryModule = new DrillModule("delivery-module"){{
            requirements(Category.production, with(Items.copper, 25, Items.lead, 20, Items.titanium, 35));
            size = 2;
            coreSend = true;
        }};
    }
}
