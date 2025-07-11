package newhorizon.expand.bullets;

import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;

/**
 * Bullet with kinetic damage and energy damage
 */
public class AdaptBulletType extends BasicBulletType implements TypeDamageBulletType {
    public String bundleName = "nh.bullet.desc";

    @Override
    public String bundleName() {
        return bundleName;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        applyExtraMultiplier(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        typedHitEntity(this, b, entity, health);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        typedCreateSplash(this, b, x, y);
    }
}
