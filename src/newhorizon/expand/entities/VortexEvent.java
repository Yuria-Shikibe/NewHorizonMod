package newhorizon.expand.entities;

import arc.math.Angles;
import arc.util.Time;

public class VortexEvent {
    public float x, y;
    public float angle;
    public float time;
    public int id;
    /** Shared shield group that owns this hit. Null means unscoped/legacy event. */
    public SharedShieldField field;

    public static final VortexEvent[] active = new VortexEvent[24];
    private static final VortexEvent[] pool = new VortexEvent[32];
    private static int poolSize;

    private static int version;

    public static int version() {
        return version;
    }

    public static void add(float x, float y) {
        add(x, y, Angles.angle(0f, 0f, x % 97f - 48.5f, y % 97f - 48.5f), null);
    }

    public static void add(float x, float y, float angle) {
        add(x, y, angle, null);
    }

    /** Adds a hit event owned by one shared shield field. */
    public static void add(float x, float y, SharedShieldField field) {
        add(x, y, Angles.angle(0f, 0f, x % 97f - 48.5f, y % 97f - 48.5f), field);
    }

    public static void add(float x, float y, float angle, SharedShieldField field) {
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
        event.field = field;
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
        event.field = null;
        if (poolSize < pool.length) pool[poolSize++] = event;
    }

    private static int nextId;
}
