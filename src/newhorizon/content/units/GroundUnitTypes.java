package newhorizon.content.units;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.EntityMapping;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.*;
import newhorizon.expand.bullets.EmpDotBulletType;
import newhorizon.expand.bullets.TracerBulletType;
import newhorizon.expand.units.unitType.NHUnitType;
import newhorizon.util.graphic.OptionalMultiEffect;

public class GroundUnitTypes {

    public static UnitType origin, thynomo, annihilation;

    static {
        EntityMapping.nameMap.put(NewHorizon.name("origin"), EntityMapping.map(4));
        EntityMapping.nameMap.put(NewHorizon.name("thynomo"), EntityMapping.map(4));
        EntityMapping.nameMap.put(NewHorizon.name("annihilation"), EntityMapping.map(4));
    }

    public static void load() {
        origin = new NHUnitType("origin") {{
            speed = 0.6f;
            hitSize = 8f;
            health = 240f;

            weapons.add(new Weapon(NewHorizon.name("origin-weapon")) {{
                x = 0f;
                y = 0f;

                shootX = 4f;
                shootY = 4.5f;
                reload = 15f;
                recoil = 1.5f;
                shake = 0.75f;
                inaccuracy = 4f;
                shootCone = 20f;
                velocityRnd = 0.15f;

                top = false;
                rotate = true;
                mirror = true;
                rotationLimit = 15f;

                shootSound = NHSounds.shootScatter1;

                shoot = new ShootSpread() {{
                    shots = 3;
                    spread = 3;
                }};

                bullet = new BasicBulletType(5f, 12f) {{
                    width = 5f;
                    height = 18f;
                    lifetime = 35f;

                    trailWidth = 1.2f;
                    trailLength = 4;
                    trailParam = 1f;

                    hitColor = NHColor.bulletFrontColor;
                    lightColor = NHColor.bulletBackColor;
                    trailColor = NHColor.bulletFrontColor;

                    frontColor = NHColor.bulletFrontColor;
                    backColor = NHColor.bulletBackColor;

                    shootEffect = NHFx.shootLine(backColor, 12, 0.85f, 3, 30);
                    despawnEffect = NHFx.square(hitColor, 16f, 2, 12, 2f);
                    hitEffect = NHFx.lightningHitSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;
                }};
            }});
        }};

        thynomo = new NHUnitType("thynomo") {{
            speed = 0.45f;
            armor = 9f;
            hitSize = 15f;
            health = 800f;

            riseSpeed = 0.05f;
            rotateSpeed = 2.5f;
            mechLandShake = 2f;
            mechFrontSway = 0.55f;


            canBoost = true;
            boostMultiplier = 2f;

            engineOffset = 7.4f;
            engineSize = 4.25f;

            weapons.add(new Weapon(NewHorizon.name("thynomo-weapon")) {{
                x = 8f;
                y = 0f;

                shootX = 0f;
                shootY = 9.25f;
                recoil = 1.5f;
                shake = 0.75f;
                inaccuracy = 4f;
                rotationLimit = 12f;

                top = false;
                rotate = true;
                alternate = false;
                alwaysContinuous = true;

                shootStatus = StatusEffects.slow;
                shootStatusDuration = 15f;

                shootSound = NHSounds.loopLaser2;

                bullet = new ContinuousLaserBulletType(18f) {{
                    length = 80f;
                    width = 1.65f;

                    incendChance = 0.025f;
                    incendSpread = 5.0f;
                    incendAmount = 1;

                    shake = 3;
                    colors = new Color[]{NHColor.lightSkyFront.cpy().mul(0.8f, 0.85f, 0.9f, 0.2f), NHColor.lightSkyBack.cpy().mul(1f, 1f, 1f, 0.5f), NHColor.lightSkyBack, Color.white};
                    oscScl = 0.2f;
                    oscMag = 1.25f;
                    lifetime = 15f;
                    lightColor = hitColor = NHColor.lightSkyBack;
                    hitEffect = NHFx.lightSkyCircleSplash;
                    shootEffect = NHFx.square(hitColor, 22f, 4, 16, 3f);
                    smokeEffect = Fx.shootBigSmoke;

                    pierce = true;
                    pierceCap = 4;
                }};
            }});
        }};

        annihilation = new NHUnitType("annihilation") {{
            abilities.add(new ForceFieldAbility(120F, 2F, 10000F, 800F, 4, 0f));
            engineOffset = 15.0F;
            engineSize = 6.5F;
            speed = 0.275f;
            hitSize = 33f;
            health = 22000f;
            buildSpeed = 2.8f;
            armor = 25f;
            rotateSpeed = 1.8f;
            singleTarget = false;
            fallSpeed = 0.016f;
            mechStepParticles = true;
            stepShake = 0.5f;
            canBoost = true;
            mechLandShake = 6f;
            boostMultiplier = 3.5f;

            weapons.add(
                    new Weapon(NewHorizon.name("large-launcher")) {{
                        x = 19.5f;
                        y = 0f;

                        shootX = 0f;
                        shootY = 16f;
                        recoil = 2.5f;
                        shake = 3.5f;
                        reload = 40f;
                        inaccuracy = 0.5f;
                        shootCone = 15f;
                        rotationLimit = 12f;

                        top = false;
                        rotate = false;
                        alternate = true;
                        predictTarget = false;

                        shootSound = NHSounds.shootFlak6;

                        shoot = new ShootSpread() {{
                            shots = 3;
                            shotDelay = 5f;
                            spread = 8f;
                        }

                            @Override
                            public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
                                for(int i = 0; i < shots; i++){
                                    float angleOffset = (Mathf.zero(totalShots % 2)? 1 : -1) * (i * spread - (shots - 1) * spread / 2f);
                                    handler.shoot(0, 0, angleOffset, firstShotDelay + shotDelay * i);
                                }
                                barrelIncrementer.run();
                            }
                        };

                        bullet = new TracerBulletType() {{
                            speed = 7.5f;
                            damage = 450f;
                            lifetime = 45f;

                            width = 18f;
                            height = 18f;
                            drawSize = 40f;
                            shrinkX = 0;
                            shrinkY = 0;

                            trailWidth = 1.75f;
                            trailLength = 15;
                            trailChance = 0.75f;
                            trailRotation = true;

                            tracerRandRange = 5f;
                            tracerUpdateInterval = 0.25f;

                            homingPower = 0.03f;
                            homingRange = 80f;
                            homingDelay = 25f;

                            knockback = 0.75f;
                            statusDuration = 30f;

                            keepVelocity = false;

                            hitColor = NHColor.bulletFrontColor;
                            lightColor = NHColor.bulletBackColor;
                            trailColor = NHColor.bulletFrontColor;

                            frontColor = NHColor.bulletFrontColor;
                            backColor = NHColor.bulletBackColor;

                            status = StatusEffects.shocked;

                            ejectEffect = Fx.none;
                            hitEffect = Fx.none;
                            trailEffect = NHFx.lineTrail(15f, 0.75f, 15f, 18f, 15f);
                            despawnEffect = NHFx.shootLine(55f, 2.25f, 4, 10f);
                            shootEffect = NHFx.shootLine(40f, 15f);
                            smokeEffect = Fx.shootBigSmoke;

                            buildingDamageMultiplier = 0.05f;

                            fragOnHit = true;
                            fragOnDespawn = true;
                            fragBullets = 1;
                            fragRandomSpread = 0f;

                            fragBullet = new ShrapnelBulletType() {{
                                lifetime = 15f;
                                width = 8f;
                                hitLarge = true;
                                length = 80;
                                damage = 300f;
                                serrationFadeOffset = 0.7f;
                                status = NHStatusEffects.ultFireBurn;
                                statusDuration = 120f;
                                fromColor = NHColor.lightSkyFront;
                                toColor = NHColor.lightSkyBack;
                                shootEffect = NHFx.lightningHitSmall(NHColor.lightSkyBack);
                            }};

                        }};
                    }}
            );

        }};
    }
}
