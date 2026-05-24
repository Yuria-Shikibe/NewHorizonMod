package newhorizon.content.units;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import mindustry.ai.types.BuilderAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.EntityMapping;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.content.NHStatusEffects;
import newhorizon.expand.units.BoostAbility;
import newhorizon.expand.units.unitType.NHUnitType;

import static mindustry.Vars.tilePayload;

public class CoreUnitTypes {

    public static UnitType scalar, liv;

    public static Color bulletFrontColor, bulletBackColor;

    static {
        EntityMapping.nameMap.put(NewHorizon.name("scalar"), EntityMapping.idMap[5]);
        EntityMapping.nameMap.put(NewHorizon.name("liv"), EntityMapping.idMap[5]);
    }

    public static void loadColor() {
        bulletFrontColor = Color.valueOf("94b3fa");
        bulletBackColor = Color.valueOf("c3d4fc");
    }

    public static void load() {
        loadColor();

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
            itemCapacity = 150;
            payloadCapacity = 2 * tilePayload;

            buildBeamOffset = 6f;
            buildSpeed = 3f;

            mineTier = 2;
            mineSpeed = 8f;

            engineSize = 0;
            engineColor = NHColor.lightSky;

            flying = true;
            mineWalls = true;
            mineFloor = true;
            aiController = BuilderAI::new;
            engines.add(new UnitEngine(0, -5.25f, 1.85f, -90));
            abilities.add(new BoostAbility(false, 1.2f, 180f));

            weapons.add(new Weapon("scalar-pulse-cannon") {{
                x = 3f;

                reload = 75;
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
                    shots = 3;
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
                    homingRange = 120f;
                    knockback = 0.75f;
                    statusDuration = 30f;

                    keepVelocity = false;

                    hitColor = bulletFrontColor;
                    lightColor = bulletBackColor;
                    trailColor = bulletFrontColor;

                    frontColor = bulletFrontColor;
                    backColor = bulletBackColor;

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
        liv = new NHUnitType("liv") {{
            outlineColor = grayOutline;

            itemCapacity = 150;
            payloadCapacity = (2 * 2) * tilePayload;

            immunities = ObjectSet.with(NHStatusEffects.scannerDown);

            aiController = BuilderAI::new;
            fogRadius = 40f;
            outlineRadius = 4;

            lightRadius = 20f;
            lightOpacity = 0.1f;

            flying = true;
            health = 1000;
            armor = 5;
            hitSize = 18f;
            drag /= 5f;

            rotateSpeed = 4.5f;
            speed = 4.5f;
            accel = 0.5f;

            engineSize = 0;
            engineColor = NHColor.lightSky;

            buildBeamOffset = 6f;
            buildSpeed = 3f;

            mineTier = 3;
            mineSpeed = 12f;

            engines.add(
                    new UnitEngine(4.5f, -7.2f, 2.2f, -115),
                    new UnitEngine(0, -9.8f, 2.8f, -90),
                    new UnitEngine(-4.5f, -7.2f, 2.2f, -65)
            );

            abilities.add(
                    new BoostAbility(false, 1.5f, 90.0f));

            weapons.add(new Weapon() {{
                reload = 42;
                recoil = 1.5f;
                inaccuracy = 5;
                shootSound = NHSounds.thermoShoot;
                top = false;
                mirror = alternate = true;
                rotate = false;
                rotateSpeed = 2.55f;
                heatColor = NHColor.lightSky;
                shootCone = 30f;


                shoot = new ShootPattern() {{
                    shots = 5;
                    shotDelay = 5f;
                }};

                bullet = new BasicBulletType(4.5f, 20f) {{
                    ejectEffect = Fx.none;
                    trailWidth = 1.5f;
                    trailLength = 15;
                    drawSize = 200f;

                    status = StatusEffects.shocked;
                    statusDuration = 30f;
                    lifetime = 40f;
                    homingPower = 0.1f;
                    homingRange = 120f;
                    width = 10f;
                    height = 25f;
                    keepVelocity = true;
                    knockback = 0.75f;
                    trailColor = backColor = lightColor = lightningColor = hitColor = NHColor.lightSkyBack;
                    frontColor = backColor.cpy().lerp(Color.white, 0.45f);
                    trailChance = 0.1f;
                    trailParam = 1f;
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 2, 12f, 2);
                    hitEffect = NHFx.lightningHitSmall(backColor);
                    shootEffect = NHFx.shootLineSmall(backColor);
                    smokeEffect = Fx.shootBigSmoke2;

                    buildingDamageMultiplier = 0.2f;
                }};
            }});

            strafePenalty = 0.3f;
        }};
    }
}
