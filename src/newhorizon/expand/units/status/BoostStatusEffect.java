package newhorizon.expand.units.status;

import arc.util.Tmp;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class BoostStatusEffect extends StatusEffect {
    public BoostStatusEffect(String name) {
        super(name);

        speedMultiplier = 4f;
        dragMultiplier = 0f;
    }

    @Override
    public void update(Unit unit, float time) {
        super.update(unit, time);
    }
}
