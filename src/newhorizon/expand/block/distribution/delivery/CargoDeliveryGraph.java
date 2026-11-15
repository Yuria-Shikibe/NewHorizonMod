package newhorizon.expand.block.distribution.delivery;

import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Groups;

public class CargoDeliveryGraph {
    public static final CargoDeliveryGraph[] graphs = new CargoDeliveryGraph[Team.all.length];
    private static boolean dirty = true;

    public final Team team;
    public final Seq<Building> relays = new Seq<>();
    public final Seq<CargoDeliveryDeposit.CargoDeliveryDepositBuild> deposits = new Seq<>();
    public final ObjectMap<Building, Seq<Building>> adjacency = new ObjectMap<>();
    private final ObjectMap<Building, Seq<Building>> componentCache = new ObjectMap<>();
    private final ObjectMap<Building, PathResult> pathCache = new ObjectMap<>();

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
        dirty = true;
        update();
    }

    public static void update() {
        if (!dirty) return;
        dirty = false;
        rebuild();
    }

    public static void ensureUpdated() {
        if (dirty) update();
    }

    public static CargoDeliveryGraph get(Team team) {
        CargoDeliveryGraph graph = graphs[team.id];
        if (graph == null) {
            graphs[team.id] = graph = new CargoDeliveryGraph(team);
        }
        return graph;
    }

    public static boolean isRelay(Building build) {
        return build.block instanceof CargoDeliveryPlatform || build.block instanceof CargoDeliveryNode;
    }

    public static boolean isDeposit(Building build) {
        return build.block instanceof CargoDeliveryDeposit;
    }

    public static boolean isParticipant(Building build) {
        return isRelay(build) || isDeposit(build);
    }

    public static float range(Building build) {
        if (build.block instanceof CargoDeliveryPlatform platform) return platform.range;
        if (build.block instanceof CargoDeliveryNode node) return node.range;
        if (build.block instanceof CargoDeliveryDeposit deposit) return deposit.range;
        return 0f;
    }

    public static boolean connected(Building a, Building b) {
        if (a == b || a.team != b.team || !isParticipant(a) || !isParticipant(b)) return false;
        if (isDeposit(a) && isDeposit(b)) return false;

        float rangeA = range(a);
        float rangeB = range(b);
        if (rangeA <= 0f || rangeB <= 0f) return false;

        return Intersector.overlaps(Tmp.cr1.set(a.x, a.y, rangeA), b.tile.getHitbox(Tmp.r1))
                || Intersector.overlaps(Tmp.cr1.set(b.x, b.y, rangeB), a.tile.getHitbox(Tmp.r1));
    }

    private static void rebuild() {
        for (CargoDeliveryGraph graph : graphs) {
            if (graph != null) graph.clearData();
        }

        for (Building build : Groups.build) {
            if (!isParticipant(build)) continue;
            get(build.team).add(build);
        }

        for (CargoDeliveryGraph graph : graphs) {
            if (graph != null) graph.connect();
        }
    }

    private void clearData() {
        relays.clear();
        deposits.clear();
        adjacency.clear();
        componentCache.clear();
        pathCache.clear();
    }

    private void add(Building build) {
        if (isRelay(build)) relays.add(build);
        if (build instanceof CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit) deposits.add(deposit);
    }

    private void connect() {
        for (int i = 0; i < relays.size; i++) {
            for (int j = i + 1; j < relays.size; j++) {
                Building a = relays.get(i);
                Building b = relays.get(j);
                if (connected(a, b)) link(a, b);
            }
        }

        for (CargoDeliveryDeposit.CargoDeliveryDepositBuild deposit : deposits) {
            for (Building relay : relays) {
                if (connected(deposit, relay)) link(deposit, relay);
            }
        }
    }

    private void link(Building a, Building b) {
        Seq<Building> aLinks = adjacency.get(a);
        if (aLinks == null) adjacency.put(a, aLinks = new Seq<>());
        aLinks.addUnique(b);

        Seq<Building> bLinks = adjacency.get(b);
        if (bLinks == null) adjacency.put(b, bLinks = new Seq<>());
        bLinks.addUnique(a);
    }

    public PathResult distances(Building from) {
        if (from == null || !isParticipant(from)) return new PathResult();

        PathResult cached = pathCache.get(from);
        if (cached != null) return cached;

        PathResult result = new PathResult();
        ObjectSet<Building> visited = new ObjectSet<>();
        result.dist.put(from, 0f);

        while (true) {
            Building best = null;
            float bestDist = Float.MAX_VALUE;

            for (ObjectMap.Entry<Building, Float> entry : result.dist) {
                if (visited.contains(entry.key)) continue;

                float dist = entry.value;
                if (dist < bestDist || (Mathf.equal(dist, bestDist, 0.0001f)
                        && (best == null || entry.key.id < best.id))) {
                    best = entry.key;
                    bestDist = dist;
                }
            }

            if (best == null) break;
            visited.add(best);

            if (isDeposit(best) && best != from) continue;

            Seq<Building> links = adjacency.get(best);
            if (links == null) continue;

            for (Building next : links) {
                if (visited.contains(next)) continue;

                float nextDist = bestDist + best.dst(next);
                Float old = result.dist.get(next);
                if (old == null || nextDist < old) {
                    result.dist.put(next, nextDist);
                    result.prev.put(next, best);
                }
            }
        }

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

    public Seq<Building> component(Building focus) {
        if (focus == null) return new Seq<>();

        Seq<Building> cached = componentCache.get(focus, () -> new Seq<>());
        if (cached.size > 0) return cached;
        cached.clear();

        ObjectSet<Building> visited = new ObjectSet<>();
        Queue<Building> queue = new Queue<>();
        queue.addLast(focus);
        visited.add(focus);

        while (!queue.isEmpty()) {
            Building current = queue.removeFirst();
            cached.add(current);

            Seq<Building> links = adjacency.get(current);
            if (links == null) continue;

            for (Building next : links) {
                if (visited.add(next)) {
                    queue.addLast(next);
                }
            }
        }

        return cached;
    }

    public static class PathResult {
        public final ObjectMap<Building, Float> dist = new ObjectMap<>();
        public final ObjectMap<Building, Building> prev = new ObjectMap<>();

        public @Nullable Float distance(Building build) {
            return dist.get(build);
        }
    }
}
