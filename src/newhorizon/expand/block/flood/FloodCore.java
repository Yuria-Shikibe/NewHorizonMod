package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.renderer;
import static mindustry.Vars.world;

public class FloodCore extends CoreBlock {
    public int maxExpandArea = 4096;
    public FloodCore(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("graph area:", (FloodCoreBuild e) -> new Bar(
            () -> "graph area: " + e.graph.area,
            () -> Pal.accent,
            () -> 1f
        ));
        addBar("limit area:", (FloodCoreBuild e) -> new Bar(
            () -> "graph area: " + e.graph.areaLimit,
            () -> Pal.accent,
            () -> 1f
        ));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return (tile.x - 3) % size == 0 && (tile.y - 3) % size == 0;
    }

    @Override
    public void drawPlan(BuildPlan plan, Eachable<BuildPlan> list, boolean valid) {
        //drawWorldGrid();

        super.drawPlan(plan, list, valid);

    }

    public void drawWorldGrid(){
        Tmp.v1.set(Core.camera.position);
        float width = Core.camera.width * renderer.getDisplayScale();
        float height = Core.camera.width * renderer.getDisplayScale();

        Draw.z(Layer.max);
        Lines.stroke(renderer.getDisplayScale() * 2f);
        Lines.rect(Tmp.v1.x + width/2f + 50, Tmp.v1.y + height/2f + 50, width - 100, height - 100);
    }

    public class FloodCoreBuild extends CoreBuild implements FloodBuildingEntity{
        public FloodGraph graph;
        public Seq<Tile> expandCandidate;

        public boolean createMargin = false;

        public void created() {
            super.created();

            for (Point2 point2: Edges.getEdges(size)){
                Tile tile = world.tile(tileX() + point2.x, tileY() + point2.y);
                if (tile != null && !tile.dangerous() && !tile.solid() && tile.build == null){
                    //Build.beginPlace(null, FloodContentBlock.dummy11, team, tile.x, tile.y, 0);
                    //ConstructBlock.constructFinish(tile, FloodContentBlock.dummy11, null, (byte) 0, team, null);
                }
            }

            expandCandidate = new Seq<>();
            createGraph();
        }

        public void updateExpandCandidate(){
            expandCandidate.clear();
            for (Point2 p: Edges.getEdges(size)){
                Tile candidate = world.tile(tileX() + p.x, tileY() + p.y);
                if (candidate != null && !candidate.dangerous() && !candidate.solid() && candidate.build == null){
                    expandCandidate.add(candidate);
                }
            }
        }

        @Override
        public void draw() {
            Draw.z(Layer.block + 0.1f);
            Draw.rect(block.region, x, y, drawrot());
        }

        @Override
        public FloodGraph graph() {
            return graph;
        }

        @Override
        public void setGraph(FloodGraph graph) {
            this.graph = graph;
        }

        @Override
        public void createGraph() {
            graph = new FloodGraph();
            graph.addBuild(this);
        }

        @Override
        public void updateGraph(){
            updateExpandCandidate();

            if (expandCandidate.isEmpty()){
                graph.expandCandidate.remove(this);
            }else {
                graph.expandCandidate.put(this, expandCandidate);
            }
        }

        @Override
        public void removeGraph() {
            graph.remove(this);
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            for (Building other : proximity) {
                if (other instanceof FloodBase.FloodBaseBuilding){
                    graph.mergeGraph(((FloodBase.FloodBaseBuilding)other).graph);
                }
            }
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
            removeGraph();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateGraph();
        }

        @Override
        public void updateTile() {
            //if (!((tileX() - 3) % size == 0 && (tileY() - 3) % size == 0)){
            //    kill();
            //}
        }
    }
}


