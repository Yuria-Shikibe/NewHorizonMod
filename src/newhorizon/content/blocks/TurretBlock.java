package newhorizon.content.blocks;

import mindustry.content.Items;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootSpread;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.draw.DrawTurret;
import newhorizon.content.*;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.expand.bullets.TrailFadeBulletType;
import newhorizon.util.graphic.EffectWrapper;
import newhorizon.util.graphic.OptionalMultiEffect;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.math.Angles.randLenVectors;
import static mindustry.type.ItemStack.with;

public class TurretBlock {
    public static Block electro;

    public static Block testShooter;

    public static void load(){
        electro = new ItemTurret("electro"){{
            requirements(Category.turret, with(Items.copper, 10));

            outlineColor = Pal.darkOutline;
            velocityRnd = 0.2f;

            ammo(NHItems.juniorProcessor, new TrailFadeBulletType(){{
                damage = 100;
                speed = 6f;
                lifetime = 120f;
                despawnHit = true;
                hitShake = despawnShake = 2f;

                fragBullet = new DOTBulletType();

                homingRange = 20f;
                homingPower = 0.12f;

                trailChance = 0.8f;
                trailEffect = NHFx.triSpark;

                tracers = 4;
                tracerStroke = 5f;
                tracerUpdateSpacing = 3;
                tracerFadeOffset = 5;
                tracerRandX = 16f;
                tracerStrokeOffset = 6;
                addBeginPoint = true;

                backColor = lightColor = lightningColor = trailColor = hitColor = Pal.techBlue;

                despawnEffect = hitEffect = new OptionalMultiEffect(
                    NHFx.smoothColorCircle(Pal.techBlue, 92f, 150f),
                    NHFx.circleOut(95f, 82f, 2)
                );

            }});

            reload = 120f;
            shootY = 10f;
            rotateSpeed = 2f;
            shootCone = 15f;
            consumeAmmoOnce = true;
            shootSound = NHSounds.laser2;

            consumePower(1000/60f);

            drawer = new DrawTurret("reinforced-"){{
                parts.addAll(
                    new RegionPart("-blade"){{
                        mirror = true;
                        moveX = 1f;
                        moveY = -1.5f;
                        progress = PartProgress.warmup;

                        heatColor = Pal.techBlue;
                        heatLightOpacity = 0.2f;
                    }},
                    new RegionPart("-barrel"){{
                        moveY = -3f;
                        progress = PartProgress.recoil;

                        heatColor = Pal.techBlue;
                        heatLightOpacity = 0.2f;
                    }},
                    new RegionPart("-bottom"){{
                        mirror = true;
                        under = true;
                        moveY = -0.8f;
                        moveX = 0.8f;
                        progress = PartProgress.recoil;

                        heatColor = Pal.techBlue;
                        heatLightOpacity = 0.2f;
                    }},
                    new RegionPart("-upper"){{
                        mirror = true;
                        under = true;
                    }}
                );
            }};

            minWarmup = 0.8f;

            shootWarmupSpeed = 0.08f;

            scaledHealth = 300;
            range = 600f;
            size = 4;

            shootEffect = NHFx.square(Pal.techBlue, 55f, 12, 60, 6);

            limitRange(-5f);
        }};
    }

    public static void loadTest(){
        testShooter = new ItemTurret("emp-turret"){{
            requirements(Category.turret, with(NHItems.presstanium, 10));

            size = 3;
            health = 3200;
            range = 200f;
            reload = 120f;
        }};
    }
}
