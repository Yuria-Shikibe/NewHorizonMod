package newhorizon.expand.game;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.Team;
import mindustry.gen.Player;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventInterventionAction;
import newhorizon.expand.net.NHCall;
import newhorizon.expand.net.packet.InterventionClearPacket;
import newhorizon.expand.net.packet.InterventionScalePacket;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public final class InterventionSync {
    private InterventionSync() {
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
        action.duration = action.alertTime + 10f * Time.toSeconds + 30f;
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
        if (action == null) return;
        if (action.spawned() || action.lifeTimer >= action.alertTime) return;

        clearClientIntervention();
        ActionBus bus = new ActionBus();
        bus.add(action);
        cutscene.addSubActionBus(bus);
    }

    public static void pushStateTo(Player player) {
        if (!Vars.net.server() || !Vars.net.active() || player == null) return;
        var con = player.con();
        if (con == null) return;

        InterventionScalePacket scalePacket = new InterventionScalePacket();
        scalePacket.scale = InterventionState.scale();
        con.send(scalePacket, true);

        EventInterventionAction active = findActiveInterventionAction();
        if (active != null && !active.spawned() && active.lifeTimer < active.alertTime) {
            NHCall.syncInterventionAlertTo(active, player);
        } else {
            con.send(new InterventionClearPacket(), true);
        }
    }

    public static EventInterventionAction findActiveInterventionAction() {
        EventInterventionAction fromDefault = DefaultIntervention.activeInterventionAction();
        if (fromDefault != null) return fromDefault;

        for (ActionBus bus : cutscene.subBuses) {
            EventInterventionAction action = interventionFromBus(bus);
            if (action != null) return action;
        }
        return null;
    }

    public static void clearClientIntervention() {
        if (Vars.headless) return;
        cutsceneUI.clearMarkers();
        for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = cutscene.subBuses.get(i);
            if (isInterventionBus(bus)) {
                bus.clear();
                cutscene.subBuses.remove(i);
            }
        }
    }

    private static EventInterventionAction interventionFromBus(ActionBus bus) {
        if (bus == null) return null;
        if (bus.current instanceof EventInterventionAction action) return action;
        for (Action queued : bus.queue) {
            if (queued instanceof EventInterventionAction action) return action;
        }
        return null;
    }

    private static boolean isInterventionBus(ActionBus bus) {
        return interventionFromBus(bus) != null;
    }

    private static int statusId(StatusEffect status) {
        return status == null ? -1 : status.id;
    }
}
