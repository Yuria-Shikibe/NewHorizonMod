package newhorizon.expand.block.struct;

import arc.graphics.Color;
import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.gen.Building;
import newhorizon.expand.block.AdaptBlock;
import newhorizon.expand.block.AdaptBuilding;

import static newhorizon.expand.block.struct.GraphUpdater.xenGraphAll;

public class XenGraph {
    private static final Queue<AdaptBuilding> queue = new Queue<>();
    public static int lastID = 0;
    //do not modify any of these unless you know what you're doing!
    public final Seq<AdaptBuilding> producers = new Seq<>(false);
    public final Seq<AdaptBuilding> consumers = new Seq<>(false);
    public final Seq<AdaptBuilding> distributors = new Seq<>(false);
    public final Seq<AdaptBuilding> allBuildings = new Seq<>(false);
    public final int graphID;

    public float area;
    public float height;
    public float amount = 0f;
    protected transient boolean added;

    //todo
    public Color xenColor;

    public XenGraph(float area, float altitude) {
        this.area = area;
        this.height = altitude;

        graphID = lastID++;
        register();
    }

    public XenGraph(float area) {
        this.area = area;

        graphID = lastID++;
        register();
    }

    public XenGraph(){
        graphID = lastID++;
        register();
    }

    public void calcCurrentHeight() {
        height = amount / area;
    }

    public boolean handleXen(float amount, float threshold){
        if ((amount > 0 && threshold < height) || (amount < 0 && threshold > height)) return false;
        this.amount += amount;
        calcCurrentHeight();
        return true;
    }

    public void addXen(float amount, float threshold) {
        if (threshold > height) {
            this.amount += amount;
        }
        calcCurrentHeight();
    }

    public void removeXen(float amount, float threshold) {
        if (threshold < height) {
            this.amount -= amount;
        }
        calcCurrentHeight();
    }

    public void mergeGraph(XenGraph graph) {
        if (graph == this) return;

        //merge into other graph instead.
        if (allBuildings.size > graph.allBuildings.size) {
            for (AdaptBuilding tile : graph.allBuildings) {
                addBuild(tile);
                calcCurrentHeight();
            }
            graph.removeGraph();

        } else {
            for (AdaptBuilding tile : allBuildings) {
                graph.addBuild(tile);
                graph.calcCurrentHeight();
            }
            removeGraph();
        }

    }

    public void addBuild(AdaptBuilding building) {
        AdaptBlock nhBlock = building.getBlock();

        if (nhBlock != null && nhBlock.hasXen) {
            if (!allBuildings.contains(building)) {
                XenGraph graph = building.xen.graph;

                area += nhBlock.xenArea;
                amount += graph.height * nhBlock.xenArea;

                //add this block to it
                allBuildings.add(building);
                if (nhBlock.xenArea > 0) {
                    this.distributors.add(building);
                }
                if (nhBlock.xenInput) {
                    this.consumers.add(building);
                }
                if (nhBlock.xenOutput) {
                    this.producers.add(building);
                }

                building.xen.setGraph(this);

                calcCurrentHeight();
            }
        }
    }

    public void register() {
        this.createGraph();
    }

    public void clear() {
        allBuildings.clear();
        distributors.clear();
        producers.clear();
        consumers.clear();
    }

    /**
     * Note that this does not actually remove the Building from the graph;
     * it creates *new* graphs that contain the correct buildings. Doing this invalidates the graph.
     */
    public void remove(AdaptBuilding building) {

        float temp = height;
        //go through all the connections of this tile
        for (Building other : building.proximity) {
            AdaptBuilding b1 = checkXen(other);
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
                    AdaptBuilding child = queue.removeFirst();
                    //add it to the new branch graph
                    graph.addBuild(child);
                    //go through connections
                    for (Building next : child.proximity) {
                        AdaptBuilding b2 = checkXen(next);
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

    private AdaptBuilding checkXen(Building building){
        if (building instanceof AdaptBuilding){
            AdaptBuilding b = (AdaptBuilding) building;
            if (b.getBlock() != null && b.getBlock().hasXen){
                return b;
            }
        }
        return null;
    }
}
