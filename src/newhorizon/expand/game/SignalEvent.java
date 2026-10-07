package newhorizon.expand.game;

import arc.func.Boolp;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Time;
import newhorizon.expand.logic.components.ui.HudMarkers;

import static mindustry.Vars.headless;

/** A lightweight, client-side world signal used by cutscenes and event logic. */
public class SignalEvent {
    private static final Seq<SignalEvent> active = new Seq<>();

    public float x, y;
    public float maxDistance = 1200f;
    public Color markColor = Color.lightGray;
    public Boolp removeCondition = () -> false;
    private float timer;
    private boolean removed;

    public SignalEvent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public SignalEvent maxDistance(float value) {
        maxDistance = value;
        return this;
    }

    public SignalEvent color(Color value) {
        markColor = value == null ? Color.lightGray : value;
        return this;
    }

    public SignalEvent removeWhen(Boolp condition) {
        removeCondition = condition == null ? () -> false : condition;
        return this;
    }

    public SignalEvent add() {
        if (!headless && !active.contains(this, true)) active.add(this);
        return this;
    }

    public void remove() {
        removed = true;
        active.remove(this, true);
    }

    public boolean removed() {
        return removed;
    }

    public void update() {
        if (removed) return;
        timer += Time.delta;
        if (timer >= 180f) {
            timer %= 180f;
            HudMarkers.forceMarkSignal(x, y, maxDistance, markColor);
        }
        if (removeCondition.get()) remove();
    }

    public static void updateAll() {
        for (int i = active.size - 1; i >= 0; i--) active.get(i).update();
    }

    public static void clear() {
        active.clear();
    }
}
