package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;
import newhorizon.expand.block.env.DataFloor;
import newhorizon.expand.block.env.DataFloorPlacer;
import newhorizon.expand.block.env.GrooveFloor;
import newhorizon.expand.block.env.TiledFloor;

public class EnvironmentBlock {
    public static Floor metalFloorGroove;
    public static TiledFloor metalFloorPlain;
    public static DataFloor steepCliff, lineMarkingFloor, lineMarkingFloorAncient, lineMarkingFloorQuantum;
    public static Block dataFloorPlacer;
    public static void load(){
        metalFloorGroove = new GrooveFloor("metal-floor-groove", 16);
        metalFloorPlain = new TiledFloor("plating-metal-floor");
        lineMarkingFloor = new DataFloor("line-marking-floor");
        //lineMarkingFloorAncient = new DataFloor("line-marking-floor-ancient");
        //lineMarkingFloorQuantum = new DataFloor("line-marking-floor-quantum");

        steepCliff = new DataFloor("steep-cliff");
        dataFloorPlacer = new DataFloorPlacer("data-floor-placer");
    }
}
