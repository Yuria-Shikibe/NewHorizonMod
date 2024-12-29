package newhorizon.expand.block.struct;

import arc.struct.IntMap;
import arc.struct.ObjectMap;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.expand.block.flood.FloodGraph;
import newhorizon.expand.block.floodv3.SyntherGraph;

public class GraphUpdater {
    public static IntMap<FloodGraph> allGraph = new IntMap<>();
    public static ObjectMap<Integer, SyntherGraph> syntherEntity = new ObjectMap<>();
    public static IntMap<XenGraph> xenGraphAll = new IntMap<>();
    public static IntMap<GraphEntity<AdaptBuilding>> graphEntities = new IntMap<>();
}
