package newhorizon.content.blocks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.*;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.BuildVisibility;
import newhorizon.content.*;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.block.turrets.AdaptItemTurret;
import newhorizon.expand.block.turrets.ContinuousOverheatTurret;
import newhorizon.expand.block.turrets.SpeedupTurret;
import newhorizon.expand.bullets.AdaptBulletType;
import newhorizon.expand.bullets.DOTBulletType;
import newhorizon.expand.bullets.PosLightningType;
import newhorizon.expand.bullets.UpgradePointLaserBulletType;
import newhorizon.expand.game.NHUnitSorts;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.type.ItemStack.with;

public class TurretBlock {
    public static Block thermo, pulse, electro, argmot, synchro, slavio, concentration;

    public static Block testShooter;

    public static void load() {
        thermo = new PowerTurret("thermo-turret") {{
            requirements(Category.turret, BuildVisibility.shown, with(
                    NHItems.titanium, 20,
                    NHItems.silicon, 25
            ));

            size = 1;
            health = 300;
            range = 160f;
            reload = 90f;
            inaccuracy = 3f;
            shootCone = 50f;
            rotateSpeed = 8f;

            shootEffect = Fx.lightningShoot;
            smokeEffect = Fx.shootSmallSmoke;
            shootSound = NHSounds.thermoShoot;

            shootType = new AdaptBulletType() {{
                setDamage(this, 20, 30);
                bundleName = "basic-thermo-bullet";

                speed = 6.5f;
                lifetime = 27f;
                knockback = 0.5f;

                width = 5f;
                height = 22f;
                drawSize = 120f;

                trailWidth = 1.25f;
                trailLength = 15;
                trailColor = Pal.lancerLaser;

                homingDelay = 1f;
                homingPower = 0.2f;
                homingRange = 120f;

                backColor = hitColor = Pal.lancerLaser;
                frontColor = Color.white;
                hitEffect = new Effect(12f, (e) -> {
                    Draw.color(Pal.lancerLaser, Color.white, e.fout() * 0.75f);
                    Lines.stroke(e.fout() * 1.5F);
                    Angles.randLenVectors(e.id, 3, e.finpow() * 17.0F, e.rotation, 360.0F, (x, y) -> {
                        float ang = Mathf.angle(x, y);
                        Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4.0F + 1.0F);
                    });
                });
            }};
            shoot = new ShootPattern() {{
                shots = 5;
                shotDelay = 6f;
            }};

            consumeLiquid(NHLiquids.xenFluid, 5 / 60f);
            consumePower(250.0001f / 60f);
        }};

        pulse = new ItemTurret("pulse") {{
            requirements(Category.turret, with(
                    NHItems.titanium, 50,
                    NHItems.silicon, 75
            ));

            size = 2;
            health = 960;
            range = 160;
            reload = 120f;
            recoil = 3f;
            shake = 3f;
            shootCone = 30f;
            inaccuracy = 4f;
            maxAmmo = 120;
            ammoPerShot = 12;
            minWarmup = 0.8f;

            outlineColor = Pal.darkOutline;
            smokeEffect = Fx.shootBigSmoke;
            shootSound = Sounds.shootSnap;

            ammo(Items.titanium, new AdaptBulletType() {{
                setDamage(this, 45, 20);
                bundleName = "pulse-bullet-titanium";

                width = 8f;
                height = 25f;
                speed = 5f;
                ammoMultiplier = 4;

                hitColor = backColor = lightColor = trailColor = Items.titanium.color.cpy().lerp(Color.white, 0.1f);
                frontColor = backColor.cpy().lerp(Color.white, 0.35f);

                hitEffect = NHFx.crossBlast(hitColor, height + width);
                shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 12f, 2f);
            }}, Items.plastanium, new AdaptBulletType() {{
                setDamage(this, 20, 30, 45, 15);
                bundleName = "pulse-bullet-plastanium";

                width = 8f;
                height = 25f;
                speed = 5f;
                ammoMultiplier = 4;

                hitColor = backColor = lightColor = trailColor = Items.plastanium.color.cpy().lerp(Color.white, 0.1f);
                frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                hitEffect = NHFx.hitSpark(hitColor, 30, 6, 32, 1.4f, 7f);
                shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
            }}, NHItems.zeta, new AdaptBulletType() {{
                setDamage(this, 32, 80, 60);
                bundleName = "pulse-bullet-zeta";

                speed = 5f;
                width = 8f;
                height = 25f;
                ammoMultiplier = 4;

                status = StatusEffects.shocked;
                statusDuration = 60f;

                lightningColor = hitColor = backColor = lightColor = trailColor = Items.pyratite.color.cpy().lerp(Color.white, 0.1f);
                frontColor = backColor.cpy().lerp(Color.white, 0.35f);
                hitEffect = NHFx.crossBlast(hitColor, height + width);
                shootEffect = despawnEffect = NHFx.square(hitColor, 20f, 3, 20f, 2f);
            }});
            shoot = new ShootPattern() {{
                shots = 8;
                shotDelay = 3f;
            }};
            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-barrel") {{
                    under = true;
                    outline = true;
                    moveY = -3f;
                    progress = PartProgress.recoil;
                }});
            }};

            coolant = consumeCoolant(0.1f);

            limitRange();
        }};

        synchro = new AdaptItemTurret("synchro") {{
            requirements(Category.turret, BuildVisibility.shown, with(
                    NHItems.juniorProcessor, 60, NHItems.presstanium, 80, Items.tungsten, 50));

            size = 3;
            health = 240 * size * size;
            reload = 12f;
            inaccuracy = 0.75f;

            recoil = 0.5f;

            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-shooter") {{
                    under = true;
                    outline = true;
                    moveY = -3f;
                    progress = PartProgress.recoil;
                }});
            }};

            coolant = consumeCoolant(0.2F);
            coolantMultiplier = 2.5f;

            velocityRnd = 0.075f;
            unitSort = NHUnitSorts.noShield;

            range = 250f;

            shootSound = NHSounds.synchro;

            shoot = new ShootMulti(
                    new ShootPattern(),
                    new ShootBarrel() {{
                        barrels = new float[]{-6.5f, 3f, 0f};
                    }},
                    new ShootBarrel() {{
                        barrels = new float[]{6.5f, 3f, 0f};
                    }}
            );

            ammo(
                    Items.titanium, NHBullets.synchroTitanium,
                    Items.tungsten, NHBullets.synchroTungsten,
                    NHItems.zeta, NHBullets.synchroZeta,
                    NHItems.fusionEnergy, NHBullets.synchroFusionEnergy
            );

            ammoPerShot = 1;
            maxAmmo = 40;
        }};

        argmot = new SpeedupTurret("argmot") {{
            requirements(Category.turret, with(NHItems.juniorProcessor, 80, NHItems.presstanium, 120, Items.tungsten, 80));

            shoot = new ShootAlternate() {{
                spread = 7f;
            }};

            drawer = new DrawTurret() {{
                parts.add(new RegionPart() {{
                    drawRegion = false;
                    mirror = true;
                    moveY = -2.75f;
                    progress = PartProgress.recoil;
                    children.add(new RegionPart("-shooter") {{
                        heatLayerOffset = 0.001f;
                        heatColor = NHColor.thurmixRed.cpy().a(0.85f);
                        progress = PartProgress.warmup;
                        mirror = outline = true;
                        moveX = 2f;
                        moveY = 2f;
                        moveRot = 11.25f;
                    }});
                }}, new RegionPart("-up") {{
                    layerOffset = 0.3f;

                    turretHeatLayer += layerOffset + 0.1f;

                    heatColor = NHColor.thurmixRed.cpy().a(0.85f);
                    outline = false;
                }});
            }};

            warmupMaintainTime = 120f;

            rotateSpeed = 3f;
            health = 960;
            maxSpeedupScl = 4f;
            speedupPerShoot = 0.2f;
            overheatTime = 600f;
            overheatCoolAmount = 2f;
            hasLiquids = true;
            coolant = new ConsumeCoolant(0.15f) {{
                booster = false;
                optional = false;
            }};
            size = 3;
            range = 200;
            reload = 60f;
            shootCone = 24f;
            shootSound = NHSounds.laser3;
            shootType = new PosLightningType() {{
                damage = 150f;
                shieldDamageMultiplier = 0.2f;
                lightningColor = hitColor = NHColor.lightSkyBack;
                maxRange = rangeOverride = 250f;
                hitEffect = NHFx.hitSpark;
                smokeEffect = Fx.shootBigSmoke2;
            }};

            consumePowerCond(5f, TurretBuild::isActive);
        }};
        slavio = new ItemTurret("slavio") {{
            requirements(Category.turret, with(NHItems.juniorProcessor, 120, NHItems.presstanium, 150, Items.carbide, 150, NHItems.metalOxhydrigen, 80));

            ammo(
                    NHItems.zeta, new AdaptBulletType() {{
                        damage = 10;
                        splashDamage = 60f;
                        splashDamageRadius = 16f;
                        shieldDamageMultiplier = 0.6f;

                        backSprite = "missile-large-back";
                        sprite = "mine-bullet";

                        height = 9f;
                        width = 5.6f;

                        frontColor = NHItems.zeta.color;
                        backColor = trailColor = hitColor = Pal.bulletYellowBack;

                        trailChance = 0.44f;
                        trailLength = 12;
                        trailWidth = 2f;
                        trailEffect = NHFx.triSpark;
                        trailRotation = true;

                        shootEffect = Fx.shootBig2;
                        smokeEffect = Fx.shootSmokeDisperse;
                        hitEffect = despawnEffect = NHFx.hitSpark;

                        despawnShake = 7f;

                        speed = 5.2f;
                        shrinkY = 0.3f;

                        homingDelay = 0f;
                        homingRange = 40f;
                        homingPower = 0.05f;

                        ammoMultiplier = 3f;
                        lifetime = 80f;
                    }},
                    NHItems.metalOxhydrigen, new AdaptBulletType() {{
                        damage = 10;
                        splashDamage = 60f;
                        splashDamageRadius = 20f;
                        shieldDamageMultiplier = 0.5f;

                        backSprite = "missile-large-back";
                        sprite = "mine-bullet";

                        height = 11f;
                        width = 6f;

                        frontColor = NHColor.lightSky;
                        backColor = trailColor = hitColor = NHColor.lightSkyBack;

                        trailChance = 0.44f;
                        trailLength = 12;
                        trailWidth = 2f;
                        trailEffect = NHFx.triSpark;
                        trailRotation = true;

                        shootEffect = Fx.shootBig2;
                        smokeEffect = Fx.shootSmokeDisperse;
                        hitEffect = despawnEffect = NHFx.hitSpark;

                        despawnShake = 7f;

                        speed = 7f;
                        shrinkY = 0.3f;

                        homingDelay = 20f;
                        homingRange = 100f;
                        homingPower = 0.05f;

                        ammoMultiplier = 4f;
                        reloadMultiplier = 1.35f;
                        lifetime = 60f;
                    }},
                    Items.surgeAlloy, new AdaptBulletType() {{
                        damage = 10;
                        splashDamage = 120f;
                        splashDamageRadius = 20f;
                        shieldDamageMultiplier = 0.8f;
                        status = StatusEffects.shocked;
                        backSprite = "missile-large-back";
                        sprite = "mine-bullet";

                        height = 11f;
                        width = 6f;

                        lightningDamage = 25;
                        lightning = 4;
                        lightningLength = 6;

                        frontColor = Pal.surgeAmmoBack;
                        backColor = trailColor = hitColor = Pal.surgeAmmoBack;

                        trailChance = 0.44f;
                        trailLength = 12;
                        trailWidth = 2f;
                        trailEffect = NHFx.triSpark;
                        trailRotation = true;

                        shootEffect = Fx.shootBig2;
                        smokeEffect = Fx.shootSmokeDisperse;
                        hitEffect = despawnEffect = NHFx.hitSpark;

                        despawnShake = 7f;

                        speed = 8f;
                        shrinkY = 0.3f;

                        homingDelay = 20f;
                        homingRange = 100f;
                        homingPower = 0.05f;

                        ammoMultiplier = 2f;
                        reloadMultiplier = 1.5f;
                        lifetime = 60f;
                    }}
            );
            shoot = new ShootAlternate() {{
                spread = 4.8f;
                shotDelay = 4;
                shots = 4;
                barrels = 4;
            }};

            reload = 40f;
            shootY = 12f;
            rotateSpeed = 5f;
            shootCone = 15f;
            consumeAmmoOnce = true;
            shootSound = NHSounds.scatter;

            unitSort = NHUnitSorts.noShield;

            drawer = new DrawTurret() {{
                parts.addAll(
                        new RegionPart("-barrel") {{
                            moveY = -2f;
                            progress = PartProgress.recoil;
                        }},
                        new RegionPart("-bottom") {{
                            mirror = true;
                            under = true;
                            moveX = -0.5f;
                            moveY = -2f;
                            moveRot = 45f;
                        }},
                        new RegionPart("-bottom") {{
                            mirror = true;
                            under = true;
                            moveX = -2f;
                            moveY = 0.5f;
                        }},
                        new RegionPart("-front") {{
                            mirror = true;
                            under = true;
                            moveY = -1f;
                            moveX = -1f;
                        }});
            }};

            shootWarmupSpeed = 0.08f;

            minWarmup = 0.8f;

            scaledHealth = 300;
            range = 320f;
            trackingRange = range * 1.4f;
            size = 3;

            limitRange(-5f);

            coolant = consumeCoolant(0.25f);
            coolantMultiplier = 2.5f;
        }};
        concentration = new ContinuousOverheatTurret("concentration") {{
            requirements(Category.turret, with(Items.carbide, 500, NHItems.setonAlloy, 300, NHItems.seniorProcessor, 200));

            shootType = new UpgradePointLaserBulletType() {{
                damage = 500;
                shieldDamageMultiplier = 2f;

                hitEffect = NHFx.hitSpark;
                buildingDamageMultiplier = 0.5f;
                damageInterval = 6;
                sprite = "laser-white";
                status = NHStatusEffects.emp3;
                statusDuration = 60;
                oscScl /= 1.77f;
                oscMag /= 1.33f;
                hitShake = 2;
                range = 75 * 8;

                trailLength = 8;
            }};

            drawer = new DrawTurret() {{
                parts.add(new RegionPart("-charger") {{
                    mirror = true;
                    under = true;
                    moveRot = 10;
                    moveX = 4.677f;
                    moveY = 6.8f;
                }});
                parts.add(new RegionPart("-side") {{
                    mirror = true;
                    under = true;
                    moveRot = 10;
                    moveX = 2.75f;
                    moveY = 2;
                }});
                parts.add(new RegionPart("-barrel") {{
                    moveY = -7.5f;
                    progress = progress.curve(Interp.pow2Out);
                }});
            }};

            shootSound = Sounds.none;
            loopSoundVolume = 1f;
            loopSound = NHSounds.largeBeam;

            shootWarmupSpeed = 0.08f;
            shootCone = 360f;

            aimChangeSpeed = 1.75f;
            rotateSpeed = 1.45f;
            canOverdrive = false;

            shootY = 16f;
            minWarmup = 0.8f;
            warmupMaintainTime = 45;
            shootWarmupSpeed /= 2;
            outlineColor = Pal.darkOutline;
            size = 5;
            range = 75 * 8f;
            scaledHealth = 300;
            armor = 10;

            unitSort = NHUnitSorts.slowest;

            consumePower(16);
            consumeLiquid(NHLiquids.xenFluid, 12f / 60f);
        }};
        electro = new ItemTurret("electro") {{
            requirements(Category.turret, with(NHItems.juniorProcessor, 200, Items.carbide, 150, Items.surgeAlloy, 100, NHItems.phaseFabric, 100));
            canOverdrive = false;

            health = 3200;
            outlineColor = Pal.darkOutline;
            velocityRnd = 0.2f;

            ammo(
                    NHItems.juniorProcessor, new ArtilleryBulletType() {{
                        damage = 40;
                        speed = 6f;
                        lifetime = 120f;
                        hitShake = despawnShake = 1.2f;
                        status = NHStatusEffects.emp2;
                        hitSound = Sounds.none;

                        fragBullet = new DOTBulletType() {{
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
                    NHItems.seniorProcessor, new ArtilleryBulletType() {{
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
                            lightningColor = NHItems.seniorProcessor.color;
                        }};
                        fragBullets = 1;

                        homingRange = 22f;
                        homingPower = 0.13f;

                        trailChance = 0.8f;
                        trailEffect = NHFx.triSpark2;

                        backColor = lightColor = lightningColor = trailColor = hitColor = NHItems.seniorProcessor.color;
                        rangeChange = 45;

                        despawnEffect = Fx.none;
                        hitEffect = new OptionalMultiEffect(
                                NHFx.smoothColorCircle(NHItems.seniorProcessor.color, 100f, 125f, 0.3f),
                                NHFx.circleOut(150f, 100f, 4),
                                NHFx.circleOut(78f, 75f, 2),
                                NHFx.subEffect(130f, 85f, 12, 30f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                                    float fout = Interp.pow2Out.apply(1 - fin);
                                    float finpow = Interp.pow3Out.apply(fin);
                                    Tmp.v1.trns(rot, 25 * finpow);
                                    Draw.color(NHItems.seniorProcessor.color);
                                    for (int s : Mathf.signs) {
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

            consumePower(1000 / 60f);

            drawer = new DrawTurret("reinforced-") {{
                parts.addAll(
                        new RegionPart("-blade") {{
                            mirror = true;
                            moveX = 1f;
                            moveY = -1.5f;
                            progress = PartProgress.warmup;

                            heatColor = Pal.techBlue;
                            heatLightOpacity = 0.2f;
                        }},
                        new RegionPart("-barrel") {{
                            moveY = -3f;
                            progress = PartProgress.recoil;

                            heatColor = Pal.techBlue;
                            heatLightOpacity = 0.2f;
                        }},
                        new RegionPart("-bottom") {{
                            mirror = true;
                            under = true;
                            moveY = -0.8f;
                            moveX = 0.8f;
                            progress = PartProgress.recoil;

                            heatColor = Pal.techBlue;
                            heatLightOpacity = 0.2f;
                        }},
                        new RegionPart("-upper") {{
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
    }

    public static void loadTest() {
        testShooter = new ItemTurret("emp-turret") {{
            requirements(Category.turret, with(NHItems.presstanium, 10));

            size = 3;
            health = 3200;
            range = 200f;
            reload = 120f;

            ammo(NHItems.presstanium, RaidBullets.raidBullet_1);
        }};
    }
}
