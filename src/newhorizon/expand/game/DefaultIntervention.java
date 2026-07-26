package newhorizon.expand.game;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.meta.BlockFlag;
import newhorizon.content.NHBullets;
import newhorizon.content.NHLogic;
import newhorizon.content.NHStatusEffects;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.expand.net.NHCall;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutscene;

public class DefaultIntervention {
    private static final float PROTECTION_TIME = 600f;
    private static final float COOLDOWN_MIN = 180f;
    private static final float COOLDOWN_RANGE = 300f;
    private static final int OVERRIDE_CHECK_INTERVAL = 120;

    private static final Interval overrideCheck = new Interval(OVERRIDE_CHECK_INTERVAL);
    private static final IntMap<FleetEvent> fleets = new IntMap<>();
    private static final Seq<int[]> tierPoolBuilder = new Seq<>();
    private static int[][] tierPools;

    private static long nextInterventionAt = Long.MAX_VALUE;
    private static boolean interventionRunning;
    private static ActionBus currentBus;

    public static final class FleetEvent {
        public final int id;
        public final float alertTime;
        public final float spawnRange;
        public final float spawnReloadTime;
        public final float spawnDelay;
        public final StatusEffect status;
        public final float statusDuration;
        public final double flag;
        public final Seq<EventInterventionAction.UnitEntry> units;
        public final boolean special;
        public final Prov<Team> teamProv;
        public final boolean looping;
        public final float loopInterval;

        public FleetEvent(int id, float alertTime, float spawnRange, float spawnReloadTime, float spawnDelay,
                          StatusEffect status, float statusDuration, double flag,
                          Seq<EventInterventionAction.UnitEntry> units,
                          boolean special, Prov<Team> teamProv, boolean looping, float loopInterval) {
            this.id = id;
            this.alertTime = alertTime;
            this.spawnRange = spawnRange;
            this.spawnReloadTime = spawnReloadTime;
            this.spawnDelay = spawnDelay;
            this.status = status;
            this.statusDuration = statusDuration;
            this.flag = flag;
            this.units = units;
            this.special = special;
            this.teamProv = teamProv;
            this.looping = looping;
            this.loopInterval = loopInterval;
        }

        public Team resolveTeam(Team fallback) {
            if (teamProv == null) return fallback;
            Team t = teamProv.get();
            return t != null ? t : fallback;
        }

        public boolean ally() {
            return teamProv != null && teamProv.get() == state.rules.defaultTeam;
        }
    }

    private static void event(int id, float alert, Object... spawns) {
        putEvent(id, alert, 180f, 50f, 15f, StatusEffects.none, 600f, Double.NaN, false, null, false, 0f, spawns);
    }

    private static void eventWithStatus(int id, float alert, StatusEffect status, float statusDuration, Object... spawns) {
        putEvent(id, alert, 180f, 50f, 15f, status, statusDuration, Double.NaN, false, null, false, 0f, spawns);
    }

    private static void putEvent(int id, float alert, float spawnRange, float spawnReload, float spawnDelay,
                                 StatusEffect status, float statusDuration, double flag,
                                 boolean special, Prov<Team> teamProv, boolean looping, float loopInterval,
                                 Object... spawns) {
        fleets.put(id, new FleetEvent(id, alert, spawnRange, spawnReload, spawnDelay, status, statusDuration, flag,
                spawn(spawns), special, teamProv, looping, loopInterval));
    }

    private static Seq<EventInterventionAction.UnitEntry> spawn(Object... items) {
        Seq<EventInterventionAction.UnitEntry> seq = new Seq<>();
        for (int i = 0; i < items.length; i += 2) {
            seq.add(new EventInterventionAction.UnitEntry((UnitType) items[i], ((Number) items[i + 1]).intValue()));
        }
        return seq;
    }

    private static void tier(int... ids) {
        tierPoolBuilder.add(ids);
    }

    public static void load() {
        registerFleets();

        Events.on(EventType.PlayEvent.class, event -> {
            NHLogic.refreshCustomInterventionLogic();
            InterventionState.init();
            reset();
        });
        Events.on(EventType.WorldLoadEvent.class, event -> {
            NHLogic.refreshCustomInterventionLogic();
            InterventionState.init();
            reset();
            if (RaidLogic.isRemoteClient()) {
                Core.app.post(NHCall::requestInterventionSync);
            }
        });

        Events.on(EventType.PlayerConnect.class, e -> {
            if (!net.server() || !net.active() || e.player == null) return;
            InterventionSync.pushStateTo(e.player);
        });
    }

    public static EventInterventionAction activeInterventionAction() {
        if (!interventionRunning || currentBus == null) return null;
        if (currentBus.current instanceof EventInterventionAction action) return action;
        for (Action queued : currentBus.queue) {
            if (queued instanceof EventInterventionAction action) return action;
        }
        return null;
    }

    public static FleetEvent get(int id) {
        return fleets.get(id);
    }

    public static IntMap<FleetEvent> all() {
        return fleets;
    }

    public static boolean isSpecial(int id) {
        return DefaultSpecialEvent.contains(id);
    }

    private static void registerFleets() {
        if (fleets.size > 0) return;
        tierPoolBuilder.clear();

        registerFleetEvents();
        registerTierPools();

        tierPools = tierPoolBuilder.toArray(int[].class);
    }

    private static void registerFleetEvents() {
        event(1, 60f, NHUnitTypes.branch, 4, NHUnitTypes.sharp, 4);
        event(2, 60f, UnitTypes.horizon, 16, NHUnitTypes.sharp, 4);
        event(3, 180f, NHUnitTypes.warper, 1, NHUnitTypes.assaulter, 6, NHUnitTypes.branch, 4);
        event(4, 90f, NHUnitTypes.warper, 4, NHUnitTypes.histone, 6);
        event(5, 180f, NHUnitTypes.naxos, 2, NHUnitTypes.branch, 4, NHUnitTypes.warper, 8, NHUnitTypes.assaulter, 9);
        event(6, 240f, NHUnitTypes.macrophage, 3);
        eventWithStatus(7, 180f,NHStatusEffects.emp1,600, NHUnitTypes.saviour, 1, NHUnitTypes.naxos, 2);
        eventWithStatus(8, 180f,NHStatusEffects.emp1,600, NHUnitTypes.destruction, 2, NHUnitTypes.naxos, 4);
        event(9, 120f, NHUnitTypes.warper, 10, NHUnitTypes.assaulter, 4, NHUnitTypes.branch, 6);
        event(10, 240f, NHUnitTypes.guardian, 1,NHUnitTypes.naxos,4);
        event(11, 180f, NHUnitTypes.longinus, 2, NHUnitTypes.naxos, 4, NHUnitTypes.saviour, 1);
        eventWithStatus(12, 180f,NHStatusEffects.phased,600,NHUnitTypes.anvil, 1);
        event(13, 120f, NHUnitTypes.lymph, 3,NHUnitTypes.restrictionEnzyme, 6);
        eventWithStatus(14, 120f, NHStatusEffects.overphased,600,NHUnitTypes.destruction, 3, NHUnitTypes.naxos, 4, NHUnitTypes.saviour, 1);
    }

    private static void registerTierPools() {
        tier(1, 2, 3);
        tier(2, 3, 4);
        tier(3, 4, 5);
        tier(5, 6, 7, 8);
        tier(7, 8, 9, 10);
        tier(9, 10, 11, 12);
        tier(11, 12, 13);
        tier(11, 12, 13, 14);
    }

    public static void reset() {
        scheduleNext(PROTECTION_TIME);
        interventionRunning = false;
        currentBus = null;
        overrideCheck.reset(0, OVERRIDE_CHECK_INTERVAL);
    }

    private static void scheduleNext(float delaySeconds) {
        nextInterventionAt = Time.millis() + (long) (delaySeconds * 1000f);
    }

    public static void update() {
        if (!InterventionState.enabled()) return;
        if (!RaidLogic.isLogicSide()) return;
        if (!state.isPlaying()) return;
        if (state.rules.editor || state.rules.mode() == Gamemode.sandbox || state.rules.mode() == Gamemode.pvp) return;

        if (overrideCheck.get(0, OVERRIDE_CHECK_INTERVAL)) {
            NHLogic.refreshCustomInterventionLogic();
        }
        if (NHLogic.hasCustomInterventionLogic()) return;

        Team wave = state.rules.waveTeam;
        Team player = state.rules.defaultTeam;
        if (wave == null || player == null) return;

        if (interventionRunning) {
            if (currentBus == null || currentBus.complete()) {
                interventionRunning = false;
                currentBus = null;
                scheduleNext(COOLDOWN_MIN + new Rand((int) Time.time).random(COOLDOWN_RANGE));
            }
            return;
        }

        if (Time.millis() < nextInterventionAt) return;

        dispatch(wave, player);
    }

    private static void dispatch(Team wave, Team player) {
        int tier = getInterventionTier(player);
        FleetEvent fleet = pickFleet(tier, interventionSeed());
        if (fleet == null || fleet.special) return;

        float[] target = pickHostileTarget(wave, player, interventionSeed());
        if (target[0] == 0f && target[1] == 0f) return;

        currentBus = new ActionBus();
        currentBus.add(createAction(fleet, wave, target[0], target[1]));
        cutscene.addSubActionBus(currentBus);
        interventionRunning = true;
    }

    public static EventInterventionAction createAction(FleetEvent fleet, Team team, float targetX, float targetY) {
        EventInterventionAction action = new EventInterventionAction();
        action.applyPreset(fleet);
        action.team = fleet.resolveTeam(team);
        action.overrideStats = true;
        action.overrideDefaultCoordinate = true;
        action.syncSeed = InterventionSync.nextSyncSeed();
        action.targetX = targetX * tilesize;
        action.targetY = targetY * tilesize;
        action.postInit();
        return action;
    }

    private static FleetEvent pickFleet(int tier, int seed) {
        if (tierPools == null || tierPools.length == 0) return fleets.get(1);

        int[] pool = tierPools[Mathf.clamp(tier - 1, 0, tierPools.length - 1)];
        if (pool.length == 0) return fleets.get(1);

        int id = pool[new Rand(seed).random(0, pool.length - 1)];
        FleetEvent event = fleets.get(id);
        if (event == null || event.special) return fleets.get(1);
        return event;
    }

    private static int interventionSeed() {
        return (int) (Time.time + state.rules.waveTeam.id * 17L);
    }

    public static int getInterventionTier(Team player) {
        int maxTier = tierPools != null && tierPools.length > 0 ? tierPools.length : DefaultRaidStrength.maxTier();
        return DefaultRaidStrength.toTier(player, maxTier);
    }

    public static float[] pickHostileTarget(Team wave, Team player, int seed) {
        float[] out = pickTarget(wave, player, seed);
        if (out[0] == 0f && out[1] == 0f) {
            out = pickTargetCore(wave, player, seed + 1);
        }
        return out;
    }

    public static float[] pickAllyTarget(Team player, int seed) {
        float[] out = new float[2];
        Building core = player.core();
        if (core == null) return out;

        Rand r = new Rand(seed);
        float ang = r.random(360f);
        float dst = r.random(4f, 12f);
        out[0] = core.tileX() + Mathf.cosDeg(ang) * dst;
        out[1] = core.tileY() + Mathf.sinDeg(ang) * dst;
        return out;
    }

    private static float[] pickTarget(Team wave, Team player, int seed) {
        float[] out = new float[2];
        Rand r = new Rand(seed);
        float wx = r.random(0f, world.unitWidth());
        float wy = r.random(0f, world.unitHeight());

        AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.turret);
        WeightedRandom.random(
                new WeightedOption(3f, () -> flag.set(BlockFlag.turret)),
                new WeightedOption(3f, () -> flag.set(BlockFlag.generator)),
                new WeightedOption(3f, () -> flag.set(BlockFlag.factory))
        );

        Building b = findClosestBuilding(wave, player, flag.get(), wx, wy);
        if (b == null) {
            for (BlockFlag fallback : new BlockFlag[]{BlockFlag.turret, BlockFlag.generator, BlockFlag.factory}) {
                if (fallback == flag.get()) continue;
                b = findClosestBuilding(wave, player, fallback, wx, wy);
                if (b != null) break;
            }
        }
        if (b == null) return out;

        out[0] = b.tileX();
        out[1] = b.tileY();
        return out;
    }

    private static float[] pickTargetCore(Team wave, Team player, int seed) {
        float[] out = new float[2];
        Rand r = new Rand(seed);
        float wx = r.random(0f, world.unitWidth());
        float wy = r.random(0f, world.unitHeight());

        Building b = findClosestBuilding(wave, player, BlockFlag.core, wx, wy);
        if (b == null) b = player.core();
        if (b == null) return out;

        out[0] = b.tileX();
        out[1] = b.tileY();
        return out;
    }

    private static Building findClosestBuilding(Team wave, Team player, BlockFlag flag, float wx, float wy) {
        Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(wave, flag));
        if (b == null) b = Geometry.findClosest(wx, wy, indexer.getFlagged(player, flag));
        return b;
    }
}
