package newhorizon.expand.game.MapObjectives;

import arc.util.Time;
import mindustry.game.MapObjectives;
import newhorizon.expand.game.MapMarker.RaidIndicator;

public class TriggerObjective extends MapObjectives.MapObjective {
    public @MapObjectives.Second float duration = 60f * 10f;
    public String timer = "event-timer";

    protected boolean triggered = false;
    protected float countup;

    public TriggerObjective(String timer) {
        this.timer = timer;

        markers = new MapObjectives.ObjectiveMarker[]{new RaidIndicator(timer)};
    }

    public TriggerObjective() {}

    @Override
    public boolean update() {
        if (countup <= duration) {
            countup += Time.delta;
        } else {
            triggered = false;
        }
        return false;
    }

    public void trigger(float duration) {
        this.duration = duration;
        countup = 0;

        triggered = true;
    }

    public float getCountup() {
        return countup;
    }

    @Override
    public boolean qualified() {
        return triggered;
    }
}
