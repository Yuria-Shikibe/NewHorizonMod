package newhorizon.expand.game;

import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import newhorizon.NHUI;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.net.NHCall;
import newhorizon.expand.net.packet.InterventionClearPacket;
import newhorizon.expand.net.packet.InterventionScalePacket;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public final class InterventionSync {
    private static int seedSeq;

    private InterventionSync() {
    }

    public static int nextSyncSeed() {
        seedSeq++;
        return (int) (Time.millis() ^ (((long) seedSeq) << 16) ^ (long) (Time.time * 1000));
    }

    public static void writeAction(Writes write, EventInterventionAction action) {
        write.i(action.syncSeed);
        write.b((byte) action.team.id);
        write.i(action.eventId);
        write.f(action.alertTime);
        write.f(action.spawnRange);
        write.f(action.spawnReloadTime);
        write.f(action.spawnDelay);
        write.f(action.targetX);
        write.f(action.targetY);
        write.i(statusId(action.status));
        write.f(action.statusDuration);
        write.d(action.flag);
        write.bool(action.spawned());
        write.f(action.lifeTimer);
        write.i(action.units.size);
        for (EventInterventionAction.UnitEntry entry : action.units) {
            write.i(entry.type == null ? -1 : entry.type.id);
            write.i(entry.count);
        }
    }

    public static EventInterventionAction readAction(Reads read) {
        EventInterventionAction action = new EventInterventionAction();
        action.syncSeed = read.i();
        action.team = Team.get(read.b());
        action.eventId = read.i();
        action.alertTime = read.f();
        action.spawnRange = read.f();
        action.spawnReloadTime = read.f();
        action.spawnDelay = read.f();
        action.targetX = read.f();
        action.targetY = read.f();
        action.overrideStats = true;
        action.overrideDefaultCoordinate = true;
        int status = read.i();
        action.status = StatusEffects.none;
        if (status >= 0) {
            for (StatusEffect effect : Vars.content.statusEffects()) {
                if (effect != null && effect.id == status) {
                    action.status = effect;
                    break;
                }
            }
        }
        action.statusDuration = read.f();
        action.flag = read.d();
        boolean spawned = read.bool();
        action.presentationOnly = true;
        action.duration = Math.max(action.alertTime + 30f, action.alertTime + 10f * Time.toSeconds + 30f);
        action.applyNetworkState(read.f(), spawned);
        int size = read.i();
        action.units.clear();
        for (int i = 0; i < size; i++) {
            int typeId = read.i();
            int count = read.i();
            UnitType type = typeId >= 0 ? Vars.content.unit(typeId) : null;
            if (type != null && count > 0) action.units.add(new EventInterventionAction.UnitEntry(type, count));
        }
        return action;
    }

    public static void applyClientAction(EventInterventionAction action) {
        if (!RaidLogic.isRemoteClient()) return;
        if (action == null || action.complete()) return;
        if (action.spawned() || action.lifeTimer >= action.alertTime) return;
        if (findLogicBySeed(action.syncSeed) != null) return;
        if (hasMarker(action.syncSeed) || hasMarkerAt(action.targetX, action.targetY)) return;

        action.presentationOnly = true;
        removeInterventionBySeed(action.syncSeed);

        ActionBus bus = new ActionBus();
        bus.add(action);
        cutscene.addSubActionBus(bus);
    }

    public static void pushStateTo(Player player) {
        if (!Vars.net.server() || !Vars.net.active() || player == null) return;
        if (player.isLocal()) return;
        var con = player.con();
        if (con == null) return;

        InterventionScalePacket scalePacket = new InterventionScalePacket();
        scalePacket.scale = InterventionState.scale();
        con.send(scalePacket, true);

        Seq<EventInterventionAction> actives = findActiveAlertActions();
        con.send(new InterventionClearPacket(0), true);
        for (EventInterventionAction action : actives) {
            NHCall.syncInterventionAlertTo(action, player);
        }
    }

    public static void broadcastClear() {
        broadcastClear(0);
    }

    public static void broadcastClear(int syncSeed) {
        if (!Vars.net.server() || !Vars.net.active()) return;
        Vars.net.send(new InterventionClearPacket(syncSeed), true);
    }

    public static void broadcastState() {
        if (!Vars.net.server() || !Vars.net.active()) return;
        for (Player player : Groups.player) {
            if (player.isLocal()) continue;
            pushStateTo(player);
        }
    }

    public static void finishAlert(EventInterventionAction action) {
        if (action == null || action.presentationOnly) return;
        removeInterventionMarkers(action);
        if (RaidLogic.isLogicSide() && Vars.net.server() && Vars.net.active()) {
            broadcastClear(action.syncSeed);
        }
    }

    public static Seq<EventInterventionAction> findActiveAlertActions() {
        ObjectSet<EventInterventionAction> seen = new ObjectSet<>();
        Seq<EventInterventionAction> out = new Seq<>();

        EventInterventionAction fromDefault = DefaultIntervention.activeInterventionAction();
        if (isAlerting(fromDefault) && seen.add(fromDefault)) out.add(fromDefault);

        for (ActionBus bus : cutscene.subBuses) {
            EventInterventionAction action = interventionFromBus(bus);
            if (isAlerting(action) && seen.add(action)) out.add(action);
        }
        return out;
    }

    public static EventInterventionAction findActiveAlertAction() {
        Seq<EventInterventionAction> actives = findActiveAlertActions();
        return actives.isEmpty() ? null : actives.first();
    }

    public static EventInterventionAction findActiveInterventionAction() {
        return findActiveAlertAction();
    }

    public static void clearClientIntervention() {
        clearClientIntervention(0);
    }

    public static void clearClientIntervention(int syncSeed) {
        if (!RaidLogic.isRemoteClient()) return;
        if (syncSeed == 0) {
            cutsceneUI.clearMarkers(HudMarker.Kind.INTERVENTION);
            for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
                ActionBus bus = cutscene.subBuses.get(i);
                EventInterventionAction action = interventionFromBus(bus);
                if (action == null || !action.presentationOnly) continue;
                bus.skip();
                cutscene.subBuses.remove(i);
            }
            return;
        }
        removeInterventionBySeed(syncSeed);
    }

    private static void removeInterventionBySeed(int syncSeed) {
        if (!RaidLogic.isRemoteClient()) return;
        for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = cutscene.subBuses.get(i);
            EventInterventionAction action = interventionFromBus(bus);
            if (action == null || action.syncSeed != syncSeed) continue;
            if (!action.presentationOnly) continue;
            removeInterventionMarkers(action);
            bus.skip();
            cutscene.subBuses.remove(i);
        }
        removeMarkersBySeed(syncSeed);
    }

    public static void removeInterventionMarkers(EventInterventionAction action) {
        if (Vars.headless || cutsceneUI == null || action == null) return;
        for (int i = cutsceneUI.markers.size - 1; i >= 0; i--) {
            HudMarker marker = cutsceneUI.markers.get(i);
            if (marker.kind != HudMarker.Kind.INTERVENTION) continue;
            boolean sameSeed = marker.syncSeed != 0 && marker.syncSeed == action.syncSeed;
            boolean samePos = Math.abs(marker.markPoint.x - action.targetX) <= 8f
                    && Math.abs(marker.markPoint.y - action.targetY) <= 8f;
            if (!sameSeed && !samePos) continue;
            marker.removeMarkerNow();
        }
        if (NHUI.eventList != null) NHUI.rebuildEventList();
    }

    private static void removeMarkersBySeed(int syncSeed) {
        if (Vars.headless || cutsceneUI == null || syncSeed == 0) return;
        for (int i = cutsceneUI.markers.size - 1; i >= 0; i--) {
            HudMarker marker = cutsceneUI.markers.get(i);
            if (marker.kind != HudMarker.Kind.INTERVENTION) continue;
            if (marker.syncSeed != syncSeed) continue;
            marker.removeMarkerNow();
        }
        if (NHUI.eventList != null) NHUI.rebuildEventList();
    }

    public static boolean hasMarker(int syncSeed) {
        if (Vars.headless || cutsceneUI == null || syncSeed == 0) return false;
        for (HudMarker marker : cutsceneUI.markers) {
            if (marker.kind == HudMarker.Kind.INTERVENTION && marker.syncSeed == syncSeed) return true;
        }
        return false;
    }

    public static boolean hasMarkerAt(float x, float y) {
        if (Vars.headless || cutsceneUI == null) return false;
        for (HudMarker marker : cutsceneUI.markers) {
            if (marker.kind != HudMarker.Kind.INTERVENTION) continue;
            if (Math.abs(marker.markPoint.x - x) <= 8f && Math.abs(marker.markPoint.y - y) <= 8f) return true;
        }
        return false;
    }

    public static EventInterventionAction findLogicBySeed(int syncSeed) {
        EventInterventionAction def = DefaultIntervention.activeInterventionAction();
        if (def != null && !def.presentationOnly && def.syncSeed == syncSeed) return def;
        for (ActionBus bus : cutscene.subBuses) {
            EventInterventionAction action = interventionFromBus(bus);
            if (action != null && !action.presentationOnly && action.syncSeed == syncSeed) return action;
        }
        return null;
    }

    private static boolean isAlerting(EventInterventionAction action) {
        return action != null && !action.presentationOnly && !action.complete()
                && !action.spawned() && action.lifeTimer < action.alertTime;
    }

    private static EventInterventionAction interventionFromBus(ActionBus bus) {
        if (bus == null) return null;
        if (bus.current instanceof EventInterventionAction action) return action;
        for (Action queued : bus.queue) {
            if (queued instanceof EventInterventionAction action) return action;
        }
        return null;
    }

    private static int statusId(StatusEffect status) {
        return status == null ? -1 : status.id;
    }
}
