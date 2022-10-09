package newhorizon.expand.packets;

import mindustry.Vars;
import mindustry.net.NetConnection;

public class NHCall{
	public static void infoDialog(String s, NetConnection c){
		if (Vars.net.server()) {
			LongInfoMessageCallPacket packet = new LongInfoMessageCallPacket();
			packet.message = s;
			c.send(packet, true);
		}
	}
}
