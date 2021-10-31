package newhorizon.feature.cutscene.packets;

import arc.struct.ObjectMap;
import arc.struct.StringMap;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.io.TypeIO;
import mindustry.net.Packet;

import static mindustry.Vars.state;

public class TagPacket extends Packet{
	private byte[] DATA;
	public StringMap tags;
	
	public TagPacket(){
		this.DATA = NODATA;
	}
	
	public void write(Writes WRITE){
		String[][] strings = new String[tags.size][2];
		
		int i = 0;
		for(ObjectMap.Entry<String, String> entry : tags.entries()){
			strings[i][0] = entry.key;
			strings[i][1] = entry.value;
			i++;
		}
		
		TypeIO.writeStrings(WRITE, strings);
	}
	
	public void read(Reads READ, int LENGTH){
		this.DATA = READ.b(LENGTH);
	}
	
	public void handled(){
		BAIS.setBytes(this.DATA);
		
		String[][] strings = TypeIO.readStrings(READ);
		
		tags = new StringMap();
		for(String[] kv : strings){
			tags.put(kv[0], kv[1]);
		}
	}
	
	public int getPriority(){
		return priorityLow;
	}
	
	public void handleClient(){
		state.rules.tags = tags;
	}
}
