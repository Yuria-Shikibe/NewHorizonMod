package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import newhorizon.content.NHItems;
import newhorizon.expand.block.defence.ShieldGenerator;

import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block riftShield;
    public static void load(){
        riftShield = new ShieldGenerator("rift-shield"){{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.upgradeSort, 300));
        }};
    }
}
