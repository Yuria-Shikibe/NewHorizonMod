package newhorizon;

import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import newhorizon.expand.block.commandable.CommandableBlock;
import newhorizon.expand.block.special.RemoteCoreStorage;
import newhorizon.expand.entities.GravityTrapField;

import static mindustry.Vars.world;

public class NHGroups {
    protected static final Seq<GravityTrapField> tmpGravityTraps = new Seq<>();
    protected static final Rect tmpRect = new Rect();

    public static final ObjectMap<Building, Seq<Building>> beaconBoostLinks = new ObjectMap<>();
    public static final ObjectSet<RemoteCoreStorage.RemoteCoreStorageBuild>[] placedRemoteCore = new ObjectSet[Team.all.length];
    public static final Seq<CommandableBlock.CommandableBlockBuild> commandableBuilds = new Seq<>();
    public static QuadTree<GravityTrapField> gravityTraps = new QuadTree<>(new Rect());

    static {
        for (int i = 0; i < Team.all.length; i++) {
            NHGroups.placedRemoteCore[i] = new ObjectSet<>();
        }
    }

    public static void worldInit() {
        gravityTraps = new QuadTree<>(world.getQuadBounds(new Rect()));
    }

    public static void clear() {
        RemoteCoreStorage.clear();

        beaconBoostLinks.clear();
        commandableBuilds.clear();
        gravityTraps.clear();
    }

    public static void worldReset() {}

    public static void update() {}

    public static void draw() {}

    public static boolean inGravityTrap(Building entity, boolean friendly) {
        tmpGravityTraps.clear();
        entity.hitbox(tmpRect);
        gravityTraps.intersect(tmpRect, g -> {
            if (friendly) {
                if (g.owner == entity.team) tmpGravityTraps.add(g);
            }else {
                if (g.owner != entity.team) tmpGravityTraps.add(g);
            }
        });
        return !tmpGravityTraps.isEmpty();
    }
}
