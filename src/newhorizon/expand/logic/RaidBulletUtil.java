package newhorizon.expand.logic;

import mindustry.entities.bullet.BulletType;
import newhorizon.NewHorizon;
import newhorizon.content.NHBullets;
import newhorizon.content.bullets.RaidBullets;

import static mindustry.Vars.content;

public class RaidBulletUtil {
    public static BulletType resolve(int type) {
        if (type < 10000) {
            return switch (type) {
                case 1 -> RaidBullets.defaultRaidBullet1;
                case 2 -> RaidBullets.defaultRaidBullet2;
                case 3 -> RaidBullets.defaultRaidBullet3;
                case 4 -> RaidBullets.raidBullet_4;
                case 5 -> RaidBullets.raidBullet_5;
                case 6 -> RaidBullets.raidBullet_6;
                case 7 -> RaidBullets.raidBullet_7;
                case 8 -> RaidBullets.raidBullet_8;
                default -> NHBullets.railGun1;
            };
        }
        BulletType bt = content.bullet(type - 10000);
        if (bt != null) return bt;
        return content.bullet(0);
    }

    public static String alertKey(BulletType type) {
        int legacy = legacyId(type);
        if (legacy > 0) return alertKey(legacy);
        return "css-raid.bullet-content-" + type.id + ".alert";
    }

    public static String popupKey(BulletType type) {
        int legacy = legacyId(type);
        if (legacy > 0) return popupKey(legacy);
        return "css-raid.bullet-content-" + type.id + ".popup";
    }

    public static String warningIcon(BulletType type) {
        int legacy = legacyId(type);
        if (legacy > 0) return warningIcon(legacy);
        return NewHorizon.name("event-default-raid-t1");
    }

    public static int legacyId(BulletType type) {
        if (type == null) return 0;
        if (type == RaidBullets.defaultRaidBullet1) return 1;
        if (type == RaidBullets.defaultRaidBullet2) return 2;
        if (type == RaidBullets.defaultRaidBullet3) return 3;
        if (type == RaidBullets.raidBullet_3 || type == RaidBullets.raidBullet_4 || type == NHBullets.railGun1 || type == NHBullets.railGun2) return 4;
        if (type == RaidBullets.raidBullet_5 || type == NHBullets.railGun3) return 5;
        if (type == RaidBullets.raidBullet_6
                || type == RaidBullets.raidBullet_9
                || type == RaidBullets.raidBullet_10
                || type == NHBullets.saviourBullet
                || type == NHBullets.guardianBulletLightningBall
                || type == NHBullets.blastEnergyNgt
                || type == NHBullets.airRaidBomb) return 6;
        if (type == RaidBullets.raidBullet_7 || type == NHBullets.shieldDestroyer || type == NHBullets.collapserBullet) return 7;
        if (type == RaidBullets.raidBullet_8 || type == NHBullets.ancientArtilleryProjectile || type == NHBullets.arc_9000) return 8;
        if (type == NHBullets.synchroZeta || type == NHBullets.warperBullet || type == NHBullets.synchroFusionEnergy) return 1;
        if (type == NHBullets.laugraBullet || type == NHBullets.artilleryFusion || type == NHBullets.artilleryNgt) return 3;
        return 0;
    }

    public static String alertKey(int type) {
        return "css-raid." + bundleId(type) + ".alert";
    }

    public static String popupKey(int type) {
        return "css-raid." + bundleId(type) + ".popup";
    }

    public static String warningIcon(int type) {
        String icon = switch (type) {
            case 1, 2 -> "event-default-raid-t1";
            case 3 -> "event-default-raid-t2";
            case 4, 5, 7, 8 -> "event-rail-raid-t1";
            case 6 -> "event-explosive-raid-t1";
            default -> "event-default-raid-t1";
        };
        if (type >= 10000) return NewHorizon.name("event-default-raid-t1");
        return NewHorizon.name(icon);
    }

    private static String bundleId(int type) {
        if (type >= 10000) return "bullet-content-" + (type - 10000);
        if (type >= 1 && type <= 8) return "bullet-" + type;
        return "custom-raid";
    }
}
