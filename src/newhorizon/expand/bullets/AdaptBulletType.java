package newhorizon.expand.bullets;

import arc.Events;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Fires;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.EventType;
import mindustry.gen.*;

import static mindustry.Vars.indexer;

/**Bullet with kinetic damage and energy damage*/
public class AdaptBulletType extends BasicBulletType {
    static final EventType.UnitDamageEvent bulletDamageEvent = new EventType.UnitDamageEvent();
    public float kineticDamage, energyDamage;
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

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        boolean wasDead = entity instanceof Unit u && u.dead;

        if(entity instanceof Healthc h){
            float shield = entity instanceof Shieldc s ? Math.max(s.shield(), 0f) : 0f;
            float damage = shield > 0? energyDamage: kineticDamage;
            if(pierceArmor) h.damagePierce(damage);
            else h.damage(damage);
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
        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(heals()){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    healEffect.at(other.x, other.y, 0f, healColor, other.block);
                    other.heal(healPercent / 100f * other.maxHealth() + healAmount);
                });
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }
}
