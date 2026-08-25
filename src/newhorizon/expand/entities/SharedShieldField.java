package newhorizon.expand.entities;

import arc.math.geom.Intersector;
import arc.util.Time;
import newhorizon.expand.block.defence.QuantumVortexProjector;
import mindustry.gen.Building;

import java.util.Iterator;

public class SharedShieldField {
    private static final Building[] tmpBuildings = new Building[64];
    private static int tmpBuildingCount;

    public float buildup;
    public boolean broken = false;
    public transient float radscl, warmup, hit;
    private float cooldownTimer;
    private final Building[] sources = new Building[64];
    private int sourceCount;

    public void add(Building source) {
        if (sourceCount < sources.length) sources[sourceCount++] = source;
    }

    public void remove(Building source) {
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i] == source) {
                System.arraycopy(sources, i + 1, sources, i, sourceCount - i - 1);
                sources[--sourceCount] = null;
                return;
            }
        }
    }

    public boolean active() {
        if (broken || sourceCount == 0) return false;
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i].efficiency > 0.01f) return true;
        }
        return false;
    }

    public boolean hasSource(Building source) {
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i] == source) return true;
        }
        return false;
    }

    public float maxRadius() {
        float radius = 0f;
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i].block instanceof QuantumVortexProjector p && sources[i].efficiency > 0.01f) {
                radius = Math.max(radius, p.realRadius((QuantumVortexProjector.QuantumBuild)sources[i]));
            }
        }
        return radius * radscl;
    }

    public boolean contains(float x, float y) {
        float radius = maxRadius();
        if (radius <= 0f) return false;
        for (int i = 0; i < sourceCount; i++) {
            if (!(sources[i].block instanceof QuantumVortexProjector)) continue;
            QuantumVortexProjector.QuantumBuild build = (QuantumVortexProjector.QuantumBuild)sources[i];
            if (Intersector.isInRegularPolygon(((QuantumVortexProjector)build.block).sides, build.x, build.y,
                    build.realRadius(), ((QuantumVortexProjector)build.block).shieldRotation, x, y)) return true;
        }
        return false;
    }

    public void update() {
        cleanupSources();
        if (sourceCount == 0) {
            remove();
            return;
        }

        float targetWarmup = 0f;
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i].efficiency > 0.01f) {
                targetWarmup = 1f;
                break;
            }
        }

        radscl = Time.delta <= 0 ? radscl : arc.math.Mathf.lerpDelta(radscl, broken ? 0f : targetWarmup, 0.05f);
        warmup = arc.math.Mathf.lerpDelta(warmup, targetWarmup, 0.1f);
        hit = Math.max(hit - Time.delta / 5f, 0f);

        QuantumVortexProjector block = firstBlock();
        float recovery = block == null ? 1f : block.cooldownNormal;

        if (buildup > 0f) buildup = Math.max(buildup - Time.delta * recovery, 0f);
        if (broken && buildup > 0f) {
            cooldownTimer += Time.delta;
            float threshold = block == null ? 300f : block.shieldHealth / block.cooldownBrokenBase;
            if (cooldownTimer >= threshold) {
                broken = false;
                cooldownTimer = 0f;
            }
        }else if(!broken){
            cooldownTimer = 0f;
        }

        if (buildup >= capacity()) {
            broken = true;
            buildup = capacity();
        }
    }

    public float capacity() {
        QuantumVortexProjector block = firstBlock();
        return block == null ? 100f : block.shieldHealth + block.phaseShieldBoost;
    }

    public void damage(float amount, float hitX, float hitY) {
        if (broken || amount <= 0f) return;
        buildup += amount;
        hit = 1f;
        VortexEvent.add(hitX, hitY);
    }

    public void remove() {
        SharedShieldFields.remove(this);
    }

    private void cleanupSources() {
        tmpBuildingCount = 0;
        for (Iterator<Building> iterator = iterator(); iterator.hasNext(); ) {
            Building building = iterator.next();
            if (!building.isValid() || !building.isAdded()) continue;
            if (tmpBuildings.length > tmpBuildingCount) tmpBuildings[tmpBuildingCount++] = building;
        }
        clear();
        for (int i = 0; i < tmpBuildingCount; i++) add(tmpBuildings[i]);
    }

    public Iterable<Building> iterable() {
        return this::iterator;
    }

    public Iterator<Building> iterator() {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < sourceCount;
            }

            @Override
            public Building next() {
                return sources[index++];
            }
        };
    }

    public void clear() {
        for (int i = 0; i < sourceCount; i++) sources[i] = null;
        sourceCount = 0;
    }

    public boolean empty() {
        return sourceCount == 0;
    }

    private QuantumVortexProjector firstBlock() {
        for (int i = 0; i < sourceCount; i++) {
            if (sources[i].block instanceof QuantumVortexProjector p) return p;
        }
        return null;
    }
}
