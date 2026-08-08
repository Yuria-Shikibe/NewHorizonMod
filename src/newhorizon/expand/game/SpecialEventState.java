package newhorizon.expand.game;

import mindustry.Vars;
import newhorizon.NHSetting;

/** Per-world switch for automatic default special events. */
public final class SpecialEventState {
    public static final String TAG = "nh-special-event-enabled";

    private static boolean enabled = true;

    private SpecialEventState() {
    }

    public static void init() {
        boolean fromSetting = defaultEnabled();

        if (Vars.state.rules.tags.containsKey(TAG)) {
            enabled = Boolean.parseBoolean(Vars.state.rules.tags.get(TAG));
        } else {
            enabled = fromSetting;
        }

        if (!Vars.net.active()) {
            if (!fromSetting) {
                enabled = false;
            } else if (!enabled) {
                enabled = true;
            }
        }

        writeTag();
    }

    private static boolean defaultEnabled() {
        return Vars.headless || NHSetting.getBool(NHSetting.EVENT_SPECIAL);
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
