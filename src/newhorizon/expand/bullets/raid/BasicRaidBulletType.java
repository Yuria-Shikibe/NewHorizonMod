package newhorizon.expand.bullets.raid;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import newhorizon.content.NHBullets;

//change every single effect which are color related and change the color to bullet's team color
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
        hitSound = Sounds.explosion;
    }

    @Override
    public void updateTrailEffects(Bullet b) {
        if (trailChance > 0) {
            if (Mathf.chanceDelta(trailChance)) {
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }

        if (trailInterval > 0f) {
            if (b.timer(0, trailInterval)) {
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }
    }

    @Override
    public void drawTrail(Bullet b) {
        if (trailLength > 0 && b.trail != null) {
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            b.trail.draw(b.team.color, trailWidth);
            Draw.z(z);
        }
    }

    @Override
    public void drawLight(Bullet b) {
        if (lightOpacity <= 0f || lightRadius <= 0f) return;
        Drawf.light(b, lightRadius, b.team.color, lightOpacity);
    }

    @Override
    public void despawned(Bullet b) {
        if (despawnHit) {
            hit(b);
        } else {
            createUnits(b, b.x, b.y);
        }

        if (!fragOnHit) createFrags(b, b.x, b.y);

        despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        hitEffect.at(x, y, b.rotation(), b.team.color);
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        Effect.shake(hitShake, hitShake, b);

        if (fragOnHit) {
            createFrags(b, x, y);
        }
        createPuddles(b, x, y);
        createIncend(b, x, y);
        createUnits(b, x, y);

        if (suppressionRange > 0) {
            //bullets are pooled, require separate Vec2 instance
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
        }

        createSplashDamage(b, x, y);

        for (int i = 0; i < lightning; i++) {
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y) {
        if (splashDamageRadius > 0 && !b.absorbed) {
            Damage.damage(b.team, x, y, splashDamageRadius, b.damage() * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);
        }
    }

    public void removed(Bullet b) {
        if (trailLength > 0 && b.trail != null && b.trail.size() > 0) {
            Fx.trailFade.at(b.x, b.y, trailWidth, b.team.color, b.trail.copy());
        }
    }

    @Override
    public void draw(Bullet b) {
        drawTrail(b);
        drawParts(b);

        float shrink = shrinkInterp.apply(b.fout());
        float height = this.height * ((1f - shrinkY) + shrinkY * shrink);
        float width = this.width * ((1f - shrinkX) + shrinkX * shrink);
        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f) + rotationOffset;

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        if (backRegion.found()) {
            Draw.color(b.team.color);
            Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
        }

        Draw.color(Color.white);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);

        Draw.reset();
    }
}
