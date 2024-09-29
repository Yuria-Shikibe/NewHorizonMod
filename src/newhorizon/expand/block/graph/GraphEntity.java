package newhorizon.expand.block.graph;

import arc.func.Cons;
import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.gen.Building;
import newhorizon.expand.block.NHBlock;
import newhorizon.expand.block.NHBuilding;

import static newhorizon.expand.block.graph.GraphUpdater.xenGraphAll;

/** A graph used for builds. */
public class GraphEntity<T extends NHBuilding>{
    /*
    private final Queue<T> queue = new Queue<>();

    public final Seq<T> allBuildings = new Seq<>(false, 16);

    public static int lastID = 0;
    public final int graphID;

    protected transient boolean added;


    public GraphEntity() {
        graphID = lastID++;
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

    public void addBuild(T building) {
        if (!allBuildings.contains(building)) {
            //add this block to it
            allBuildings.add(building);
            building.xen.setGraph(this);
        }
    }

    public void register() {
        this.createGraph();
    }

    public void clear() {
        allBuildings.clear();
    }

    public void remove(T building) {

        //go through all the connections of this tile
        for (Building other : building.proximity) {
            NHBuilding b1 = checkXen(other);
            if (b1 != null){
                //check if it contains the graph
                if (b1.xen.graph != this) continue;

                //create graph for this branch
                XenGraph graph = new XenGraph(0, temp);
                graph.register();
                graph.addBuild(b1);

                //BFS time
                queue.clear();
                queue.addLast(b1);
                while (queue.size > 0) {
                    //get child from queue
                    NHBuilding child = queue.removeFirst();
                    //add it to the new branch graph
                    graph.addBuild(child);
                    //go through connections
                    for (Building next : child.proximity) {
                        NHBuilding b2 = checkXen(next);
                        if (b2 != null){
                            //make sure it hasn't looped back, and that the new graph being assigned hasn't already been assigned
                            //also skip closed tiles
                            if (b2 != building && b2.xen.graph != graph) {
                                graph.addBuild(b2);
                                queue.addLast(b2);
                            }
                        }
                    }
                }
                //calculate the graph at last
                graph.height = temp;
                graph.amount = graph.height * graph.area;
            }

        }

        //implied empty graph here
        removeGraph();
    }

    public void createGraph() {
        if (!added) {
            xenGraphAll.put(graphID, this);
            added = true;
        }
    }

    public void removeGraph() {
        if (added) {
            clear();
            xenGraphAll.remove(graphID);
            added = false;
        }
    }

     */
}
