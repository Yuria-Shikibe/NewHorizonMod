package newhorizon.content.units;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import mindustry.ai.types.BuilderAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.EntityMapping;
import mindustry.gen.Sounds;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.bullets.DecelerateFlakBulletType;
import newhorizon.expand.bullets.TracerBulletType;
import newhorizon.expand.units.BoostAbility;
import newhorizon.expand.units.unitType.NHUnitType;
import newhorizon.util.graphic.OptionalMultiEffect;

import static mindustry.Vars.tilePayload;

public class CoreUnitTypes {

    public static UnitType scalar, vector, martix, tensor;


    static {
        EntityMapping.nameMap.put(NewHorizon.name("scalar"), EntityMapping.idMap[5]);
        EntityMapping.nameMap.put(NewHorizon.name("vector"), EntityMapping.idMap[5]);
        EntityMapping.nameMap.put(NewHorizon.name("martix"), EntityMapping.idMap[5]);
        EntityMapping.nameMap.put(NewHorizon.name("tensor"), EntityMapping.idMap[5]);
    }

    public static void load() {
        scalar = new NHUnitType("scalar") {{
            armor = 2;
            health = 600;
            hitSize = 12f;

            drag = 0.1f;
            speed = 3.5f;
            accel = 0.5f;
            rotateSpeed = 12.5f;
            strafePenalty = 0.3f;

            lightRadius = 15f;
            lightOpacity = 0.1f;

            trailLength = 8;
            outlineRadius = 4;
            itemCapacity = 70;
            payloadCapacity = 2 * tilePayload;

            buildBeamOffset = 2f;
            buildSpeed = 2f;

            mineTier = 2;
            mineSpeed = 8f;
            engineSize = 0;

            flying = true;
            mineWalls = true;
            mineFloor = true;
            aiController = BuilderAI::new;
            engines.add(new UnitEngine(0, -5.25f, 1.85f, -90));
            abilities.add(new BoostAbility(false, 1.2f, 180f));

            weapons.add(new Weapon("scalar-pulse-cannon") {{
                x = 3f;

                reload = 60;
                recoil = 1.5f;
                inaccuracy = 5f;
                shootCone = 15f;
                rotateSpeed = 8.25f;

                top = false;
                mirror = true;
                rotate = true;
                alternate = true;

                heatColor = NHColor.lightSky;
                shootSound = NHSounds.shootPulse5;

                shoot = new ShootSpread() {{
                    shots = 4;
                    spread = 16f;
                }};

                bullet = new BasicBulletType(4.5f, 25f) {{
                    lifetime = 28f;
                    inaccuracy = 4f;

                    width = 8f;
                    height = 20f;
                    drawSize = 120f;

                    trailWidth = 1.5f;
                    trailLength = 6;
                    trailParam = 1f;
                    trailChance = 0.1f;

                    homingPower = 0.1f;
                    homingRange = 60f;
                    homingDelay = 10f;
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
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 2, 12f, 2);
                    hitEffect = NHFx.lightningHitSmall(backColor);
                    shootEffect = NHFx.shootLineSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;

                    buildingDamageMultiplier = 0.05f;
                }};
            }});


            //immunities = ObjectSet.with(NHStatusEffects.scannerDown);
        }};

        vector = new NHUnitType("vector") {{
            armor = 5;
            health = 1000;
            hitSize = 15f;

            drag = 0.1f;
            speed = 4.5f;
            accel = 0.85f;
            rotateSpeed = 12.5f;
            strafePenalty = 0.3f;

            lightRadius = 18f;
            lightOpacity = 0.1f;

            trailLength = 9;
            outlineRadius = 4;
            itemCapacity = 100;
            payloadCapacity = 4 * tilePayload;

            buildBeamOffset = 3f;
            buildSpeed = 2.5f;

            mineTier = 3;
            mineSpeed = 10f;
            engineSize = 0;

            flying = true;
            mineWalls = true;
            mineFloor = true;
            aiController = BuilderAI::new;
            engines.add(new UnitEngine(0, -5.25f, 1.85f, -90));
            abilities.add(new BoostAbility(false, 1.2f, 180f));

            weapons.add(new Weapon("vector-flak-cannon") {{
                reload = 60;
                recoil = 1.5f;
                inaccuracy = 5f;
                shootCone = 15f;
                rotateSpeed = 8.25f;

                top = false;
                mirror = true;
                rotate = true;
                alternate = true;

                heatColor = NHColor.lightSky;
                shootSound = NHSounds.shootFlak5;

                bullet = new DecelerateFlakBulletType() {{
                    speed = 5.5f;
                    damage = 80f;
                    lifetime = 35f;
                    inaccuracy = 2f;
                    explodeRange = 5f;
                    splashDamage = 80;
                    splashDamageRadius = 32f;

                    width = 7f;
                    height = 7f;
                    drawSize = 40f;
                    shrinkX = 0;
                    shrinkY = 0;

                    trailWidth = 2.5f;
                    trailLength = 10;
                    trailParam = 1f;
                    trailChance = 0.1f;

                    homingPower = 0.02f;
                    homingRange = 120f;
                    homingDelay = 15f;
                    knockback = 0.75f;
                    statusDuration = 30f;

                    keepVelocity = false;
                    sprite = NewHorizon.name("circle-bolt");

                    hitColor = NHColor.bulletFrontColor;
                    lightColor = NHColor.bulletBackColor;
                    trailColor = NHColor.bulletFrontColor;

                    frontColor = NHColor.bulletFrontColor;
                    backColor = NHColor.bulletBackColor;

                    status = StatusEffects.shocked;

                    ejectEffect = Fx.none;
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 3, 16f, 3);
                    hitEffect = new OptionalMultiEffect(
                            NHFx.lineCircleOut(backColor, 32, 15, 1.2f),
                            NHFx.hitSpark(backColor, 45f, 12, 30, 1, 8)
                    );
                    shootEffect = NHFx.shootLineSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;
                    hitSound = Sounds.explosionMissile;

                    buildingDamageMultiplier = 0.05f;
                }};
            }});
        }};

        martix = new NHUnitType("martix") {{
            armor = 10;
            health = 1500;
            hitSize = 20f;

            drag = 0.12f;
            speed = 4.5f;
            accel = 0.85f;
            rotateSpeed = 12.5f;
            strafePenalty = 0.3f;

            lightRadius = 22f;
            lightOpacity = 0.1f;

            trailLength = 10;
            outlineRadius = 4;
            itemCapacity = 150;
            payloadCapacity = 6 * tilePayload;

            buildBeamOffset = 5f;
            buildSpeed = 3.5f;

            mineTier = 5;
            mineSpeed = 15f;
            engineSize = 0;

            flying = true;
            mineWalls = true;
            mineFloor = true;
            aiController = BuilderAI::new;
            engines.add(new UnitEngine(3, -8f, 1.85f, -90), new UnitEngine(-3, -8f, 1.85f, -90));
            abilities.add(new BoostAbility(false, 1.2f, 180f));

            weapons.add(new Weapon("martix-mx-pulse-gun") {{
                x = 0f;

                reload = 45;
                recoil = 1.5f;
                inaccuracy = 1.2f;
                shootCone = 15f;
                rotateSpeed = 8.25f;

                top = false;
                mirror = false;
                rotate = true;
                alternate = true;

                heatColor = NHColor.lightSky;
                shootSound = NHSounds.shootPulse3;

                shoot = new ShootMulti(
                        new ShootPattern() {{
                            shots = 3;
                            shotDelay = 5f;
                        }},
                        new ShootAlternate() {{
                            shots = 2;
                            spread = 8f;
                        }}
                );

                bullet = new BasicBulletType() {{
                    speed = 6.5f;
                    damage = 75f;
                    lifetime = 40f;
                    inaccuracy = 1f;

                    width = 8f;
                    height = 12f;
                    drawSize = 40f;
                    shrinkX = 0;
                    shrinkY = 0;

                    trailWidth = 2.5f;
                    trailLength = 3;
                    trailParam = 1f;
                    trailChance = 0.1f;

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
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 3, 24f, 3);
                    hitEffect = NHFx.hitSpark(backColor, 45f, 8, 15, 1, 4);
                    shootEffect = NHFx.shootCircleSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;

                    buildingDamageMultiplier = 0.05f;
                }};
            }});
        }};

        tensor = new NHUnitType("tensor") {{
            armor = 15;
            health = 2500;
            hitSize = 24f;

            drag = 0.12f;
            speed = 5.5f;
            accel = 0.85f;
            rotateSpeed = 14.5f;
            strafePenalty = 0.3f;

            lightRadius = 24f;
            lightOpacity = 0.1f;

            trailLength = 10;
            outlineRadius = 4;
            itemCapacity = 150;
            payloadCapacity = 9 * tilePayload;

            buildBeamOffset = 6f;
            buildSpeed = 3.5f;

            mineTier = 8;
            mineSpeed = 20f;
            engineSize = 0;

            flying = true;
            mineWalls = true;
            mineFloor = true;
            aiController = BuilderAI::new;
            engines.add(
                    new UnitEngine(4.5f, -7.2f, 2.2f, -115),
                    new UnitEngine(0, -9.8f, 2.8f, -90),
                    new UnitEngine(-4.5f, -7.2f, 2.2f, -65)
            );
            abilities.add(new BoostAbility(false, 1.5f, 180f));

            weapons.add(new Weapon("martix-mx-pulse-gun") {{
                x = 5f;

                reload = 8f;
                recoil = 1.5f;
                inaccuracy = 1.2f;
                shootCone = 15f;
                rotateSpeed = 8.25f;

                top = false;
                mirror = true;
                rotate = true;
                alternate = true;

                heatColor = NHColor.lightSky;
                shootSound = NHSounds.shootThermo3;

                bullet = new TracerBulletType() {{
                    speed = 6.5f;
                    damage = 55f;
                    lifetime = 45f;
                    inaccuracy = 1f;

                    width = 8f;
                    height = 15f;
                    drawSize = 40f;
                    shrinkX = 0;
                    shrinkY = 0;

                    trailWidth = 1.25f;
                    trailLength = 10;
                    trailParam = 1f;
                    trailChance = 0.1f;

                    tracerRandRange = 3f;
                    tracerUpdateInterval = 0.2f;

                    homingPower = 0.02f;
                    homingRange = 120f;
                    homingDelay = 10f;
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
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 3, 24f, 3);
                    hitEffect = NHFx.hitSpark(backColor, 45f, 8, 15, 1, 4);
                    shootEffect = NHFx.shootCircleSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;

                    buildingDamageMultiplier = 0.05f;
                }};
            }});
        }};
    }
}
