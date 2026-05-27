package newhorizon.content.units;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.EntityMapping;
import mindustry.gen.Sounds;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.units.unitType.NHUnitType;
import newhorizon.util.func.NHPixmap;

public class GroundUnitTypes {

    public static UnitType origin, thynomo;

    static {
        EntityMapping.nameMap.put(NewHorizon.name("origin"), EntityMapping.idMap[4]);
        EntityMapping.nameMap.put(NewHorizon.name("thynomo"), EntityMapping.idMap[4]);
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
    }
}
