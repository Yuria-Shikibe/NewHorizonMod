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
    protected static final Seq<GravityTrapField> tmpGravityFields = new Seq<>();
    protected static final Rect tmpRect = new Rect();

    public static final ObjectMap<Building, Seq<Building>> beaconBoostLinks = new ObjectMap<>();
    public static final ObjectSet<RemoteCoreStorage.RemoteCoreStorageBuild>[] placedRemoteCore = new ObjectSet[Team.all.length];
    public static final Seq<CommandableBlock.CommandableBlockBuild> commandableBuilds = new Seq<>();
    public static QuadTree<GravityTrapField> gravityFields = new QuadTree<>(new Rect());
    public static Seq<GravityTrapField> gravityFieldSeq = new Seq<>();

    static {
        for (int i = 0; i < Team.all.length; i++) {
            NHGroups.placedRemoteCore[i] = new ObjectSet<>();
        }
    }

    public static void worldInit() {
        gravityFields = new QuadTree<>(world.getQuadBounds(new Rect()));
    }

    public static void clear() {
        RemoteCoreStorage.clear();

        beaconBoostLinks.clear();
        commandableBuilds.clear();
        gravityFields.clear();
    }

    public static void worldReset() {}

    public static void update() {}

    public static void draw() {}

    public static float getGravityTrapForTeam(Team team) {
        float out = 0;
        for (int i = 0; i < gravityFieldSeq.size; i++){
            var field = gravityFieldSeq.get(i);
            if (field.owner == team) out += field.getGravityTrap();
        }
        return out;
    }

    public static boolean inGravityTrap(Building entity, boolean friendly) {
        tmpGravityFields.clear();
        entity.hitbox(tmpRect);
        gravityFields.intersect(tmpRect, g -> {
            if (friendly) {
                if (g.owner == entity.team) tmpGravityFields.add(g);
            }else {
                if (g.owner != entity.team) tmpGravityFields.add(g);
            }
        });
        return !tmpGravityFields.isEmpty();
    }
}
