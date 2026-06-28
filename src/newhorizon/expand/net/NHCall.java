package newhorizon.expand.net;

import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.RaidState;
import newhorizon.expand.logic.components.action.EventRaidAction;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import newhorizon.expand.net.packet.RaidAlertPacket;
import newhorizon.expand.net.packet.RaidBulletPacket;
import newhorizon.expand.net.packet.RaidClearPacket;
import newhorizon.expand.net.packet.RaidScalePacket;
import newhorizon.expand.net.packet.RaidScaleRequestPacket;
import newhorizon.expand.net.packet.RaidSyncRequestPacket;

public class NHCall {
    public static void triggerActiveAbility(Unit unit, int abilityId) {
        if (Vars.net.server() || !Vars.net.active()) {
            if (unit != null && unit.abilities.length > abilityId) {
                Ability ability = unit.abilities[abilityId];
                if (ability instanceof ActiveAbility) {
                    ((ActiveAbility) ability).trigger(unit);
                }
            }
        }
        if (Vars.net.server() || Vars.net.client()) {
            ActiveAbilityTriggerPacket packet = new ActiveAbilityTriggerPacket();
            packet.unit = unit;
            packet.abilityId = abilityId;
            Vars.net.send(packet, true);
        }
    }

    public static void setRaidScale(float scale, Player player) {
        if (player != null && !player.admin) {
            player.sendMessage("[scarlet]Admin only.");
            return;
        }

        if (Vars.net.client() && !Vars.net.server()) {
            RaidScaleRequestPacket packet = new RaidScaleRequestPacket();
            packet.scale = scale;
            Vars.net.send(packet, true);
            return;
        }

        applyRaidScale(scale, player);
    }

    public static void applyRaidScale(float scale, Player player) {
        RaidState.setScale(scale);
        Log.info("Raid scale set to @", scale);
        if (player != null) {
            player.sendMessage("[accent]Raid scale: []" + scale + (scale > 0.001f ? " [green](on)" : " [lightgray](off)"));
        }
        if (Vars.net.server() && Vars.net.active()) {
            RaidScalePacket packet = new RaidScalePacket();
            packet.scale = scale;
            Vars.net.send(packet, true);
        }
    }

    public static void syncRaidAlert(EventRaidAction action) {
        if (!Vars.net.server() || !Vars.net.active() || action == null) return;
        RaidAlertPacket packet = new RaidAlertPacket(action);
        Vars.net.send(packet, true);
    }

    public static void syncRaidAlertTo(EventRaidAction action, Player player) {
        if (!Vars.net.server() || !Vars.net.active() || action == null || player == null) return;
        var con = player.con();
        if (con == null) return;
        con.send(new RaidAlertPacket(action), true);
    }

    public static void requestRaidSync() {
        if (!RaidLogic.isRemoteClient() || !Vars.net.active()) return;
        Vars.net.send(new RaidSyncRequestPacket(), true);
    }

    public static void syncRaidBullet(BulletType type, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, float aimX, float aimY) {
        if (!Vars.net.server() || !Vars.net.active() || type == null || team == null) return;
        RaidBulletPacket packet = new RaidBulletPacket();
        packet.bulletId = type.id;
        packet.teamId = team.id;
        packet.x = x;
        packet.y = y;
        packet.angle = angle;
        packet.damage = damage;
        packet.velocityScl = velocityScl;
        packet.lifetimeScl = lifetimeScl;
        packet.aimX = aimX;
        packet.aimY = aimY;
        Vars.net.send(packet, true);
    }
}
