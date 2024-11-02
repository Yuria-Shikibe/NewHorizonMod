package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.expand.block.defence.AdaptWall;
import newhorizon.expand.block.defence.ShieldGenerator;

import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block riftShield;
    public static AdaptWall presstaniumWall, refactoringPlasticityWall, setonPhasedWall, shapedWall;
    public static void load(){
        riftShield = new ShieldGenerator("rift-shield"){{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.upgradeSort, 300));
        }};

        presstaniumWall = new AdaptWall("presstanium-wall"){{
            health = 800;
            armor = 2f;
            maxShareStep = 1;

            requirements(Category.defense, with(NHItems.presstanium, 12, Items.copper, 6));
        }};

        refactoringPlasticityWall = new AdaptWall("refactoring-plasticity-wall"){{
            health = 1250;
            armor = 4f;
            maxShareStep = 2;

            requirements(Category.defense, with(NHItems.multipleSteel, 12, Items.plastanium, 6));
        }};

        setonPhasedWall = new AdaptWall("seton-phased-wall"){{
            health = 3000;
            armor = 8f;
            maxShareStep = 2;

            requirements(Category.defense, with(NHItems.setonAlloy, 12, Items.phaseFabric, 6));
        }};

        shapedWall = new AdaptWall("shaped-wall"){{
            health = 6000;
            armor = 10f;

            crushDamageMultiplier = 0.5f;

            requirements(Category.defense, with(NHItems.upgradeSort, 5, NHItems.juniorProcessor, 2, NHItems.ancimembrane, 10));
        }};
    }
}
