package newhorizon.expand.bullets.raid;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static newhorizon.content.NHFx.EFFECT_BOTTOM;
import static newhorizon.content.NHFx.EFFECT_MASK;

public class BasicRaidBulletType extends BasicBulletType {
    public BasicRaidBulletType() {
        speed = 8f;
        lifetime = 120f;

        splashDamageRadius = 80f;

        splashDamagePierce = true;
        collides = false;
        collidesGround = true;
        collideFloor = true;
        collidesAir = true;

        hittable = true;
        reflectable = false;
        absorbable = true;
        despawnHit = true;

        trailLength = 14;
        trailChance = 0.075f;
        trailParam = 6;
        drawSize = 120f;
        hitShake = despawnShake = 16f;

        shrinkX = shrinkY = 0;
        height = 66f;
        width = 20f;

        sprite = NHBullets.STRIKE;
        hitSound = Sounds.explosionbig;

        trailEffect = new Effect(25f, e -> {
            color(e.color, Pal.gray, e.fin());
            randLenVectors(e.id, 4, 46f * e.fin(), (x, y) -> {
                Fill.poly(e.x + x, e.y + y, 6, e.rotation * e.fslope() * e.fout());
                Drawf.light(e.x + x, e.y + y, e.fout() * e.rotation * 1.15f, e.color, 0.7f);
            });
        });
    }

    //change every single effect which are color related and change the color to bullet's team color
    @Override
    public void updateTrailEffects(Bullet b){
        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }
    }

    @Override
    public void drawTrail(Bullet b) {
        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            b.trail.draw(b.team.color, trailWidth);
            Draw.z(z);
        }
    }

    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        Drawf.light(b, lightRadius, b.team.color, lightOpacity);
    }

    @Override
    public void despawned(Bullet b){
        if(despawnHit) hit(b);
        
        NHFx.instHit(b.team.color, 4, 180f).at(b.x, b.y, b.rotation());
        
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    //only splash damage
    public void hit(Bullet b, float x, float y){
        new OptionalMultiEffect(
                new Effect(50f, e -> {
                    color(b.team.color);
                    Fill.circle(e.x, e.y, e.fout() * 44);
                    stroke(e.fout() * 3.2f);
                    circle(e.x, e.y, e.fin() * 80);
                    stroke(e.fout() * 2.5f);
                    circle(e.x, e.y, e.fin() * 50);
                    stroke(e.fout() * 3.2f);
                    randLenVectors(e.id, 30, 18 + 80 * e.fin(), (ex, ey) -> {
                        lineAngle(e.x + ex, e.y + ey, Mathf.angle(ex, ey), e.fslope() * 14 + 5);
                    });

                    Draw.z(EFFECT_MASK);
                    color(b.team.color, Color.black, 0.85f);
                    Fill.circle(e.x, e.y, e.fout() * 30);
                    Drawf.light(e.x, e.y, e.fout() * 80f, b.team.color, 0.7f);

                    Draw.z(EFFECT_BOTTOM);
                    Fill.circle(e.x, e.y, e.fout() * 31);
                    Draw.z(Layer.effect - 0.0001f);
                }).layer(Layer.effect - 0.0001f), 
                NHFx.square(b.team.color, 100f, 3, 80f, 8f),
                new Effect(20f, e -> {
                    color(b.team.color);
                    Fill.circle(e.x, e.y, e.fout() * 44);
                    randLenVectors(e.id, 5, 60f * e.fin(), (ex,ey) -> Fill.circle(e.x + ex, e.y + ey, e.fout() * 8));
                    color(b.team.color, Color.black, 0.85f);
                    Fill.circle(e.x, e.y, e.fout() * 30);
                    Drawf.light(e.x, e.y, e.fout() * 55f, b.team.color, 0.7f);
                }))
                .at(x, y, b.rotation(), b.team.color);
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        Effect.shake(hitShake, hitShake, b);
        createSplashDamage(b, x, y);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y){
        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, damage * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);
        }
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);

        float shrink = shrinkInterp.apply(b.fout());
        float height = this.height * ((1f - shrinkY) + shrinkY * shrink);
        float width = this.width * ((1f - shrinkX) + shrinkX * shrink);
        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f) + rotationOffset;

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        if(backRegion.found()){
            Draw.color(b.team.color);
            Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
        }

        Draw.color(Color.white);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);

        Draw.reset();
    }
}
