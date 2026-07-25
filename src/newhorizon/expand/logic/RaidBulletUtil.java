package newhorizon.expand.logic;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import newhorizon.NewHorizon;
import newhorizon.expand.net.NHCall;
import newhorizon.content.NHBullets;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.bullets.AccelBulletType;
import newhorizon.expand.bullets.LightningLinkerBulletType;
import newhorizon.expand.bullets.raid.RandomRaidBulletType;
import newhorizon.expand.game.RaidLogic;

import static mindustry.Vars.content;
import static mindustry.Vars.net;

public class RaidBulletUtil {
    public static final int RANDOM_RAID_ID = 666;

    public static boolean isRandomId(int type) {
        return type == RANDOM_RAID_ID || type == 11;
    }

    public static boolean isRandomType(BulletType type) {
        return RandomRaidBulletType.handles(type);
    }

    public static BulletType resolve(int type) {
        if (isRandomId(type)) return RaidBullets.raidBullet_11;
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
                case 9 -> RaidBullets.raidBullet_9;
                case 10 -> RaidBullets.raidBullet_10;
                default -> NHBullets.railGun1;
            };
        }
        BulletType bt = content.bullet(type - 10000);
        if (bt != null) return bt;
        return content.bullet(0);
    }

    public static float raidRange(BulletType type) {
        if (type.range > 0f) return type.range;
        if (type instanceof LightningLinkerBulletType linker && linker.range > 0f) return linker.range;
        return type.speed * type.lifetime;
    }

    public static float lifetimeScl(BulletType type, float dst) {
        if (type == null || dst <= 0f) return 1f;
        float travel = raidRange(type);
        if (travel <= 0.001f) return 1f;
        return Mathf.clamp(dst / travel, 0.05f, 8f);
    }

    public static BulletType prepareForRaid(BulletType type) {
        BulletType copy = null;
        if (type instanceof LightningLinkerBulletType) {
            copy = type.copy();
            copy.collidesTiles = false;
            copy.collideFloor = false;
            copy.drag = 0f;
            copy.scaleLife = false;
        } else if (type instanceof AccelBulletType) {
            copy = type.copy();
            AccelBulletType accel = (AccelBulletType) copy;
            if (accel.velocityIncrease > 0.001f) {
                float begin = accel.velocityBegin > 0.001f ? accel.velocityBegin : Math.max(copy.speed, 0.1f);
                accel.velocityBegin = begin + accel.velocityIncrease * 0.5f;
                accel.velocityIncrease = 0f;
                accel.disableAccel();
                copy.speed = accel.velocityBegin;
            }
        }
        return copy != null ? copy : type;
    }

    public static void spawn(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float dst, float aimX, float aimY) {
        spawn(type, team, x, y, angle, damage, velocityScl, dst, aimX, aimY, null);
    }

    public static void spawn(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float dst, float aimX, float aimY, BulletType syncAs) {
        if (RaidLogic.isRemoteClient()) return;
        if (type == null || team == null) return;
        if (isRandomType(type)) {
            RandomRaidBulletType.fire(team, x, y, angle, damage, velocityScl, dst, aimX, aimY);
            return;
        }
        float lifetimeScl = lifetimeScl(type, dst);
        createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY, null);
        if (net.server() && net.active()) {
            BulletType syncType = resolveSyncType(type, syncAs);
            if (syncType != null) {
                float syncLifeScl = lifetimeScl(syncType, dst);
                NHCall.syncRaidBullet(syncType, team, x, y, angle, damage, velocityScl, syncLifeScl, aimX, aimY, raidTint(type));
            }
        }
    }

    private static Color raidTint(BulletType type) {
        if (type == null) return Color.white;
        if (type.hitColor != null && !type.hitColor.equals(Color.white)) return type.hitColor;
        if (type.trailColor != null && !type.trailColor.equals(Color.white)) return type.trailColor;
        if (type instanceof BasicBulletType basic && basic.backColor != null) return basic.backColor;
        return Color.white;
    }

    private static BulletType resolveSyncType(BulletType type, BulletType syncAs) {
        if (syncAs != null) {
            BulletType fromKey = content.bullet(syncAs.id);
            if (fromKey != null) return fromKey;
        }
        if (type != null) {
            BulletType fromType = content.bullet(type.id);
            if (fromType != null) return fromType;
        }
        return syncAs != null ? syncAs : type;
    }

    public static void createSynced(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY) {
        createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY, null);
    }

    public static void createSynced(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY, Color tint) {
        if (type == null || team == null) return;
        BulletType bt = prepareForRaid(type);
        if (tint != null && !tint.equals(Color.white)) {
            bt = applyRaidTint(bt, tint);
        }
        bt.create(null, team, x, y, angle, damage, velocityScl, lifetimeScl, null, null, aimX, aimY);
    }

    public static BulletType applyRaidTint(BulletType type, Color tint) {
        if (type == null || tint == null) return type;
        BulletType copy = type.copy();
        Color back = tint.cpy();
        Color front = tint.cpy().lerp(Color.white, 0.3f);
        if (copy instanceof BasicBulletType basic) {
            basic.backColor = back;
            basic.frontColor = front;
        }
        copy.trailColor = back.cpy();
        copy.hitColor = back.cpy();
        copy.lightColor = back.cpy();
        copy.lightningColor = back.cpy();
        if (copy.fragBullet != null) {
            BulletType frag = copy.fragBullet.copy();
            if (frag instanceof BasicBulletType fragBasic) {
                fragBasic.backColor = back.cpy();
                fragBasic.frontColor = front.cpy();
            }
            frag.trailColor = back.cpy();
            frag.hitColor = back.cpy();
            frag.lightColor = back.cpy();
            frag.lightningColor = back.cpy();
            boolean shareInterval = copy.intervalBullet == copy.fragBullet;
            copy.fragBullet = frag;
            if (shareInterval) copy.intervalBullet = frag;
        }
        return copy;
    }

    public static String alertKey(BulletType type) {
        return raidKey(bundleCategory(type), "alert");
    }

    public static String popupKey(BulletType type) {
        return raidKey(bundleCategory(type), "popup");
    }

    public static String warningIcon(BulletType type) {
        return warningIcon(bundleCategory(type));
    }

    public static String alertKey(int type) {
        return raidKey(bundleCategory(type), "alert");
    }

    public static String popupKey(int type) {
        return raidKey(bundleCategory(type), "popup");
    }

    public static String warningIcon(int type) {
        return warningIcon(bundleCategory(type));
    }

    public static String bundleCategory(BulletType type) {
        if (type == null) return "custom-raid";
        if (isRandomType(type)) return "bullet-random";

        if (is(type,
                NHBullets.collapserBullet,
                NHBullets.arc_9000,
                NHBullets.arc_9000_frag,
                NHBullets.hyperBlastLinker,
                NHBullets.hyperBlast,
                NHBullets.guardianBulletLightningBall
        )) return "bullet-area-ionization";

        if (is(type,
                NHBullets.pesterBlackHole,
                NHBullets.nuBlackHole,
                NHBullets.declineProjectile,
                NHBullets.guardianBullet
        )) return "bullet-black-hole";

        if (is(type,
                NHBullets.missileTitanium,
                NHBullets.missileThorium,
                NHBullets.missileZeta,
                NHBullets.missileNormal,
                NHBullets.missileStrike,
                NHBullets.annMissile
        )) return "bullet-missile";

        if (is(type,
                NHBullets.synchroZeta,
                NHBullets.synchroThermoPst,
                NHBullets.synchroFusionEnergy,
                NHBullets.synchroTitanium,
                NHBullets.synchroTungsten,
                NHBullets.eternity,
                NHBullets.warperBullet
        )) return "bullet-synchro";

        if (type == NHBullets.atomSeparator) return "bullet-atom";

        if (is(type, NHBullets.shieldDestroyer, RaidBullets.raidBullet_7)) return "bullet-shield-breaker";

        if (is(type,
                NHBullets.ancientArtilleryProjectile,
                NHBullets.ancientBall,
                NHBullets.ancientStd,
                RaidBullets.raidBullet_8
        )) return "bullet-ancient";

        if (is(type, NHBullets.railGun3, RaidBullets.raidBullet_5, RaidBullets.railRaidBullet2, RaidBullets.railRaidBullet3)) {
            return "bullet-railgun-heavy";
        }

        if (is(type,
                NHBullets.railGun1,
                NHBullets.railGun2,
                RaidBullets.raidBullet_3,
                RaidBullets.raidBullet_4,
                RaidBullets.railRaidBullet1
        )) return "bullet-railgun";

        if (is(type,
                RaidBullets.raidBullet_6,
                NHBullets.blastEnergyPst,
                NHBullets.blastEnergyNgt,
                NHBullets.saviourBullet
        )) return "bullet-emp";

        if (is(type,
                RaidBullets.raidBullet_9,
                RaidBullets.raidBullet_10,
                RaidBullets.explosiveRaidBullet1,
                RaidBullets.explosiveRaidBullet2,
                RaidBullets.explosiveRaidBullet3,
                NHBullets.airRaidBomb,
                NHBullets.ultFireball,
                NHBullets.basicSkyFrag
        )) return "bullet-cluster";

        if (is(type,
                RaidBullets.defaultRaidBullet3,
                NHBullets.laugraBullet,
                NHBullets.artilleryHydro,
                NHBullets.artilleryMulti,
                NHBullets.artilleryNgt,
                NHBullets.artilleryFusion,
                NHBullets.artilleryPhase
        )) return "bullet-artillery-super";

        if (type == RaidBullets.defaultRaidBullet2) return "bullet-artillery-heavy";

        if (is(type, RaidBullets.defaultRaidBullet1, NHBullets.basicRaid, NHBullets.raidBulletType)) {
            return "bullet-artillery-light";
        }

        return "custom-raid";
    }

    public static String bundleCategory(int type) {
        if (type >= 10000) return "bullet-content-" + (type - 10000);
        if (isRandomId(type)) return "bullet-random";
        return switch (type) {
            case 1 -> "bullet-artillery-light";
            case 2 -> "bullet-artillery-heavy";
            case 3 -> "bullet-artillery-super";
            case 4 -> "bullet-railgun";
            case 5 -> "bullet-railgun-heavy";
            case 6 -> "bullet-emp";
            case 7 -> "bullet-shield-breaker";
            case 8 -> "bullet-ancient";
            default -> "custom-raid";
        };
    }

    public static String warningIcon(String category) {
        String icon = switch (category) {
            case "bullet-artillery-light", "bullet-artillery-heavy", "bullet-artillery-super",
                    "bullet-synchro", "bullet-atom" -> "event-default-raid-t1";
            case "bullet-railgun", "bullet-railgun-heavy", "bullet-shield-breaker", "bullet-ancient" -> "event-rail-raid-t1";
            case "bullet-emp", "bullet-cluster", "bullet-area-ionization", "bullet-black-hole", "bullet-missile",
                    "bullet-random" -> "event-explosive-raid-t1";
            default -> "event-default-raid-t1";
        };
        if (category.startsWith("bullet-content-")) return NewHorizon.name("event-default-raid-t1");
        return NewHorizon.name(icon);
    }

    private static String raidKey(String category, String suffix) {
        return "css-raid." + category + "." + suffix;
    }

    private static boolean is(BulletType type, BulletType... candidates) {
        for (BulletType candidate : candidates) {
            if (type == candidate) return true;
        }
        return false;
    }
}




