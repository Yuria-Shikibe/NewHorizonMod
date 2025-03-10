package newhorizon.content.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import newhorizon.content.*;
import newhorizon.content.blocks.TurretBlock;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.expand.bullets.raid.BasicRaidBulletType;
import newhorizon.expand.bullets.raid.TracerRaidBulletType;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.graphics.g2d.Lines.circle;
import static arc.math.Angles.randLenVectors;

public class RaidBullets {
    public static final Rand rand = new Rand();
    public static BulletType raidBullet_1, raidBullet_2, raidBullet_3, raidBullet_4, raidBullet_5, raidBullet_6, raidBullet_7, raidBullet_8;

    public static void load(){
        raidBullet_1 = new BasicRaidBulletType(){{
            speed = 12f;
            lifetime = 120f;

            damage = 2000;

            splashDamageRadius = 80f;
            splashDamage = 1000f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
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

            trailEffect = spark(45, 40);

            despawnEffect = new OptionalMultiEffect(
                spark(90, 120), spark(40, 150), spark(60, 135),
                circle(35, 45), circle(25, 60), circle(25, 75), circle(30, 100)
            );
        }};

        raidBullet_2 = new TracerRaidBulletType(){{
            speed = 3.5f;
            lifetime = 120f;

            damage = 500;

            splashDamageRadius = 60f;
            splashDamage = 500f;

            splashDamagePierce = true;
            scaledSplashDamage = true;
            collides = false;
            collidesGround = true;
            collideFloor = true;
            collidesAir = true;

            hittable = true;
            reflectable = false;
            absorbable = true;
            despawnHit = true;

            trailLength = 30;
            trailChance = 0.8f;
            trailParam = 3;
            drawSize = 120f;
            hitShake = despawnShake = 16f;

            tracers = 2;
            tracerFadeOffset = 15;
            tracerStrokeOffset = 15;
            tracerStroke = 3f;
            tracerSpacing = 8f;
            tracerRandX = 6f;
            tracerUpdateSpacing = 2f;

            shrinkX = shrinkY = 0;
            height = 22f;
            width = 22f;

            sprite = "large-orb";
            hitSound = Sounds.explosionbig;

            trailEffect = triSpark(45, 15);

            despawnEffect = new OptionalMultiEffect(
                    spark(90, 90), spark(40, 60), spark(60, 85),
                    circle(35, 25), circle(25, 40), circle(25, 65),
                    crossBlast(45, 80, 0)
            );
        }};
        raidBullet_3 = NHBullets.railGun1;
        raidBullet_4 = NHBullets.railGun2;
        raidBullet_5 = NHBullets.railGun3;
        raidBullet_6 = new ArtilleryBulletType(){{
            damage = 75;
            speed = 6.5f;
            lifetime = 150f;
            hitShake = despawnShake = 2f;
            status = NHStatusEffects.emp3;
            hitSound = Sounds.none;

            fragBullet = new DOTBulletType(){{
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
                        for(int s : Mathf.signs) {
                            Drawf.tri(x, y, 14 * fout, 30 * Mathf.curve(finpow, 0, 0.3f) * NHFx.fout(fin, 0.15f), rot + s * 90);
                        }
                    }))
            );
        }};
        raidBullet_7 = NHBullets.shieldDestroyer;
        raidBullet_8 = NHBullets.ancientArtilleryProjectile;
    }

    public static Effect circle(float lifetime, float radius){
        return new Effect(lifetime, e -> {
            color(e.color, Color.white, e.fout() * 0.75f);
            Lines.stroke((radius / 30f) * e.fslope());
            Lines.circle(e.x, e.y, radius * e.fin());
            Drawf.light(e.x, e.y, e.fout() * radius * 1.25f, e.color, 0.7f);
        });
    }

    public static Effect spark(float lifetime, float radius){
        return new Effect(lifetime, e -> {
            color(e.color, Color.white, e.fout() * 0.3f);
            stroke(e.fout() * Mathf.sqrt(radius / 6f) * 1.5f);

            rand.setSeed(e.id);
            randLenVectors(e.id, (int)(radius / 6f), e.finpow() * radius, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * rand.random(2f, 4f) * radius / 10f + 1f));
        });
    }

    public static Effect triSpark(float lifetime, float radius){
        return new Effect(lifetime, e -> {
            rand.setSeed(e.id);
            Draw.color(e.color, Color.white, e.fin());
            randLenVectors(e.id, (int)(radius / 8), 3f + radius * e.fin(), 5f, (x, y) -> {
                float randN = rand.random(120f);
                Fill.poly(e.x + x, e.y + y, 3, e.fout() * (radius / 3f) * rand.random(0.8f, 1.2f), e.rotation + randN * e.fin());
            });
        });
    }

    public static Effect crossBlast(float lifetime, float size, float rotate){
        return new Effect(lifetime, size * 2, e -> {
            color(e.color, Color.white, e.fout() * 0.55f);
            Drawf.light(e.x, e.y, e.fout() * size, e.color, 0.7f);

            rand.setSeed(e.id);
            float sizeDiv = size / 1.5f;
            float randL = rand.random(sizeDiv);

            for(int i = 0; i < 4; i++){
                DrawFunc.tri(e.x, e.y, size / 20 * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + rotate);
            }
        });
    }
}
