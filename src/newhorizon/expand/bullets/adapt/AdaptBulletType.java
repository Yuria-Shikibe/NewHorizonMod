package newhorizon.expand.bullets.adapt;

import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import newhorizon.expand.bullets.TypeDamageBulletType;

/**
 * Bullet with kinetic damage and energy damage.
 * integrated with some other types.
 */
public class AdaptBulletType extends BasicBulletType implements TypeDamageBulletType {
    public String bundleName = "bullet-name";

    public boolean hasAccel = false;
    public float velocityBegin = -1;
    public float velocityIncrease = 0;
    public float accelerateBegin = 0.1f;
    public float accelerateEnd = 0.6f;
    public Interp accelInterp = Interp.linear;

    public boolean hasTrailFx = false;
    public float trailMult = 1f, trailSize = 4f;

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
    protected float calculateRange() {
        if (!hasAccel) return super.calculateRange();
        if (velocityBegin < 0) velocityBegin = speed;

        boolean computeRange = rangeOverride < 0;
        float cal = 0;

        FloatSeq speeds = new FloatSeq();
        for (float i = 0; i <= 1; i += 0.05f) {
            float s = velocityBegin + accelInterp.apply(Mathf.curve(i, accelerateBegin, accelerateEnd)) * velocityIncrease;
            speeds.add(s);
            if (computeRange) cal += s * lifetime * 0.05f;
        }
        speed = speeds.sum() / speeds.size;

        if (computeRange) cal += 1;

        return cal;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        typedHitEntity(this, b, entity, health);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        typedCreateSplash(this, b, x, y);
    }

    @Override
    public void update(Bullet b){
        if (hasAccel) b.vel.setLength((velocityBegin + accelInterp.apply(Mathf.curve(b.fin(), accelerateBegin, accelerateEnd)) * velocityIncrease));

        super.update(b);

        if(hasTrailFx && b.timer(0, (3 + b.fslope() * 2f) * trailMult)){
            trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : b.fslope() * trailSize, backColor);
        }
    }
}
