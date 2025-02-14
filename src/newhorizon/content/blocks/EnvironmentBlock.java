package newhorizon.content.blocks;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHColor;
import newhorizon.expand.block.env.*;

public class EnvironmentBlock {
    public static Atlas_4_12_Floor metalFloorGroove, metalFloorGrooveDeep, metalFloorRidge, metalFloorRidgeHigh;
    public static Atlas_4_4_Floor armorAncient, armorAncientSub, armorQuantum;
    public static TiledFloor metalFloorPlain, labFloorLight, labFloorDark;
    public static DataFloor
        lineMarkingFloor, lineMarkingFloorQuantum, lineMarkingFloorQuantumDark, lineMarkingFloorAncient, lineMarkingFloorAncientDark;
    public static Block dataFloorPlacer;
    public static void load(){
        metalFloorGroove = new Atlas_4_12_Floor("metal-floor-groove");
        metalFloorGrooveDeep = new Atlas_4_12_Floor("metal-floor-deep-groove");
        metalFloorRidge = new Atlas_4_12_Floor("metal-floor-ridge");
        metalFloorRidgeHigh = new Atlas_4_12_Floor("metal-floor-high-ridge");

        armorAncient = new Atlas_4_4_Floor("armor-ancient"){{
            lightColor = NHColor.ancient.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};
        armorAncientSub = new Atlas_4_4_Floor("armor-ancient-sub"){{
            lightColor = NHColor.ancient.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};
        armorQuantum = new Atlas_4_4_Floor("armor-quantum"){{
            lightColor = NHColor.darkEnrColor.cpy().a(0.7f);
            lightRadius = 15f;
            emitLight = true;
        }};

        metalFloorPlain = new TiledFloor("plating-metal-floor");
        labFloorLight = new TiledFloor("lab-floor-light", 8, 1);
        labFloorDark = new TiledFloor("lab-floor-dark", 8, 1);

        lineMarkingFloor = new DataFloor("line-marking-floor");
        lineMarkingFloorQuantum = new DataFloor("line-marking-floor-quantum");
        lineMarkingFloorQuantumDark = new DataFloor("line-marking-floor-quantum-dark");
        lineMarkingFloorAncient = new DataFloor("line-marking-floor-ancient");
        lineMarkingFloorAncientDark = new DataFloor("line-marking-floor-ancient-dark");

        dataFloorPlacer = new DataFloorPlacer("data-floor-placer");

        metalFloorGroove.baseFloor = metalFloorPlain;
        metalFloorGrooveDeep.baseFloor = metalFloorPlain;
        metalFloorRidge.baseFloor = metalFloorPlain;
        metalFloorRidgeHigh.baseFloor = metalFloorPlain;

        armorAncient.blendFloors.add(armorAncientSub);
        armorAncientSub.blendFloors.add(armorAncient);
    }
}
