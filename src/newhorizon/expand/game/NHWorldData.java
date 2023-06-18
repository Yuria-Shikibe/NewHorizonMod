package newhorizon.expand.game;

import arc.struct.Seq;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import newhorizon.expand.eventsys.AutoEventTrigger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NHWorldData implements SaveFileReader.CustomChunk{
	public static NHWorldData data;
	
	public void addTaskOnSave(Runnable runnable){
		taskOnSave.add(runnable);
	}
	public final Seq<Runnable> taskOnSave = new Seq<>();
	
	public NHWorldData(){
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
		
		while(taskOnSave.any()){
			taskOnSave.pop().run();
		}
	}
	
	@Override
	public void read(DataInput stream) throws IOException{
		version = stream.readShort();
		
		eventReloadSpeed = stream.readFloat();
	}
	
	public void afterRead(){
		if(!Float.isNaN(eventReloadSpeed))AutoEventTrigger.setScale(eventReloadSpeed);
	}
}
