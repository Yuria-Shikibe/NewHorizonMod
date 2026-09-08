package newhorizon.expand.game;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.Vars;

public final class InterventionState {
    public static final String TAG = "nh-intervention-scale";

    private static float scale = 1f;

    private InterventionState() {
    }

    public static void init() {
        float fromSetting = defaultScale();

        if (Vars.state.rules.tags.containsKey(TAG)) {
            scale = readTag();
        } else {
            scale = fromSetting;
        }

        if (!Vars.net.active()) {
            if (fromSetting <= 0f) {
                scale = 0f;
            } else if (scale <= 0f) {
                scale = fromSetting;
            }
        }

        writeTag();
    }

    private static float defaultScale() {
        return NHDefaultEventSettings.enabledForCurrentGame() ? 1f : 0f;
    }

    public static float scale() {
        return scale;
    }

    public static boolean enabled() {
        return scale > 0.001f;
    }

    public static void setScale(float value) {
        scale = Mathf.clamp(value, 0f, 1f);
        writeTag();
    }

    private static float readTag() {
        return Mathf.clamp(Strings.parseFloat(Vars.state.rules.tags.get(TAG, "1"), 1f), 0f, 1f);
    }

    private static void writeTag() {
        Vars.state.rules.tags.put(TAG, Float.toString(scale));
    }
}
