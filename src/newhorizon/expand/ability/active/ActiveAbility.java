package newhorizon.expand.ability.active;

import arc.util.Time;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

public abstract class ActiveAbility extends Ability {
    public static final int RELOAD = 0, READY = 1;

    //time used for ability charge.
    public float reloadTimer = 300f;
    //max charge count.
    public int chargeCount = 1;
    //cooldown time
    public float cooldownTimer = 60f;

    protected float timer, stack, cooldown;

    public void trigger(Unit unit){

    }

    @Override
    public void update(Unit unit) {
        if (stack <= chargeCount){
            timer += Time.delta;
        }

        if (timer > reloadTimer){
            stack++;
            timer %= reloadTimer;
        }

        if (cooldown > 0){
            cooldown -= Time.delta;
        }
    }
}
