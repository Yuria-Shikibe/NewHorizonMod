package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import newhorizon.NHSetting;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.util.feature.PosLightning;
import newhorizon.util.func.NHFunc;
import newhorizon.util.graphic.DrawFunc;

import static mindustry.Vars.headless;

public class UpgradePointLaserBulletType extends AdaptBulletType {
    public Color from = frontColor, to = NHColor.darkEnrColor;
    public float oscScl = 2f, oscMag = 0.3f;

    public float chargeReload = 90f;
    public float lerpReload = 10f;
    public float maxDamageMultiplier = 5f;

    public float damageInterval = 5f;

    private final Color tmpColor = new Color();

    public UpgradePointLaserBulletType(float kineticDamage, float energyDamage) {
        super(kineticDamage, energyDamage);
    }

    public boolean charged(Bullet b){
        return b.fdata > chargeReload;
    }

    public Color getColor(Bullet b){
        return tmpColor.set(from).lerp(to, warmup(b));
    }

    public float warmup(Bullet b){
        return Mathf.curve(b.fdata, chargeReload - lerpReload / 2f, chargeReload + lerpReload / 2f);
    }

    @Override
    public float continuousDamage(){
        return damage / damageInterval * 60f;
    }

    @Override
    public float estimateDPS(){
        return damage / damageInterval * 60f * maxDamageMultiplier;
    }

    @Override
    public void updateTrailEffects(Bullet b){
        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.aimX, b.aimY, trailRotation ? b.angleTo(b.aimX, b.aimY) : (trailParam * b.fslope()), trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(b.aimX, b.aimY, trailRotation ? b.angleTo(b.aimX, b.aimY) : (trailParam * b.fslope()), trailColor);
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            b.trail.update(b.aimX, b.aimY, b.fslope() * (1f - (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void updateBulletInterval(Bullet b){
        if(intervalBullet != null && b.time >= intervalDelay && b.timer.get(2, bulletInterval)){
            float ang = b.rotation();
            for(int i = 0; i < intervalBullets; i++){
                intervalBullet.create(b, b.aimX, b.aimY, ang + Mathf.range(intervalRandomSpread) + intervalAngle + ((i - (intervalBullets - 1f)/2f) * intervalSpread));
            }
        }
    }

    @Override
    public void update(Bullet b){
        updateTrail(b);
        updateTrailEffects(b);
        updateBulletInterval(b);

        if(b.timer.get(0, damageInterval)) Damage.collidePoint(b, b.team, hitEffect, b.aimX, b.aimY);

        b.damage = damage * (1 + warmup(b) * maxDamageMultiplier);
        b.fdata += Time.delta;
        b.fdata = Math.min(b.fdata, chargeReload + lerpReload / 2f);

        if(charged(b)){
            if(!Vars.headless && b.timer(3, 3)){
                PosLightning.createEffect(b, Tmp.v1.set(b.aimX, b.aimY), getColor(b), 1, 2);
                if(Mathf.chance(0.25)) NHFx.hitSparkLarge.at(b.x, b.y, tmpColor);
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float darkenPartWarmup = warmup(b);
        float stroke =  b.fslope() * (1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag)) * (darkenPartWarmup + 1) * 5;

        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            b.trail.draw(getColor(b), stroke);
            Draw.z(z);
        }

        Draw.color(getColor(b));
        DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke);
        Draw.color(Color.white);
        DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.64f * (2 + darkenPartWarmup) / 3f);

        Drawf.light(b.aimX, b.aimY, b.x, b.y, stroke, tmpColor, 0.76f);
        Drawf.light(b.x, b.y, stroke * 4, tmpColor, 0.76f);
        Drawf.light(b.aimX, b.aimY, stroke * 3, tmpColor, 0.76f);

        Draw.color(tmpColor);
        if(charged(b)){
            float qW = Mathf.curve(warmup(b), 0.5f, 0.7f);

            for(int s : Mathf.signs){
                Drawf.tri(b.x, b.y, 6, 21 * qW, 90 * s + Time.time * 1.8f);
            }

            for(int s : Mathf.signs){
                Drawf.tri(b.x, b.y, 7.2f, 25 * qW, 90 * s + Time.time * -1.1f);
            }
        }

        if(NHSetting.enableDetails()){
            int particles = 44;
            float particleLife = 74f;
            float particleLen = 7.5f;
            Rand rand = NHFunc.rand(b.id);

            float base = (Time.time / particleLife);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin, fslope = NHFx.fslope(fin);
                float len = rand.random(particleLen * 0.7f, particleLen * 1.3f) * Mathf.curve(fin, 0.2f, 0.9f) * (darkenPartWarmup / 2.5f + 1);
                float centerDeg = rand.random(Mathf.pi);

                Tmp.v1.trns(b.rotation(), Interp.pow3In.apply(fin) * rand.random(44, 77) - rand.range(11) - 8, (((rand.random(22, 35) * (fout + 1) / 2 + 2) / (3 * fin / 7 + 1.3f) - 1) + rand.range(4)) * Mathf.cos(centerDeg));
                float angle = Mathf.slerp(Tmp.v1.angle() - 180, b.rotation(), Interp.pow2Out.apply(fin));
                Tmp.v1.scl(darkenPartWarmup / 3.7f + 1);
                Tmp.v1.add(b);

                Draw.color(Tmp.c2.set(tmpColor), Color.white, fin * 0.7f);
                Lines.stroke(Mathf.curve(fslope, 0, 0.42f) * 1.4f * b.fslope() * Mathf.curve(fin, 0, 0.6f));
                Lines.lineAngleCenter(Tmp.v1.x, Tmp.v1.y, angle, len);
            }
        }

        if(darkenPartWarmup > 0.005f){
            tmpColor.lerp(Color.black, 0.86f);
            Draw.color(tmpColor);
            DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.55f * darkenPartWarmup);
            Draw.z(NHFx.EFFECT_BOTTOM);
            DrawFunc.basicLaser(b.x, b.y, b.aimX, b.aimY, stroke * 0.6f * darkenPartWarmup);
            Draw.z(Layer.bullet);
        }

        Draw.reset();
    }
}
