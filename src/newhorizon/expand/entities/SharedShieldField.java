package newhorizon.expand.entities;

import arc.math.geom.Intersector;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import newhorizon.expand.block.defence.QuantumVortexProjector;
import mindustry.gen.Building;
import mindustry.game.Team;

import java.util.Iterator;

public class SharedShieldField {
    private static final Seq<Building> tmpBuildings = new Seq<>(false, 16, Building.class);

    public float buildup;
    public boolean broken = false;
    public transient float radscl, warmup, hit;
    private float cooldownTimer;
    /** Sources are unbounded; fields can contain any number of projectors. */
    private final Seq<Building> sources = new Seq<>(false, 8, Building.class);

    public void add(Building source) {
        if (source != null && !sources.contains(source, true)) {
            sources.add(source);
            SharedShieldFields.markDirty();
        }
    }

    public void remove(Building source) {
        if (sources.remove(source, true)) SharedShieldFields.markDirty();
    }

    public boolean active() {
        if (broken || sources.isEmpty()) return false;
        for (int i = 0; i < sources.size; i++) {
            if (sources.get(i).efficiency > 0.01f) return true;
        }
        return false;
    }

    public boolean hasSource(Building source) {
        return sources.contains(source, true);
    }

    public int indexOf(Building source) {
        return sources.indexOf(source, true);
    }

    public int sourceCount() {
        return sources.size;
    }

    public float maxRadius() {
        float radius = 0f;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (source.block instanceof QuantumVortexProjector p && source.efficiency > 0.01f) {
                radius = Math.max(radius, p.realRadius((QuantumVortexProjector.QuantumBuild)source));
            }
        }
        // realRadius already includes the source warmup scale. Applying the
        // shared scale a second time makes the bullet query shrink twice while
        // the rendered polygon only shrinks once.
        return radius;
    }

    public boolean contains(float x, float y) {
        float radius = maxRadius();
        if (radius <= 0f) return false;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!(source.block instanceof QuantumVortexProjector)) continue;
            QuantumVortexProjector.QuantumBuild build = (QuantumVortexProjector.QuantumBuild)source;
            QuantumVortexProjector projector = (QuantumVortexProjector)build.block;
            if (Intersector.isInRegularPolygon(projector.sides, build.x, build.y,
                    projector.realRadius(build), projector.shieldRotation, x, y)) return true;
        }
        return false;
    }

    public void update() {
        cleanupSources();
        if (sources.isEmpty()) {
            remove();
            return;
        }

        float targetWarmup = 0f;
        for (int i = 0; i < sources.size; i++) {
            if (sources.get(i).efficiency > 0.01f) {
                targetWarmup = 1f;
                break;
            }
        }

        radscl = Time.delta <= 0 ? radscl : arc.math.Mathf.lerpDelta(radscl, broken ? 0f : targetWarmup, 0.05f);
        warmup = arc.math.Mathf.lerpDelta(warmup, targetWarmup, 0.1f);
        hit = Math.max(hit - Time.delta / 5f, 0f);

        float recovery = normalRecoveryRate();
        float currentCapacity = capacity();

        // The shared field owns all shield state.  Keep the inherited ForceBuild
        // buildup/broken values out of this calculation; each source contributes
        // its rate to the aggregate field instead.
        if (broken) {
            float liquidRate = recoveryWithLiquid();
            cooldownTimer += Time.delta;
            if (buildup > 0f) buildup = Math.max(buildup - Time.delta * liquidRate, 0f);
            if (cooldownTimer >= cooldownDurationTicks()) {
                broken = false;
                cooldownTimer = 0f;
                buildup = 0f;
                // The field was visually collapsed while broken. Restore its
                // active scale immediately on the recovery tick; otherwise a
                // stale zero scale can leave a fully recharged shield hidden
                // until another topology or warmup transition occurs.
                radscl = targetWarmup;
                warmup = targetWarmup;
            }
        } else {
            cooldownTimer = 0f;
            if (buildup > 0f) buildup = Math.max(buildup - Time.delta * recovery, 0f);
        }

        if (!broken && buildup >= currentCapacity) {
            broken = true;
            buildup = currentCapacity;
            cooldownTimer = 0f;
        }

        // A healthy field must never remain visually collapsed after a broken
        // transition or topology rebuild.
        if (!broken && targetWarmup > 0f && radscl < 0.999f) radscl = targetWarmup;
    }

    public float capacity() {
        float total = 0f;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!(source.block instanceof QuantumVortexProjector block)) continue;
            QuantumVortexProjector.QuantumBuild build = (QuantumVortexProjector.QuantumBuild)source;
            total += block.shieldHealth + block.phaseShieldBoost * arc.math.Mathf.clamp(build.phaseHeat);
        }
        return total;
    }

    /** Aggregate normal recovery rate (points per tick) from powered sources. */
    public float normalRecoveryRate() {
        float total = 0f;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (source.block instanceof QuantumVortexProjector block) {
                // ForceProjector's regeneration is a fixed per-building rate;
                // efficiency gates interception, but does not scale the rate.
                total += block.cooldownNormal;
            }
        }
        return total;
    }

    /** Recovery rate after applying coolant to each source independently. */
    public float recoveryWithLiquid() {
        float total = 0f;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!(source.block instanceof QuantumVortexProjector block)) continue;
            float rate = block.cooldownNormal;
            if (block.coolantConsumer != null && block.coolantConsumer.efficiency(source) > 0.01f) {
                rate *= block.cooldownLiquid > 0f ? block.cooldownLiquid : 1f;
            }
            total += rate;
        }
        return total;
    }

    /** Progress of the current broken cooldown, in simulation ticks. */
    public float cooldownProgress() {
        return cooldownTimer;
    }

    /** Elapsed cooldown as a normalized value for the building bar. */
    public float cooldownProgressRatio() {
        if (!broken) return 0f;
        float duration = cooldownDurationTicks();
        return duration <= 0.001f ? 0f : Mathf.clamp(cooldownTimer / duration);
    }

    /** Current broken duration in simulation ticks, including coolant effects. */
    private float cooldownDurationTicks() {
        float recovery = normalRecoveryRate();
        float liquidRate = recoveryWithLiquid();
        float speed = recovery <= 0.001f ? 1f : Math.max(liquidRate / recovery, 1f);
        float rawDuration = brokenDurationSeconds(capacity());
        boolean hasLiquid = liquidRate > recovery + 0.001f;
        // Without coolant, enforce the 20..60 second range. Coolant scales the
        // theoretical duration by aggregate per-projector coverage and may go
        // outside that range as designed.
        float duration = hasLiquid ? rawDuration / speed : Mathf.clamp(rawDuration, 20f, 60f);
        return Math.max(duration * 60f, 1f);
    }

    public void setCooldownProgress(float progress) {
        cooldownTimer = Math.max(progress, 0f);
    }

    /** Unclamped no-liquid break duration in seconds, proportional to capacity. */
    private float brokenDurationSeconds(float currentCapacity) {
        QuantumVortexProjector block = firstBlock();
        if (block == null || block.shieldHealth <= 0f) return 20f;
        return currentCapacity / (Math.max(block.cooldownBrokenBase, 0.001f) * 60f);
    }

    public void damage(float amount, float hitX, float hitY) {
        if (broken || amount <= 0f) return;
        buildup += amount;
        hit = 1f;
        VortexEvent.add(hitX, hitY, this);
    }

    public void remove() {
        SharedShieldFields.remove(this);
    }

    /** Returns this field's team, or null when it has no valid source. */
    public Team team() {
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (source != null && source.isValid() && source.isAdded()) return source.team;
        }
        return null;
    }

    public boolean sameTeam(Building source) {
        Team owner = team();
        return owner != null && source != null && owner == source.team;
    }

    /**
     * Connection range deliberately ignores warmup/radscl and phase-fabric range effects.
     * A projector contributes its configured shield radius to grouping immediately on placement.
     */
    private static float connectionRadius(Building source) {
        if (!(source.block instanceof QuantumVortexProjector projector)) return 0f;
        return Math.max(projector.radius, 0f);
    }

    public boolean overlaps(Building source) {
        if (!sameTeam(source)) return false;
        for (int i = 0; i < sources.size; i++) {
            Building other = sources.get(i);
            if (projectorsOverlap(other, source)) return true;
        }

        return false;
    }

    /** Fast broad-phase connection test for two stationary projectors. */
    public static boolean projectorsOverlap(Building a, Building b) {
        if (a == null || b == null || a.team == null || a.team != b.team) return false;
        if (!(a.block instanceof QuantumVortexProjector pa) || !(b.block instanceof QuantumVortexProjector pb)) return false;

        float radiusA = connectionRadius(a), radiusB = connectionRadius(b);
        if (radiusA <= 0f || radiusB <= 0f) return false;

        // Cheap circumscribed-circle rejection keeps the exact SAT pass small
        // when many projectors are present. The circle is only a broad phase;
        // the polygon test below remains authoritative.
        float broadRadius = (radiusA + radiusB) * 1.41421356f;
        if (a.dst2(b) > broadRadius * broadRadius + 0.01f) return false;

        // Use the actual convex shield polygons rather than a circumscribed
        // circle. Circle broad-phase tests incorrectly join projectors whose
        // square corners are near each other while their shield areas do not
        // overlap. SAT also handles containment and edge-touching correctly.
        int sidesA = Math.max(pa.sides, 3), sidesB = Math.max(pb.sides, 3);
        float[] vertsA = polygonVertices(a.x, a.y, radiusA, pa.shieldRotation, sidesA);
        float[] vertsB = polygonVertices(b.x, b.y, radiusB, pb.shieldRotation, sidesB);
        return overlapConvexPolygons(vertsA, vertsB, sidesA, sidesB);
    }

    private static float[] polygonVertices(float cx, float cy, float radius, float rotation, int sides) {
        float[] vertices = new float[sides * 2];
        for (int i = 0; i < sides; i++) {
            float angle = rotation + i * 360f / sides;
            vertices[i * 2] = cx + Mathf.cosDeg(angle) * radius;
            vertices[i * 2 + 1] = cy + Mathf.sinDeg(angle) * radius;
        }
        return vertices;
    }

    private static boolean overlapConvexPolygons(float[] a, float[] b, int sidesA, int sidesB) {
        return separatesOnAnyAxis(a, sidesA, b, sidesB) == false && separatesOnAnyAxis(b, sidesB, a, sidesA) == false;
    }

    private static boolean separatesOnAnyAxis(float[] axisPolygon, int axisSides, float[] otherPolygon, int otherSides) {
        for (int i = 0; i < axisSides; i++) {
            int j = (i + 1) % axisSides;
            float ex = axisPolygon[j * 2] - axisPolygon[i * 2];
            float ey = axisPolygon[j * 2 + 1] - axisPolygon[i * 2 + 1];
            float nx = -ey, ny = ex;

            float minA = Float.POSITIVE_INFINITY, maxA = Float.NEGATIVE_INFINITY;
            for (int k = 0; k < axisSides; k++) {
                float projection = axisPolygon[k * 2] * nx + axisPolygon[k * 2 + 1] * ny;
                minA = Math.min(minA, projection);
                maxA = Math.max(maxA, projection);
            }

            float minB = Float.POSITIVE_INFINITY, maxB = Float.NEGATIVE_INFINITY;
            for (int k = 0; k < otherSides; k++) {
                float projection = otherPolygon[k * 2] * nx + otherPolygon[k * 2 + 1] * ny;
                minB = Math.min(minB, projection);
                maxB = Math.max(maxB, projection);
            }

            // Keep touching polygons connected: a shared boundary counts as
            // overlap for shield networking.
            if (maxA < minB - 0.001f || maxB < minA - 0.001f) return true;
        }
        return false;
    }

    private void cleanupSources() {
        tmpBuildings.clear();
        for (Iterator<Building> iterator = iterator(); iterator.hasNext(); ) {
            Building building = iterator.next();
            if (building.isValid() && building.isAdded()) tmpBuildings.add(building);
        }
        clear();
        sources.addAll(tmpBuildings);
    }

    public Iterable<Building> iterable() {
        return this::iterator;
    }

    public Iterator<Building> iterator() {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < sources.size;
            }

            @Override
            public Building next() {
                return sources.get(index++);
            }
        };
    }

    public void clear() {
        sources.clear();
    }

    public boolean empty() {
        return sources.isEmpty();
    }

    private QuantumVortexProjector firstBlock() {
        for (int i = 0; i < sources.size; i++) {
            if (sources.get(i).block instanceof QuantumVortexProjector p) return p;
        }
        return null;
    }
}
