package newhorizon.expand.block.graph;

import arc.func.Boolf;
import arc.func.Func;
import arc.struct.Queue;
import arc.struct.Seq;
import newhorizon.expand.block.AdaptBuilding;

import static newhorizon.expand.block.graph.GraphUpdater.GraphEntities;

/**A graph used for builds.
 *
 * @see mindustry.world.blocks.power.PowerGraph
 * */
public class GraphEntity<T extends AdaptBuilding>{

    private final Queue<T> queue = new Queue<>();

    public final Seq<T> allBuildings = new Seq<>(false, 16);

    public static int lastID = 0;
    public final int graphID;

    protected transient boolean added;


    public GraphEntity() {
        graphID = lastID++;
        createGraph();
    }

    public void mergeGraph(GraphEntity<T> graph) {
        if (graph == this) return;

        //merge into other graph instead.
        if (allBuildings.size > graph.allBuildings.size) {
            graph.allBuildings.each(this::addBuild);
            graph.removeGraph();
        } else {
            allBuildings.each(graph::addBuild);
            removeGraph();
        }
    }

    @SuppressWarnings("unchecked")
    public void addBuild(T building) {
        if (!allBuildings.contains(building)) {
            //add this block to it
            allBuildings.add(building);
            building.graph = (GraphEntity<AdaptBuilding>) this;
        }
    }

    public void clear() {
        allBuildings.clear();
    }

    public void remove(T building, Func<T, Seq<T>> targetBuilds, Boolf<T> isTarget) {

        //go through all the connections of this tile
        for (T other : targetBuilds.get(building)) {

            //check if it contains the graph or is the target graph that can be merged
            if (!isTarget.get(other)) continue;
            if (other.graph != this) continue;

            //create graph for this branch
            GraphEntity<T> graph = new GraphEntity<>();
            graph.createGraph();
            graph.addBuild(other);

            //BFS time
            queue.clear();
            queue.addLast(other);
            while (queue.size > 0) {
                //get child from queue
                T child = queue.removeFirst();
                //add it to the new branch graph
                graph.addBuild(child);
                //go through connections
                for (T next : targetBuilds.get(child)) {
                    //make sure it hasn't looped back, and that the new graph being assigned hasn't already been assigned
                    //also skip closed tiles
                    if (next != building && next.graph != graph) {
                        graph.addBuild(next);
                        queue.addLast(next);
                    }
                }
            }

        }

        //implied empty graph here
        removeGraph();
    }

    @SuppressWarnings("unchecked")
    public void createGraph() {
        if (!added) {
            GraphEntities.put(graphID, (GraphEntity<AdaptBuilding>) this);
            added = true;
        }
    }

    public void removeGraph() {
        if (added) {
            clear();
            GraphEntities.remove(graphID);
            added = false;
        }
    }
}
