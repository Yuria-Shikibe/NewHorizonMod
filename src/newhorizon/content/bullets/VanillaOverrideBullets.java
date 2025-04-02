package newhorizon.content.bullets;

import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import newhorizon.expand.bullets.AdaptBulletType;

public class VanillaOverrideBullets {
    public static BulletType alpha0;

    public static void load(){
        alpha0 = new AdaptBulletType(20, 20){{
            speed = 2.5f;
            width = 7f;
            height = 9f;
            lifetime = 60f;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            buildingDamageMultiplier = 0.01f;
        }};
    }
}
