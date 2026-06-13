package newhorizon.content.units;

import arc.graphics.Color;
import arc.math.Angles;
import arc.util.Time;
import mindustry.ai.types.BuilderAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.Bullet;
import mindustry.gen.EntityMapping;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.content.NHFx;
import newhorizon.content.NHSounds;
import newhorizon.expand.bullets.TracerBulletType;
import newhorizon.expand.units.BoostAbility;
import newhorizon.expand.units.unitType.NHUnitType;

import static mindustry.Vars.tilePayload;

public class AirUnitTypes {

    public static UnitType apparition;

    static {
        EntityMapping.nameMap.put(NewHorizon.name("apparition"), EntityMapping.map(3));
    }

    public static void load() {
        apparition = new NHUnitType("apparition") {{
            armor = 5;
            health = 3200;
            hitSize = 24f;

            drag = 0.15f;
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
            engines.add(
                    new UnitEngine(5, -14.75f, 2.25f, -90),
                    new UnitEngine(-5, -14.75f, 2.25f, -90),
                    new UnitEngine(0, -15.75f, 3.85f, -90)
            );

            weapons.add(new Weapon(NewHorizon.name("apparition-laser-weapon")) {{
                x = 0f;
                y = 0f;

                shootX = 4.5f;
                shootY = 12f;

                reload = 60;
                recoil = 1.5f;
                inaccuracy = 5f;
                shootCone = 15f;

                top = false;
                mirror = true;
                rotate = false;
                alternate = true;

                heatColor = NHColor.lightSky;
                shootSound = NHSounds.shootBlaster3;

                shoot = new ShootMulti(
                        new ShootPattern() {{
                            shots = 8;
                            shotDelay = 3f;
                        }},
                        new ShootPattern() {{
                            shots = 2;
                        }}
                );

                bullet = new TracerBulletType() {{
                    speed = 8.5f;
                    damage = 40f;
                    lifetime = 45f;
                    inaccuracy = 15f;

                    width = 12f;
                    height = 20f;
                    drawSize = 120f;

                    trailWidth = 1.2f;
                    trailLength = 8;
                    trailParam = 1f;
                    trailChance = 0.1f;

                    tracerRandRange = 3f;
                    tracerUpdateInterval = 0.2f;

                    followAimSpeed = 8f;

                    homingPower = 0.3f;
                    homingRange = 60f;
                    homingDelay = 20f;
                    knockback = 0.75f;
                    statusDuration = 30f;

                    keepVelocity = false;

                    hitColor = NHColor.thurmixRed;
                    lightColor = NHColor.thurmixRed;
                    trailColor = NHColor.thurmixRed;

                    backColor = NHColor.thurmixRed;
                    frontColor = Color.white;

                    status = StatusEffects.shocked;

                    ejectEffect = Fx.none;
                    trailEffect = NHFx.trailToGray;
                    despawnEffect = NHFx.square(backColor, 18f, 2, 12f, 2);
                    hitEffect = NHFx.lightningHitSmall(backColor);
                    shootEffect = NHFx.shootLineSmall(backColor);
                    smokeEffect = Fx.shootSmallSmoke;

                    buildingDamageMultiplier = 0.05f;
                }

                    public void updateHoming(Bullet b){
                        if(homingPower > 0.0001f && b.time >= homingDelay){
                            float realAimX = b.aimX < 0 ? b.x : b.aimX;
                            float realAimY = b.aimY < 0 ? b.y : b.aimY;

                            Teamc target;
                            //home in on allies if possible
                            if(heals()){
                                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                                        e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                                        t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                                );
                            }else{
                                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                                    target = b.aimTile.build;
                                }else{
                                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                                            e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                                            t -> t != null && collidesGround && !b.hasCollided(t.id));
                                }
                            }

                            if(target != null){
                                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                            } else {
                                if(followAimSpeed > 0f && b.shooter instanceof Unit u){
                                    float angle = b.angleTo(u.aimX, u.aimY);
                                    b.vel.setAngle(Angles.moveToward(b.vel.angle(), angle, followAimSpeed * Time.delta));
                                }
                            }
                        }
                    }
                };
            }});
        }};
    }
}
