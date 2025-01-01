package newhorizon.expand.block.floodv3;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.content.blocks.FloodContentBlock;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import static mindustry.Vars.*;
import static newhorizon.expand.block.struct.GraphUpdater.syntherEntity;

public class SyntherGraph {
    private static final Queue<SyntherBuildingEntity> queue = new Queue<>();

    public QuadTree<SyntherBuildingEntity> quadTreeBuildings = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));

    public Seq<SyntherBuildingEntity> allBuilding = new Seq<>(false, 16);
    public Seq<SyntherCore.SyntherCoreBuilding> coreBuilding = new Seq<>(false, 16);

    public ObjectMap<SyntherBuildingEntity, Seq<Tile>> expandCandidate = new ObjectMap<>();

    public Seq<SyntherBuildingEntity> merge1tiles = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> merge2tiles = new Seq<>(false, 16);

    //public Seq<SyntherBuildingEntity> size2 = new Seq<>(false, 16);
    //public Seq<SyntherBuildingEntity> size4 = new Seq<>(false, 16);
    /*


    public Seq<SyntherBuildingEntity> size2Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> size4Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> size8Candidate = new Seq<>(false, 16);

     */

    //values indicated current state.
    //current flood's area.
    public int expandCount = 1;
    public int areaLimit;
    public int area;

    //evolution factor from core. global factor maybe?
    public float evolution;

    //options control. weighted, depend on current state.
    public WeightedOption expand = new WeightedOption(5, this::expand11Block);
    public WeightedOption merge1 = new WeightedOption(5, this::merge1to2);
    public WeightedOption merge2 = new WeightedOption(5, this::merge2to4);

    //inner values
    private static final float UPDATE_INTERVAL = 2f;
    private static int lastID = 0;

    private final int graphID;
    private float timer;

    private Vec2 center;

    protected transient boolean added;

    public SyntherGraph() {
        graphID = lastID++;
        createGraph();
    }

    public void mergeGraph(SyntherGraph graph) {
        if (graph == this) return;
        if (allBuilding.size > graph.allBuilding.size) {
            for (SyntherBuildingEntity tile : graph.allBuilding) {
                addBuild(tile);
            }
            graph.removeGraph();
        } else {
            for (SyntherBuildingEntity tile : allBuilding) {
                graph.addBuild(tile);
            }
            removeGraph();
        }

    }

    public void addBuild(SyntherBuildingEntity building) {
        if (!contain(building)) {
            //add this block to it
            allBuilding.add(building);
            quadTreeBuildings.insert(building);
            building.setGraph(this);

            if (building instanceof SyntherCore.SyntherCoreBuilding){
                coreBuilding.add((SyntherCore.SyntherCoreBuilding) building);
                if (center == null) center = new Vec2(building.x(), building.y());
            }


            if (building.block().size == 1 && building.tileX() % 2 == 0 && building.tileY() % 2 == 0){
                merge1tiles.add(building);
            }

            if (building.block().size == 2 && building.tileX() % 4 == 0 && building.tileY() % 4 == 0){
                merge2tiles.add(building);
            }
        }

        building.updateGraph();
    }

    public boolean contain(SyntherBuildingEntity building){
        return quadTreeBuildings.any(
                building.x() - building.hitSize()/2f + 1f,
                building.y() - building.hitSize()/2f + 1f,
                building.hitSize() - 2f,
                building.hitSize() - 2f
        );
    }

    public void clear() {
        allBuilding.clear();
        quadTreeBuildings.clear();

        coreBuilding.clear();
        expandCandidate.clear();

        merge1tiles.clear();
        merge2tiles.clear();
    }

    public void remove(SyntherBuildingEntity building) {

        for (Building other : building.proximity()) {
            if (!(other instanceof SyntherBuildingEntity)) continue;
            SyntherBuildingEntity fbOther = (SyntherBuildingEntity) other;

            if (fbOther.graph() != this) continue;

            SyntherGraph graph = new SyntherGraph();
            graph.addBuild(fbOther);

            queue.clear();
            queue.addLast(fbOther);

            while (queue.size > 0) {
                SyntherBuildingEntity child = queue.removeFirst();

                graph.addBuild(child);

                for (Building next : child.proximity()) {
                    if (!(next instanceof SyntherBuildingEntity)) continue;
                    SyntherBuildingEntity fbNext = (SyntherBuildingEntity) next;

                    if (fbNext != building && fbNext.graph() != graph) {
                        graph.addBuild(fbNext);
                        queue.addLast(fbNext);
                    }
                }
            }
        }

        removeGraph();
    }


    public void createGraph() {
        if (!added){
            syntherEntity.put(graphID, this);
            added = true;
        }
    }

    public void removeGraph() {
        if (added){
            clear();
            syntherEntity.remove(graphID);
            added = false;
        }
    }

    public void update(){
        if (!added) return;
        if (coreBuilding.isEmpty()) return;
        if (state.isEditor()) return;

        updateOptions();

        timer += Time.delta;
        int count = 0;

        if (timer >= UPDATE_INTERVAL){

            while (count < expandCount){
                WeightedRandom.random(expand, merge1, merge2);
                count++;
            }



            timer %= UPDATE_INTERVAL;
        }
    }

    public void updateOptions(){
        expand.setWeight(10);
        merge1.setWeight(10);
        merge2.setWeight(10);
    }

    public void expand11Block(){
        if (expandCandidate.isEmpty()) return;
        SyntherBuildingEntity building = expandCandidate.keys().toSeq().random();
        Seq<Tile> tiles = expandCandidate.get(building);
        if (tiles.size > 0) {
            Tile tile = tiles.random();
            Call.setTile(tile, FloodContentBlock.syntherVein1, building.team(), 0);
        }
    }

    public void merge1to2(){
        if (merge1tiles == null || merge1tiles.isEmpty()){
            expand11Block();
        }else {
            SyntherBuildingEntity building = merge1tiles.random();
            Tile tile = building.tile();
            if (checkBuilding(building)){
                for (int x = 0; x < 2; x++){
                    for (int y = 0; y < 2; y++){
                        Call.removeTile(world.tile(tile.x + x, tile.y + y));
                    }
                }
                Call.setTile(tile, FloodContentBlock.syntherVein2, building.team(), 0);
            }else {
                expand11Block();
            }
        }
    }

    public void merge2to4(){
        if (merge2tiles == null || merge2tiles.isEmpty()){
            merge1to2();
        }else {
            SyntherBuildingEntity building = merge2tiles.random();
            Tile tile = world.tile(building.tileX() + 1, building.tileY() + 1);
            if (checkBuilding(building)){
                for (int x = 0; x < 4; x++){
                    for (int y = 0; y < 4; y++){
                        Call.removeTile(world.tile(building.tileX() + x, building.tileY() + y));
                    }
                }
                Call.setTile(tile, FloodContentBlock.syntherVein4, building.team(), 0);
            }else {
                merge1to2();
            }
        }
    }

    public boolean checkBuilding(SyntherBuildingEntity building){

        Block block = building.block();
        Tile tile = building.tile();

        /*
        int shift = (block.size + 1)/2;
        int tx = tile.x - shift;
        int ty = tile.y - shift;

        for (int x = 0; x < block.size * 2 + 2; x++){
            for (int y = 0; y < block.size * 2 + 2; y++){
                if (!(world.build(tx + x, ty + y) instanceof SyntherBuildingEntity)){
                    return false;
                }
            }
        }

         */

        return checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y + block.size)) && checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y)) && checkBuildingSameTile(block, world.tile(tile.x, tile.y + block.size));
    }

    public boolean checkBuildingSameTile(Block block, Tile tile){
        return tile != null && tile.build != null && tile.build.block == block && tile.build.tile == tile;
    }

    public void draw(){
        Lines.stroke(1f);

        //drawTree(quadTreeBuildings);

        if (allBuilding == null || allBuilding.isEmpty()) return;
        allBuilding.each(building -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.accent);
            Draw.alpha(0.25f);
            Fill.square(building.x(), building.y(), building.block().size * tilesize/2f);
        });

        if (NewHorizon.DEBUGGING){

        }
        expandCandidate.each(((building, tiles) -> tiles.each(tile -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.techBlue);
            Draw.alpha(0.2f);
            Lines.line(building.x(), building.y(), tile.drawx(), tile.drawy());
            Lines.square(tile.drawx(), tile.drawy(), 4);
        })));

        coreBuilding.each(building -> {
            Draw.z(Layer.blockOver);
            Draw.color(Pal.accent);
            Draw.alpha(0.3f);
            Fill.square(building.x, building.y, 2f);
        });
        /*







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
        //syntherEntity.values().toArray().each(SyntherGraph::removeGraph);
    }
}
