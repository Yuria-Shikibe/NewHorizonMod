package newhorizon.expand.block.distribution.delivery;

import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.struct.IntSeq;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;

import static mindustry.Vars.world;

public class CargoDeliveryGraph {
    public static final CargoDeliveryGraph[] graphs = new CargoDeliveryGraph[Team.all.length];
    private static final IntSeq emptyLinks = new IntSeq();
    private static boolean dirty = true;

    public final Team team;
    public final Seq<Building> relays = new Seq<>();
    public final Seq<CargoDeliveryDeposit.CargoDeliveryDepositBuild> deposits = new Seq<>();
    private final ObjectMap<Building, PathResult> pathCache = new ObjectMap<>();
    private final Seq<PathResult> pathResultPool = new Seq<>();
    private final ObjectSet<Building> tmpVisited = new ObjectSet<>();

    public CargoDeliveryGraph(Team team) {
        this.team = team;
    }

    public static void markDirty() {
        dirty = true;
    }

    public static void clear() {
        for (int i = 0; i < graphs.length; i++) {
            graphs[i] = null;
        }
        dirty = true;
    }

    public static void worldInit() {
        clear();

        for (int i = 0; i < Groups.build.size(); i++) {
            Building build = Groups.build.index(i);
            if (isParticipant(build)) get(build.team).add(build);
        }

        dirty = true;
        update();
    }

    public static void update() {
        if (!dirty) return;
        dirty = false;

        for (CargoDeliveryGraph graph : graphs) {
            if (graph != null) graph.clearPaths();
        }
    }

    public static void ensureUpdated() {
        if (dirty) update();
    }

    public static void register(Building build) {
        if (build == null || !isParticipant(build)) return;

        get(build.team).add(build);
        markDirty();
    }

    public static void unregister(Building build) {
        if (build == null) return;

        for (CargoDeliveryGraph graph : graphs) {
            if (graph != null) graph.remove(build);
        }
        markDirty();
    }

    public static CargoDeliveryGraph get(Team team) {
        CargoDeliveryGraph graph = graphs[team.id];
        if (graph == null) {
            graphs[team.id] = graph = new CargoDeliveryGraph(team);
        }
        return graph;
    }

    public static IntSeq linksOf(Building build) {
        return build instanceof CargoDeliveryBuild cargo ? cargo.links : emptyLinks;
    }

    public static float range(Building build) {
        if (build == null) return 0f;
        if (build.block instanceof CargoDeliveryPlatform platform) return platform.range;
        if (build.block instanceof CargoDeliveryNode node) return node.range;
        if (build.block instanceof CargoDeliveryDeposit deposit) return deposit.range;
        return 0f;
    }

    public static int maxLinks(Building build) {
        if (build == null) return 0;
        if (build.block instanceof CargoDeliveryPlatform platform) return platform.maxLinks;
        if (build.block instanceof CargoDeliveryNode node) return node.maxLinks;
        return 0;
    }

    public static boolean isRelay(Building build) {
        return build != null && (build.block instanceof CargoDeliveryPlatform || build.block instanceof CargoDeliveryNode);
    }

    public static boolean isDeposit(Building build) {
        return build != null && build.block instanceof CargoDeliveryDeposit;
    }

    public static boolean isParticipant(Building build) {
        return isRelay(build) || isDeposit(build);
    }

    public static boolean connected(Building a, Building b) {
        if (a == null || b == null || a == b || a.team != b.team || !isParticipant(a) || !isParticipant(b)) {
            return false;
        }
        if (isDeposit(a) && isDeposit(b)) return false;

        float rangeA = range(a);
        float rangeB = range(b);
        if (rangeA <= 0f || rangeB <= 0f) return false;

        return Intersector.overlaps(Tmp.cr1.set(a.x, a.y, rangeA), b.tile.getHitbox(Tmp.r1))
                || Intersector.overlaps(Tmp.cr1.set(b.x, b.y, rangeB), a.tile.getHitbox(Tmp.r1));
    }

    private void add(Building build) {
        if (isRelay(build)) relays.addUnique(build);
        if (build instanceof CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit) deposits.addUnique(deposit);
    }

    private void remove(Building build) {
        relays.remove(build);
        if (build instanceof CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit) deposits.remove(deposit);
    }

    private void clearPaths() {
        for (PathResult result : pathCache.values()) {
            result.dist.clear();
            result.prev.clear();
            pathResultPool.add(result);
        }
        pathCache.clear();
    }

    public PathResult distances(Building from) {
        if (from == null || !isParticipant(from)) return new PathResult();

        ensureUpdated();
        PathResult cached = pathCache.get(from);
        if (cached != null) return cached;

        PathResult result = pathResultPool.size > 0 ? pathResultPool.pop() : new PathResult();
        result.dist.clear();
        result.prev.clear();
        tmpVisited.clear();
        result.dist.put(from, 0f);

        while (true) {
            Building best = null;
            float bestDist = Float.MAX_VALUE;

            for (ObjectFloatMap.Entry<Building> entry : result.dist) {
                if (tmpVisited.contains(entry.key)) continue;

                float dist = entry.value;
                if (dist < bestDist || (Mathf.equal(dist, bestDist, 0.0001f)
                        && (best == null || entry.key.id < best.id))) {
                    best = entry.key;
                    bestDist = dist;
                }
            }

            if (best == null) break;
            tmpVisited.add(best);

            if (isDeposit(best) && best != from) continue;

            IntSeq links = linksOf(best);
            for (int i = 0; i < links.size; i++) {
                Building next = world.build(links.get(i));
                if (next == null || next.team != best.team || !isParticipant(next) || tmpVisited.contains(next)) continue;

                float nextDist = bestDist + best.dst(next);
                float old = result.dist.get(next, Float.MAX_VALUE);
                if (old == Float.MAX_VALUE || nextDist < old) {
                    result.dist.put(next, nextDist);
                    result.prev.put(next, best);
                }
            }
        }

        tmpVisited.clear();
        pathCache.put(from, result);
        return result;
    }

    public @Nullable Seq<Building> pathTo(PathResult result, Building from, Building to) {
        Seq<Building> path = new Seq<>();
        return pathTo(result, from, to, path) ? path : null;
    }

    public boolean pathTo(PathResult result, Building from, Building to, Seq<Building> path) {
        path.clear();
        if (result == null || !result.dist.containsKey(to)) return false;

        Building current = to;

        while (current != null && current != from) {
            path.add(current);
            current = result.prev.get(current);
        }

        if (current != from) {
            path.clear();
            return false;
        }

        path.reverse();
        return true;
    }

    public static class PathResult {
        public final ObjectFloatMap<Building> dist = new ObjectFloatMap<>();
        public final ObjectMap<Building, Building> prev = new ObjectMap<>();

        public float distance(Building build) {
            return dist.get(build, Float.MAX_VALUE);
        }
    }
}
