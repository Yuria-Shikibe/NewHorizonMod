package newhorizon.content;

import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;

public class NHStatusEffects{
    public static final StatusEffect
        staticVel = new StatusEffect("staticVel") {
        @Override
        public void update(Unit unit, float time){
            super.update(unit, time);
            unit.vel = unit.vel.scl(0.65f);
        }

        {
            this.color = Pal.gray;
            this.speedMultiplier = 0.00001F;
        }
    },
        emp1 = new StatusEffect("emp1"){{
            damage = 0.5f;
            effect = NHFx.emped;
            effectChance = 0.2f;
            reactive = false;
            speedMultiplier = 0.65f;
            reloadMultiplier = 0.65f;
            damageMultiplier = 0.75f;
        }},
    
        emp2 = new StatusEffect("emp2"){{
            damage = 1f;
            effect = NHFx.emped;
            effectChance = 0.4f;
            reactive = false;
            speedMultiplier = 0.5f;
            reloadMultiplier = 0.35f;
            damageMultiplier = 0.55f;
        }},
        
        emp3 = new StatusEffect("emp3"){{
            damage = 1.5f;
            effect = NHFx.emped;
            effectChance = 0.6f;
            reactive = false;
            speedMultiplier = 0.35f;
            reloadMultiplier = 0.05f;
            damageMultiplier = 0.35f;
        }};
}
