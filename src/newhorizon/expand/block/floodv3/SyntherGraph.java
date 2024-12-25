package newhorizon.expand.block.floodv3;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.expand.block.flood.FloodCore;
import newhorizon.util.func.MathUtil;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.struct.WeightedOption;

import static mindustry.Vars.*;
import static mindustry.Vars.world;
import static newhorizon.expand.block.struct.GraphUpdater.syntherEntity;

public class SyntherGraph {
    private static final Queue<SyntherBuildingEntity> queue = new Queue<>();

    public QuadTree<SyntherBuildingEntity> quadTreeBuildings = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));

    public Seq<SyntherBuildingEntity> allBuilding = new Seq<>(false, 16);
    public Seq<SyntherCore.SyntherCoreBuilding> coreBuilding = new Seq<>(false, 16);

    public ObjectMap<SyntherBuildingEntity, Seq<Tile>> expandCandidate = new ObjectMap<>();
    /*
    public Seq<SyntherBuildingEntity> merge1to2Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> merge2to4Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> merge4to8Candidate = new Seq<>(false, 16);

    public Seq<SyntherBuildingEntity> size2Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> size4Candidate = new Seq<>(false, 16);
    public Seq<SyntherBuildingEntity> size8Candidate = new Seq<>(false, 16);

     */

    //values indicated current state.
    //current flood's area.
    public int expandCount = 10;
    public int areaLimit;
    public int area;

    //evolution factor from core. global factor maybe?
    public float evolution;

    //options control. weighted, depend on current state.
    public WeightedOption expand = new WeightedOption(0, this::expand11Block);
    //public WeightedOption merge1 = new WeightedOption(5, this::merge1to2);
    //public WeightedOption merge2 = new WeightedOption(5, this::merge2to4);
    //public WeightedOption merge4 = new WeightedOption(5, this::merge4to8);
    public WeightedOption summon = new WeightedOption(5, this::createUnit);

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
        if (!added || !graph.added) return;
        if (allBuilding == null || graph.allBuilding == null) return;
        //merge into other graph instead.
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

                //SyntherCore core = (SyntherCore) building.block();
                //areaLimit += core.maxExpandArea;
                //if (center == null) center = new Vec2(building.x(), building.y());
            }

            //area += (int) Mathf.sqr(building.block().size);

            /*
            if (building.block().size == 1 && building.tileX() % 2 == 0 && building.tileY() % 2 == 0){
                merge1to2Candidate.add(building);
            }

            if (building.block().size == 2){
                if (building.block() == FloodContentBlock.dummy22){
                    size2Candidate.add(building);
                }
                if (building.tileX() % 4 == 0 && building.tileY() % 4 == 0){
                    merge2to4Candidate.add(building);
                }
            }
            if (building.block().size == 4){
                if (building.block() == FloodContentBlock.dummy44){
                    size4Candidate.add(building);
                }
                if ((building.tileX() - 1) % 8 == 0 && (building.tileY() - 1) % 8 == 0){
                    merge4to8Candidate.add(building);
                }
            }
            if (building.block().size == 8){
                size8Candidate.add(building);
            }

             */
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

        allBuilding = null;
        quadTreeBuildings = null;

        /*

        merge1to2Candidate.clear();
        merge2to4Candidate.clear();
        merge4to8Candidate.clear();

        size2Candidate.clear();
        size4Candidate.clear();


        merge1to2Candidate = null;
        merge2to4Candidate = null;
        merge4to8Candidate = null;

         */
    }

    public void remove(SyntherBuildingEntity building) {
        //Seq<SyntherBuildingEntity> visited = new Seq<>();

        for (Building other : building.proximity()) {
            if (!(other instanceof SyntherBuildingEntity)) continue;
            SyntherBuildingEntity fbOther = (SyntherBuildingEntity) other;

            if (fbOther.graph() != this) continue;

            SyntherGraph graph = new SyntherGraph();
            graph.addBuild(fbOther);

            queue.clear();
            queue.addLast(fbOther);
            //visited.add(fbOther);

            while (queue.size > 0) {
                SyntherBuildingEntity child = queue.removeFirst();
                //if (visited.contains(child)) continue;

                graph.addBuild(child);
                //visited.add(child);

                for (Building next : child.proximity()) {
                    if (!(next instanceof SyntherBuildingEntity)) continue;
                    SyntherBuildingEntity fbNext = (SyntherBuildingEntity) next;

                    if (fbNext != building && fbNext.graph() != graph
                            //&& !visited.contains(fbNext)
                    ) {
                        graph.addBuild(fbNext);
                        queue.addLast(fbNext);
                        //visited.add(fbNext);
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

        //idk why but sometimes there are random duplicated graphs
        //random check to remove them
        //this sucks
        if (coreBuilding.random().graph != this){
                removeGraph();
                return;
        }
        if (allBuilding.random().graph() != this){
            removeGraph();
            return;
        }

        updateOptions();

        timer += Time.delta;
        int count = 0;

        if (timer >= UPDATE_INTERVAL){
            while (count < expandCount){
                WeightedRandom.random(expand/*, merge1, merge2, merge4, summon*/);
                count++;
            }
            timer %= UPDATE_INTERVAL;
        }
    }

    public void construct(Tile tile, Building building){
        //Build.beginPlace(null, FloodContentBlock.dummy11, building.team(), tile.x, tile.y, 0);
        //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy11, null, (byte) 0, building.team(), null);
    }

    public void updateOptions(){
        expand.setWeight(12 + (1 - (float) area / areaLimit) * 40);
        //merge1.setWeight(10 + (1 - (float) area / areaLimit) * 10f);
        //merge2.setWeight(5 + (1 - (float) area / areaLimit) * 5f);
        //merge4.setWeight(4 + (1 - (float) area / areaLimit) * 2f);
        summon.setWeight(2 + ((float) area / areaLimit) * 3);
    }

    public void expand11Block(){
        if (expandCandidate.isEmpty()) return;
        SyntherBuildingEntity building = expandCandidate.keys().toSeq().random();
        Seq<Tile> tiles = expandCandidate.get(building);
        if (tiles.size > 0) {
            Tile tile = tiles.random();
            //Build.beginPlace(null, FloodContentBlock.dummy11, building.team(), tile.x, tile.y, 0);
            //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy11, null, (byte) 0, building.team(), null);
            Building b = tile.build;
            if (b != null){
                buildEffect(b).at(b.x, b.y);
            }
        }
    }

    public Effect buildEffect(Building b){
        return new Effect(45f, e -> {
            float width = b.block.size * tilesize / 2f * 1.2f;
            float rotation = b.relativeTo((int) (center.x / tilesize), (int) (center.y / tilesize)) * 90;
            Draw.color(Tmp.c1.set(b.team.color).lerp(Color.clear, e.fin(Interp.pow10In) * 0.9f + 0.1f));
            Fill.square(e.x, e.y, width);
            DrawFunc.gradient(
                    Tmp.v2.trns(rotation + 180, width - 2 * e.fout(Interp.pow2In) * width).add(e.x, e.y),
                    Tmp.v1.trns(rotation + 180, width - 2 * e.fout(Interp.pow5Out) * width).add(e.x, e.y),
                    width,
                    Tmp.c1.set(b.team.color).mul(1 + 0.1f * e.fslope())
            );
        });
    }

    /*

    public void merge1to2(){
        if (merge1to2Candidate == null || merge1to2Candidate.isEmpty()) return;
        int step = 0;
        while (step < 4){
            SyntherBuildingEntity building = merge1to2Candidate.random();
            Tile tile = building.tile();
            if (checkBuilding(building)){
                //Build.beginPlace(null, FloodContentBlock.dummy22, building.team(), tile.x, tile.y, 0);
                //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy22, null, (byte) 0, building.team(), null);
                Building b = tile.build;
                if (b != null){
                    buildEffect(b).at(b.x, b.y);
                }
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
            SyntherBuildingEntity building = merge2to4Candidate.random();
            Tile tile = world.tile(building.tileX() + 1, building.tileY() + 1);
            if (checkBuilding(building)){
                //Build.beginPlace(null, FloodContentBlock.dummy44, building.team(), tile.x, tile.y, 0);
                //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy44, null, (byte) 0, building.team(), null);
                Building b = tile.build;
                if (b != null){
                    buildEffect(b).at(b.x, b.y);
                }break;
            }else {
                step++;
            }
        }
    }

    public void merge4to8(){
        if (merge4to8Candidate == null || merge4to8Candidate.isEmpty()) return;
        int step = 0;
        while (step < 4){
            SyntherBuildingEntity building = merge4to8Candidate.random();
            Tile tile = world.tile(building.tileX() + 2, building.tileY() + 2);
            if (checkBuilding(building)){
                //Build.beginPlace(null, FloodContentBlock.dummy88, building.team(), tile.x, tile.y, 0);
                //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy88, null, (byte) 0, building.team(), null);
                Building b = tile.build;
                if (b != null){
                    buildEffect(b).at(b.x, b.y);
                }break;
            }else {
                step++;
            }
        }
    }

     */

    public void createUnit(){
        if (allBuilding == null || allBuilding.isEmpty()) return;
        SyntherBuildingEntity building = allBuilding.random();
        SyntherCore.SyntherCoreBuilding core = coreBuilding.random();
        if (building == null || core == null)return;
        UnitType unitType;
        if (area > 2500 && Mathf.chance(0.2f)){
            unitType = UnitTypes.collaris;
        }else if (area > 1800 && Mathf.chance(0.3f)){
            unitType = UnitTypes.tecta;
        }else if (area > 1250 && Mathf.chance(0.4f)){
            unitType = UnitTypes.anthicus;
        }else if (area > 720 && Mathf.chance(0.4f)){
            unitType = UnitTypes.cleroi;
        }else if (area > 350){
            unitType = UnitTypes.merui;
        }else {
            return;
        }

        if (Groups.unit.tree().any(building.x() - unitType.hitSize/2f + 1f, building.y() - unitType.hitSize/2f + 1f, unitType.hitSize - 2f, unitType.hitSize - 2f)) return;
        float angle = MathUtil.angle(core, building);
        //WarpRift rift = new WarpRift();
        //rift.create(building.team(), unitType, building.x(), building.y(), angle);
        //rift.add();
    }

    public boolean checkBuilding(SyntherBuildingEntity building){
        Block block = building.block();
        Tile tile = building.tile();

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

        return checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y + block.size)) && checkBuildingSameTile(block, world.tile(tile.x + block.size, tile.y)) && checkBuildingSameTile(block, world.tile(tile.x, tile.y + block.size));
    }

    public boolean checkBuildingSameTile(Block block, Tile tile){
        return tile != null && tile.build != null && tile.build.block == block && tile.build.tile == tile;
    }

    public void draw(){
        Lines.stroke(1f);

        //drawTree(quadTreeBuildings);

        quadTreeBuildings.objects.each(building -> {
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
        //allGraph.values().toArray().each(SyntherGraph::removeGraph);
    }
}
