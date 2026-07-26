package newhorizon.expand.logic;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import newhorizon.NewHorizon;
import newhorizon.expand.net.NHCall;
import newhorizon.content.NHBullets;
import newhorizon.content.NHFx;
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

    public static float estimateTravel(BulletType type) {
        if (type == null) return 1f;
        if (type instanceof AccelBulletType accel) {
            return estimateAccelTravel(accel);
        }
        if (type.speed > 0.001f && type.lifetime > 0.001f) {
            return type.speed * type.lifetime;
        }
        if (type instanceof LightningLinkerBulletType linker && linker.range > 0f) {
            return linker.range;
        }
        return Math.max(type.range, 1f);
    }

    private static float estimateAccelTravel(AccelBulletType accel) {
        if (accel.lifetime <= 0.001f) return 1f;
        if (accel.accelerateBegin >= 1f || Math.abs(accel.velocityIncrease) < 0.001f) {
            float v = accel.velocityBegin > 0.001f ? accel.velocityBegin : Math.max(accel.speed, 0.1f);
            return v * accel.lifetime;
        }
        float cal = 0f;
        for (float i = 0f; i <= 1f; i += 0.05f) {
            float s = accel.velocityBegin
                    + accel.accelInterp.apply(Mathf.curve(i, accel.accelerateBegin, accel.accelerateEnd)) * accel.velocityIncrease;
            cal += Math.max(s, 0f) * accel.lifetime * 0.05f;
        }
        return Math.max(cal, 0.1f);
    }

    public static float lifetimeScl(BulletType type, float dst) {
        if (type == null || dst <= 0f) return 1f;
        float travel = estimateTravel(type);
        if (travel <= 0.001f) return 1f;
        return dst / travel;
    }

    public static BulletType prepareForRaid(BulletType type) {
        if (type == null) return null;
        if (type instanceof LightningLinkerBulletType) {
            BulletType copy = type.copy();
            copy.collidesTiles = false;
            copy.collideFloor = false;
            copy.drag = 0f;
            copy.scaleLife = false;
            return copy;
        }

        BulletType copy = type.copy();
        copy.collideFloor = false;
        if (!(copy instanceof LightningLinkerBulletType)) {
            copy.scaleLife = true;
        }
        if (copy instanceof AccelBulletType) {
            AccelBulletType accel = (AccelBulletType) copy;
            if (accel.velocityIncrease > 0.001f) {
                float begin = accel.velocityBegin > 0.001f ? accel.velocityBegin : Math.max(copy.speed, 0.1f);
                accel.velocityBegin = begin + accel.velocityIncrease * 0.5f;
                accel.velocityIncrease = 0f;
                accel.disableAccel();
                copy.speed = accel.velocityBegin;
            }
        }
        return copy;
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
        BulletType prepared = prepareForRaid(type);
        float lifeScl = lifetimeScl(prepared, dst);
        prepared.create(null, team, x, y, angle, damage, velocityScl, lifeScl, null, null, aimX, aimY);
        if (net.server() && net.active()) {
            BulletType syncType = resolveSyncType(type, syncAs);
            if (syncType != null) {
                int lightning = prepared.lightning;
                int lightningLength = prepared.lightningLength;
                float lightningDamage = prepared.lightningDamage;
                if (lightning <= 0 || lightningLength <= 0) {
                    lightning = 0;
                    lightningLength = 0;
                    lightningDamage = 0f;
                }
                NHCall.syncRaidBullet(syncType, team, x, y, angle, damage, velocityScl, lifeScl, aimX, aimY, raidTint(type), lightning, lightningLength, lightningDamage, prepared.speed, prepared.lifetime);
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
        createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY, null, 0, 0, 0f, 0f, 0f);
    }

    public static void createSynced(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY, Color tint) {
        createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY, tint, 0, 0, 0f, 0f, 0f);
    }

    public static void createSynced(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY, Color tint, int lightning, int lightningLength, float lightningDamage) {
        createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY, tint, lightning, lightningLength, lightningDamage, 0f, 0f);
    }

    public static void createSynced(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY, Color tint, int lightning, int lightningLength, float lightningDamage, float bulletSpeed, float bulletLifetime) {
        if (type == null || team == null) return;
        BulletType bt = prepareForRaid(type);
        if (tint != null && !tint.equals(Color.white)) {
            bt = applyRaidTint(bt, tint);
        }
        applySyncedMotion(bt, bulletSpeed, bulletLifetime);
        applySyncedLightning(bt, lightning, lightningLength, lightningDamage);
        bt.create(null, team, x, y, angle, damage, velocityScl, lifetimeScl, null, null, aimX, aimY);
    }

    private static void applySyncedMotion(BulletType type, float bulletSpeed, float bulletLifetime) {
        if (type == null) return;
        if (bulletSpeed > 0.001f) {
            type.speed = bulletSpeed;
        }
        if (bulletLifetime > 0.001f) {
            type.lifetime = bulletLifetime;
        }
        if (type instanceof AccelBulletType accel) {
            accel.velocityBegin = type.speed;
            accel.velocityIncrease = 0f;
            accel.disableAccel();
        }
        if (!(type instanceof LightningLinkerBulletType)) {
            type.scaleLife = true;
            type.range = type.speed * type.lifetime;
        }
    }

    private static void applySyncedLightning(BulletType type, int lightning, int lightningLength, float lightningDamage) {
        if (type == null) return;
        if (lightning <= 0 || lightningLength <= 0) {
            type.lightning = 0;
            type.lightningLength = 0;
            type.lightningLengthRand = 0;
            type.lightningDamage = 0f;
        } else {
            type.lightning = lightning;
            type.lightningLength = lightningLength;
            type.lightningLengthRand = Math.max(0, lightningLength / 3);
            type.lightningDamage = Math.max(lightningDamage, 0f);
        }
        if (type.fragBullet != null) {
            BulletType frag = type.fragBullet.copy();
            frag.lightning = 0;
            frag.lightningLength = 0;
            frag.lightningLengthRand = 0;
            frag.lightningDamage = 0f;
            boolean shareInterval = type.intervalBullet == type.fragBullet;
            type.fragBullet = frag;
            if (shareInterval) type.intervalBullet = frag;
        }
        if (type.intervalBullet != null && type.intervalBullet != type.fragBullet) {
            BulletType interval = type.intervalBullet.copy();
            interval.lightning = 0;
            interval.lightningLength = 0;
            interval.lightningLengthRand = 0;
            interval.lightningDamage = 0f;
            type.intervalBullet = interval;
        }
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
        if (copy instanceof AccelBulletType) {
            Color fx = tint.cpy();
            float blastSize = Math.max(72f, copy.splashDamageRadius * 0.65f);
            if (NHBullets.blastEnergyPst != null && type.id == NHBullets.blastEnergyPst.id) {
                copy.hitEffect = NHFx.crossBlast(fx, blastSize);
                copy.despawnEffect = NHFx.hyperBlast(fx);
            } else {
                copy.hitEffect = NHFx.lightningHitLarge(fx);
                copy.despawnEffect = NHFx.crossBlast(fx, blastSize);
            }
            copy.shootEffect = NHFx.shootCircleSmall(fx);
        }
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




