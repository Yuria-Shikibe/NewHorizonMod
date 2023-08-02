package newhorizon.expand.cutscene;

import arc.func.Boolp;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.gen.Building;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MapCutscene{
	public ObjectMap<String, Seq<Boolp>> checkers = new ObjectMap<>();
	public ObjectMap<String, Seq<Building>> targetGroups = new ObjectMap<>();
	
	public Interval timer = new Interval(3);
	
	public float reloadParam0 = 0;
	public float reloadParam1 = 0;
	public float reloadParam2 = 0;
	public float reloadParam3 = 0;
	public float reloadParam4 = 0;
	
	public MapCutscene(String mapName){
		this.mapName = mapName;
	}
	
	public String mapName;
	public NHCSS_Core core;
	public Seq<Runnable> updaters = new Seq<>();
	public Seq<Runnable> initers = new Seq<>();
	public Seq<Runnable> enders = new Seq<>();
	
	public Seq<Cons<MapCutscene>> drawers = new Seq<>();
	
	public Runnable register;
	
	public void putTag(String key, String value){
		Vars.state.rules.tags.put(key, value);
	}
	
	public void putTag(String key){
		Vars.state.rules.tags.put(key, "true");
	}
	
	public String getValue(String key){
		return Vars.state.rules.tags.get(key);
	}
	
	public boolean getValueBool(String key){
		return Boolean.parseBoolean(Vars.state.rules.tags.get(key));
	}
	
	public void load(){
		core.loadedUpdaters = updaters.copy();
		core.loadedIniters = initers.copy();
		core.loadedEnders = enders.copy();
	}
	
	public void register(){
		if(register != null){
			register.run();
		}
	}
	
	public boolean checked(String key){
		if(checkers.containsKey(key)){
			Seq<Boolp> boolps = checkers.get(key);
			if(boolps.isEmpty())return false;
			for(Boolp b : boolps){
				if(!b.get())return false;
			}
			return true;
		}else return false;
	}
	
	public void draw(){
		for(Cons<MapCutscene> d : drawers){
			d.get(this);
		}
	}
	
	public void write(DataOutput stream) throws IOException{
		stream.writeFloat(reloadParam0);
		stream.writeFloat(reloadParam1);
		stream.writeFloat(reloadParam2);
		stream.writeFloat(reloadParam3);
		stream.writeFloat(reloadParam4);
	}
	
	public void read(DataInput stream) throws IOException{
		reloadParam0 = stream.readFloat();
		reloadParam1 = stream.readFloat();
		reloadParam2 = stream.readFloat();
		reloadParam3 = stream.readFloat();
		reloadParam4 = stream.readFloat();
	}
}
