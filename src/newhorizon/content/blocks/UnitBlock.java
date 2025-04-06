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
                    new UnitSet(UnitTypes.poly, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
                            with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                    ),
                    new UnitSet(NHUnitTypes.assaulter, new byte[]{NHUnitTypes.AIR_LINE_2, 1}, 15 * 60f,
                            with(Items.silicon, 16, Items.copper, 30, NHItems.zeta, 20)
                    ),
                    new UnitSet(NHUnitTypes.sharp, new byte[]{NHUnitTypes.AIR_LINE_1, 1}, 15 * 60f,
                            with(Items.titanium, 30, Items.silicon, 15)
                    ),
                    new UnitSet(NHUnitTypes.branch, new byte[]{NHUnitTypes.AIR_LINE_1, 2}, 30 * 60f,
                            with(Items.titanium, 60, Items.silicon, 45, Items.graphite, 30)
                    ),
                    new UnitSet(NHUnitTypes.origin, new byte[]{NHUnitTypes.GROUND_LINE_1, 1}, 20 * 60f,
                            with(Items.lead, 15, Items.silicon, 10 ,Items.copper, 10)
                    ),
                    new UnitSet(NHUnitTypes.thynomo, new byte[]{NHUnitTypes.GROUND_LINE_1, 2}, 35 * 60f,
                            with(Items.lead, 30, Items.titanium, 60, Items.graphite, 45, Items.silicon, 30)
                    ),
                    new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
                            ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
                    ),
                    new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
                            with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
                    ),
                    new UnitSet(NHUnitTypes.aliotiat, new byte[]{NHUnitTypes.GROUND_LINE_1, 3}, 55 * 60f,
                            with(Items.copper, 120, NHItems.multipleSteel, 50, NHItems.presstanium, 60, NHItems.juniorProcessor, 45)
                    ),
                    new UnitSet(NHUnitTypes.rhino, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
                            with(Items.lead, 80, Items.graphite, 60, NHItems.presstanium, 60, NHItems.metalOxhydrigen, 60, NHItems.juniorProcessor, 60)
                    ),
                    new UnitSet(UnitTypes.mega, new byte[]{NHUnitTypes.OTHERS, 2}, 45 * 60f,
                            with(Items.copper, 80, Items.metaglass, 30, NHItems.presstanium, 40, Items.graphite, 40, NHItems.juniorProcessor, 35)
                    ),
                    new UnitSet(NHUnitTypes.gather, new byte[]{NHUnitTypes.OTHERS, 3}, 60f * 60f,
                            with(Items.thorium, 80, Items.metaglass, 30, NHItems.presstanium, 80, NHItems.zeta, 120, NHItems.juniorProcessor, 80)
                    )
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

            adaptBase = UnitBlock.jumpGatePrimary;
            adaptable = true;
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
                    new UnitSet(NHUnitTypes.naxos, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 120 * 60f,
                            with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, Items.surgeAlloy, 50, NHItems.metalOxhydrigen, 120)
                    ),
                    new UnitSet(NHUnitTypes.restrictionEnzyme, new byte[]{NHUnitTypes.ANCIENT_GROUND, 3}, 75f * 60f,
                            with(Items.tungsten, 200, Items.plastanium, 100, NHItems.presstanium, 100, NHItems.zeta, 60, NHItems.juniorProcessor, 80)
                    ),
                    new UnitSet(NHUnitTypes.aliotiat, new byte[]{NHUnitTypes.GROUND_LINE_1, 3}, 55 * 60f,
                            with(Items.copper, 120, NHItems.multipleSteel, 50, NHItems.presstanium, 60, NHItems.juniorProcessor, 45)
                    ),
                    new UnitSet(NHUnitTypes.tarlidor, new byte[]{NHUnitTypes.GROUND_LINE_1, 4}, 130 * 60f,
                            ItemStack.with(Items.plastanium, 300, NHItems.juniorProcessor, 250, NHItems.presstanium, 500, NHItems.zeta, 250)
                    ),
                    new UnitSet(NHUnitTypes.ghost, new byte[]{NHUnitTypes.NAVY_LINE_1, 3}, 60 * 60f,
                            ItemStack.with(NHItems.presstanium, 60, NHItems.multipleSteel, 50, NHItems.juniorProcessor, 50)
                    ),
                    new UnitSet(NHUnitTypes.warper, new byte[]{NHUnitTypes.AIR_LINE_1, 3}, 65 * 60f,
                            with(Items.thorium, 90, Items.graphite, 50, NHItems.multipleSteel, 60, NHItems.juniorProcessor, 50)
                    ),
                    new UnitSet(NHUnitTypes.macrophage, new byte[]{NHUnitTypes.ANCIENT_AIR, 4}, 180 * 60f,
                            with(Items.phaseFabric, 100, NHItems.irayrondPanel, 150, NHItems.presstanium, 320, Items.tungsten, 400, NHItems.seniorProcessor, 100)
                    ),
                    new UnitSet(NHUnitTypes.zarkov, new byte[]{NHUnitTypes.NAVY_LINE_1, 4}, 140 * 60f,
                            ItemStack.with(NHItems.multipleSteel, 400, NHItems.juniorProcessor, 300, NHItems.presstanium, 400, NHItems.metalOxhydrigen, 200)
                    )/*,
				new UnitSet(NHUnitTypes.striker, new byte[]{NHUnitTypes.AIR_LINE_1, 4}, 150 * 60f,
						ItemStack.with(Items.phaseFabric, 200, NHItems.juniorProcessor, 300, NHItems.presstanium, 350, NHItems.seniorProcessor, 75)
				)*/
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
                    new UnitSet(NHUnitTypes.longinus, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 400 * 60f,
                            with(NHItems.setonAlloy, 300, Items.surgeAlloy, 150, NHItems.seniorProcessor, 400, NHItems.thermoCoreNegative, 250)
                    ),
                    new UnitSet(NHUnitTypes.saviour, new byte[]{NHUnitTypes.OTHERS, 5}, 300 * 60f,
                            with(NHItems.setonAlloy, 250, Items.surgeAlloy, 400, NHItems.seniorProcessor, 350, NHItems.thermoCoreNegative, 150, Items.plastanium, 400, NHItems.zeta, 500)
                    ),
                    new UnitSet(NHUnitTypes.declining, new byte[]{NHUnitTypes.NAVY_LINE_1, 6}, 420 * 60f,
                            with(NHItems.upgradeSort, 500, NHItems.irayrondPanel, 500, NHItems.seniorProcessor, 300, NHItems.thermoCoreNegative, 300, Items.tungsten, 1200)
                    ),
                    new UnitSet(NHUnitTypes.guardian, new byte[]{NHUnitTypes.OTHERS, 5}, 9600f,
                            new ItemStack(NHItems.darkEnergy, 1500)
                    ),
                    new UnitSet(NHUnitTypes.sin, new byte[]{NHUnitTypes.GROUND_LINE_1, 6}, 480 * 60f,
                            with(NHItems.setonAlloy, 600, NHItems.upgradeSort, 750, NHItems.seniorProcessor, 300, NHItems.thermoCorePositive, 500, NHItems.presstanium, 1500)
                    ),
                    new UnitSet(NHUnitTypes.anvil, new byte[]{NHUnitTypes.AIR_LINE_2, 6}, 540 * 60f,
                            with(NHItems.multipleSteel, 1000, NHItems.setonAlloy, 800, NHItems.upgradeSort, 600, NHItems.seniorProcessor, 600, NHItems.thermoCorePositive, 750)
                    ),
                    new UnitSet(NHUnitTypes.hurricane, new byte[]{NHUnitTypes.AIR_LINE_1, 6}, 600 * 60f,
                            with(NHItems.setonAlloy, 800, NHItems.upgradeSort, 900, NHItems.seniorProcessor, 1200, NHItems.thermoCoreNegative, 500)
                    ),
                    new UnitSet(NHUnitTypes.annihilation, new byte[]{NHUnitTypes.GROUND_LINE_1, 5}, 320 * 60f,
                            with(NHItems.setonAlloy, 250, NHItems.multipleSteel, 400, NHItems.seniorProcessor, 400, NHItems.fusionEnergy, 400)
                    ),
                    new UnitSet(NHUnitTypes.destruction, new byte[]{NHUnitTypes.AIR_LINE_1, 5}, 360 * 60f,
                            with(NHItems.setonAlloy, 350, NHItems.irayrondPanel, 300, NHItems.seniorProcessor, 300, NHItems.fusionEnergy, 250)
                    ),
                    new UnitSet(NHUnitTypes.collapser, new byte[]{NHUnitTypes.AIR_LINE_2, 7}, 900 * 60f,
                            new ItemStack(NHItems.thermoCorePositive, 2500),
                            new ItemStack(NHItems.thermoCoreNegative, 2500),
                            new ItemStack(NHItems.upgradeSort, 4500)
                    ),
                    new UnitSet(NHUnitTypes.pester, new byte[]{NHUnitTypes.ANCIENT_AIR, 7}, 1200 * 60f,
                            new ItemStack(NHItems.ancimembrane, 3500),
                            new ItemStack(NHItems.upgradeSort, itemCapacity / 2)
                    ),
                    new UnitSet(NHUnitTypes.nucleoid, new byte[]{NHUnitTypes.ANCIENT_AIR, 8}, 3600 * 60f * 2,
                            with(NHItems.ancimembrane, itemCapacity,
                                    NHItems.upgradeSort, itemCapacity,
                                    NHItems.darkEnergy, itemCapacity
                            )
                    ),
                    new UnitSet(NHUnitTypes.laugra, new byte[]{NHUnitTypes.ANCIENT_GROUND, 5}, 480 * 60f,
                            with(NHItems.setonAlloy, 300, NHItems.seniorProcessor, 300, Items.surgeAlloy, 200)
                    )
            );
        }};
    }
}
