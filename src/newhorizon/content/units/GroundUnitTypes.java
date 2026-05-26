package newhorizon.content.units;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.EntityMapping;
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

    public static UnitType origin;

    static {
        EntityMapping.nameMap.put(NewHorizon.name("origin"), EntityMapping.idMap[4]);
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
    }
}
