package newhorizon.expand.block.flood;

import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import static mindustry.Vars.world;

public class FloodBase extends Block {
    public FloodBase(String name) {
        super(name);

        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        canOverdrive = false;
        envEnabled = Env.any;
        update = false;
        drawCracks = false;

        placeSound = Sounds.none;

        placeEffect = Fx.none;
    }

    public class FloodBaseBuilding extends Building implements FloodBuildingEntity{
        public FloodGraph graph;
        public Seq<Tile> expandCandidate;

        @Override
        public void created() {
            super.created();

            expandCandidate = new Seq<>();
            createGraph();
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            for (Building other : proximity) {
                if (other instanceof FloodBaseBuilding){
                    graph.mergeGraph(((FloodBaseBuilding)other).graph);
                }
            }
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
            removeGraph();
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
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateGraph();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            graph.draw();
        }
    }
}
