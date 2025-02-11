package newhorizon.content.blocks;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.expand.block.env.*;

public class EnvironmentBlock {
    public static Floor metalFloorGroove, metalFloorGrooveDeep, metalFloorRidge, metalFloorRidgeHigh;
    public static TiledFloor metalFloorPlain, labFloorLight, labFloorDark;
    public static DataFloor
        lineMarkingFloor, lineMarkingFloorQuantum, lineMarkingFloorQuantumDark, lineMarkingFloorAncient, lineMarkingFloorAncientDark;
    public static Block dataFloorPlacer;
    public static void load(){
        metalFloorGroove = new Atlas_4_12_Floor("metal-floor-groove");
        metalFloorGrooveDeep = new Atlas_4_12_Floor("metal-floor-deep-groove");
        metalFloorRidge = new Atlas_4_12_Floor("metal-floor-ridge");
        metalFloorRidgeHigh = new Atlas_4_12_Floor("metal-floor-high-ridge");

        metalFloorPlain = new TiledFloor("plating-metal-floor");
        labFloorLight = new TiledFloor("lab-floor-light", 8, 1);
        labFloorDark = new TiledFloor("lab-floor-dark", 8, 1);

        lineMarkingFloor = new DataFloor("line-marking-floor");
        lineMarkingFloorQuantum = new DataFloor("line-marking-floor-quantum");
        lineMarkingFloorQuantumDark = new DataFloor("line-marking-floor-quantum-dark");
        lineMarkingFloorAncient = new DataFloor("line-marking-floor-ancient");
        lineMarkingFloorAncientDark = new DataFloor("line-marking-floor-ancient-dark");

        dataFloorPlacer = new DataFloorPlacer("data-floor-placer");

        ((Atlas_4_12_Floor) metalFloorGroove).baseFloor = metalFloorPlain;
        ((Atlas_4_12_Floor) metalFloorGrooveDeep).baseFloor = metalFloorPlain;
        ((Atlas_4_12_Floor) metalFloorRidge).baseFloor = metalFloorPlain;
        ((Atlas_4_12_Floor) metalFloorRidgeHigh).baseFloor = metalFloorPlain;
    }
}
