package newhorizon.expand.entities;

import arc.math.Angles;
import arc.util.Time;

public class VortexEvent {
    public float x, y;
    public float angle;
    public float time;
    public int id;

    public static final VortexEvent[] active = new VortexEvent[24];
    private static final VortexEvent[] pool = new VortexEvent[32];
    private static int poolSize;

    private static int version;

    public static int version() {
        return version;
    }

    public static void add(float x, float y) {
        add(x, y, Angles.angle(0f, 0f, x % 97f - 48.5f, y % 97f - 48.5f));
    }

    public static void add(float x, float y, float angle) {
        int slot = -1;
        for (int i = 0; i < active.length; i++) {
            if (active[i] == null) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;

        VortexEvent event = poolSize > 0 ? pool[--poolSize] : new VortexEvent();
        event.x = x;
        event.y = y;
        event.angle = angle;
        event.id = nextId++;
        event.time = 0f;
        active[slot] = event;
        version++;
    }

    public static void update() {
        for (int i = 0; i < active.length; i++) {
            VortexEvent event = active[i];
            if (event == null) continue;
            event.time += Time.delta / 90f;
            if (event.time >= 1f) {
                active[i] = null;
                release(event);
                version++;
            }
        }
    }

    public static void clear() {
        for (int i = 0; i < active.length; i++) {
            if (active[i] != null) release(active[i]);
            active[i] = null;
        }
    }

    private static void release(VortexEvent event) {
        if (poolSize < pool.length) pool[poolSize++] = event;
    }

    private static int nextId;
}
