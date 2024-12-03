package newhorizon.expand.block.flood;

import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.world.Tile;
import newhorizon.content.blocks.FloodContentBlock;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.expand.block.flood.FloodBlock.FloodBuilding;
import newhorizon.expand.block.struct.XenGraph;

import static mindustry.Vars.world;
import static newhorizon.expand.block.struct.GraphUpdater.allGraph;

//todo this is chaos
public class FloodGraph {

    private static final Queue<FloodBuilding> queue = new Queue<>();

    public final Seq<FloodBuilding> allBuildings = new Seq<>(false, 16);
    public final Seq<FloodBuilding> coreBuilding = new Seq<>(false, 16);

    //public final ObjectMap<FloodBuilding, Seq<Tile>> expandCandidate = new ObjectMap<>();
    //public final Seq<FloodBuilding> mergeCandidate = new Seq<>(false, 16);

    public float updateInterval = 2f;
    public float timer;
    public static int lastID = 0;
    public final int graphID;

    protected transient boolean added;

    public FloodGraph() {
        graphID = lastID++;
        createGraph();
    }

    public void mergeGraph(FloodGraph graph) {
        if (graph == this) return;

        //merge into other graph instead.
        if (allBuildings.size > graph.allBuildings.size) {
            for (FloodBuilding tile : graph.allBuildings) {
                addBuild(tile);
            }
            graph.removeGraph();

        } else {
            for (FloodBuilding tile : allBuildings) {
                graph.addBuild(tile);
            }
            removeGraph();
        }

    }

    public void addBuild(FloodBuilding building) {
        if (!allBuildings.contains(building)) {
            //add this block to it
            allBuildings.add(building);
            building.graph = this;
        }
    }

    public void clear() {
        allBuildings.clear();
    }

    public void remove(FloodBuilding building) {
        //go through all the connections of this tile
        for (Building other : building.proximity) {
            if (!(other instanceof FloodBuilding))continue;
            FloodBuilding fbOther = (FloodBuilding) other;
            //check if it contains the graph or is the target graph that can be merged
            if (fbOther.graph != this) continue;

            //create graph for this branch
            FloodGraph graph = new FloodGraph();
            graph.addBuild(fbOther);

            //BFS time
            queue.clear();
            queue.addLast(fbOther);
            while (queue.size > 0) {
                //get child from queue
                FloodBuilding child = queue.removeFirst();
                //add it to the new branch graph
                graph.addBuild(child);
                //go through connections
                for (Building next : child.proximity) {
                    if (!(next instanceof FloodBuilding))continue;
                    FloodBuilding fbNext = (FloodBuilding) next;

                    //make sure it hasn't looped back, and that the new graph being assigned hasn't already been assigned
                    //also skip closed tiles
                    if (fbNext != building && fbNext.graph != graph) {
                        graph.addBuild(fbNext);
                        queue.addLast(fbNext);
                    }
                }
            }
        }
        //implied empty graph here
        removeGraph();
    }

    public void createGraph() {
        if (!added){
            allGraph.put(graphID, this);
            added = true;
        }
    }

    public void removeGraph() {
        if (added){
            clear();
            allGraph.remove(graphID);
            added = false;
        }
    }

    public void update(){
        timer += Time.delta;
        int expandCount = 1;
        int count = 0;
        if (timer >= updateInterval){
            /*
            while (count < expandCount){
                if (Mathf.randomBoolean() && hasValidExpandCandidate()){
                    FloodBuilding building = expandCandidate.keys().toSeq().random();
                    if (building != null){
                        Tile tile = expandCandidate.get(building).random();
                        if (tile == null){
                            Log.info(building.tileX() + " " + building.tileY());
                        }else {
                            tile.setBlock(FloodContentBlock.dummy11, building.team);
                        }
                    }
                }
                //else {
                //    FloodBuilding building = mergeCandidate.random();
                //    if (building != null){
                //        Tile tile = building.tile;
                //        tile.setBlock(FloodContentBlock.dummy22, building.team);
                //    }
                //}

                count++;
            }

             */
            timer %= updateInterval;
        }
    }

    /*
    public boolean hasValidExpandCandidate(){
        return !expandCandidate.isEmpty();
    }

     */

    public void reset(){
        allGraph.values().toArray().each(FloodGraph::removeGraph);
    }
}
