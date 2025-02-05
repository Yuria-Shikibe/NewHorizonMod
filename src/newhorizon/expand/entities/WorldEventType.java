package newhorizon.expand.entities;

import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.struct.ObjectMap;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import newhorizon.content.NHContent;
import newhorizon.util.annotation.ClientDisabled;

import static mindustry.Vars.tilesize;

public class WorldEventType{
	public static final ObjectMap<String, WorldEventType> allTypes = new ObjectMap<>();
	public static final ObjectMap<String, WorldEventType> costumeTypes = new ObjectMap<>();

	public static WorldEventType getStdType(String name){
		return allTypes.get(name);
	}

	public static void addType(WorldEventType type){
		allTypes.put(type.name, type);
		costumeTypes.put(type.name, type);
	}

	public static final WorldEventType NULL = new WorldEventType("null"){};

	static{
		allTypes.remove(NULL.name);
	}

	public final String name;
	public boolean removeAfterTrigger;
	
	public boolean drawable = false, minimapMarkable = false;
	public boolean warnOnHUD = true;
	
	public int initPos = -1;
	
	public Prov<? extends WorldEvent> eventProv = WorldEvent::new;
	
	public WorldEventType(String name){
		if(allTypes.containsKey(name))throw new IllegalArgumentException("Existed Name For A World Event");
		this.name = name;
		addType(this);
	}
	
	@ClientDisabled
	@SuppressWarnings("unchecked")
	public <T extends WorldEvent> T create(){
		T event = (T)eventProv.get();
		event.type = this;
		
		event.init();
		
		if(initPos != -1){
			Tmp.p1.set(Point2.unpack(initPos));
			event.set(Tmp.p1.x * tilesize, Tmp.p1.y * tilesize);
		}
		
		event.add();
		
		return event;
	}

	public Position source(){
		return null;
	}
	
	public Position target(){
		return null;
	}
	
	public TextureRegion icon(){
		return NHContent.objective;
	}

	public void init(){}

	public void trigger(WorldEvent event){
		if(removeAfterTrigger)event.remove();
	}

	public void draw(WorldEvent e){}

	public boolean shouldUpdate(WorldEvent event){
		return true;
	}

	public void updateEvent(WorldEvent event){}
	
	public void onRemove(WorldEvent event){}
	
	public void read(WorldEvent event, Reads read){}
	
	public void write(WorldEvent event, Writes read){}
	
	public void readOnSync(WorldEvent event, Reads read){}
	
	public void writeOnSync(WorldEvent event, Writes read){}
}
