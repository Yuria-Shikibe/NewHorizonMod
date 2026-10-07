package newhorizon.expand.entities;

import arc.math.geom.Intersector;
import arc.math.geom.Rect;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.entities.Effect;
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
    private transient boolean visualActive;
    private transient Team cachedTeam;
    private transient boolean teamCacheValid;
    /** Sources are unbounded; fields can contain any number of projectors. */
    private final Seq<Building> sources = new Seq<>(false, 8, Building.class);

    public void add(Building source) {
        // Keep the source list itself powered-only. Callers also filter during
        // topology rebuilds, but enforcing it here prevents future paths from
        // accidentally granting an unpowered projector shared-shield effects.
        if (isPowered(source) && !sources.contains(source, true)) {
            sources.add(source);
            teamCacheValid = false;
            SharedShieldFields.markDirty();
        }
    }

    public void remove(Building source) {
        if (sources.remove(source, true)) {
            teamCacheValid = false;
            SharedShieldFields.markDirty();
        }
    }

    public boolean active() {
        if (broken || sources.isEmpty()) return false;
        for (int i = 0; i < sources.size; i++) {
            if (isPowered(sources.get(i))) return true;
        }
        return false;
    }

    public boolean hasSource(Building source) {
        return sources.contains(source, true);
    }

    /** Fast per-source validity check used from every projector update. */
    public boolean isValidSource(Building source) {
        if (!isPowered(source) || !hasSource(source) || source.team == null) return false;
        if (!teamCacheValid) refreshTeamCache();
        // The cached team is refreshed once per field update. Checking the
        // first source as well catches the common team-change case without
        // re-scanning the complete source list for every projector.
        return cachedTeam != null && cachedTeam == source.team && !sources.isEmpty()
                && sources.first().team == cachedTeam;
    }

    public int indexOf(Building source) {
        return sources.indexOf(source, true);
    }

    /** The first source is the field's single bullet-scan owner. */
    public boolean isPrimarySource(Building source) {
        return !sources.isEmpty() && sources.first() == source;
    }

    public int sourceCount() {
        int count = 0;
        for (int i = 0; i < sources.size; i++) {
            if (isPowered(sources.get(i))) count++;
        }
        return count;
    }

    public float maxRadius() {
        float radius = 0f;
        float scale = sharedScale();
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (isPowered(source) && source.block instanceof QuantumVortexProjector p) {
                radius = Math.max(radius, p.radius * scale);
            }
        }
        // realRadius already includes the source warmup scale. Applying the
        // shared scale a second time makes the bullet query shrink twice while
        // the rendered polygon only shrinks once.
        return radius;
    }

    /** Broad-phase bounds for one bullet query covering every source polygon. */
    public void bulletBounds(Rect out) {
        if (out == null) return;
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!isPowered(source) || !(source instanceof QuantumVortexProjector.QuantumBuild build)
                    || !(source.block instanceof QuantumVortexProjector projector)) continue;
            float radius = Math.max(projector.radius * sharedScale(), 0f);
            minX = Math.min(minX, source.x - radius);
            minY = Math.min(minY, source.y - radius);
            maxX = Math.max(maxX, source.x + radius);
            maxY = Math.max(maxY, source.y + radius);
        }
        if (minX == Float.POSITIVE_INFINITY) {
            out.set(0f, 0f, 0f, 0f);
        } else {
            out.set(minX, minY, maxX - minX, maxY - minY);
        }
    }

    public boolean contains(float x, float y) {
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!isPowered(source) || !(source.block instanceof QuantumVortexProjector)) continue;
            QuantumVortexProjector.QuantumBuild build = (QuantumVortexProjector.QuantumBuild)source;
            QuantumVortexProjector projector = (QuantumVortexProjector)build.block;
            float radius = projector.radius * sharedScale();
            if (radius <= 0f) continue;
            if (Intersector.isInRegularPolygon(projector.sides, build.x, build.y,
                    radius, projector.shieldRotation, x, y)) return true;
        }
        return false;
    }

    public void update() {
        cleanupSources();
        refreshTeamCache();
        if (sources.isEmpty()) {
            remove();
            return;
        }

        float targetWarmup = 0f;
        for (int i = 0; i < sources.size; i++) {
            if (isPowered(sources.get(i))) {
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

        boolean activeNow = !broken && targetWarmup > 0f && radscl > 0.01f;
        if (activeNow && !visualActive) triggerVisualEffect(true);
        else if (visualActive && broken) triggerVisualEffect(false);
        visualActive = activeNow;

        // A healthy field must never remain visually collapsed after a broken
        // transition or topology rebuild.
        if (!broken && targetWarmup > 0f && radscl < 0.999f) radscl = targetWarmup;
    }

    private void triggerVisualEffect(boolean activating) {
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!(source.block instanceof QuantumVortexProjector projector)) continue;

            Effect effect = activating ? projector.shieldActivateEffect : projector.shieldBreakEffect;
            if (effect == null) continue;

            // Effect.rotation carries the polygon radius, matching the
            // ForceProjector shield-break convention. The configured radius
            // is used on activation so the pulse is visible even while the
            // shared warmup scale is still approaching one.
            float effectRadius = activating
                    ? projector.radius
                    : projector.radius * Math.max(Math.max(radscl, warmup), 0.001f);
            effect.at(source.x, source.y, effectRadius, source.team.color);
        }
    }

    public float capacity() {
        float total = 0f;
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (!isPowered(source) || !(source.block instanceof QuantumVortexProjector block)) continue;
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
            if (isPowered(source) && source.block instanceof QuantumVortexProjector block) {
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
            if (!isPowered(source) || !(source.block instanceof QuantumVortexProjector block)) continue;
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
            if (isPowered(source) && source.isValid() && source.isAdded()) return source.team;
        }
        return null;
    }

    public boolean sameTeam(Building source) {
        if (!isPowered(source) || source.team == null) return false;

        // A field may outlive a source's team assignment by one update tick.
        // Validate every remaining source in topology operations so a stale
        // cross-team member cannot bridge two fields.
        boolean found = false;
        for (int i = 0; i < sources.size; i++) {
            Building existing = sources.get(i);
            if (existing == null || !existing.isValid() || !existing.isAdded()) continue;
            found = true;
            if (existing.team != source.team) return false;
        }
        return found;
    }

    /** Rebuild the field team cache once per field update instead of scanning
     * every source for every projector's per-tick update/draw call. */
    private void refreshTeamCache() {
        Team team = null;
        for (int i = 0; i < sources.size; i++) {
            Building existing = sources.get(i);
            if (existing == null || !existing.isValid() || !existing.isAdded() || !isPowered(existing)) continue;
            if (team == null) team = existing.team;
            else if (existing.team != team) {
                cachedTeam = null;
                teamCacheValid = true;
                return;
            }
        }
        cachedTeam = team;
        teamCacheValid = true;
    }

    /**
     * Connection range deliberately ignores warmup/radscl and phase-fabric range effects.
     * A powered projector contributes its configured shield radius to grouping.
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

    private float sharedScale() {
        return broken ? 0f : Mathf.clamp(Math.max(radscl, warmup));
    }

    /** Fast broad-phase connection test for two stationary projectors. */
    public static boolean projectorsOverlap(Building a, Building b) {
        if (!isPowered(a) || !isPowered(b) || a.team == null || a.team != b.team) return false;
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
        boolean changed = false;
        for (Iterator<Building> iterator = iterator(); iterator.hasNext(); ) {
            Building building = iterator.next();
            if (building.isValid() && building.isAdded() && isPowered(building)) {
                tmpBuildings.add(building);
            } else {
                changed = true;
                if (building instanceof QuantumVortexProjector.QuantumBuild quantum && quantum.field == this) {
                    quantum.field = null;
                }
            }
        }
        clear();
        sources.addAll(tmpBuildings);
        if (changed) SharedShieldFields.markDirty();
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
        cachedTeam = null;
        teamCacheValid = false;
    }

    public boolean empty() {
        return sources.isEmpty();
    }

    private QuantumVortexProjector firstBlock() {
        for (int i = 0; i < sources.size; i++) {
            Building source = sources.get(i);
            if (isPowered(source) && source.block instanceof QuantumVortexProjector p) return p;
        }
        return null;
    }

    private static boolean isPowered(Building source) {
        return source != null && source.efficiency >= QuantumVortexProjector.shieldActivationEfficiency;
    }
}
