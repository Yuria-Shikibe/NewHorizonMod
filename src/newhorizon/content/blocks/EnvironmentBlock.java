package newhorizon.content.blocks;

import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;
import newhorizon.expand.block.env.GrooveFloor;
import newhorizon.expand.block.env.TiledFloor;

public class EnvironmentBlock {
    public static Floor
        metalFloorGroove,  metalFloorPlainQuantum;
    public static TiledFloor metalFloorPlain;
    public static void load(){
        metalFloorGroove = new GrooveFloor("metal-floor-groove", 16);
        metalFloorPlain = new TiledFloor("plating-metal-floor"){{
            //cacheLayer = NHContent.platingLayer;
        }};
    }
}
