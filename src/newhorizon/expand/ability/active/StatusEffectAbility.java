package newhorizon.expand.ability.active;

import mindustry.content.StatusEffects;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;

public class StatusEffectAbility extends ActiveAbility{
    public StatusEffect statusEffect = StatusEffects.none;
    public float statusDuration = 15f;

    public StatusEffectAbility(StatusEffect statusEffect, float statusDuration) {
        this.statusEffect = statusEffect;
        this.statusDuration = statusDuration;
    }

    @Override
    public void trigger(Unit unit) {
        unit.apply(statusEffect, statusDuration);
    }
}
