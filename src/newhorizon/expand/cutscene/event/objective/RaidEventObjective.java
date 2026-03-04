package newhorizon.expand.cutscene.event.objective;

import arc.util.Time;
import mindustry.game.MapObjectives;

import java.util.Objects;

import static mindustry.Vars.state;

public class RaidEventObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 10f;
    public String key = "raid-event";

    protected boolean triggered = false;
    protected float countup;

    public RaidEventObjective(String key){
        this.key = key;
    }

    public RaidEventObjective(){
    }

    @Override
    public boolean update(){
        if(countup <= duration){
            countup += Time.delta;
        }else{
            triggered = false;
        }
        return false;
    }

    public void trigger(float duration){
        this.duration = Math.max(1f, duration);
        countup = 0f;
        triggered = true;
    }

    public void finish(){
        countup = duration;
        triggered = false;
    }

    public float getCountup(){
        return countup;
    }

    @Override
    public boolean qualified(){
        return triggered;
    }

    public static RaidEventObjective obtain(String key){
        final RaidEventObjective[] objective = {null};

        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof RaidEventObjective raidObj && Objects.equals(raidObj.key, key)){
                objective[0] = raidObj;
            }
        });

        if(objective[0] == null){
            objective[0] = new RaidEventObjective(key);
            state.rules.objectives.all.add(objective[0]);
        }

        return objective[0];
    }
}
