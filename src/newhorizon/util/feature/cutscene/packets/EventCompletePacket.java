package newhorizon.util.feature.cutscene.packets;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;

public class EventCompletePacket extends Packet{
	private byte[] DATA;
	public CutsceneEventEntity entity;
	
	public EventCompletePacket(){
		this.DATA = NODATA;
	}
	
	public void write(Writes WRITE){
		WRITE.i(entity.id);
	}
	
	public void read(Reads READ, int LENGTH){
		this.DATA = READ.b(LENGTH);
	}
	
	public void handled(){
		BAIS.setBytes(this.DATA);
		
		entity = CutsceneEventEntity.events.getByID(READ.i());
	}
	
	public void handleClient(){
		if(entity == null)return;
		entity.netAct();
	}
	
	public void handleServer(NetConnection con) {
		if(con.player != null && !con.kicked && entity != null) {
			entity.netAct();
		}
	}
}
