package newhorizon.expand.ability.passive;

import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import newhorizon.content.NHStatusEffects;

//override vanilla's wave shield
public class PassiveShield extends Ability {
    public float shieldAmount;

    public PassiveShield(float shieldAmount) {
        this.shieldAmount = shieldAmount;
        display = false;
    }

    @Override
    public void update(Unit unit) {
        if (!unit.hasEffect(NHStatusEffects.shieldFlag)) {
            unit.shield(shieldAmount);
            unit.apply(NHStatusEffects.shieldFlag);
        }
    }
}
