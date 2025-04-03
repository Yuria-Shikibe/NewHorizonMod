package newhorizon.expand.bullets;

import arc.Events;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.EventType;
import mindustry.gen.*;

/**Bullet with kinetic damage and energy damage*/
public class AdaptBulletType extends BasicBulletType {
    static final EventType.UnitDamageEvent bulletDamageEvent = new EventType.UnitDamageEvent();
    //this will move to shield multiplier when comes to v8
    public float kineticDamage = 30f, energyDamage = 30f;
    public float splashMultiplier = 1f;
    public AdaptBulletType(float kineticDamage, float energyDamage) {
        this.kineticDamage = kineticDamage;
        this.energyDamage = energyDamage;

        damage = (kineticDamage + energyDamage)/2f;
    }

    public float continuousKineticDamage() {
        return super.continuousDamage();
    }

    public float continuousEnergyDamage() {
        return super.continuousDamage();
    }

    public AdaptBulletType() {}

    public void hitEntity(Bullet b, Hitboxc entity, float health){
        boolean wasDead = entity instanceof Unit u && u.dead;

        if(entity instanceof Healthc h){

            if(pierceArmor){
                if (entity instanceof Unit u && u.shield() > 0){
                    h.damagePierce(energyDamage * b.damageMultiplier());
                }else {
                    h.damagePierce(kineticDamage * b.damageMultiplier());
                }
            }else{
                if (entity instanceof Unit u && u.shield() > 0){
                    h.damage(energyDamage * b.damageMultiplier());
                }else {
                    h.damage(kineticDamage * b.damageMultiplier());
                }
            }
        }

        if(entity instanceof Unit unit){
            Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
            if(impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
            unit.impulse(Tmp.v3);
            unit.apply(status, statusDuration);

            Events.fire(bulletDamageEvent.set(unit, b));
        }

        if(!wasDead && entity instanceof Unit unit && unit.dead){
            Events.fire(new EventType.UnitBulletDestroyEvent(unit, b));
        }

        handlePierce(b, health, entity.x(), entity.y());
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        super.createSplashDamage(b, x, y);
    }
}
