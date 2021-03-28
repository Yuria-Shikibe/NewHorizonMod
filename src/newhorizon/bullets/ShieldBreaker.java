package newhorizon.bullets;

import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;

public class ShieldBreaker extends NHTrailBulletType{
    public float maxShieldDamage;
    
    protected BulletType breakType = new EffectBulletType(1f){{
        this.absorbable = true;
        this.damage = 1;
    }};
    
    public ShieldBreaker(float speed, float damage, String bulletSprite, float shieldDamage) {
        super(speed, damage, bulletSprite);
        this.splashDamage = this.splashDamageRadius = -1f;
        this.maxShieldDamage = shieldDamage;
        this.absorbable = false;
    }

    public ShieldBreaker(float speed, float damage, float shieldDamage) {
        this(speed, damage, "bullet", shieldDamage);
    }

    public ShieldBreaker() {
        this(1.0F, 1.0F, "bullet", 500f);
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        breakType.create(b, b.team, b.x, b.y, 0, maxShieldDamage, 0, 1, new Object());
    }
}
