package newhorizon.expand.block.flood;

import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Build;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import newhorizon.util.struct.WeightedOption;

import static mindustry.Vars.state;
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
        return true;
    }

    public class FloodCoreBuild extends CoreBuild implements FloodBuildingEntity{
        public FloodGraph graph;
        public Seq<Tile> expandCandidate;

        public void created() {
            super.created();

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
                if (other instanceof FloodBlock.FloodBuilding){
                    graph.mergeGraph(((FloodBlock.FloodBuilding)other).graph);
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
    }
}
