package newhorizon.content.blocks;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.unit.JumpGate;

import static mindustry.type.ItemStack.with;

public class UnitBlock {
    public static Block jumpGateMK1, jumpGateMK2, jumpGateMk3;

    public static void load(){
        jumpGateMK1 = new JumpGate("jump-gate-mk1"){{
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

            spawnList = Seq.with(UnitTypes.poly, UnitTypes.cleroi, UnitTypes.quasar, UnitTypes.zenith, UnitTypes.cyerce, NHUnitTypes.ghost, NHUnitTypes.warper, NHUnitTypes.aliotiat, NHUnitTypes.rhino, NHUnitTypes.gather);

            consumePowerCond(8, JumpGateBuild::canConsume);
        }};

        jumpGateMK2 = new JumpGate("jump-gate-mk2"){{
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

            spawnList = Seq.with(NHUnitTypes.naxos, NHUnitTypes.tarlidor, NHUnitTypes.zarkov, UnitTypes.eclipse, UnitTypes.disrupt, UnitTypes.corvus, UnitTypes.navanax, UnitTypes.collaris);

            consumePowerCond(30, JumpGateBuild::canConsume);
        }};

        jumpGateMK2 = new JumpGate("jump-gate-mk3"){{
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

            spawnList = Seq.with(NHUnitTypes.destruction, NHUnitTypes.longinus, NHUnitTypes.annihilation, NHUnitTypes.saviour, NHUnitTypes.declining, NHUnitTypes.anvil, NHUnitTypes.sin, NHUnitTypes.collapser);

            consumePowerCond(60, JumpGateBuild::canConsume);
        }};
    }
}
