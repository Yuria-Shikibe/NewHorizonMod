package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.expand.block.special.JumpGate;

import static mindustry.type.ItemStack.with;

public class UnitBlock {
    public static Block jumpGateBasic,jumpGatePrimary, jumpGateStandard, jumpGateHyper;

    public static void load() {
        jumpGateBasic = new JumpGate("basic-jump-gate") {{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.silicon, 40,
                    NHItems.graphite, 40,
                    NHItems.titanium, 40
            ));

            health = 900;
            armor = 5f;
            size = 2;

            warmupPerSpawn = 0.2f;
            maxWarmupSpeed = 8f;

            maxRadius = 120f;

            consumePowerCond(4, JumpGateBuild::canConsume);
        }};

        jumpGatePrimary = new JumpGate("primary-jump-gate") {{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 80,
                    NHItems.juniorProcessor, 80,
                    Items.tungsten, 80
            ));

            health = 1800;
            armor = 10f;
            size = 3;

            warmupPerSpawn = 0.2f;
            maxWarmupSpeed = 8f;

            maxRadius = 180f;

            consumePowerCond(8, JumpGateBuild::canConsume);
        }};

        jumpGateStandard = new JumpGate("standard-jump-gate") {{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 800,
                    NHItems.metalOxhydrigen, 300,
                    NHItems.juniorProcessor, 600,
                    NHItems.zeta, 1000
            ));

            warmupPerSpawn = 0.4f;
            maxWarmupSpeed = 4f;

            health = 10000;
            armor = 20f;
            size = 5;

            maxRadius = 240f;

            consumePowerCond(30, JumpGateBuild::canConsume);
        }};

        jumpGateHyper = new JumpGate("hyper-jump-gate") {{
            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 1800,
                    NHItems.metalOxhydrigen, 800,
                    NHItems.seniorProcessor, 800,
                    NHItems.multipleSteel, 1000,
                    NHItems.irayrondPanel, 400
            ));

            warmupPerSpawn = 0.5f;
            maxWarmupSpeed = 3f;

            health = 80000;
            armor = 20f;
            size = 8;

            maxRadius = 320f;

            consumePowerCond(60, JumpGateBuild::canConsume);
        }};
    }
}
