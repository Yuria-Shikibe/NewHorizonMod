package newhorizon.content.bullets;

import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import newhorizon.expand.bullets.AdaptBulletType;

public class VanillaOverrideBullets {
    public static BulletType alpha0, beta0, gamma0;

    public static void load(){
        alpha0 = new AdaptBulletType(15, 15){{
            speed = 2.5f;
            width = 7f;
            height = 9f;
            lifetime = 75f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            buildingDamageMultiplier = 0.01f;

            keepVelocity = false;
        }};

        beta0 = new AdaptBulletType(18, 18){{
            speed = 3f;
            width = 7f;
            height = 9f;
            lifetime = 75f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            buildingDamageMultiplier = 0.01f;

            keepVelocity = false;
        }};

        gamma0 = new AdaptBulletType(22, 22){{
            speed = 3.5f;

            width = 6.5f;
            height = 11f;
            lifetime = 90f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            buildingDamageMultiplier = 0.01f;
            homingPower = 0.04f;

            keepVelocity = false;
        }};
    }
}
