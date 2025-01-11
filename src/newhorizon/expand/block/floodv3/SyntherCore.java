package newhorizon.expand.block.floodv3;

import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.world;
import static newhorizon.NHVars.rectControl;
//import static newhorizon.NHVars.rectControl;

public class SyntherCore extends CoreBlock {
    public int maxExpandArea = 4096;

    public SyntherCore(String name) {
        super(name);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return (tile.x - 3) % size == 0 && (tile.y - 3) % size == 0;
    }

    public class SyntherCoreBuilding extends CoreBuild implements SyntherBuildingEntity{
        public SyntherGraph graph;
        public Seq<Tile> expandCandidate;

        public void created() {
            super.created();

            //for (Point2 point2: Edges.getEdges(size)){
            //    Tile tile = world.tile(tileX() + point2.x, tileY() + point2.y);
            //    if (tile != null && !tile.dangerous() && !tile.solid() && tile.build == null){
            //        Call.setTile(tile, FloodContentBlock.syntherVein1, team, 0);
            //    }
            //}

            expandCandidate = new Seq<>();
            createGraph();
        }

        public void updateExpandCandidate(){
            expandCandidate.clear();
            for (Point2 p: Edges.getEdges(size)){
                if (rectControl.getPos((tileX() + p.x) / 4, (tileY() + p.y) / 4) == 0) continue;
                Tile candidate = world.tile(tileX() + p.x, tileY() + p.y);
                if (candidate != null && !candidate.dangerous() && !candidate.solid() && candidate.build == null){
                    expandCandidate.add(candidate);
                }
            }
        }

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            for (Building other : proximity) {
                if (other instanceof SyntherBuildingEntity){
                    graph.mergeGraph(((SyntherBuildingEntity)other).graph());
                }
            }
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateGraph();
        }

        @Override
        public void onProximityRemoved() {
            super.onProximityRemoved();
            removeGraph();
        }

        @Override
        public SyntherGraph graph() {
            return graph;
        }

        @Override
        public void remove() {
            super.remove();
            //removeGraph();
        }

        @Override
        public void setGraph(SyntherGraph graph) {
            this.graph = graph;
        }

        @Override
        public void createGraph() {
            graph = new SyntherGraph();
            graph.addBuild(this);
        }

        @Override
        public void updateGraph() {
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
    }
}
