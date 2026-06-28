package newhorizon.expand.game;

import arc.Events;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newhorizon.NHSetting;
import newhorizon.content.*;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutscene;

public class DefaultRaid {
    private static final float PROTECTION_TIME = 3f;
    private static final float COOLDOWN_MIN = 60f;
    private static final float COOLDOWN_RANGE = 120f;
    private static final int OVERRIDE_CHECK_INTERVAL = 120;

    private static final Interval overrideCheck = new Interval(OVERRIDE_CHECK_INTERVAL);
    private static final IntMap<RaidEvent> raids = new IntMap<>();
    private static final Seq<int[]> tierPoolBuilder = new Seq<>();
    private static int[][] tierPools;

    private static float waitTimer;
    private static boolean raidRunning;
    private static ActionBus currentRaidBus;

    public static final class RaidEvent {
        public final int id;
        public final BulletType bullet;
        public final float alertTime;
        public final float raidTime;
        public final float raidScale;
        public final float inaccuracy;

        public RaidEvent(int id, BulletType bullet, float alertTime, float raidTime, float raidScale, float inaccuracy) {
            this.id = id;
            this.bullet = bullet;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.raidScale = raidScale;
            this.inaccuracy = inaccuracy;
        }
    }

    private static void event(int id, BulletType bullet, float alert, float raid, float scale, float spread) {
        register(id, bullet, alert, raid, scale, spread);
    }

    private static void tier(int... ids) {
        tierPoolBuilder.add(ids);
    }

    public static void load() {
        registerRaids();

        Events.on(EventType.PlayEvent.class, event -> {
            NHLogic.refreshCustomRaidLogic();
            reset();
        });
        Events.on(EventType.WorldLoadEvent.class, event -> {
            NHLogic.refreshCustomRaidLogic();
            reset();
        });
    }

    public static void register(int id, BulletType bullet, float alertTime, float raidTime, float raidScale, float inaccuracy) {
        raids.put(id, new RaidEvent(id, bullet, alertTime, raidTime, raidScale, inaccuracy));
    }

    public static RaidEvent get(int id) {
        return raids.get(id);
    }

    public static IntMap<RaidEvent> all() {
        return raids;
    }

    private static void registerRaids() {
        if (raids.size > 0) return;
        tierPoolBuilder.clear();

        registerRaidEvents();
        registerTierPools();

        tierPools = tierPoolBuilder.toArray(int[].class);
    }

    private static void registerRaidEvents() {
        event(1, RaidBullets.defaultRaidBullet1, 60, 8, 2.0f, 60);
        event(2, NHBullets.synchroZeta, 120, 10, 8f, 90);
        event(3, RaidBullets.raidBullet_9, 180, 20, 3f, 75);
        event(4, NHBullets.warperBullet, 180, 10, 12f, 120);
        event(5, NHBullets.synchroFusionEnergy, 180, 10, 8f, 120);
        event(6, NHBullets.laugraBullet, 180, 3, 2f, 60);
        event(7, NHBullets.saviourBullet, 240, 20, 3f, 90);
        event(8, NHBullets.artilleryFusion, 180, 40, 1.2f, 72);
        event(9, RaidBullets.raidBullet_10, 30, 14, 3f, 80);
        event(10, NHBullets.guardianBulletLightningBall, 180, 6, 2f, 60);
        event(11, NHBullets.railGun1, 120, 8, 3.0f, 30);
        event(12, RaidBullets.raidBullet_6, 120, 12, 2f, 180);
        event(13, NHBullets.saviourBullet, 240, 30, 4f, 180);
        event(14, NHBullets.artilleryNgt, 180, 10, 8f, 120);
        event(15, NHBullets.blastEnergyNgt, 240, 4, 10f, 30);
        event(16, NHBullets.collapserBullet, 240, 8, 6, 240);
        event(17, NHBullets.shieldDestroyer, 60, 9, 2f, 120);
        event(18, RaidBullets.raidBullet_8, 300, 6, 1f, 90);
        event(19, NHBullets.airRaidBomb, 360, 20, 1f, 90);
        event(20, NHBullets.arc_9000, 300, 3, 1f, 60);
    }

    private static void registerTierPools() {
        tier(1, 2, 3);                     // tier 1
        tier(3, 4, 5);                     // tier 2
        tier(5, 6, 7);                     // tier 3
        tier(7, 8, 9);                     // tier 4
        tier(7, 9, 10, 11);                   // tier 5
        tier(6, 11, 12, 13);                  // tier 6
        tier(13, 14, 15, 16, 17);          // tier 7
        tier(16, 17, 18, 19, 20);          // tier 8
    }

    public static void reset() {
        waitTimer = PROTECTION_TIME;
        raidRunning = false;
        currentRaidBus = null;
        overrideCheck.reset(0, OVERRIDE_CHECK_INTERVAL);
    }

    public static void update() {
        if (!NHSetting.getBool(NHSetting.EVENT_RAID)) return;
        if (!state.isPlaying()) return;
        if (state.rules.mode() == Gamemode.sandbox || state.rules.mode() == Gamemode.pvp) return;

        if (overrideCheck.get(0, OVERRIDE_CHECK_INTERVAL)) {
            NHLogic.refreshCustomRaidLogic();
        }
        if (NHLogic.hasCustomRaidLogic()) return;

        Team wave = state.rules.waveTeam;
        Team player = state.rules.defaultTeam;
        if (wave == null || player == null) return;

        if (raidRunning) {
            if (currentRaidBus == null || currentRaidBus.complete()) {
                raidRunning = false;
                currentRaidBus = null;
                waitTimer = COOLDOWN_MIN + Mathf.random(COOLDOWN_RANGE);
            }
            return;
        }

        if (waitTimer > 0f) {
            waitTimer -= Time.delta / 60f;
            return;
        }

        dispatchRaid(wave, player);
    }

    private static void dispatchRaid(Team wave, Team player) {
        int tier = getRaidTier(player);
        RaidEvent raid = pickRaid(tier, raidSeed());
        if (raid == null) return;

        float[] target = pickTarget(wave, player, raidSeed());
        if (target[0] == 0f && target[1] == 0f) {
            target = pickTargetCore(wave, player, raidSeed() + 1);
        }
        if (target[0] == 0f && target[1] == 0f) return;

        float[] source = pickSpawn(target[0], target[1]);

        currentRaidBus = new ActionBus();
        currentRaidBus.add(createAction(raid, wave, source[0], source[1], target[0], target[1]));
        cutscene.addSubActionBus(currentRaidBus);
        raidRunning = true;
    }

    private static RaidEvent pickRaid(int tier, int seed) {
        if (tierPools == null || tierPools.length == 0) return raids.get(1);

        int[] pool = tierPools[Mathf.clamp(tier - 1, 0, tierPools.length - 1)];
        if (pool.length == 0) return raids.get(1);

        int id = pool[new Rand(seed).random(0, pool.length - 1)];
        RaidEvent event = raids.get(id);
        return event != null ? event : raids.get(1);
    }

    private static EventRaidAction createAction(RaidEvent raid, Team wave, float sourceX, float sourceY, float targetX, float targetY) {
        EventRaidAction action = new EventRaidAction();
        action.raidType = RaidPreset.CUSTOM_RAID;
        action.customBullet = raid.bullet;
        action.team = wave;
        action.overrideRaidStats = true;
        action.alertTime = raid.alertTime * Time.toSeconds;
        action.raidTime = raid.raidTime * Time.toSeconds;
        action.raidScale = raid.raidScale;
        action.inaccuracy = raid.inaccuracy;
        action.overrideDefaultCoordinate = true;
        action.sourceX = sourceX * tilesize;
        action.sourceY = sourceY * tilesize;
        action.targetX = targetX * tilesize;
        action.targetY = targetY * tilesize;
        action.postInit();
        return action;
    }

    private static int raidSeed() {
        return (int) (Time.time + state.rules.waveTeam.id);
    }

    public static int getRaidTier(Team player) {
        int maxTier = tierPools != null && tierPools.length > 0 ? tierPools.length : DefaultRaidStrength.maxTier();
        return DefaultRaidStrength.toTier(player, maxTier);
    }

    public static float getRaidStrength(Team player) {
        return DefaultRaidStrength.evaluate(player);
    }

    private static float[] pickTarget(Team wave, Team player, int seed) {
        return pickTarget(wave, player, seed, 3f, 3f, 3f, 1f);
    }

    private static float[] pickTargetCore(Team wave, Team player, int seed) {
        return pickTarget(wave, player, seed, 0f, 0f, 0f, 1f);
    }

    private static float[] pickTarget(Team wave, Team player, int seed, float w1, float w2, float w3, float w4) {
        float[] out = new float[2];
        Rand r = new Rand(seed);
        float wx = r.random(0f, world.unitWidth());
        float wy = r.random(0f, world.unitHeight());

        AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.core);
        WeightedRandom.random(
                new WeightedOption(w1, () -> flag.set(BlockFlag.turret)),
                new WeightedOption(w2, () -> flag.set(BlockFlag.generator)),
                new WeightedOption(w3, () -> flag.set(BlockFlag.factory)),
                new WeightedOption(w4, () -> flag.set(BlockFlag.core))
        );

        Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(wave, flag.get()));
        if (b == null) b = Geometry.findClosest(wx, wy, indexer.getFlagged(player, flag.get()));
        if (b == null) b = player.core();
        if (b == null) return out;

        out[0] = b.tileX();
        out[1] = b.tileY();
        return out;
    }

    private static float[] pickSpawn(float targetX, float targetY) {
        float[] out = new float[2];
        Seq<Tile> spawns = spawner.getSpawns();
        if (spawns.isEmpty()) return out;

        Tile closest = spawns.first();
        float minDst = Mathf.dst2(closest.x, closest.y, targetX, targetY);

        for (int i = 1; i < spawns.size; i++) {
            Tile t = spawns.get(i);
            float dst = Mathf.dst2(t.x, t.y, targetX, targetY);
            if (dst < minDst) {
                minDst = dst;
                closest = t;
            }
        }

        out[0] = closest.x;
        out[1] = closest.y;
        return out;
    }
}
