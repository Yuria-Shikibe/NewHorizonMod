package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import newhorizon.expand.block.defence.ShieldGenerator;

import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block shield;
    public static void load(){
        shield = new ShieldGenerator("shield"){{
            requirements(Category.defense, with());

            size = 5;
            clipSize = 600f;
        }};
    }
}
