package newhorizon.expand.bullets;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;
import static newhorizon.content.NHStatValues.buildSharedBulletTypeStat;

/**Bullet with kinetic damage and energy damage*/
public class AdaptBulletType extends BasicBulletType {
    static final EventType.UnitDamageEvent bulletDamageEvent = new EventType.UnitDamageEvent();
    public String bundleName = "nh.bullet.desc";

    public float kineticDamage, energyDamage;
    public float splashKineticDamage, splashEnergyDamage;
    public AdaptBulletType(float kineticDamage, float energyDamage) {
        this.kineticDamage = kineticDamage;
        this.energyDamage = energyDamage;

        damage = (kineticDamage + energyDamage)/2f;
    }

    public AdaptBulletType(){}

    @SuppressWarnings("UnusedReturnValue")
    public AdaptBulletType setDamage(float kineticDamage, float energyDamage) {
        this.kineticDamage = kineticDamage;
        this.energyDamage = energyDamage;

        damage = (kineticDamage + energyDamage)/2f;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public AdaptBulletType setSplash(float kineticDamage, float energyDamage, float splashRadius, int maxTarget) {
        splashKineticDamage = kineticDamage;
        splashEnergyDamage = energyDamage;
        splashDamageRadius = splashRadius;
        return this;
    }


    @SuppressWarnings("UnusedReturnValue")
    public AdaptBulletType setDescription(String key) {
        bundleName = "nh.bullet." + key;
        return this;
    }

    @Override
    public void init() {
        super.init();
        splashDamage = -1;
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
            float damage = shield > 0? Math.max(energyDamage, shield): kineticDamage;
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

    public void buildStat(UnlockableContent t, Table bt, boolean compact){
        if (Core.bundle.getOrNull(bundleName) != null) {
            bt.add(Core.bundle.get(bundleName)).wrap().fillX().padTop(8).padBottom(8).width(500);
            bt.row();
        }
        if((kineticDamage > 0 || energyDamage > 0) && collides){
            if(continuousDamage() > 0) bt.add(Core.bundle.format("nh.damage-detail", continuousKineticDamage(), continuousEnergyDamage()) + StatUnit.perSecond.localized());
            else bt.add(Core.bundle.format("nh.damage-detail", kineticDamage, energyDamage));
            bt.row();
        }

        if(splashKineticDamage > 0 || splashEnergyDamage > 0){
            bt.add(Core.bundle.format("nh.splash-detail", splashKineticDamage, splashEnergyDamage, Strings.autoFixed((splashDamageRadius / tilesize), 1)));
        }

        buildSharedBulletTypeStat(this, t, bt, compact);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        if(splashDamageRadius > 0){
            //apply kinetic damage to build
            Damage.tileDamage(b.team, World.toTile(x), World.toTile(y), splashDamageRadius, splashKineticDamage * b.type.buildingDamageMultiplier, b);
            Damage.damageUnits(
                    b.team, x, y, splashDamageRadius, 0,
                    unit -> unit.within(b, splashDamageRadius + unit.hitSize / 2f),
                    unit -> {
                        float shield = Math.max(unit.shield(), 0f);
                        float damage = (shield > 0? splashEnergyDamage: splashKineticDamage);
                        Log.info(damage);
                        unit.damage(damage);
                        if(status != StatusEffects.none){
                            unit.apply(status, statusDuration);
                        }
                    });
        }
    }
}
