package newhorizon.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import newhorizon.content.NHItems;
import newhorizon.expand.block.defence.ShieldGenerator;
import newhorizon.expand.block.defence.TileWall;

import static mindustry.type.ItemStack.with;

public class DefenseBlock {
    public static Block riftShield, ancientRuinWallLarge, ancientRuinWall;
    public static void load(){
        riftShield = new ShieldGenerator("rift-shield"){{
            requirements(Category.effect, with(NHItems.setonAlloy, 300, NHItems.ancimembrane, 350, NHItems.seniorProcessor, 400, NHItems.upgradeSort, 300));
        }};

        ancientRuinWallLarge = new Wall("ancient-ruin-wall-large"){{
            requirements(Category.defense, with(NHItems.setonAlloy, 32));
            size = 2;
            //largeWall = this;
        }};

        ancientRuinWall = new Wall("ancient-ruin-wall"){{
            requirements(Category.defense, with(NHItems.setonAlloy, 8));
            size = 1;
            //smallWall = this;
        }};

        {
            //((TileWall)ancientRuinWallLarge).smallWall = (TileWall) ancientRuinWall;
            //((TileWall)ancientRuinWall).largeWall = (TileWall) ancientRuinWallLarge;
        }
    }
}
