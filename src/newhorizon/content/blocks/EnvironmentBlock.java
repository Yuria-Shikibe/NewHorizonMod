package newhorizon.content.blocks;

import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHContent;
import newhorizon.expand.block.env.*;

public class EnvironmentBlock {
    public static Floor metalFloorGroove;
    public static TiledFloor metalFloorPlain, labFloorLight, labFloorDark;
    public static AtlasFloor granite, diorite;
    public static DataFloor
        lineMarkingFloor, lineMarkingFloorQuantum, lineMarkingFloorQuantumDark, lineMarkingFloorAncient, lineMarkingFloorAncientDark;
    public static Block dataFloorPlacer;
    public static void load(){
        metalFloorGroove = new GrooveFloor("metal-floor-groove", 16);

        metalFloorPlain = new TiledFloor("plating-metal-floor");
        labFloorLight = new TiledFloor("lab-floor-light", 8, 1);
        labFloorDark = new TiledFloor("lab-floor-dark", 8, 1);

        lineMarkingFloor = new DataFloor("line-marking-floor");
        lineMarkingFloorQuantum = new DataFloor("line-marking-floor-quantum");
        lineMarkingFloorQuantumDark = new DataFloor("line-marking-floor-quantum-dark");
        lineMarkingFloorAncient = new DataFloor("line-marking-floor-ancient");
        lineMarkingFloorAncientDark = new DataFloor("line-marking-floor-ancient-dark");

        //steepCliff = new DataFloor("steep-cliff");
        dataFloorPlacer = new DataFloorPlacer("data-floor-placer");
    }
}
