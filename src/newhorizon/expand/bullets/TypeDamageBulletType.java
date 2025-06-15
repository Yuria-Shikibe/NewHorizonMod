package newhorizon.expand.bullets;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.Fires;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.world.meta.StatUnit;
import newhorizon.expand.block.turrets.AdaptTurret;

import static mindustry.Vars.indexer;
import static mindustry.Vars.tilesize;
import static newhorizon.content.NHStatValues.buildSharedBulletTypeStat;

public interface TypeDamageBulletType {
    EventType.UnitDamageEvent bulletDamageEvent = new EventType.UnitDamageEvent();

    String bundleName();

    default float getKineticMultiplier(BulletType type) {
        return 1f;
    }

    default float getEnergyMultiplier(BulletType type) {
        return type.shieldDamageMultiplier;
    }

    default float getKineticDamage(BulletType type) {
        return type.damage * getKineticMultiplier(type);
    }

    default float getEnergyDamage(BulletType type) {
        return type.damage * getEnergyMultiplier(type);
    }

    default float getSplashKineticDamage(BulletType type) {
        return type.splashDamage * getKineticMultiplier(type);
    }

    default float getSplashEnergyDamage(BulletType type) {
        return type.splashDamage * getEnergyMultiplier(type);
    }

    default float getContinuousKineticDamage(BulletType type) {
        return type.continuousDamage() * getKineticMultiplier(type);
    }

    default float getContinuousEnergyDamage(BulletType type) {
        return type.continuousDamage() * getEnergyMultiplier(type);
    }

    default float kineticDamage(BulletType type, Bullet b, float damage) {
        float extraMultiplier = 1f;
        if (b.owner instanceof AdaptTurret adaptTurret) {
            extraMultiplier = adaptTurret.kineticModifier();
        }
        return damage * getKineticMultiplier(type) * extraMultiplier;
    }

    default float energyDamage(BulletType type, Bullet b, float damage) {
        float extraMultiplier = 1f;
        if (b.owner instanceof AdaptTurret adaptTurret) {
            extraMultiplier = adaptTurret.energyModifier();
        }
        return damage * getEnergyMultiplier(type) * extraMultiplier;
    }

    default float getTotalDamageToUnit(BulletType type, Bullet b, float damage, Healthc entity) {
        if (entity instanceof Unit unit && unit.type != null) {
            float shield = Math.max(unit.shield(), 0f);
            return Mathf.clamp(shield / energyDamage(type, b, damage)) * energyDamage(type, b, damage) +
                    Math.max(((energyDamage(type, b, damage) - Math.max(shield, 0)) / energyDamage(type, b, damage)), 0f) * kineticDamage(type, b, damage);
        } else {
            return damage;
        }
    }

    default void applyExtraMultiplier(Bullet b) {
        if (b.owner instanceof AdaptTurret adaptTurret) {
            b.lifetime(b.lifetime() * adaptTurret.rangeModifier());
        }
    }

    default void buildStat(BulletType type, UnlockableContent t, Table bt, boolean compact) {
        bt.row();
        if (Core.bundle.getOrNull(bundleName()) != null) {
            bt.add(Core.bundle.get(bundleName())).wrap().fillX().padTop(8).padBottom(8).width(500);
            bt.row();
        }
        if ((getKineticMultiplier(type) > 0 || getEnergyMultiplier(type) > 0) && type.collides) {
            if (type.continuousDamage() > 0) {
                bt.add(Core.bundle.format("nh.damage-detail", getContinuousKineticDamage(type), getContinuousEnergyDamage(type)) + StatUnit.perSecond.localized());
            } else bt.add(Core.bundle.format("nh.damage-detail", getKineticDamage(type), getEnergyDamage(type)));
            bt.row();
        }

        if (type.splashDamage > 0 && type.splashDamageRadius > 0) {
            bt.add(Core.bundle.format("nh.splash-detail", getSplashKineticDamage(type), getSplashEnergyDamage(type), Strings.autoFixed((type.splashDamageRadius / tilesize), 1)));
        }

        buildSharedBulletTypeStat(type, t, bt, compact);
    }

    default void typedHitEntity(BulletType type, Bullet b, Hitboxc entity, float health) {
        boolean wasDead = entity instanceof Unit u && u.dead;

        if (entity instanceof Healthc h) {
            float damage = getTotalDamageToUnit(type, b, b.damage, h);
            float shield = entity instanceof Shieldc s ? Math.max(s.shield(), 0f) : 0f;
            if (type.maxDamageFraction > 0) {
                float cap = h.maxHealth() * type.maxDamageFraction + shield;
                damage = Math.min(damage, cap);
                //cap health to effective health for handlePierce to handle it properly
                health = Math.min(health, cap);
            } else {
                health += shield;
            }
            if (type.lifesteal > 0f && b.owner instanceof Healthc o) {
                float result = Math.max(Math.min(h.health(), damage), 0);
                o.heal(result * type.lifesteal);
            }
            if (type.pierceArmor) {
                h.damagePierce(damage);
            } else {
                h.damage(damage);
            }
        }

        if (entity instanceof Unit unit) {
            Tmp.v3.set(unit).sub(b).nor().scl(type.knockback * 80f);
            if (type.impact) Tmp.v3.setAngle(b.rotation() + (type.knockback < 0 ? 180f : 0f));
            unit.impulse(Tmp.v3);
            unit.apply(type.status, type.statusDuration);

            Events.fire(bulletDamageEvent.set(unit, b));
        }

        if (!wasDead && entity instanceof Unit unit && unit.dead) {
            Events.fire(new EventType.UnitBulletDestroyEvent(unit, b));
        }

        type.handlePierce(b, health, entity.x(), entity.y());
    }

    default void typedCreateSplash(BulletType type, Bullet b, float x, float y) {
        if (type.splashDamageRadius > 0 && !b.absorbed) {
            //do seperated damage
            Damage.tileDamage(b.team, World.toTile(x), World.toTile(y), type.splashDamageRadius / tilesize, type.splashDamage * b.type.buildingDamageMultiplier, b);

            Damage.damageUnits(
                    b.team, x, y, type.splashDamageRadius, 0,
                    unit -> unit.within(b, type.splashDamageRadius + unit.hitSize / 2f),
                    unit -> {
                        unit.damage(getTotalDamageToUnit(type, b, type.splashDamage * b.damageMultiplier(), unit));
                        if (type.status != StatusEffects.none) unit.apply(type.status, type.statusDuration);
                    });

            if (type.heals()) {
                indexer.eachBlock(b.team, x, y, type.splashDamageRadius, Building::damaged, other -> {
                    type.healEffect.at(other.x, other.y, 0f, type.healColor, other.block);
                    other.heal(type.healPercent / 100f * other.maxHealth() + type.healAmount);
                });
            }

            if (type.makeFire) {
                indexer.eachBlock(null, x, y, type.splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }
}
