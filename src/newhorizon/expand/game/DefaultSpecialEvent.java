package newhorizon.expand.game;

import arc.Events;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.IntMap;
import arc.struct.IntSet;
import arc.util.Interval;
import arc.util.Time;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.game.Team;
import newhorizon.content.NHItems;
import newhorizon.content.NHLogic;
import newhorizon.content.NHStatusEffects;
import newhorizon.content.NHUnitTypes;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventInterventionAction;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutscene;
import static newhorizon.expand.game.SpecialEvent.Triggers;

public class DefaultSpecialEvent {
    private static final int OVERRIDE_CHECK_INTERVAL = 120;

    private static final Interval overrideCheck = new Interval(OVERRIDE_CHECK_INTERVAL);
    private static final IntMap<SpecialEvent> events = new IntMap<>();
    private static final IntMap<Long> nextAt = new IntMap<>();
    private static final IntSet fired = new IntSet();

    public static void load() {
        registerEvents();

        Events.on(EventType.PlayEvent.class, e -> reset());
        Events.on(EventType.WorldLoadEvent.class, e -> reset());
    }

    public static void register(int id, SpecialEvent.Builder builder) {
        register(id, builder.build());
    }

    public static void register(int id, SpecialEvent event) {
        event.id = id;
        events.put(id, event);
    }

    public static SpecialEvent get(int id) {
        return events.get(id);
    }

    public static IntMap<SpecialEvent> all() {
        return events;
    }

    public static boolean contains(int id) {
        return events.containsKey(id);
    }

    public static void reset() {
        nextAt.clear();
        fired.clear();
        for (SpecialEvent e : events.values()) {
            if (e.looping) {
                nextAt.put(e.id, Time.millis() + (long) (Math.max(e.loopInterval, 1f) * 1000f));
            }
        }
        overrideCheck.reset(0, OVERRIDE_CHECK_INTERVAL);
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

        updateAuto(wave, player);
    }

    private static void updateAuto(Team wave, Team player) {
        for (SpecialEvent special : events.values()) {
            boolean auto = special.looping || special.triggers.any();
            if (!auto) continue;
            if (!special.triggersMet()) continue;

            if (special.looping) {
                long next = nextAt.get(special.id, 0L);
                if (Time.millis() < next) continue;
            } else if (special.disposable && fired.contains(special.id)) {
                continue;
            }

            float[] target = special.ally()
                    ? DefaultIntervention.pickAllyTarget(player, InterventionSync.nextSyncSeed() + special.id)
                    : DefaultIntervention.pickHostileTarget(wave, player, InterventionSync.nextSyncSeed() + special.id);
            if (target[0] == 0f && target[1] == 0f) {
                if (special.looping) {
                    nextAt.put(special.id, Time.millis() + (long) (Math.max(special.loopInterval, 1f) * 1000f));
                }
                continue;
            }

            ActionBus bus = new ActionBus();
            bus.add(createAction(special, target[0], target[1]));
            cutscene.addSubActionBus(bus);

            if (special.looping) {
                nextAt.put(special.id, Time.millis() + (long) (Math.max(special.loopInterval, 1f) * 1000f));
            } else if (special.disposable) {
                fired.add(special.id);
            }
        }
    }

    public static EventInterventionAction createAction(SpecialEvent special, float tileX, float tileY) {
        EventInterventionAction action = new EventInterventionAction();
        action.applyPreset(special);
        action.team = special.resolveTeam();
        action.overrideStats = true;
        action.overrideDefaultCoordinate = true;
        action.syncSeed = InterventionSync.nextSyncSeed();
        action.targetX = tileX * tilesize;
        action.targetY = tileY * tilesize;
        action.postInit();
        return action;
    }

    public static void runAt(SpecialEvent special, float worldX, float worldY, int syncSeed) {
        if (special == null) return;
        special.runEffects(special.resolveTeam(), worldX, worldY, syncSeed, special.toUnitEntries());
    }

    private static void registerEvents() {
        if (events.size > 0) return;

        register(100, new SpecialEvent.Builder()
                .ally()
                .alert(20f)
                .spawnRange(80f)
                .requireAll()
                .trigger(
                        Triggers.afterMinutes(3f),
                        Triggers.coreItemsAll(NHItems.presstanium, 200,NHItems.juniorProcessor, 200)
                )
                .loop(300)
                .unit(NHUnitTypes.rhino, 1, u -> u
                        .item(NHItems.zeta, 200)
                        .status(StatusEffects.overdrive, 600f))
                .unit(NHUnitTypes.gather, 2, u -> u.status(StatusEffects.overdrive, 600f)));

        register(101, new SpecialEvent.Builder()
                .ally()
                .alert(25f)
                .spawnRange(80f)
                .requireAny()
                .trigger(
                        Triggers.afterMinutes(20f),
                        Triggers.coreItemsAll(NHItems.multipleSteel, 500, NHItems.zeta, 1000)
                )
                .once()
                .unit(NHUnitTypes.saviour, 1, u -> u.status(NHStatusEffects.overphased, 480f))
                .unit(NHUnitTypes.naxos, 2));

        register(102, new SpecialEvent.Builder()
                .ally()
                .alert(30f)
                .spawnRange(160f)
                .trigger(Triggers.and(
                        Triggers.waveAtLeast(100)
                ))
                .once()
                .unit(NHUnitTypes.hurricane, 1, u -> u.status(StatusEffects.overdrive, 600f))
                .unit(NHUnitTypes.longinus, 2, u -> u.status(StatusEffects.overdrive, 600f)));

        register(200, new SpecialEvent.Builder()
                .enemy()
                .alert(200f)
                .trigger(Triggers.and(
                        Triggers.afterMinutes(120f),
                        Triggers.coreItemsAll(NHItems.darkEnergy, 10000,NHItems.hyperProcessor,10000,NHItems.hadronicomp,10000)
                ))
                .once()
                .unit(NHUnitTypes.nucleoid, 1, u -> u
                        .status(NHStatusEffects.overphased, 900f))
                .unit(NHUnitTypes.pester, 2,u -> u
                        .status(NHStatusEffects.overphased, 900f))
                .unit(NHUnitTypes.guardian, 4, u -> u.status(NHStatusEffects.overphased, 600f)));
    }
}
