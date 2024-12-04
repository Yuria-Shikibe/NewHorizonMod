package newhorizon.expand.block.flood;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import newhorizon.content.blocks.FloodContentBlock;
import newhorizon.expand.block.flood.FloodBlock.FloodBuilding;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import static mindustry.Vars.*;
import static newhorizon.expand.block.struct.GraphUpdater.allGraph;

public class FloodGraph {

    private static final Queue<FloodBuilding> queue = new Queue<>();

    public QuadTree<FloodBuilding> quadTreeBuildings = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
    //public final Seq<FloodBuilding> allBuildings = new Seq<>(false, 16);
    public Seq<FloodBuilding> allBuilding = new Seq<>(false, 16);
    public Seq<FloodBuilding> coreBuilding = new Seq<>(false, 16);

    public ObjectMap<FloodBuilding, Seq<Tile>> expandCandidate = new ObjectMap<>();
    public Seq<FloodBuilding> merge1to2Candidate = new Seq<>(false, 16);
    public Seq<FloodBuilding> merge2to4Candidate = new Seq<>(false, 16);
    public Seq<FloodBuilding> merge4to8Candidate = new Seq<>(false, 16);

    public Seq<FloodBuilding> size2Candidate = new Seq<>(false, 16);
    public Seq<FloodBuilding> size4Candidate = new Seq<>(false, 16);


    public final Seq<WeightedOption> options = new Seq<>();

    public float updateInterval = 2f;
    public float timer;
    public static int lastID = 0;
    public final int graphID;

    protected transient boolean added;

    public FloodGraph() {
        graphID = lastID++;
        createGraph();

        options.add(
            new WeightedOption(100, this::expand11Block),

            new WeightedOption(200, this::merge1to2),
            new WeightedOption(100, this::merge2to4),
            new WeightedOption(50, this::merge4to8)
        );
    }

    public void mergeGraph(FloodGraph graph) {
        if (graph == this) return;
        //merge into other graph instead.
        Log.info(allBuilding.size + "/" + graph.allBuilding.size);
        if (allBuilding.size > graph.allBuilding.size) {
            for (FloodBuilding tile : graph.allBuilding) {
                addBuild(tile);
            }
            graph.removeGraph();
        } else {
            for (FloodBuilding tile : allBuilding) {
                graph.addBuild(tile);
            }
            removeGraph();
        }

    }

    public void addBuild(FloodBuilding building) {
        if (!contain(building)) {
            //add this block to it
            allBuilding.add(building);
            quadTreeBuildings.insert(building);
            //allBuildings.add(building);
            building.graph = this;

            if (building.block.size == 1 && building.tileX() % 2 == 0 && building.tileY() % 2 == 0){
                merge1to2Candidate.add(building);
            }
            if (building.block.size == 2){
                if (building.block == FloodContentBlock.dummy22){
                    size2Candidate.add(building);
                }
                if (building.tileX() % 4 == 0 && building.tileY() % 4 == 0){
                    merge2to4Candidate.add(building);
                }
            }
            if (building.block.size == 4){
                if (building.block == FloodContentBlock.dummy44){
                    size4Candidate.add(building);
                }
                if ((building.tileX() - 1) % 8 == 0 && (building.tileY() - 1) % 8 == 0){
                    merge4to8Candidate.add(building);
                }
            }
        }

        building.updateGraph();
    }

    public boolean contain(Building building){
        return quadTreeBuildings.any(
            building.x - building.hitSize()/2f + 1f,
            building.y - building.hitSize()/2f + 1f,
            building.hitSize() - 2f,
            building.hitSize() - 2f
        );
    }

    public void clear() {
        allBuilding.clear();
        quadTreeBuildings.clear();


        coreBuilding.clear();

        expandCandidate.clear();

        merge1to2Candidate.clear();
        merge2to4Candidate.clear();
        merge4to8Candidate.clear();

        size2Candidate.clear();
        size4Candidate.clear();

        allBuilding = null;
        quadTreeBuildings = null;
        merge1to2Candidate = null;
        merge2to4Candidate = null;
        merge4to8Candidate = null;

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
        if (!added) return;
        timer += Time.delta;
        int expandCount = 10;
        int count = 0;

        if (timer >= updateInterval){
            while (count < expandCount){
                WeightedRandom.random(
                    options

                    //new WeightedOption(3, this::upgrade22Turret),
                    //new WeightedOption(3, this::upgrade22Unit),
                    //new WeightedOption(3, this::upgrade44Turret),
                    //new WeightedOption(3, this::upgrade44Unit)

                    );
                count++;
            }
            timer %= updateInterval;
        }
    }

    public void construct(Tile tile, Block block, Team team){
        ConstructBlock.constructed(tile, block, null, (byte) 0, team, null);
    }

    public void expand11Block(){
        if (expandCandidate.isEmpty()) return;
        FloodBuilding building = expandCandidate.keys().toSeq().random();
        Seq<Tile> tiles = expandCandidate.get(building);
        if (tiles.size > 0) {
            Tile tile = tiles.random();
            Build.beginPlace(null, FloodContentBlock.dummy11, building.team, tile.x, tile.y, 0);
            ConstructBlock.constructFinish(tile, FloodContentBlock.dummy11, null, (byte) 0, building.team, null);
        }

    }

    public void upgrade22Turret(){
        if (size2Candidate.isEmpty()) return;
        Building building = size2Candidate.random();
        Tile tile = building.tile;
        tile.setBlock(FloodContentBlock.turret22, building.team);
    }

    public void upgrade22Unit(){
        if (size2Candidate.isEmpty()) return;
        Building building = size2Candidate.random();
        Tile tile = building.tile;
        tile.setBlock(FloodContentBlock.unit22, building.team);
    }

    public void upgrade44Turret(){
        if (size4Candidate.isEmpty()) return;
        Building building = size4Candidate.random();
        Tile tile = building.tile;
        tile.setBlock(FloodContentBlock.turret44, building.team);
    }

    public void upgrade44Unit(){
        if (size4Candidate.isEmpty()) return;
        Building building = size4Candidate.random();
        Tile tile = building.tile;
        tile.setBlock(FloodContentBlock.unit44, building.team);
    }

    public void merge1to2(){
        if (merge1to2Candidate == null || merge1to2Candidate.isEmpty()) return;
        int step = 0;
        while (step < 4){
            FloodBuilding building = merge1to2Candidate.random();
            Tile tile = building.tile;
            if (checkBuilding(building)){
                Build.beginPlace(null, FloodContentBlock.dummy22, building.team, tile.x, tile.y, 0);
                ConstructBlock.constructFinish(tile, FloodContentBlock.dummy22, null, (byte) 0, building.team, null);
                //tile.setBlock(FloodContentBlock.dummy22, building.team);
                break;
            }else {
                step++;
            }
        }
    }

    public void merge2to4(){
        if (merge2to4Candidate == null || merge2to4Candidate.isEmpty()) return;
        int step = 0;
        while (step < 4){
            Building building = merge2to4Candidate.random();
            Tile tile = world.tile(building.tileX() + 1, building.tileY() + 1);
            if (checkBuilding(building)){
                Build.beginPlace(null, FloodContentBlock.dummy44, building.team, tile.x, tile.y, 0);
                ConstructBlock.constructFinish(tile, FloodContentBlock.dummy44, null, (byte) 0, building.team, null);                break;
            }else {
                step++;
            }
        }
    }
    public void merge4to8(){
        if (merge4to8Candidate == null || merge4to8Candidate.isEmpty()) return;
        int step = 0;
        while (step < 4){
            Building building = merge4to8Candidate.random();
            Tile tile = world.tile(building.tileX() + 2, building.tileY() + 2);
            if (checkBuilding(building)){
                Build.beginPlace(null, FloodContentBlock.dummy88, building.team, tile.x, tile.y, 0);
                ConstructBlock.constructFinish(tile, FloodContentBlock.dummy88, null, (byte) 0, building.team, null);                break;
            }else {
                step++;
            }
        }
    }

    public boolean checkBuilding(Building building){
        Block block = building.block;
        Tile tile = building.tile;

        int shift = (block.size + 1)/2;
        int tx = tile.x - shift;
        int ty = tile.y - shift;

        for (int x = 0; x < block.size * 2 + 2; x++){
            for (int y = 0; y < block.size * 2 + 2; y++){
                if (!(world.build(tx + x, ty + y) instanceof FloodBuilding)){
                    return false;
                }
            }
        }

        return checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y + block.size)) && checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y)) && checkBuildingSameTile(block, world.tile(tile.x, tile.y + block.size));
    }

    public boolean checkBuildingSameTile(Block block, Tile tile){
        return tile != null && tile.build != null && tile.build.block == block && tile.build.tile == tile;
    }

    public void draw(){
        Lines.stroke(1f);

        //drawTree(quadTreeBuildings);

        //quadTreeBuildings.objects.each(building -> {
        //    Draw.z(Layer.blockOver);
        //    Draw.color(Pal.accent);
        //    Draw.alpha(0.25f);
        //    Fill.square(building.x, building.y, building.block.size * tilesize/2f);
        //});

        expandCandidate.each(((building, tiles) -> tiles.each(tile -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.techBlue);
            Draw.alpha(0.2f);
            Lines.line(building.x, building.y, tile.drawx(), tile.drawy());
            Lines.square(tile.drawx(), tile.drawy(), 4);
        })));

        /*
        merge2to4Candidate.each(building -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.accent);
            Draw.alpha(0.3f);
            Fill.square(building.x, building.y, 2f);
        });

        merge4to8Candidate.each(building -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.berylShot);
            Draw.alpha(0.3f);
            Fill.square(building.x, building.y, 2f);
        });

         */
    }


    public boolean hasValidExpandCandidate(){
        return !expandCandidate.isEmpty();
    }

    public void reset(){
        allGraph.values().toArray().each(FloodGraph::removeGraph);
    }
}
