package newhorizon.content.blocks;

import mindustry.world.blocks.environment.Floor;
import newhorizon.expand.block.env.GrooveFloor;
import newhorizon.expand.block.env.TiledFloor;

public class EnvironmentBlock {
    public static Floor
        metalFloorGroove, metalFloorPlain, metalFloorPlainQuantum;
    public static void load(){
        metalFloorGroove = new GrooveFloor("metal-floor-groove", 16);
        metalFloorPlain = new TiledFloor("metal-floor");
        metalFloorPlainQuantum = new TiledFloor("metal-floor-quantum"){{
            //blendGroup = metalFloorPlain;
        }};
    }
}
