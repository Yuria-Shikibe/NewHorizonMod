package newhorizon.util.feature.cutscene.packets;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.entities.NHGroups;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;

public class EventUICallPacket extends Packet{
	private byte[] DATA;
	public CutsceneEventEntity entity;
	
	public int getPriority(){
		return priorityLow;
	}
	
	public EventUICallPacket(CutsceneEventEntity entity){
		this.entity = entity;
	}
	
	public EventUICallPacket(){
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
		
		entity = NHGroups.event.getByID(READ.i());
	}
	
	public void handleClient(){
		if(entity == null)return;
		entity.eventType().onCallUI(entity);
	}
	
	public void handleServer(NetConnection con) {
		if(con.player != null && !con.kicked && entity != null) {
			EventUICallPacket packet = new EventUICallPacket();
			packet.entity = entity;
			Vars.net.sendExcept(con, this, true);
		}
	}
}
