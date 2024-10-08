package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import newhorizon.expand.cutscene.NHCSS_Core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NHWorldData implements SaveFileReader.CustomChunk{
	public static short CURRENT_VER = 1;
	
	public static WorldTileData worldTileData;
	
	public NHWorldData(){
		worldTileData = new WorldTileData();

		//todo
		SaveVersion.addCustomChunk("nh-world-data", this);
		SaveVersion.addCustomChunk("nh-world-tile-data", worldTileData);
	}
	
	public short version = 0;
	public float eventReloadSpeed = -1;
	public boolean jumpGateUsesCoreItems = true;
	public boolean applyEventTriggers = false;

	@Override
	public void write(DataOutput stream) throws IOException{
		stream.writeShort(CURRENT_VER);
		
		stream.writeFloat(eventReloadSpeed);
		stream.writeBoolean(jumpGateUsesCoreItems);
		stream.writeBoolean(applyEventTriggers);
		
		if(NHCSS_Core.core.currentScene != null){
			NHCSS_Core.core.currentScene.write(stream);
		}
	}
	
	@Override
	public void read(DataInput stream) throws IOException{
		version = stream.readShort();
		
		eventReloadSpeed = stream.readFloat();
		
		if(version > 0){
			jumpGateUsesCoreItems = stream.readBoolean();
			applyEventTriggers = stream.readBoolean();
		}
		
		version = CURRENT_VER;
		
		if(NHCSS_Core.core.currentScene != null){
			NHCSS_Core.core.currentScene.read(stream);
		}
		
		afterRead();
	}
	
	public void afterRead(){
		if(Vars.headless && (Float.isNaN(eventReloadSpeed) || eventReloadSpeed > 5.55f)){
			eventReloadSpeed = -1;
		}
	}
}
