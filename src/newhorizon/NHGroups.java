package newhorizon;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Building;
import newhorizon.expand.block.commandable.CommandableBlock;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.world;

public class NHGroups {
    public static final ObjectMap<Building, Building> bridgeLinks = new ObjectMap<>();
    public static final ObjectMap<Building, Seq<Building>> beaconBoostLinks = new ObjectMap<>();
    public static final Seq<GravityTrapField> gravityTrapsDraw = new Seq<>();
    public static final Seq<CommandableBlock.CommandableBlockBuild> commandableBuilds = new Seq<>();
    public static QuadTree<GravityTrapField> gravityTraps = new QuadTree<>(world.getQuadBounds(new Rect()));

    public static void worldInit() {
        gravityTraps = new QuadTree<>(world.getQuadBounds(new Rect()));
        gravityTrapsDraw.each(g -> gravityTraps.insert(g));
    }

    public static void clear() {
        beaconBoostLinks.clear();
        bridgeLinks.clear();
        gravityTraps.clear();
        gravityTrapsDraw.clear();
        commandableBuilds.clear();
    }

    public static void worldReset() {}

    public static void update() {}

    public static void draw() {}
}
