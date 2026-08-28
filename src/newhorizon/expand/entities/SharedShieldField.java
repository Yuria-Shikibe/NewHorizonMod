package newhorizon.expand.entities;

import arc.math.geom.Intersector;
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
            if (Intersector.isInRegularPolygon(((QuantumVortexProjector)build.block).sides, build.x, build.y,
                    build.realRadius(), ((QuantumVortexProjector)build.block).shieldRotation, x, y)) return true;
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
            float speed = recovery <= 0.001f ? 1f : Math.max(liquidRate / recovery, 1f);
            float rawDuration = brokenDurationSeconds(currentCapacity);
            boolean hasLiquid = liquidRate > recovery + 0.001f;
            // The 20..60 second rule applies to an uncooled field only. Coolant
            // is allowed to shorten the cooldown below 20s or, for very large
            // fields, leave it above 60s; partial coolant coverage scales the
            // all-cooled theoretical duration by its aggregate rate ratio.
            float duration = hasLiquid ? rawDuration / speed : arc.math.Mathf.clamp(rawDuration, 20f, 60f);
            cooldownTimer += Time.delta;
            if (buildup > 0f) buildup = Math.max(buildup - Time.delta * liquidRate, 0f);
            if (cooldownTimer >= duration) {
                broken = false;
                cooldownTimer = 0f;
                buildup = 0f;
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
        float sourceRadius = connectionRadius(source);
        if (sourceRadius <= 0f) return false;
        for (int i = 0; i < sources.size; i++) {
            Building other = sources.get(i);
            float otherRadius = connectionRadius(other);
            float combined = sourceRadius + otherRadius;
            if (otherRadius > 0f && other.dst2(source) <= combined * combined + 0.01f) return true;
        }
        return false;
    }

    /** Fast broad-phase connection test for two stationary projectors. */
    public static boolean projectorsOverlap(Building a, Building b) {
        if (a == null || b == null || a.team == null || a.team != b.team) return false;
        float radiusA = connectionRadius(a);
        float radiusB = connectionRadius(b);
        if (radiusA <= 0f || radiusB <= 0f) return false;
        float combined = radiusA + radiusB;
        return a.dst2(b) <= combined * combined + 0.01f;
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
