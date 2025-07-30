package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.ForceProjector;
import mindustry.world.consumers.ConsumeCoolant;
import newhorizon.content.NHItems;
import newhorizon.expand.block.defence.AdaptWall;
import newhorizon.expand.block.defence.ShieldGenerator;

import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block
            presstaniumWall, refactoringMultiWall, setonPhasedWall, shapedWall,
            standardForceProjector, largeShieldGenerator, riftShield;

    public static void load() {
        presstaniumWall = new AdaptWall("presstanium-wall") {{
            health = 1200;
            armor = 2f;
            maxShareStep = 1;
            damageReduction = 0.1f;

            requirements(Category.defense, with(NHItems.presstanium, 10, NHItems.juniorProcessor, 6));
        }};

        refactoringMultiWall = new AdaptWall("refactoring-multi-wall") {{
            health = 1800;
            armor = 4f;
            maxShareStep = 2;
            damageReduction = 0.2f;

            requirements(Category.defense, with(NHItems.metalOxhydrigen, 8, Items.carbide, 12));
        }};

        setonPhasedWall = new AdaptWall("seton-phased-wall") {{
            health = 2400;
            armor = 8f;
            maxShareStep = 2;
            damageReduction = 0.3f;

            requirements(Category.defense, with(NHItems.setonAlloy, 6, NHItems.irayrondPanel, 8));
        }};

        shapedWall = new AdaptWall("shaped-wall") {{
            health = 3000;
            armor = 10f;
            damageReduction = 0.5f;

            crushDamageMultiplier = 0.5f;

            requirements(Category.defense, with(NHItems.nodexPlate, 8, NHItems.ancimembrane, 8));
        }};

        standardForceProjector = new ForceProjector("standard-shield-generator") {{
            requirements(Category.effect, with(NHItems.juniorProcessor, 100, NHItems.presstanium, 100, Items.carbide, 50));

            size = 2;
            sides = 4;
            health = 1500;
            armor = 5;
            itemCapacity = 20;
            shieldRotation = 45f;
            radius = 140f;
            shieldHealth = 2500f;
            cooldownNormal = 10f;
            cooldownBrokenBase = shieldHealth / (5f * 60);
            phaseRadiusBoost = 80f;
            phaseShieldBoost = 2500f;
            itemConsumer = consumeItem(NHItems.zeta).boost();
            consumePower(5f);
        }};

        largeShieldGenerator = new ForceProjector("large-shield-generator") {{
            requirements(Category.effect, with(NHItems.seniorProcessor, 150, NHItems.presstanium, 150, Items.phaseFabric, 100, NHItems.multipleSteel, 50));

            size = 4;
            sides = 4;
            health = 2500;
            armor = 10;
            itemCapacity = 20;
            shieldRotation = 45f;
            radius = 240f;
            shieldHealth = 20000f;
            cooldownNormal = 25f;
            cooldownBrokenBase = shieldHealth / (20f * 60);
            phaseRadiusBoost = 100f;
            phaseShieldBoost = 20000f;
            itemConsumer = consumeItem(NHItems.fusionEnergy).boost();
            consumePower(20f);
        }};

        riftShield = new ShieldGenerator("rift-shield") {{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.nodexPlate, 300));
        }};

    }
}
