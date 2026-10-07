package newhorizon.expand.logic.components.ui;

import arc.func.Boolp;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Interval;
import arc.util.Tmp;
import newhorizon.content.NHContent;
import newhorizon.content.NHSounds;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutsceneUI;

/** Client-side entry points for world-anchored HUD markers. */
public final class HudMarkers {
    private static final Interval signalInterval = new Interval();

    private HudMarkers() {
    }

    public static HudMarker mark(float x, float y, float radius, float lifetime,
                                 Color color, Boolp removeCheck) {
        return mark(x, y, radius, lifetime, color, MarkStyle.defaultStyle, removeCheck);
    }

    public static HudMarker mark(float x, float y, float radius, float lifetime,
                                 Color color, MarkStyle style, Boolp removeCheck) {
        if (headless || cutsceneUI == null) return null;
        HudMarker marker = new HudMarker()
                .setMarkPosition(x, y)
                .setRadius(radius)
                .setDuration(lifetime)
                .setMarkColor(color == null ? Color.white : color)
                .setStyle(style)
                .setIcon(NHContent.objective)
                .setRemoveCheck(removeCheck);
        marker.addMarker();
        return marker;
    }

    public static void markSignal(float x, float y, float maxDistance, Color color) {
        if (!signalInterval.get(60f)) return;
        forceMarkSignal(x, y, maxDistance, color);
    }

    public static void forceMarkSignal(float x, float y, float maxDistance, Color color) {
        if (headless || player == null || maxDistance <= 0f) return;
        float distance = player.dst(x, y);
        if (distance > maxDistance) return;

        float scale = Mathf.clamp(distance / maxDistance);
        Tmp.v1.setToRandomDirection().scl(scale * 320f + 8f);
        if (NHSounds.uiSignal != null) NHSounds.uiSignal.at(x, y, 1f, 0.55f);
        mark(x + Tmp.v1.x, y + Tmp.v1.y, 9f, 45f,
                color == null ? Color.lightGray : color,
                MarkStyle.signalShake, () -> false);
    }
}
