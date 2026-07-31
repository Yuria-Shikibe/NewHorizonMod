package newhorizon.expand.game;

import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import newhorizon.NHUI;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.expand.net.NHCall;
import newhorizon.expand.net.packet.RaidClearPacket;
import newhorizon.expand.net.packet.RaidScalePacket;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public final class RaidSync {
    private static final Seq<EventRaidAction> logicActions = new Seq<>();

    private RaidSync() {
    }

    public static void registerLogicAction(EventRaidAction action) {
        if (action == null) return;
        logicActions.addUnique(action);
    }

    public static void unregisterLogicAction(EventRaidAction action) {
        if (action == null) return;
        logicActions.remove(action);
    }

    public static void writeAction(Writes write, EventRaidAction action) {
        write.i(action.syncSeed);
        write.b((byte) action.team.id);
        write.f(action.alertTime);
        write.f(action.raidTime);
        write.f(action.raidScale);
        write.f(action.inaccuracy);
        write.f(action.sourceX);
        write.f(action.sourceY);
        write.f(action.targetX);
        write.f(action.targetY);
        write.b((byte) action.raidType.ordinal());
        write.i(action.customBulletType);
        write.i(bulletId(action.customBullet != null ? action.customBullet : action.bulletType()));
        write.i(bulletId(action.keyBullet));
        write.bool(action.gatedByRaidState);
        write.f(action.lifeTimer);
        write.i(action.raidCounter());
    }

    public static EventRaidAction readAction(Reads read) {
        EventRaidAction action = new EventRaidAction();
        action.syncSeed = read.i();
        action.team = Team.get(read.b());
        action.alertTime = read.f();
        action.raidTime = read.f();
        action.raidScale = read.f();
        action.inaccuracy = read.f();
        action.sourceX = read.f();
        action.sourceY = read.f();
        action.targetX = read.f();
        action.targetY = read.f();
        action.overrideRaidStats = true;
        action.overrideDefaultCoordinate = true;
        int preset = read.b();
        action.raidType = preset >= 0 && preset < RaidPreset.all.length ? RaidPreset.all[preset] : RaidPreset.CUSTOM_RAID;
        action.customBulletType = read.i();
        int id = read.i();
        if (id >= 0) {
            BulletType bt = Vars.content.bullet(id);
            if (bt != null) action.customBullet = bt;
        }
        int keyId = read.i();
        if (keyId >= 0) {
            BulletType key = Vars.content.bullet(keyId);
            if (key != null) action.keyBullet = key;
        }
        action.gatedByRaidState = read.bool();
        action.presentationOnly = true;
        action.duration = action.alertTime + action.raidTime;
        action.applyNetworkState(read.f(), read.i());
        return action;
    }

    public static void applyClientAction(EventRaidAction action) {
        if (!RaidLogic.isRemoteClient()) return;
        if (action == null || action.complete()) return;
        action.presentationOnly = true;
        removeRaidBySeed(action.syncSeed);
        ActionBus bus = new ActionBus();
        bus.add(action);
        cutscene.addSubActionBus(bus);
    }

    public static void pushStateTo(Player player) {
        if (!Vars.net.server() || !Vars.net.active() || player == null) return;
        var con = player.con();
        if (con == null) return;

        RaidScalePacket scalePacket = new RaidScalePacket();
        scalePacket.scale = RaidState.scale();
        con.send(scalePacket, true);

        Seq<EventRaidAction> actives = findActiveRaidActions();
        con.send(new RaidClearPacket(), true);
        for (EventRaidAction action : actives) {
            NHCall.syncRaidAlertTo(action, player);
        }
    }

    public static void broadcastState() {
        if (!Vars.net.server() || !Vars.net.active()) return;
        for (Player player : Groups.player) {
            if (player.isLocal()) continue;
            pushStateTo(player);
        }
    }

    public static Seq<EventRaidAction> findActiveRaidActions() {
        ObjectSet<EventRaidAction> seen = new ObjectSet<>();
        Seq<EventRaidAction> out = new Seq<>();

        for (int i = logicActions.size - 1; i >= 0; i--) {
            EventRaidAction action = logicActions.get(i);
            if (action == null || action.complete()) {
                logicActions.remove(i);
                continue;
            }
            if (seen.add(action)) out.add(action);
        }

        EventRaidAction def = DefaultRaid.activeRaidAction();
        if (def != null && !def.complete() && !def.presentationOnly && seen.add(def)) out.add(def);
        for (ActionBus bus : cutscene.subBuses) {
            EventRaidAction action = raidFromBus(bus);
            if (action != null && !action.complete() && !action.presentationOnly && seen.add(action)) out.add(action);
        }
        return out;
    }

    public static void clearClientRaid() {
        if (!RaidLogic.isRemoteClient()) return;
        cutsceneUI.clearMarkers(HudMarker.Kind.RAID);
        for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = cutscene.subBuses.get(i);
            EventRaidAction action = raidFromBus(bus);
            if (action == null) continue;
            if (!action.presentationOnly) continue;
            bus.skip();
            cutscene.subBuses.remove(i);
        }
    }

    private static void removeRaidBySeed(int syncSeed) {
        if (!RaidLogic.isRemoteClient()) return;
        for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = cutscene.subBuses.get(i);
            EventRaidAction action = raidFromBus(bus);
            if (action == null || action.syncSeed != syncSeed) continue;
            if (!action.presentationOnly) continue;
            removeRaidMarkers(action);
            bus.skip();
            cutscene.subBuses.remove(i);
        }
    }

    private static void removeRaidMarkers(EventRaidAction action) {
        if (cutsceneUI == null) return;
        for (int i = cutsceneUI.markers.size - 1; i >= 0; i--) {
            HudMarker marker = cutsceneUI.markers.get(i);
            if (marker.kind != HudMarker.Kind.RAID) continue;
            if (Math.abs(marker.markPoint.x - action.targetX) > 8f || Math.abs(marker.markPoint.y - action.targetY) > 8f) continue;
            if (marker instanceof RaidMarker raidMarker) raidMarker.clearMinimapMarker();
            marker.clearActions();
            cutsceneUI.root.removeChild(marker);
            cutsceneUI.markers.remove(i);
        }
        if (NHUI.eventList != null) NHUI.rebuildEventList();
    }

    private static EventRaidAction raidFromBus(ActionBus bus) {
        if (bus == null) return null;
        if (bus.current instanceof EventRaidAction action) return action;
        for (Action queued : bus.queue) {
            if (queued instanceof EventRaidAction action) return action;
        }
        return null;
    }

    private static int bulletId(BulletType type) {
        return type == null ? -1 : type.id;
    }
}

