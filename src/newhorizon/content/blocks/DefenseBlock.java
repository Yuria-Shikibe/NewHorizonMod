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
    public static AdaptWall presstaniumWall, refactoringMultiWall, setonPhasedWall, shapedWall;
    public static void load(){
        riftShield = new ShieldGenerator("rift-shield"){{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.upgradeSort, 300));
        }};

        presstaniumWall = new AdaptWall("presstanium-wall"){{
            health = 1200;
            armor = 2f;
            maxShareStep = 1;
            damageReduction = 0.1f;

            requirements(Category.defense, with(NHItems.presstanium, 4, Items.copper, 10));
        }};

        refactoringMultiWall = new AdaptWall("refactoring-multi-wall"){{
            health = 1800;
            armor = 4f;
            maxShareStep = 2;
            damageReduction = 0.2f;

            requirements(Category.defense, with(NHItems.multipleSteel, 4, Items.thorium, 12));
        }};

        setonPhasedWall = new AdaptWall("seton-phased-wall"){{
            health = 2400;
            armor = 8f;
            maxShareStep = 2;
            damageReduction = 0.3f;

            requirements(Category.defense, with(NHItems.setonAlloy, 6, Items.phaseFabric, 8, NHItems.zeta, 6));
        }};

        shapedWall = new AdaptWall("shaped-wall"){{
            health = 3000;
            armor = 10f;
            damageReduction = 0.5f;

            crushDamageMultiplier = 0.5f;

            requirements(Category.defense, with(NHItems.upgradeSort, 4, NHItems.juniorProcessor, 2, NHItems.ancimembrane, 10));
        }};
    }
}
