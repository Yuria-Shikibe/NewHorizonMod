package newhorizon.expand.net;

import arc.math.geom.Vec2;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.type.UnitType;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.entities.Spawner;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;

import static mindustry.Vars.net;

public class NHCall {
    public static void infoDialog(String s, NetConnection c) {
        if (Vars.net.server()) {
            LongInfoMessageCallPacket packet = new LongInfoMessageCallPacket();
            packet.message = s;
            c.send(packet, true);
        }
    }

    public static void unitJumpIn(UnitType unitType, Team team, Vec2 spawn, Vec2 command, float rotation, float time) {
        if (!net.client()) {
            Spawner spawner = new Spawner();
            spawner.init(unitType, team, spawn, rotation, time);
            if (command != null) spawner.commandPos.set(command.cpy());
            spawner.add();
        }
    }

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
}
