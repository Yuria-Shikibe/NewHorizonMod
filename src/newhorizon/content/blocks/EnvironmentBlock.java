package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;
import newhorizon.expand.block.env.*;

public class EnvironmentBlock {
    public static Floor metalFloorGroove;
    public static TiledFloor metalFloorPlain;
    public static AtlasFloor granite, diorite;
    public static DataFloor steepCliff, lineMarkingFloor, lineMarkingFloorQuantum, lineMarkingFloorQuantumDark;
    public static Block dataFloorPlacer;
    public static void load(){
        metalFloorGroove = new GrooveFloor("metal-floor-groove", 16);
        metalFloorPlain = new TiledFloor("plating-metal-floor");

        lineMarkingFloor = new DataFloor("line-marking-floor");
        lineMarkingFloorQuantum = new DataFloor("line-marking-floor-quantum");
        lineMarkingFloorQuantumDark = new DataFloor("line-marking-floor-quantum-dark");
        
        granite = new AtlasFloor("granite");
        //diorite = new AtlasFloor("diorite");

        steepCliff = new DataFloor("steep-cliff");
        dataFloorPlacer = new DataFloorPlacer("data-floor-placer");
    }
}
