package newhorizon.expand.block.synth;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.Seq;
import newhorizon.expand.block.flood.FloodBuildingEntity;

import static mindustry.Vars.world;

public class SynthUpdater {
    public Seq<FloodBuildingEntity> allCores = new Seq<>();
    public QuadTree<FloodBuildingEntity> coreBuildings = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
}
