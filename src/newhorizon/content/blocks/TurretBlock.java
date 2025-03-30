package newhorizon.content.blocks;

import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.UnitSorts;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.*;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.block.turrets.AdaptItemTurret;
import newhorizon.expand.block.turrets.SpeedupTurret;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.expand.bullets.PosLightningType;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.type.ItemStack.with;

public class TurretBlock {
    public static Block electro;

    public static Block argmot;
    public static Block synchro;

    public static Block testShooter;

    public static void load(){
        synchro = new AdaptItemTurret("synchro"){{
            requirements(Category.turret, BuildVisibility.shown, with(Items.phaseFabric, 20, NHItems.metalOxhydrigen, 90, NHItems.juniorProcessor, 60, NHItems.zeta, 120, Items.plastanium, 80));

            size = 3;
            health = 1420;
            reload = 12f;
            inaccuracy = 0.75f;

            recoil = 0.5f;

            drawer = new DrawTurret(){{
                parts.add(new RegionPart("-shooter"){{
                    under = true;
                    outline = true;
                    moveY = -3f;
                    progress = PartProgress.recoil;
                }});
            }};

            coolant = consumeCoolant(0.2F);
            coolantMultiplier = 2.5f;

            velocityRnd = 0.075f;
            unitSort = UnitSorts.weakest;

            range = 250f;

            shootSound = NHSounds.synchro;

            shoot = new ShootMulti(
                    new ShootPattern(),
                    new ShootBarrel(){{
                        barrels = new float[]{-6.5f, 3f, 0f};
                    }},
                    new ShootBarrel(){{
                        barrels = new float[]{6.5f, 3f, 0f};
                    }}
            );

            ammo(
                    NHItems.zeta, NHBullets.synchroZeta,
                    Items.phaseFabric, NHBullets.synchroPhase
            );

            ammoPerShot = 1;
            maxAmmo = 40;
        }};
        argmot = new SpeedupTurret("argmot"){{
            shoot = new ShootAlternate(){{
                spread = 7f;
            }};

            drawer = new DrawTurret(){{
                parts.add(new RegionPart(){{
                    drawRegion = false;
                    mirror = true;
                    moveY = -2.75f;
                    progress = PartProgress.recoil;
                    children.add(new RegionPart("-shooter"){{
                        heatLayerOffset = 0.001f;
                        heatColor = NHColor.thurmixRed.cpy().a(0.85f);
                        progress = PartProgress.warmup;
                        mirror = outline = true;
                        moveX = 2f;
                        moveY = 2f;
                        moveRot = 11.25f;
                    }});
                }}, new RegionPart("-up"){{
                    layerOffset = 0.3f;

                    turretHeatLayer += layerOffset + 0.1f;

                    heatColor = NHColor.thurmixRed.cpy().a(0.85f);
                    outline = false;
                }});
            }};

            warmupMaintainTime = 120f;

            rotateSpeed = 3f;
            health = 960;
            requirements(Category.turret, with(Items.phaseFabric, 150, NHItems.multipleSteel, 120, NHItems.juniorProcessor, 80, Items.plastanium, 120));
            maxSpeedupScl = 9f;
            speedupPerShoot = 0.3f;
            hasLiquids = true;
            coolant = new ConsumeCoolant(0.15f);
            consumePowerCond(35f, TurretBuild::isActive);
            size = 3;
            range = 200;
            reload = 60f;
            shootCone = 24f;
            shootSound = NHSounds.laser3;
            shootType = new PosLightningType(30f, 120f){{
                lightningColor = hitColor = NHColor.lightSkyBack;
                maxRange = rangeOverride = 250f;
                hitEffect = NHFx.hitSpark;
                smokeEffect = Fx.shootBigSmoke2;
            }};
        }};
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

        //loadTest();
    }

    public static void loadTest(){
        testShooter = new ItemTurret("emp-turret"){{
            requirements(Category.turret, with(NHItems.presstanium, 10));

            size = 3;
            health = 3200;
            range = 200f;
            reload = 120f;

            ammo(NHItems.presstanium, RaidBullets.raidBullet_1);
        }};
    }
}
