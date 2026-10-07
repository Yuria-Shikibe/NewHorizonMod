package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.game.Gamemode;

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

        writeTag();
    }

    private static boolean defaultEnabled() {
        return NHDefaultEventSettings.enabledForCurrentGame();
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean defaultEventsExempt() {
        if (Vars.state == null || Vars.state.rules == null) return false;
        return Vars.state.rules.editor
                || Vars.state.rules.mode() == Gamemode.sandbox
                || Vars.state.rules.mode() == Gamemode.pvp;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        writeTag();
    }

    private static void writeTag() {
        Vars.state.rules.tags.put(TAG, Boolean.toString(enabled));
    }
}
