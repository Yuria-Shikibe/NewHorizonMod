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
            VortexEvent existing = active[i];
            if (existing != null && existing.field == field && existing.time < 0.12f) {
                // Collapse bursts from rapid-fire projectiles into one smooth
                // wave per field instead of filling all event slots at once.
                return;
            }
            if (existing == null) {
                slot = i;
                break;
            }
        }
        if (slot < 0) {
            // Keep the newest hit visible when the global pool is saturated.
            // Replacing the oldest event avoids the visible stop/start pattern
            // caused by silently dropping every subsequent hit.
            float oldest = -1f;
            for (int i = 0; i < active.length; i++) {
                if (active[i] != null && active[i].time > oldest) {
                    oldest = active[i].time;
                    slot = i;
                }
            }
        }
        if (slot < 0) return;

        // Return the evicted event to the small object pool before reusing its
        // slot.  This also clears its field reference, preventing stale group
        // ownership from being retained across a saturated event stream.
        if (active[slot] != null) release(active[slot]);
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
