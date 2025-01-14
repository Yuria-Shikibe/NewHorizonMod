package newhorizon.expand.net;

import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.net.packet.ActiveAbilityTriggerPacket;
import newhorizon.expand.net.packet.LongInfoMessageCallPacket;

public class NHCall{
	public static void infoDialog(String s, NetConnection c){
		if (Vars.net.server()) {
			LongInfoMessageCallPacket packet = new LongInfoMessageCallPacket();
			packet.message = s;
			c.send(packet, true);
		}
	}

	public static void triggerActiveAbility(Unit unit, int abilityId){
		if(Vars.net.server() || !Vars.net.active()) {
			if (unit != null && unit.abilities.length > abilityId) {
				Ability ability = unit.abilities[abilityId];
				if (ability instanceof ActiveAbility){
					((ActiveAbility) ability).trigger(unit);
				}
			}
		}
		if (Vars.net.server() ||Vars.net.client()) {
			ActiveAbilityTriggerPacket packet = new ActiveAbilityTriggerPacket();
			packet.unit = unit;
			packet.abilityId = abilityId;
			Vars.net.send(packet, true);
		}
	}
}
