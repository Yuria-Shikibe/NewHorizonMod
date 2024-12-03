package newhorizon.expand.block.flood;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import newhorizon.expand.block.AdaptBuilding;
import newhorizon.expand.block.module.XenModule;
import newhorizon.expand.block.struct.GraphEntity;

import static mindustry.Vars.world;

public class FloodBlock extends Block {
    public FloodBlock(String name) {
        super(name);

        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        canOverdrive = false;
        envEnabled = Env.any;
        update = true;
    }

    public class FloodBuilding extends Building{
        public FloodGraph graph;
        public Seq<Tile> expandCandidate;

        @Override
        public void created() {
            super.created();

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

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
        }
    }
}
