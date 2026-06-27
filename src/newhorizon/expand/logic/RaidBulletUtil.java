package newhorizon.expand.logic;

import mindustry.entities.bullet.BulletType;
import newhorizon.content.NHBullets;
import newhorizon.content.bullets.RaidBullets;

import static mindustry.Vars.content;

public class RaidBulletUtil {
    public static BulletType resolve(int type) {
        if (type < 10000) {
            return switch (type) {
                case 1 -> RaidBullets.defaultRaidBullet1;
                default -> NHBullets.railGun1;
            };
        }
        BulletType bt = content.bullet(type - 10000);
        if (bt != null) return bt;
        return content.bullet(0);
    }
}
