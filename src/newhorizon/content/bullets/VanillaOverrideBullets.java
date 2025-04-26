package newhorizon.content.bullets;

import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.entities.bullet.BulletType;
import mindustry.type.Weapon;
import newhorizon.expand.bullets.AdaptBulletType;

public class VanillaOverrideBullets {
    public static AdaptBulletType alpha0, beta0, gamma0;
    public static Seq<AdaptBulletType> bullets = new Seq<>();
    public static ObjectMap<Weapon, BulletType> vanillaUnitBullets = new ObjectMap<>();

    public static void load(){
        alpha0 = new AdaptBulletType(0, 0);
        beta0 = new AdaptBulletType(0, 0);
        gamma0 = new AdaptBulletType(0, 0);
    }
}
