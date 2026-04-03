package newhorizon.expand.logic.components;

import arc.math.Mathf;
import arc.util.Time;
import newhorizon.util.struct.TimeQueue;

public abstract class Action implements TimeQueue.Timed {
    public float lifeTimer = 0f;
    public float duration = 0f;

    public Action() {}

    public boolean skippable() {
        return true;
    }

    public String actionName() {
        return "Action";
    }

    public void parseTokens(String[] tokens) {}

    public void postInit() {}

    @Override
    public void update() {
        if (lifeTimer < duration) {
            lifeTimer += Time.delta;
            act();
        }
    }

    @Override
    public boolean complete() {return lifeTimer >= duration;}

    @Override
    public void begin(){}

    @Override
    public void end(){}

    public void act(){}

    public float progress() {
        if (duration <= 0f) return 0f;
        return Mathf.clamp(lifeTimer / duration, 0f, 1f);
    }
}
