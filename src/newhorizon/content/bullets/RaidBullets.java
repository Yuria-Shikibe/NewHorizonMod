package newhorizon.content.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import newhorizon.content.NHBullets;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.expand.bullets.raid.TracerRaidBulletType;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;

public class RaidBullets {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();
    public static BulletType
            defaultRaidBullet1, defaultRaidBullet2, defaultRaidBullet3,
            explosiveRaidBullet1, explosiveRaidBullet2, explosiveRaidBullet3,
            raidBullet_3, raidBullet_4, raidBullet_5, raidBullet_6, raidBullet_7, raidBullet_8;

    public static void load() {
        defaultRaidBullet1 = new BasicRaidBulletType() {{
            speed = 12f;
            lifetime = 120f;

            damage = 600;

            splashDamageRadius = 55f;
            splashDamage = 400f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
            collides = false;
            collidesGround = true;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = true;
            despawnHit = false;
            setDefaults = false;

            trailLength = 25;
            trailChance = 1f;
            trailParam = 6;
            drawSize = 120f;
            hitShake = despawnShake = 16f;

            shrinkX = shrinkY = 0;
            height = 55f;
            width = 22f;

            sprite = NHBullets.STRIKE;
            hitSound = Sounds.explosion;

            trailRotation = true;
            trailEffect = new Effect(25, e -> {
                color(e.color, Color.white, e.fin());
                stroke(0.6f + e.fout() * 1.7f);
                rand.setSeed(e.id);

                for(int i = 0; i < 2; i++){
                    float rot = e.rotation + rand.range(15f) + 180f;
                    v.trns(rot, rand.random(e.fin() * 27f));
                    lineAngle(e.x + v.x, e.y + v.y, rot, e.fout() * rand.random(15f, 27f) + 1.5f);
                }
            });

            despawnEffect = new OptionalMultiEffect(
                    spark(90, 40), spark(40, 60), spark(60, 75),
                    circle(35, 35), circle(25, 40), circle(25, 50), circle(30, 60)
            );
        }};
        defaultRaidBullet2 = new BasicRaidBulletType() {{
            speed = 13.5f;
            lifetime = 120f;

            damage = 1200;

            splashDamageRadius = 75f;
            splashDamage = 750f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
            collides = false;
            collidesGround = true;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = true;
            despawnHit = false;
            setDefaults = false;

            trailLength = 30;
            trailChance = 1f;
            trailParam = 7;
            drawSize = 120f;
            hitShake = despawnShake = 20f;

            shrinkX = shrinkY = 0;
            height = 65f;
            width = 32f;

            sprite = NHBullets.STRIKE;
            hitSound = Sounds.explosion;

            trailRotation = true;
            trailEffect = new Effect(25, e -> {
                color(e.color, Color.white, e.fin());
                stroke(0.6f + e.fout() * 1.7f);
                rand.setSeed(e.id);

                for(int i = 0; i < 2; i++){
                    float rot = e.rotation + rand.range(18f) + 180f;
                    v.trns(rot, rand.random(e.fin() * 27f));
                    lineAngle(e.x + v.x, e.y + v.y, rot, e.fout() * rand.random(15f, 27f) + 1.5f);
                }

                randLenVectors(e.id, 2, 3f + rand.range(26f) * e.fin(), 5f, (x, y) -> {
                    float randN = rand.random(120f);
                    Fill.poly(e.x + x, e.y + y, 3, e.fout() * 6 * rand.random(1.2f, 1.8f), e.rotation + randN * e.fin());
                });
            });

            despawnEffect = new OptionalMultiEffect(
                    spark(90, 60), spark(40, 80), spark(60, 85), spark(60, 95),
                    circle(35, 55), circle(25, 60), circle(25, 70), circle(30, 80)
            );
        }};
        defaultRaidBullet3 = new BasicRaidBulletType() {{
            speed = 15f;
            lifetime = 120f;

            damage = 2000;

            splashDamageRadius = 100f;
            splashDamage = 1200f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
            collides = false;
            collidesGround = true;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = true;
            despawnHit = false;
            setDefaults = false;

            trailLength = 40;
            trailChance = 1f;
            trailParam = 8.5f;
            drawSize = 120f;
            hitShake = despawnShake = 25f;

            shrinkX = shrinkY = 0;
            height = 75f;
            width = 40f;

            sprite = NHBullets.STRIKE;
            hitSound = Sounds.explosion;

            trailRotation = true;
            trailEffect = new Effect(40f, e -> {
                color(e.color, Color.white, e.fin());
                rand.setSeed(e.id);

                for(int i = 0; i < 2; i++){
                    float rot = e.rotation + rand.range(18f) + 180f;
                    v.trns(rot, rand.random(e.fin() * 27f));
                    lineAngle(e.x + v.x, e.y + v.y, rot, e.fout() * rand.random(15f, 27f) + 1.5f);
                }

                randLenVectors(e.id, 2, 3f + rand.range(26f) * e.fin(), 5f, (x, y) -> {
                    float randN = rand.random(120f);
                    Fill.poly(e.x + x, e.y + y, 3, e.fout() * 6 * rand.random(1.2f, 1.8f), e.rotation + randN * e.fin());
                });

                Angles.randLenVectors(e.id, 6, 2f + 32f * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout() * 2f));
                e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2f + 32f * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 7f)));
            });

            despawnEffect = new OptionalMultiEffect(
                    spark(90, 85), spark(40, 90), spark(60, 105), spark(60, 125), spark(50, 155),
                    circle(35, 80), circle(25, 90), circle(25, 100), circle(30, 120)
            );
        }};

        explosiveRaidBullet1 = new TracerRaidBulletType() {{
            speed = 4f;
            lifetime = 120f;

            damage = 5000;

            splashDamageRadius = 125f;
            splashDamage = 300f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
            collides = false;
            collidesGround = false;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = false;
            despawnHit = true;
            setDefaults = false;

            tracerCount = 1;
            tracerLength = 12;
            tracerWidth = 4f;
            tracerRandRange = 10f;
            tracerUpdateInterval = 0.75f;

            trailLength = 35;
            trailChance = 1f;
            trailParam = 8;
            drawSize = 120f;
            hitShake = despawnShake = 16f;

            shrinkX = shrinkY = 0;
            height = 48f;
            width = 48f;

            sprite = "large-bomb";
            hitSound = Sounds.explosion;

            trailRotation = true;
            trailEffect = new Effect(50, e -> {
                color(e.color);
                rand.setSeed(e.id);
                float fin = e.fin() / rand.random(0.5f, 1f), fout = 1f - fin, angle = rand.random(360f), len = rand.random(0.5f, 1f);
                if(fin <= 1f){
                    Tmp.v1.trns(angle, fin * 24f * len);

                    alpha((0.5f - Math.abs(fin - 0.5f)) * 2f);
                    Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 0.5f + fout * 4f);
                }
            });

            despawnEffect = new OptionalMultiEffect(
                    spark(90, 40), spark(40, 60), spark(60, 75),
                    circle(35, 100), circle(25, 110), circle(25, 120), circle(30, 135)
            );
        }};

        raidBullet_3 = NHBullets.railGun1;
        raidBullet_4 = NHBullets.railGun2;
        raidBullet_5 = NHBullets.railGun3;
        raidBullet_6 = new ArtilleryBulletType() {{
            damage = 75;
            speed = 6.5f;
            lifetime = 150f;
            hitShake = despawnShake = 2f;
            status = NHStatusEffects.emp3;
            hitSound = Sounds.none;

            fragBullet = new DOTBulletType() {{
                DOTDamage = damage = 75f;
                DOTRadius = 16f;
                radIncrease = 0.28f;
                effect = NHStatusEffects.emp3;
                fx = NHFx.triSpark2;
                lightningColor = NHColor.ancient;
            }};
            fragBullets = 1;

            homingRange = 22f;
            homingPower = 0.13f;

            trailChance = 0.8f;
            trailEffect = NHFx.triSpark2;

            backColor = lightColor = lightningColor = trailColor = hitColor = NHColor.ancient;
            rangeChange = 45;

            despawnEffect = Fx.none;
            hitEffect = new OptionalMultiEffect(
                    NHFx.smoothColorCircle(NHColor.ancient, 100f, 125f, 0.3f),
                    NHFx.circleOut(150f, 100f, 4),
                    NHFx.circleOut(78f, 75f, 2),
                    NHFx.subEffect(130f, 85f, 12, 30f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                        float fout = Interp.pow2Out.apply(1 - fin);
                        float finpow = Interp.pow3Out.apply(fin);
                        Tmp.v1.trns(rot, 25 * finpow);
                        Draw.color(NHColor.ancient);
                        for (int s : Mathf.signs) {
                            Drawf.tri(x, y, 14 * fout, 30 * Mathf.curve(finpow, 0, 0.3f) * NHFx.fout(fin, 0.15f), rot + s * 90);
                        }
                    }))
            );
        }};
        raidBullet_7 = NHBullets.shieldDestroyer;
        raidBullet_8 = NHBullets.ancientArtilleryProjectile;
    }

    public static Effect circle(float lifetime, float radius) {
        return new Effect(lifetime, e -> {
            color(e.color, Color.white, e.fout() * 0.75f);
            Lines.stroke((radius / 30f) * e.fslope());
            Lines.circle(e.x, e.y, radius * e.fin());
            Drawf.light(e.x, e.y, e.fout() * radius * 1.25f, e.color, 0.7f);
        });
    }

    public static Effect spark(float lifetime, float radius) {
        return new Effect(lifetime, e -> {
            color(e.color, Color.white, e.fout() * 0.3f);
            stroke(e.fout() * Mathf.sqrt(radius / 6f) * 1.5f);

            rand.setSeed(e.id);
            randLenVectors(e.id, (int) (radius / 6f), e.finpow() * radius, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * rand.random(2f, 4f) * radius / 10f + 1f));
        });
    }

    public static Effect hexSpark(float lifetime, float radius) {
        return new Effect(lifetime, e -> {
            rand.setSeed(e.id);
            Draw.color(e.color, Color.white, e.fin());
            randLenVectors(e.id, (int) (radius / 8), 3f + radius * e.fin(), 5f, (x, y) -> {
                float randN = rand.random(120f);
                Fill.poly(e.x + x, e.y + y, 6, e.fout() * (radius / 3f) * rand.random(0.8f, 1.2f), e.rotation + randN * e.fin());
            });
        });
    }

    public static Effect triSpark(float lifetime, float radius) {
        return new Effect(lifetime, e -> {
            rand.setSeed(e.id);
            Draw.color(e.color, Color.white, e.fin());
            randLenVectors(e.id, (int) (radius / 8), 3f + radius * e.fin(), 5f, (x, y) -> {
                float randN = rand.random(120f);
                Fill.poly(e.x + x, e.y + y, 3, e.fout() * (radius / 3f) * rand.random(0.8f, 1.2f), e.rotation + randN * e.fin());
            });
        });
    }

    public static Effect crossBlast(float lifetime, float size, float rotate) {
        return new Effect(lifetime, size * 2, e -> {
            color(e.color, Color.white, e.fout() * 0.55f);
            Drawf.light(e.x, e.y, e.fout() * size, e.color, 0.7f);

            rand.setSeed(e.id);
            float sizeDiv = size / 1.5f;
            float randL = rand.random(sizeDiv);

            for (int i = 0; i < 4; i++) {
                DrawFunc.tri(e.x, e.y, size / 20 * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + rotate);
            }
        });
    }
}
