package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.NHItems;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.block.special.JumpGate;

import static mindustry.type.ItemStack.with;

public class UnitBlock {
    public static Block jumpGate;
    public static Block jumpGateJunior;
    public static Block jumpGatePrimary;

    public static void load(){
        jumpGatePrimary = new JumpGate("jump-gate-primary"){{
            size = 3;
            atlasSizeScl = 0.55f;
            squareStroke = 1.75f;
            health = 1800;
            spawnDelay = 90f;
            spawnReloadTime = 750f;
            range = 160f;

            armor = 5f;
            buildSpeedMultiplierCoefficient = 0.5f;

            itemCapacity = 500;

            consumePowerCond(8, JumpGateBuild::isCalling);

            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 80,
                    NHItems.juniorProcessor, 80,
                    Items.tungsten, 80
            ));

            addSets(
                new UnitSet(UnitTypes.poly, new byte[]{0, 114}, 40 * 60f, with(Items.titanium, 25, Items.silicon, 25, Items.tungsten, 25)),
                new UnitSet(UnitTypes.quasar, new byte[]{1, 114}, 90 * 60f, with(NHItems.presstanium, 50, NHItems.juniorProcessor, 100, Items.tungsten, 75)),
                new UnitSet(UnitTypes.zenith, new byte[]{2, 114}, 120 * 60f, with(NHItems.presstanium, 50, NHItems.juniorProcessor, 100, Items.tungsten, 75)),
                new UnitSet(UnitTypes.cyerce, new byte[]{3, 114}, 120 * 60f, with(NHItems.presstanium, 75, NHItems.juniorProcessor, 80, Items.tungsten, 80)),
                new UnitSet(NHUnitTypes.ghost, new byte[]{4, 114}, 90 * 60f, with(NHItems.presstanium, 30, NHItems.juniorProcessor, 125, Items.carbide, 40)),
                new UnitSet(NHUnitTypes.warper, new byte[]{5, 114}, 120 * 60f, with(NHItems.presstanium, 120, NHItems.juniorProcessor, 80, Items.carbide, 60)),
                new UnitSet(NHUnitTypes.aliotiat, new byte[]{6, 114}, 90 * 60f, with(NHItems.presstanium, 90, NHItems.juniorProcessor, 100, Items.carbide, 100)),
                new UnitSet(NHUnitTypes.rhino, new byte[]{7, 114}, 180 * 60f, with(NHItems.presstanium, 90, NHItems.juniorProcessor, 60, Items.carbide, 120, NHItems.zeta, 25))
            );
        }};

        jumpGateJunior = new JumpGate("jump-gate-junior"){{
            size = 5;
            atlasSizeScl = 0.75f;
            squareStroke = 2f;
            health = 6000;
            spawnDelay = 60f;
            spawnReloadTime = 600f;
            range = 300f;

            buildSpeedMultiplierCoefficient = 0.75f;

            adaptable = false;
            adaptBase = UnitBlock.jumpGatePrimary;

            consumePowerCond(30, JumpGateBuild::isCalling);

            requirements(Category.units, BuildVisibility.shown, with(
                    NHItems.presstanium, 800,
                    NHItems.metalOxhydrigen, 300,
                    NHItems.juniorProcessor, 600,
                    NHItems.zeta, 1000
            ));

            armor = 10f;

            itemCapacity = 1200;

            addSets(
                new UnitSet(NHUnitTypes.naxos, new byte[]{0, 114}, 180 * 60f, with(Items.surgeAlloy, 200, Items.phaseFabric, 150, NHItems.multipleSteel, 200, NHItems.fusionEnergy, 120)),
                new UnitSet(UnitTypes.disrupt, new byte[]{1, 114}, 180 * 60f, with(Items.surgeAlloy, 200, Items.phaseFabric, 150, NHItems.metalOxhydrigen, 150, Items.carbide, 150)),
                new UnitSet(NHUnitTypes.tarlidor, new byte[]{2, 114}, 200 * 60f, with(NHItems.presstanium, 400, NHItems.juniorProcessor, 200, Items.carbide, 200, NHItems.zeta, 200)),
                new UnitSet(NHUnitTypes.zarkov, new byte[]{3, 114}, 160 * 60f, with(NHItems.presstanium, 300, NHItems.juniorProcessor, 300, Items.phaseFabric, 200, NHItems.zeta, 300)),
                new UnitSet(UnitTypes.eclipse, new byte[]{4, 114}, 240 * 60f, with(Items.surgeAlloy, 100, Items.phaseFabric, 150, NHItems.fusionEnergy, 200, NHItems.multipleSteel, 200)),
                new UnitSet(UnitTypes.corvus, new byte[]{5, 114}, 200 * 60f, with(NHItems.irayrondPanel, 200, NHItems.seniorProcessor, 125, Items.carbide, 300, NHItems.multipleSteel, 300)),
                new UnitSet(UnitTypes.navanax, new byte[]{6, 114}, 180 * 60f, with(Items.surgeAlloy, 300, NHItems.seniorProcessor, 180, NHItems.metalOxhydrigen, 300, NHItems.presstanium, 300)),
                new UnitSet(UnitTypes.collaris, new byte[]{7, 114}, 240 * 60f, with(NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 250, NHItems.metalOxhydrigen, 300, NHItems.fusionEnergy, 300)),
                new UnitSet(NHUnitTypes.annihilation, new byte[]{8, 114}, 300 * 60f, with(NHItems.setonAlloy, 300, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 200, NHItems.fusionEnergy, 200))
            );
        }};

        jumpGate = new JumpGate("jump-gate"){{
            consumePowerCond(60, JumpGateBuild::isCalling);
            health = 80000;
            spawnDelay = 30f;
            spawnReloadTime = 300f;
            range = 600f;
            squareStroke = 2.35f;
            size = 8;
            adaptable = false;
            adaptBase = UnitBlock.jumpGateJunior;

            armor = 20f;

            buildSpeedMultiplierCoefficient = 1f;

            itemCapacity = 6000;

            requirements(Category.units, BuildVisibility.shown, with(
                NHItems.presstanium, 1800,
                NHItems.metalOxhydrigen, 800,
                NHItems.seniorProcessor, 800,
                NHItems.multipleSteel, 1000,
                NHItems.irayrondPanel, 400
            ));

            addSets(
                new UnitSet(NHUnitTypes.destruction, new byte[]{0, 114}, 300 * 60f, with(NHItems.irayrondPanel, 600, Items.carbide, 800, NHItems.seniorProcessor, 500, NHItems.thermoCoreNegative, 200)),
                new UnitSet(NHUnitTypes.longinus, new byte[]{1, 114}, 300 * 60f, with(NHItems.irayrondPanel, 500, Items.phaseFabric, 600, NHItems.metalOxhydrigen, 400, NHItems.thermoCorePositive, 100)),
                new UnitSet(NHUnitTypes.saviour, new byte[]{2, 114}, 300 * 60f, with(NHItems.irayrondPanel, 600, NHItems.setonAlloy, 400, NHItems.multipleSteel, 400, NHItems.thermoCoreNegative, 300)),
                new UnitSet(NHUnitTypes.declining, new byte[]{3, 114}, 360 * 60f, with(NHItems.irayrondPanel, 400, NHItems.setonAlloy, 800, NHItems.seniorProcessor, 500, NHItems.thermoCorePositive, 200)),
                new UnitSet(NHUnitTypes.hurricane, new byte[]{4, 114}, 600 * 60f, with(NHItems.irayrondPanel, 1000, NHItems.upgradeSort, 400, NHItems.seniorProcessor, 600, NHItems.thermoCoreNegative, 800)),
                new UnitSet(NHUnitTypes.anvil, new byte[]{5, 114}, 600 * 60f, with(NHItems.setonAlloy, 1000, NHItems.ancimembrane, 300, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 500)),
                new UnitSet(NHUnitTypes.sin, new byte[]{6, 114}, 900 * 60f, with(NHItems.irayrondPanel, 1800, NHItems.setonAlloy, 1800, NHItems.seniorProcessor, 1200, NHItems.upgradeSort, 800, NHItems.ancimembrane, 800, NHItems.darkEnergy, 500)),
                new UnitSet(NHUnitTypes.collapser, new byte[]{7, 114}, 1200 * 60f, with(NHItems.irayrondPanel, 6000, NHItems.setonAlloy, 6000, NHItems.seniorProcessor, 3000, NHItems.upgradeSort, 2000, NHItems.ancimembrane, 2000, NHItems.darkEnergy, 2000))
            );
        }};
    }
}
