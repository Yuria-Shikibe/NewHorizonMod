package newhorizon.content.blocks;

import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.draw.DrawTurret;
import newhorizon.content.*;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.type.ItemStack.with;

public class TurretBlock {
    public static Block electro, ancientRailgun;

    public static Block testShooter;

    public static void load(){
        electro = new ItemTurret("electro"){{
            requirements(Category.turret, with(Items.lead, 200, Items.plastanium, 80, NHItems.juniorProcessor, 100, NHItems.multipleSteel, 150, Items.graphite, 100));
            canOverdrive = false;

            health = 3200;
            outlineColor = Pal.darkOutline;
            velocityRnd = 0.2f;

            ammo(
                NHItems.juniorProcessor, new ArtilleryBulletType(){{
                    damage = 40;
                    speed = 6f;
                    lifetime = 120f;
                    hitShake = despawnShake = 1.2f;
                    status = NHStatusEffects.emp2;
                    hitSound = Sounds.none;

                    fragBullet = new DOTBulletType(){{
                        DOTDamage = damage = 40f;
                        DOTRadius = 12f;
                        radIncrease = 0.25f;
                        fx = NHFx.triSpark1;
                        lightningColor = Pal.techBlue;
                    }};
                    fragBullets = 1;

                    homingRange = 20f;
                    homingPower = 0.12f;

                    trailChance = 0.8f;
                    trailEffect = NHFx.triSpark1;

                    backColor = lightColor = lightningColor = trailColor = hitColor = Pal.techBlue;

                    despawnEffect = Fx.none;
                    hitEffect = new OptionalMultiEffect(
                        NHFx.smoothColorCircle(Pal.techBlue, 78f, 150f, 0.6f),
                        NHFx.circleOut(70, 60f, 2)
                    );
                }},
                NHItems.seniorProcessor, new ArtilleryBulletType(){{
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
                }}
            );

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
            range = 350f;
            size = 4;

            shootEffect = NHFx.square(NHColor.ancient, 55f, 12, 60, 6);

            limitRange(-5f);
        }};

        /*
        ancientRailgun = new ItemTurret("ancient-railgun"){{
            requirements(Category.turret, with(Items.lead, 200, Items.plastanium, 80, NHItems.juniorProcessor, 100, NHItems.multipleSteel, 150, Items.graphite, 100));
            canOverdrive = false;

            health = 3200;
            outlineColor = Pal.darkOutline;

            size = 8;
        }};

         */
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
