package newhorizon.util.feature.cutscene.packets;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class UnlockPacket extends Packet{
	private byte[] DATA;
	public UnlockableContent content;
	
	public UnlockPacket(){
		this.DATA = NODATA;
	}
	
	public void write(Writes WRITE){
		WRITE.str(content.getContentType().toString());
		WRITE.str(content.name);
	}
	
	public void read(Reads READ, int LENGTH){
		this.DATA = READ.b(LENGTH);
	}
	
	public void handled(){
		BAIS.setBytes(this.DATA);
		
		content = Vars.content.getByName(Enum.valueOf(ContentType.class, READ.str()), READ.str());
	}
	
	
	public void handleClient(){
		content.unlock();
	}
	
	public void handleServer(NetConnection con) {
		if (con.player != null && !con.kicked) {
			Player player = con.player;
			if(player == Vars.player)content.unlock();
		}
	}
}
