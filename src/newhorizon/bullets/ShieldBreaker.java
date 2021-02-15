package newhorizon.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Lightning;
import mindustry.gen.Bullet;
import newhorizon.effects.EffectTrail;

public class ShieldBreaker extends NHTrailBulletType{
    public float maxShieldDamage;
    public float rotateSpeed = 1.75f;
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
    public void draw(Bullet b){
        if (!(b.data instanceof EffectTrail))return;
        EffectTrail t = (EffectTrail)b.data;
        t.draw(trailColor);

        float height = this.height * (1.0F - this.shrinkY + this.shrinkY * b.fout());
        float width = this.width * (1.0F - this.shrinkX + this.shrinkX * b.fout());
        float offset = -90.0F + (this.spin != 0.0F ? Mathf.randomSeed(b.id, 360.0F) + b.time * this.spin : 0.0F);
        Color mix = Tmp.c1.set(this.mixColorFrom).lerp(this.mixColorTo, b.fin());
        Draw.mixcol(mix, mix.a);
        Draw.color(this.backColor);
        Draw.rect(this.backRegion, b.x, b.y, width, height, rotOffset(b) + b.rotation() + offset + Time.time * rotateSpeed);
        Draw.color(this.frontColor);
        Draw.rect(this.frontRegion, b.x, b.y, width, height, rotOffset(b) + b.rotation() + offset + Time.time * rotateSpeed);
        Draw.reset();
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        float offset = -90.0F + (this.spin != 0.0F ? Mathf.randomSeed(b.id, 360.0F) + b.time * this.spin : 0.0F);
        new EffectBulletType(1f){{
            this.absorbable = true;
            this.damage = maxShieldDamage;
        }}.create(b, b.x, b.y, 0);
        if(b.timer(3, 6f)) {
            for (int i : Mathf.signs) {
                Lightning.create(b, lightColor, lightningDamage, b.x, b.y, rotOffset(b) + b.rotation() + offset + Time.time * rotateSpeed + i * 90, lightningLength + Mathf.random(lightningLengthRand));
            }
        }
    }

    public float rotOffset(Bullet b){
        return (b.id % 360);
    }
}
