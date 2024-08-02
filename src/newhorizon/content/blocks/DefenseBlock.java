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
            requirements(Category.effect, with(NHItems.setonAlloy, 200, NHItems.ancimembrane, 240, NHItems.seniorProcessor, 300, NHItems.upgradeSort, 175));
        }};
    }
}
