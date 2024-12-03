package newhorizon.expand.block.flood;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

import static mindustry.Vars.world;

public class FloodBlock extends Block {
    public FloodBlock(String name) {
        super(name);

        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        canOverdrive = false;
        envEnabled = Env.any;
        update = false;
    }

    public class FloodBuilding extends Building{
        public FloodGraph graph;
        public Seq<Tile> expandCandidate;

        @Override
        public void created() {
            super.created();

            expandCandidate = new Seq<>();

            graph = new FloodGraph();
            graph.addBuild(this);
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            for (Building other : proximity) {
                if (other instanceof FloodBuilding){
                    graph.mergeGraph(((FloodBuilding)other).graph);
                }
            }
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
            graph.remove(this);
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

        public void updateGraph(){
            updateExpandCandidate();

            if (expandCandidate.isEmpty()){
                graph.expandCandidate.remove(this);
            }else {
                graph.expandCandidate.put(this, expandCandidate);
            }
        }

        public void syncGraph(){
            if (expandCandidate.isEmpty()){
                graph.expandCandidate.remove(this);
            }else {
                graph.expandCandidate.put(this, expandCandidate);
            }
        }


        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateGraph();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            graph.draw();
        }
    }
}
