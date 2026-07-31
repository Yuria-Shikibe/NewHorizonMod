package newhorizon.expand.game;

import mindustry.Vars;

/** Per-world switch for automatic default special events. */
public final class SpecialEventState {
    public static final String TAG = "nh-special-event-enabled";

    private static boolean enabled = true;

    private SpecialEventState() {
    }

    public static void init() {
        enabled = !Vars.state.rules.tags.containsKey(TAG)
                || Boolean.parseBoolean(Vars.state.rules.tags.get(TAG));
        writeTag();
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        writeTag();
    }

    private static void writeTag() {
        Vars.state.rules.tags.put(TAG, Boolean.toString(enabled));
    }
}
