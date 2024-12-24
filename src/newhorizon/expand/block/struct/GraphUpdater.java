package newhorizon.expand.block.struct;

import arc.struct.IntMap;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.expand.block.flood.FloodGraph;

public class GraphUpdater {
    public static IntMap<FloodGraph> allGraph = new IntMap<>();
    public static IntMap<XenGraph> xenGraphAll = new IntMap<>();
    public static IntMap<GraphEntity<AdaptBuilding>> graphEntities = new IntMap<>();
}
