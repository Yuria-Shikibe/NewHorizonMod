package newhorizon.expand.ability.passive;

import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

public class AccumulateAccelerate extends Ability {
    public float reloadMultiplier = 1f;
    public float maxMultiplier = 5f;
    public float increasePerTick = 0.2f / 60f;
    public float decreasePerTick = 2f / 60f;

    @Override
    public void update(Unit unit) {
        super.update(unit);
        float increment = (unit.isShooting? increasePerTick: -decreasePerTick) * Time.delta;
        reloadMultiplier = Mathf.clamp(reloadMultiplier + increment, 1, maxMultiplier);
        unit.reloadMultiplier *= reloadMultiplier;
    }
}
