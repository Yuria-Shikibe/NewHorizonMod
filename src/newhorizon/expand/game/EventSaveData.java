package newhorizon.expand.game;

import arc.struct.Seq;
import arc.Events;
import arc.Core;
import mindustry.game.EventType;
import arc.util.Log;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.io.SaveFileReader;
import mindustry.type.ItemStack;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.Block;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.components.action.EventSpecialAction;
import newhorizon.expand.logic.cutscene.types.RaidPreset;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.content;
import static newhorizon.NHVars.cutscene;

/** Persists active local event actions across save/load. */
public class EventSaveData implements SaveFileReader.CustomChunk {
    private static final short VERSION = 3;
    private static final byte RAID = 1;
    private static final byte INTERVENTION = 2;
    /** Retained only to read saves written before processor interventions became switches. */
    private static final byte PROCESSOR_INTERVENTION = 3;
    private static final byte SPECIAL = 4;

    private final Seq<SavedEvent> pending = new Seq<>();
    private final Seq<SavedEvent> preparedWrite = new Seq<>();
    private final Seq<EventRaidAction> activeRaids = new Seq<>();
    private final Seq<EventInterventionAction> activeInterventions = new Seq<>();
    private final Seq<EventSpecialAction> activeSpecials = new Seq<>();
    private boolean writePrepared;

    public EventSaveData() {
        Events.on(EventType.WorldLoadBeginEvent.class, event -> {
            pending.clear();
            preparedWrite.clear();
            writePrepared = false;
            activeRaids.clear();
            activeInterventions.clear();
            activeSpecials.clear();
        });
        Events.on(EventType.SaveWriteEvent.class, event -> prepareWrite());
        // Defer restoration until CutsceneControl has discarded actions from the previous world.
        Events.on(EventType.WorldLoadEvent.class, event -> Core.app.post(this::restore));
    }

    public void track(EventRaidAction action) {
        if (action != null && !action.presentationOnly) activeRaids.addUnique(action);
    }

    public void track(EventInterventionAction action) {
        if (action != null && !action.presentationOnly) activeInterventions.addUnique(action);
    }

    public void untrack(EventRaidAction action) {
        activeRaids.remove(action, true);
    }

    public void untrack(EventInterventionAction action) {
        activeInterventions.remove(action, true);
    }

    public void track(EventSpecialAction action) {
        if (action != null) activeSpecials.addUnique(action);
    }

    public void untrack(EventSpecialAction action) {
        activeSpecials.remove(action, true);
    }

    @Override
    public boolean shouldWrite() {
        // Event state is embedded in the established nh-world-data chunk.
        return false;
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        writeSnapshot(stream);
    }

    public void writeSnapshot(DataOutput stream) throws IOException {
        Seq<SavedEvent> events = writePrepared ? preparedWrite : collect();
        try {
            stream.writeShort(VERSION);
            stream.writeInt(events.size);
            int raids = 0, interventions = 0, specials = 0;
            for (SavedEvent event : events) {
                event.write(stream);
                if (event.type == RAID) raids++;
                else if (event.type == INTERVENTION) interventions++;
                else if (event.type == SPECIAL) specials++;
            }
            Log.info("[New Horizon] Saved @ active local event(s): raid @, intervention @, special @.",
                    events.size, raids, interventions, specials);
        } finally {
            preparedWrite.clear();
            writePrepared = false;
        }
    }

    @Override
    public void read(DataInput stream) throws IOException {
        readSnapshot(stream);
    }

    public void readSnapshot(DataInput stream) throws IOException {
        pending.clear();
        short version = stream.readShort();
        if (version != 1 && version != 2 && version != VERSION) return;

        int count = stream.readInt();
        if (count < 0 || count > 256) throw new IOException("Invalid New Horizon event count: " + count);
        for (int i = 0; i < count; i++) pending.add(SavedEvent.read(stream));
        Log.info("[New Horizon] Loaded @ saved local event(s).", count);
    }

    @Override
    public void read(DataInput stream, int length) throws IOException {
        read(stream);
    }

    private Seq<SavedEvent> collect() {
        Seq<SavedEvent> events = new Seq<>();
        if (cutscene == null) return events;

        for (EventRaidAction action : activeRaids) collectAction(action, events);
        for (EventInterventionAction action : activeInterventions) collectAction(action, events);
        for (EventSpecialAction action : activeSpecials) collectAction(action, events);
        collectBus(cutscene.mainBus, events);
        for (ActionBus bus : cutscene.subBuses) collectBus(bus, events);
        for (ActionBus bus : cutscene.waitingBuses) collectBus(bus, events);
        collectAction(DefaultRaid.activeRaidAction(), events);
        collectAction(DefaultIntervention.activeInterventionAction(), events);

        return events;
    }

    private void prepareWrite() {
        preparedWrite.clear();
        preparedWrite.addAll(collect());
        writePrepared = true;
    }

    private void collectBus(ActionBus bus, Seq<SavedEvent> events) {
        if (bus == null) return;
        collectAction(bus.current, events);
        for (Action action : bus.queue) collectAction(action, events);
    }

    private void collectAction(Action action, Seq<SavedEvent> events) {
        if (action == null || action.complete()) return;
        if (action instanceof EventRaidAction raid) {
            if (!raid.presentationOnly && !contains(events, raid)) events.add(SavedEvent.from(raid));
        } else if (action instanceof EventInterventionAction intervention) {
            if (!intervention.presentationOnly && !contains(events, intervention)) events.add(SavedEvent.from(intervention));
        } else if (action instanceof EventSpecialAction special) {
            if (!contains(events, special)) events.add(SavedEvent.from(special));
        }
    }

    private boolean contains(Seq<SavedEvent> events, Action action) {
        for (SavedEvent event : events) {
            if (event.action == action || event.special == action) return true;
        }
        return false;
    }

    private void restore() {
        if (pending.isEmpty() || cutscene == null || RaidLogic.isRemoteClient()) {
            pending.clear();
            DefaultSpecialEvent.finishRestore();
            return;
        }

        int restored = 0;
        for (SavedEvent saved : pending) {
            // Processor interventions were removed in favor of simple state switches.
            if (saved.type == 3) continue;
            Action action = saved.create();
            if (action == null || action.complete()) continue;

            if (action instanceof EventInterventionAction intervention
                    && DefaultSpecialEvent.contains(intervention.eventId)) {
                DefaultSpecialEvent.restoreAction(intervention);
            }

            ActionBus bus = new ActionBus();
            bus.add(action);
            cutscene.addSubActionBus(bus);
            restored++;

            if (saved.managedDefault && action instanceof EventRaidAction raid) {
                DefaultRaid.restoreAction(bus, raid);
            } else if (saved.managedDefault && action instanceof EventInterventionAction intervention) {
                DefaultIntervention.restoreAction(bus, intervention);
            }
        }
        Log.info("[New Horizon] Restored @ local event action(s).", restored);
        pending.clear();
        DefaultSpecialEvent.finishRestore();
    }


    private static final class SavedEvent {
        private byte type;
        private Action action;
        private EventSpecialAction special;
        private boolean managedDefault;

        private RaidPreset raidType;
        private int customBulletType, customBulletId, keyBulletId;
        private int raidTeam;
        private boolean overrideRaidStats, overrideDefaultCoordinate, gatedByRaidState, presentationOnly, spawnBullets;
        private float raidAlertTime, raidTime, raidScale, inaccuracy, sourceX, sourceY, targetX, targetY;
        private int raidSyncSeed, raidCounter;
        private float raidLifeTimer, raidDuration;

        private int interventionEventId, interventionTeam, interventionStatusId;
        private boolean interventionOverrideStats, interventionOverrideDefaultCoordinate;
        private boolean interventionPresentationOnly, interventionPresentationSuppressed, interventionSpawned;
        private float interventionAlertTime, spawnRange, spawnReloadTime, spawnDelay;
        private float interventionTargetX, interventionTargetY, statusDuration;
        private double flag;
        private int interventionSyncSeed;
        private float interventionLifeTimer, interventionDuration;
        private final Seq<int[]> units = new Seq<>();

        private int processorPos, processorInstructionIndex, processorFleetId;
        private float processorCurTime, processorTargetX, processorTargetY;
        private boolean processorIconShown, processorLabelShown, processorSpawned;

        private int specialTeam;
        private boolean specialOverrideDefaultCoordinate, specialSpawned;
        private int specialSyncSeed;
        private float specialAlertTime, specialSpawnRange, specialTargetX, specialTargetY;
        private float specialLifeTimer, specialDuration;
        private final Seq<SavedSpecialUnit> specialUnits = new Seq<>();

        private static SavedEvent from(EventRaidAction action) {
            SavedEvent saved = new SavedEvent();
            saved.type = RAID;
            saved.action = action;
            saved.managedDefault = DefaultRaid.activeRaidAction() == action;
            saved.raidType = action.raidType;
            saved.customBulletType = action.customBulletType;
            saved.customBulletId = bulletId(action.customBullet);
            saved.keyBulletId = bulletId(action.keyBullet);
            saved.raidTeam = action.team == null ? Team.crux.id : action.team.id;
            saved.overrideRaidStats = action.overrideRaidStats;
            saved.overrideDefaultCoordinate = action.overrideDefaultCoordinate;
            saved.gatedByRaidState = action.gatedByRaidState;
            saved.presentationOnly = action.presentationOnly;
            saved.spawnBullets = action.spawnBullets;
            saved.raidAlertTime = action.alertTime;
            saved.raidTime = action.raidTime;
            saved.raidScale = action.raidScale;
            saved.inaccuracy = action.inaccuracy;
            saved.sourceX = action.sourceX;
            saved.sourceY = action.sourceY;
            saved.targetX = action.targetX;
            saved.targetY = action.targetY;
            saved.raidSyncSeed = action.syncSeed;
            saved.raidCounter = action.raidCounter();
            saved.raidLifeTimer = action.lifeTimer;
            saved.raidDuration = action.duration;
            return saved;
        }

        private static SavedEvent from(EventInterventionAction action) {
            SavedEvent saved = new SavedEvent();
            saved.type = INTERVENTION;
            saved.action = action;
            saved.managedDefault = DefaultIntervention.activeInterventionAction() == action;
            saved.interventionEventId = action.eventId;
            saved.interventionTeam = action.team == null ? Team.crux.id : action.team.id;
            saved.interventionOverrideStats = action.overrideStats;
            saved.interventionOverrideDefaultCoordinate = action.overrideDefaultCoordinate;
            saved.interventionPresentationOnly = action.presentationOnly;
            saved.interventionPresentationSuppressed = action.presentationSuppressed;
            saved.interventionSpawned = action.spawned();
            saved.interventionAlertTime = action.alertTime;
            saved.spawnRange = action.spawnRange;
            saved.spawnReloadTime = action.spawnReloadTime;
            saved.spawnDelay = action.spawnDelay;
            saved.interventionTargetX = action.targetX;
            saved.interventionTargetY = action.targetY;
            saved.interventionStatusId = statusId(action.status);
            saved.statusDuration = action.statusDuration;
            saved.flag = action.flag;
            saved.interventionSyncSeed = action.syncSeed;
            saved.interventionLifeTimer = action.lifeTimer;
            saved.interventionDuration = action.duration;
            for (EventInterventionAction.UnitEntry entry : action.units) {
                if (entry != null && entry.type != null) saved.units.add(new int[]{entry.type.id, entry.count});
            }
            return saved;
        }

        private static SavedEvent from(EventSpecialAction action) {
            SavedEvent saved = new SavedEvent();
            saved.type = SPECIAL;
            saved.special = action;
            saved.specialTeam = action.team == null ? Team.crux.id : action.team.id;
            saved.specialAlertTime = action.alertTime;
            saved.specialSpawnRange = action.spawnRange;
            saved.specialTargetX = action.targetX;
            saved.specialTargetY = action.targetY;
            saved.specialOverrideDefaultCoordinate = action.overrideDefaultCoordinate;
            saved.specialSyncSeed = action.syncSeed;
            saved.specialLifeTimer = action.lifeTimer;
            saved.specialDuration = action.duration;
            saved.specialSpawned = action.spawned();
            for (SpecialEvent.UnitSpec unit : action.units) {
                if (unit != null && unit.type != null && unit.count > 0) {
                    saved.specialUnits.add(SavedSpecialUnit.from(unit));
                }
            }
            return saved;
        }

        private void write(DataOutput out) throws IOException {
            out.writeByte(type);
            out.writeBoolean(managedDefault);
            if (type == RAID) {
                out.writeUTF(raidType == null ? RaidPreset.PRESET_RAID_1.name() : raidType.name());
                out.writeInt(customBulletType);
                out.writeInt(customBulletId);
                out.writeInt(keyBulletId);
                out.writeInt(raidTeam);
                out.writeBoolean(overrideRaidStats);
                out.writeBoolean(overrideDefaultCoordinate);
                out.writeBoolean(gatedByRaidState);
                out.writeBoolean(presentationOnly);
                out.writeBoolean(spawnBullets);
                out.writeFloat(raidAlertTime);
                out.writeFloat(raidTime);
                out.writeFloat(raidScale);
                out.writeFloat(inaccuracy);
                out.writeFloat(sourceX);
                out.writeFloat(sourceY);
                out.writeFloat(targetX);
                out.writeFloat(targetY);
                out.writeInt(raidSyncSeed);
                out.writeInt(raidCounter);
                out.writeFloat(raidLifeTimer);
                out.writeFloat(raidDuration);
            } else if (type == PROCESSOR_INTERVENTION) {
                out.writeInt(processorPos);
                out.writeInt(processorInstructionIndex);
                out.writeInt(processorFleetId);
                out.writeFloat(processorCurTime);
                out.writeBoolean(processorIconShown);
                out.writeBoolean(processorLabelShown);
                out.writeBoolean(processorSpawned);
                out.writeFloat(processorTargetX);
                out.writeFloat(processorTargetY);
            } else if (type == SPECIAL) {
                out.writeInt(specialTeam);
                out.writeFloat(specialAlertTime);
                out.writeFloat(specialSpawnRange);
                out.writeFloat(specialTargetX);
                out.writeFloat(specialTargetY);
                out.writeBoolean(specialOverrideDefaultCoordinate);
                out.writeInt(specialSyncSeed);
                out.writeFloat(specialLifeTimer);
                out.writeFloat(specialDuration);
                out.writeBoolean(specialSpawned);
                out.writeInt(specialUnits.size);
                for (SavedSpecialUnit unit : specialUnits) unit.write(out);
            } else {
                out.writeInt(interventionEventId);
                out.writeInt(interventionTeam);
                out.writeBoolean(interventionOverrideStats);
                out.writeBoolean(interventionOverrideDefaultCoordinate);
                out.writeBoolean(interventionPresentationOnly);
                out.writeBoolean(interventionPresentationSuppressed);
                out.writeBoolean(interventionSpawned);
                out.writeFloat(interventionAlertTime);
                out.writeFloat(spawnRange);
                out.writeFloat(spawnReloadTime);
                out.writeFloat(spawnDelay);
                out.writeFloat(interventionTargetX);
                out.writeFloat(interventionTargetY);
                out.writeInt(interventionStatusId);
                out.writeFloat(statusDuration);
                out.writeDouble(flag);
                out.writeInt(interventionSyncSeed);
                out.writeFloat(interventionLifeTimer);
                out.writeFloat(interventionDuration);
                out.writeInt(units.size);
                for (int[] unit : units) {
                    out.writeInt(unit[0]);
                    out.writeInt(unit[1]);
                }
            }
        }

        private static SavedEvent read(DataInput in) throws IOException {
            SavedEvent saved = new SavedEvent();
            saved.type = in.readByte();
            saved.managedDefault = in.readBoolean();
            if (saved.type == RAID) {
                try {
                    saved.raidType = RaidPreset.valueOf(in.readUTF());
                } catch (IllegalArgumentException e) {
                    saved.raidType = RaidPreset.PRESET_RAID_1;
                }
                saved.customBulletType = in.readInt();
                saved.customBulletId = in.readInt();
                saved.keyBulletId = in.readInt();
                saved.raidTeam = in.readInt();
                saved.overrideRaidStats = in.readBoolean();
                saved.overrideDefaultCoordinate = in.readBoolean();
                saved.gatedByRaidState = in.readBoolean();
                saved.presentationOnly = in.readBoolean();
                saved.spawnBullets = in.readBoolean();
                saved.raidAlertTime = in.readFloat();
                saved.raidTime = in.readFloat();
                saved.raidScale = in.readFloat();
                saved.inaccuracy = in.readFloat();
                saved.sourceX = in.readFloat();
                saved.sourceY = in.readFloat();
                saved.targetX = in.readFloat();
                saved.targetY = in.readFloat();
                saved.raidSyncSeed = in.readInt();
                saved.raidCounter = in.readInt();
                saved.raidLifeTimer = in.readFloat();
                saved.raidDuration = in.readFloat();
            } else if (saved.type == INTERVENTION) {
                saved.interventionEventId = in.readInt();
                saved.interventionTeam = in.readInt();
                saved.interventionOverrideStats = in.readBoolean();
                saved.interventionOverrideDefaultCoordinate = in.readBoolean();
                saved.interventionPresentationOnly = in.readBoolean();
                saved.interventionPresentationSuppressed = in.readBoolean();
                saved.interventionSpawned = in.readBoolean();
                saved.interventionAlertTime = in.readFloat();
                saved.spawnRange = in.readFloat();
                saved.spawnReloadTime = in.readFloat();
                saved.spawnDelay = in.readFloat();
                saved.interventionTargetX = in.readFloat();
                saved.interventionTargetY = in.readFloat();
                saved.interventionStatusId = in.readInt();
                saved.statusDuration = in.readFloat();
                saved.flag = in.readDouble();
                saved.interventionSyncSeed = in.readInt();
                saved.interventionLifeTimer = in.readFloat();
                saved.interventionDuration = in.readFloat();
                int count = in.readInt();
                if (count < 0 || count > 1024) throw new IOException("Invalid New Horizon unit count: " + count);
                for (int i = 0; i < count; i++) saved.units.add(new int[]{in.readInt(), in.readInt()});
            } else if (saved.type == PROCESSOR_INTERVENTION) {
                saved.processorPos = in.readInt();
                saved.processorInstructionIndex = in.readInt();
                saved.processorFleetId = in.readInt();
                saved.processorCurTime = in.readFloat();
                saved.processorIconShown = in.readBoolean();
                saved.processorLabelShown = in.readBoolean();
                saved.processorSpawned = in.readBoolean();
                saved.processorTargetX = in.readFloat();
                saved.processorTargetY = in.readFloat();
            } else if (saved.type == SPECIAL) {
                saved.specialTeam = in.readInt();
                saved.specialAlertTime = in.readFloat();
                saved.specialSpawnRange = in.readFloat();
                saved.specialTargetX = in.readFloat();
                saved.specialTargetY = in.readFloat();
                saved.specialOverrideDefaultCoordinate = in.readBoolean();
                saved.specialSyncSeed = in.readInt();
                saved.specialLifeTimer = in.readFloat();
                saved.specialDuration = in.readFloat();
                saved.specialSpawned = in.readBoolean();
                int count = in.readInt();
                if (count < 0 || count > 1024) throw new IOException("Invalid New Horizon special unit count: " + count);
                for (int i = 0; i < count; i++) saved.specialUnits.add(SavedSpecialUnit.read(in));
            } else {
                throw new IOException("Unknown New Horizon event type: " + saved.type);
            }
            return saved;
        }

        private Action create() {
            if (type == RAID) {
                EventRaidAction action = new EventRaidAction();
                action.raidType = raidType;
                action.customBulletType = customBulletType;
                action.customBullet = bullet(customBulletId);
                action.keyBullet = bullet(keyBulletId);
                action.team = team(raidTeam);
                action.overrideRaidStats = overrideRaidStats;
                action.overrideDefaultCoordinate = overrideDefaultCoordinate;
                action.gatedByRaidState = gatedByRaidState;
                action.presentationOnly = presentationOnly;
                action.spawnBullets = spawnBullets;
                action.alertTime = raidAlertTime;
                action.raidTime = raidTime;
                action.raidScale = raidScale;
                action.inaccuracy = inaccuracy;
                action.sourceX = sourceX;
                action.sourceY = sourceY;
                action.targetX = targetX;
                action.targetY = targetY;
                action.syncSeed = raidSyncSeed;
                action.duration = raidDuration;
                action.applyNetworkState(raidLifeTimer, raidCounter);
                return action;
            }

            if (type == INTERVENTION) {
                EventInterventionAction action = new EventInterventionAction();
                action.eventId = interventionEventId;
                action.team = team(interventionTeam);
                action.overrideStats = interventionOverrideStats;
                action.overrideDefaultCoordinate = interventionOverrideDefaultCoordinate;
                action.presentationOnly = interventionPresentationOnly;
                action.presentationSuppressed = interventionPresentationSuppressed;
                action.alertTime = interventionAlertTime;
                action.spawnRange = spawnRange;
                action.spawnReloadTime = spawnReloadTime;
                action.spawnDelay = spawnDelay;
                action.targetX = interventionTargetX;
                action.targetY = interventionTargetY;
                action.status = status(interventionStatusId);
                action.statusDuration = statusDuration;
                action.flag = flag;
                action.syncSeed = interventionSyncSeed;
                action.duration = interventionDuration;
                for (int[] unit : units) {
                    UnitType type = content.unit(unit[0]);
                    if (type != null && unit[1] > 0) action.units.add(new EventInterventionAction.UnitEntry(type, unit[1]));
                }
                action.applyNetworkState(interventionLifeTimer, interventionSpawned);
                return action;
            }
            if (type == SPECIAL) {
                EventSpecialAction action = new EventSpecialAction();
                action.team = team(specialTeam);
                action.alertTime = specialAlertTime;
                action.spawnRange = specialSpawnRange;
                action.targetX = specialTargetX;
                action.targetY = specialTargetY;
                action.overrideDefaultCoordinate = specialOverrideDefaultCoordinate;
                action.syncSeed = specialSyncSeed;
                action.duration = specialDuration;
                for (SavedSpecialUnit unit : specialUnits) {
                    SpecialEvent.UnitSpec spec = unit.create();
                    if (spec != null) action.units.add(spec);
                }
                action.applyNetworkState(specialLifeTimer, specialSpawned);
                return action;
            }
            return null;
        }

        private static int bulletId(BulletType bullet) {
            return bullet == null ? -1 : bullet.id;
        }

        private static Team team(int id) {
            Team team = Team.get(id);
            return team == null ? Team.crux : team;
        }

        private static BulletType bullet(int id) {
            return id < 0 ? null : content.bullet(id);
        }

        private static Item item(int id) {
            return id < 0 ? null : content.item(id);
        }

        private static Block block(int id) {
            return id < 0 ? null : content.block(id);
        }

        private static int statusId(StatusEffect status) {
            return status == null ? -1 : status.id;
        }

        private static StatusEffect status(int id) {
            if (id < 0) return null;
            for (StatusEffect effect : content.statusEffects()) {
                if (effect != null && effect.id == id) return effect;
            }
            return null;
        }
    }

    private static final class SavedSpecialUnit {
        private int typeId, count, primaryStatusId, payloadId;
        private float primaryStatusDuration;
        private double flag;
        private final Seq<SavedSpecialStatus> statuses = new Seq<>();
        private final Seq<int[]> items = new Seq<>();

        private static SavedSpecialUnit from(SpecialEvent.UnitSpec unit) {
            SavedSpecialUnit saved = new SavedSpecialUnit();
            saved.typeId = unit.type.id;
            saved.count = unit.count;
            saved.primaryStatusId = unit.primaryStatus == null ? -1 : unit.primaryStatus.id;
            saved.primaryStatusDuration = unit.primaryStatusDuration;
            saved.flag = unit.flag;
            saved.payloadId = unit.payload == null ? -1 : unit.payload.id;
            for (SpecialEvent.StatusSpec status : unit.statuses) {
                if (status != null && status.effect != null) {
                    saved.statuses.add(new SavedSpecialStatus(status.effect.id, status.duration));
                }
            }
            for (ItemStack item : unit.items) {
                if (item != null && item.item != null && item.amount > 0) {
                    saved.items.add(new int[]{item.item.id, item.amount});
                }
            }
            return saved;
        }

        private void write(DataOutput out) throws IOException {
            out.writeInt(typeId);
            out.writeInt(count);
            out.writeInt(primaryStatusId);
            out.writeFloat(primaryStatusDuration);
            out.writeDouble(flag);
            out.writeInt(payloadId);
            out.writeInt(statuses.size);
            for (SavedSpecialStatus status : statuses) status.write(out);
            out.writeInt(items.size);
            for (int[] item : items) {
                out.writeInt(item[0]);
                out.writeInt(item[1]);
            }
        }

        private static SavedSpecialUnit read(DataInput in) throws IOException {
            SavedSpecialUnit saved = new SavedSpecialUnit();
            saved.typeId = in.readInt();
            saved.count = in.readInt();
            saved.primaryStatusId = in.readInt();
            saved.primaryStatusDuration = in.readFloat();
            saved.flag = in.readDouble();
            saved.payloadId = in.readInt();
            int statusCount = in.readInt();
            if (statusCount < 0 || statusCount > 1024) throw new IOException("Invalid New Horizon special status count: " + statusCount);
            for (int i = 0; i < statusCount; i++) saved.statuses.add(SavedSpecialStatus.read(in));
            int itemCount = in.readInt();
            if (itemCount < 0 || itemCount > 1024) throw new IOException("Invalid New Horizon special item count: " + itemCount);
            for (int i = 0; i < itemCount; i++) saved.items.add(new int[]{in.readInt(), in.readInt()});
            return saved;
        }

        private SpecialEvent.UnitSpec create() {
            UnitType type = content.unit(typeId);
            if (type == null || count <= 0) return null;
            SpecialEvent.UnitSpec unit = new SpecialEvent.UnitSpec(type, count);
            unit.primaryStatus = SavedEvent.status(primaryStatusId);
            unit.primaryStatusDuration = primaryStatusDuration;
            unit.flag = flag;
            unit.payload = SavedEvent.block(payloadId);
            for (SavedSpecialStatus saved : statuses) {
                StatusEffect effect = SavedEvent.status(saved.effectId);
                if (effect != null) unit.statuses.add(new SpecialEvent.StatusSpec(effect, saved.duration));
            }
            for (int[] item : items) {
                Item contentItem = SavedEvent.item(item[0]);
                if (contentItem != null && item[1] > 0) unit.items.add(new ItemStack(contentItem, item[1]));
            }
            return unit;
        }
    }

    private static final class SavedSpecialStatus {
        private int effectId;
        private float duration;

        private SavedSpecialStatus(int effectId, float duration) {
            this.effectId = effectId;
            this.duration = duration;
        }

        private void write(DataOutput out) throws IOException {
            out.writeInt(effectId);
            out.writeFloat(duration);
        }

        private static SavedSpecialStatus read(DataInput in) throws IOException {
            return new SavedSpecialStatus(in.readInt(), in.readFloat());
        }
    }
}
