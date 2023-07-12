package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import newhorizon.expand.eventsys.AutoEventTrigger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NHWorldData implements SaveFileReader.CustomChunk{
	public static NHWorldData data;
	
	public NHWorldData(){
//		Vars.mods.getMod("new-horizon").loader.loadClass("newhorizon.expand.NHVars").newInstance().worldData.eventReloadSpeed;
		
		data = this;
		
		SaveVersion.addCustomChunk("nh-world-data", this);
	}
	
	public short version = 0;
	public float eventReloadSpeed = -1;
	
	public void initFromMapRules(){
	
	}
	
	@Override
	public void write(DataOutput stream) throws IOException{
		stream.writeShort(version);
		
		stream.writeFloat(eventReloadSpeed);
	}
	
	@Override
	public void read(DataInput stream) throws IOException{
		version = stream.readShort();
		
		eventReloadSpeed = stream.readFloat();
		
		afterRead();
	}
	
	public void afterRead(){
		if(Vars.headless && (Float.isNaN(eventReloadSpeed) || eventReloadSpeed > 5.55f)){
			eventReloadSpeed = -1;
			return;
		}
		if(!Float.isNaN(eventReloadSpeed) && eventReloadSpeed > 0)AutoEventTrigger.setScale(eventReloadSpeed);
	}
}
