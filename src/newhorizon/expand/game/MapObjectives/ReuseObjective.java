package newhorizon.expand.game.MapObjectives;

import arc.util.Time;
import mindustry.game.MapObjectives;

import static mindustry.Vars.state;

public class ReuseObjective extends MapObjectives.MapObjective {
    public @MapObjectives.Second float duration = 60f * 30f;
    public String trigger = "trigger";
    public String executor = "executor";

    protected float countup;

    //always update in loop if executor not exist
    @Override
    public boolean update() {
        if (countup <= duration) {
            countup += Time.delta;
        } else {
            countup %= duration;
            state.rules.objectiveFlags.addAll(executor);
        }
        return false;
    }

    public float getCountup() {
        return countup;
    }

    //the only situation when update is contain the specific flag
    @Override
    public boolean qualified() {
        return state.rules.objectiveFlags.contains(trigger);
    }
}
