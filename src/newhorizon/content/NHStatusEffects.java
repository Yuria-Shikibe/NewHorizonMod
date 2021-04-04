package newhorizon.content;

import mindustry.ctype.ContentList;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import newhorizon.feature.NHStatusEffect;

public class NHStatusEffects implements ContentList{
    public static StatusEffect
            staticVel, emp1, emp2, emp3;
    
    @Override
    public void load(){
        staticVel = new NHStatusEffect("static-vel") {
            @Override
            public void update(Unit unit, float time){
                super.update(unit, time);
                unit.vel = unit.vel.scl(0.65f);
            }
    
            {
                this.color = Pal.gray;
                this.speedMultiplier = 0.00001F;
            }
        };
        
        emp1 = new NHStatusEffect("emp-1"){{
            damage = 0.05f;
            effect = NHFx.emped;
            effectChance = 0.2f;
            reactive = false;
            speedMultiplier = 0.65f;
            reloadMultiplier = 0.65f;
            damageMultiplier = 0.75f;
        }};
    
        emp2 = new NHStatusEffect("emp-2"){{
            damage = 0.15f;
            effect = NHFx.emped;
            effectChance = 0.4f;
            reactive = false;
            speedMultiplier = 0.5f;
            reloadMultiplier = 0.35f;
            damageMultiplier = 0.55f;
        }};
    
        emp3 = new NHStatusEffect("emp-3"){{
            damage = 0.25f;
            effect = NHFx.emped;
            effectChance = 0.6f;
            reactive = false;
            speedMultiplier = 0.35f;
            reloadMultiplier = 0.05f;
            damageMultiplier = 0.35f;
        }};
    }
}
