package newhorizon.expand.block;

import arc.graphics.Color;
import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.type.Item;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import newhorizon.expand.block.struct.XenGraph;

import static newhorizon.expand.block.struct.GraphUpdater.xenGraphAll;

public class GraphBuildings {
    private static final Queue<GraphEntity> queue = new Queue<>();
    public final Seq<GraphEntity> buildings = new Seq<>(false);

    public ItemModule items = new ItemModule();
    public LiquidModule liquids = new LiquidModule();

    public GraphBuildings() {}

    public void merge(GraphBuildings graph) {
        if (graph == this) return;

        if (buildings.size > graph.buildings.size) {
            for (GraphEntity tile : graph.buildings) {
                buildings.add(tile);
            }
        } else {
            for (GraphEntity tile : buildings) {
                graph.buildings.add(tile);
            }
        }
    }

    public void remove(GraphEntity building) {
        for (GraphEntity other : building.proximityEntities()) {
            if (other != null){
                GraphBuildings graph = new GraphBuildings();
                graph.buildings.add(other);
                queue.clear();
                queue.addLast(other);
                while (queue.size > 0) {
                    GraphEntity child = queue.removeFirst();
                    graph.buildings.add(child);
                    for (GraphEntity next : child.proximityEntities()) {
                        if (next != building && next.graph() != graph) {
                            graph.buildings.add(next);
                            queue.addLast(next);
                        }
                    }
                }
            }
        }
    }
}
