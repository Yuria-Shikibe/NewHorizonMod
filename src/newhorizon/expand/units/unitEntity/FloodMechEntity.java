package newhorizon.expand.units.unitEntity;

import mindustry.gen.MechUnit;
import newhorizon.expand.block.flood.FloodGraph;

public class FloodMechEntity extends MechUnit {
    public static final int turret = 0, transform = 1, unit = 2;
    public FloodGraph graph;
    public int state;
}
