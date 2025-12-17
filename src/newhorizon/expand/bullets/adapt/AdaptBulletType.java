package newhorizon.expand.bullets.adapt;

import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.util.Time;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.graphics.Trail;
import newhorizon.expand.bullets.TypeDamageBulletType;

import static mindustry.Vars.headless;

/**
 * Bullet with kinetic damage and energy damage.
 * integrated with some other types.
 */
public class AdaptBulletType extends BasicBulletType implements TypeDamageBulletType {
    public String bundleName = "bullet-name";

    public boolean hasTracer = false;
    public float tracerRandRange = 8f;
    public float tracerUpdateInterval = 1f;

    public boolean mineShoot = false;
    public float mineDeployTime = 60f;

    public boolean hasAccel = false;
    public float velocityBegin = 0.1f;
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

        if (mineShoot){
            b.lifetime = b.lifetime * 2;
            b.fdata = b.lifetime;
            b.lifetime += mineDeployTime;

            b.data = b.vel.len() / speed;
        }
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
        if (mineShoot) b.vel.setLength(Interp.reverse.apply((b.time / b.lifetime)) * b.fdata / b.lifetime * speed * (b.data instanceof Float scl? scl: 1f));
        if (hasAccel) b.vel.setLength(velocityBegin + (accelInterp.apply(Mathf.curve(b.fin(), accelerateBegin, accelerateEnd)) * velocityIncrease));

        super.update(b);

        if(hasTrailFx && b.timer(0, (3 + b.fslope() * 2f) * trailMult)){
            trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : b.fslope() * trailSize, backColor);
        }
    }

    @Override
    public void updateTrail(Bullet b) {
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            if (hasTracer){
                if (b.timer(1, tracerUpdateInterval)){
                    b.trail.update(b.x + Mathf.random(-tracerRandRange, tracerRandRange), b.y + Mathf.random(-tracerRandRange, tracerRandRange), trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
                }
            }else {
                b.trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
            }
        }
    }
}
