package newhorizon.content.bullets;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class RaidBullets {
    public static final Rand rand = new Rand();
    public static BasicRaidBulletType raidBullet_1;

    public static void load(){
        raidBullet_1 = new BasicRaidBulletType(){{
            speed = 12f;
            lifetime = 120f;

            splashDamageRadius = 60f;
            splashDamage = 500f;

            splashDamagePierce = true;
            collides = false;
            collidesGround = true;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = true;
            despawnHit = true;

            trailLength = 40;
            trailChance = 1f;
            trailParam = 6;
            drawSize = 120f;
            hitShake = despawnShake = 16f;

            shrinkX = shrinkY = 0;
            height = 65f;
            width = 40f;

            sprite = NHBullets.STRIKE;
            hitSound = Sounds.explosionbig;

            trailEffect = new Effect(45, e -> {
                color(e.color, Color.white, e.fout() * 0.3f);
                stroke(e.fout() * 6f);

                rand.setSeed(e.id);
                randLenVectors(e.id, 5, e.finpow() * 40f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(1.95f, 4.25f) * 3.2f + 1f);
                });
            });

            despawnEffect = new OptionalMultiEffect(
                    NHFx.triSpark1,
                    NHFx.hitSparkHuge,
                    NHFx.hyperExplode,
                    new Effect(90, e -> {
                        color(e.color, Color.white, e.fout() * 0.3f);
                        stroke(e.fout() * 3f);

                        rand.setSeed(e.id);
                        randLenVectors(e.id, 60, e.finpow() * 135f, (x, y) -> {
                            float ang = Mathf.angle(x, y);
                            lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(6, 9) * 2f + 3f);
                        });
                    })
            );
        }};
    }
}
