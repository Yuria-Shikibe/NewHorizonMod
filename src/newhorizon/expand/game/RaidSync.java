package newhorizon.expand.game;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Player;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.logic.cutscene.types.RaidPreset;
import newhorizon.expand.net.NHCall;
import newhorizon.expand.net.packet.RaidClearPacket;
import newhorizon.expand.net.packet.RaidScalePacket;
import newhorizon.expand.net.packet.RaidSyncRequestPacket;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public final class RaidSync {
    private RaidSync() {
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
        action.duration = action.alertTime + action.raidTime;
        action.applyNetworkState(read.f(), read.i());
        return action;
    }

    public static void applyClientAction(EventRaidAction action) {
        if (!Vars.headless) cutsceneUI.clearMarkers();
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

        EventRaidAction active = DefaultRaid.activeRaidAction();
        if (active != null) {
            NHCall.syncRaidAlertTo(active, player);
        } else {
            con.send(new RaidClearPacket(), true);
        }
    }

    public static void clearClientRaid() {
        if (Vars.headless) return;
        cutsceneUI.clearMarkers();
        for (int i = cutscene.subBuses.size - 1; i >= 0; i--) {
            ActionBus bus = cutscene.subBuses.get(i);
            if (isRaidBus(bus)) {
                bus.clear();
                cutscene.subBuses.remove(i);
            }
        }
    }

    private static boolean isRaidBus(ActionBus bus) {
        if (bus == null) return false;
        if (bus.current instanceof EventRaidAction) return true;
        for (Action action : bus.queue) {
            if (action instanceof EventRaidAction) return true;
        }
        return false;
    }

    private static int bulletId(BulletType type) {
        return type == null ? -1 : type.id;
    }
}
