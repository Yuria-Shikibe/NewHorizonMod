package newhorizon.expand.game;

import arc.Events;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import newhorizon.NHSetting;
import newhorizon.content.NHItems;
import newhorizon.content.NHLogic;
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
    private static final float COOLDOWN_MIN = 240f;
    private static final float COOLDOWN_RANGE = 120f;
    private static final int OVERRIDE_CHECK_INTERVAL = 120;

    private static final Interval overrideCheck = new Interval(OVERRIDE_CHECK_INTERVAL);

    private static final float[][] TIER_CONFIG = {
            {30f, 5f, 1f, 60f},
            {30f, 5f, 1.2f, 65f},
            {30f, 6f, 1.5f, 70f},
            {30f, 6f, 2f, 75f},
            {30f, 7f, 2.5f, 80f},
            {30f, 7f, 3f, 85f},
            {30f, 8f, 3.5f, 90f},
            {30f, 8f, 4f, 100f},
    };

    private static float waitTimer;
    private static boolean raidRunning;
    private static ActionBus currentRaidBus;

    public static void load() {
        Events.on(EventType.PlayEvent.class, event -> {
            NHLogic.refreshCustomRaidLogic();
            reset();
        });
        Events.on(EventType.WorldLoadEvent.class, event -> {
            NHLogic.refreshCustomRaidLogic();
            reset();
        });
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
        float[] target = pickTarget(wave, player, raidSeed());
        if (target[0] == 0f && target[1] == 0f) {
            target = pickTargetCore(wave, player, raidSeed() + 1);
        }
        if (target[0] == 0f && target[1] == 0f) return;

        float[] source = pickSpawn(target[0], target[1]);
        float[] config = TIER_CONFIG[Mathf.clamp(tier - 1, 0, TIER_CONFIG.length - 1)];

        EventRaidAction action = new EventRaidAction();
        action.raidType = RaidPreset.CUSTOM_RAID;
        action.customBulletType = tier;
        action.team = wave;
        action.overrideRaidStats = true;
        action.alertTime = config[0] * Time.toSeconds;
        action.raidTime = config[1] * Time.toSeconds;
        action.raidScale = config[2];
        action.inaccuracy = config[3];
        action.overrideDefaultCoordinate = true;
        action.sourceX = source[0] * tilesize;
        action.sourceY = source[1] * tilesize;
        action.targetX = target[0] * tilesize;
        action.targetY = target[1] * tilesize;
        action.postInit();

        currentRaidBus = new ActionBus();
        currentRaidBus.add(action);
        cutscene.addSubActionBus(currentRaidBus);
        raidRunning = true;
    }

    private static int raidSeed() {
        return (int) (Time.time + state.rules.waveTeam.id);
    }

    public static int getRaidTier(Team player) {
        int[] tier = {1};
        player.cores().each(core -> tier[0] = Math.max(tier[0], getCoreTier(core)));
        return tier[0];
    }

    private static int getCoreTier(CoreBlock.CoreBuild core) {
        if (core.items.has(NHItems.hyperProcessor) || core.items.has(NHItems.darkEnergy)) return 8;
        if (core.items.has(NHItems.ancimembrane) || core.items.has(NHItems.hadronicomp)) return 7;
        if (core.items.has(NHItems.nodexPlate)) return 6;
        if (core.items.has(NHItems.setonAlloy)) return 5;
        if (core.items.has(NHItems.multipleSteel)) return 4;
        if (core.items.has(NHItems.presstanium)) return 3;
        if (core.items.has(NHItems.silicar)) return 2;
        return 1;
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
