package newhorizon.expand.eventsys.custom;

import arc.Core;
import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.serialization.Json;
import arc.util.serialization.JsonValue;
import mindustry.Vars;
import newhorizon.expand.eventsys.types.InterventionEventType;
import newhorizon.expand.eventsys.types.RaidEventType;
import newhorizon.expand.eventsys.types.ReachWaveEvent;
import newhorizon.expand.eventsys.types.WorldEventType;
import newhorizon.expand.eventsys.annotation.Customizable;
import newhorizon.expand.eventsys.annotation.NumberParam;
import newhorizon.expand.eventsys.annotation.Parserable;

import java.lang.reflect.Field;

public class Customizer{
	public final ObjectMap<Field, CustomParam> inited = new ObjectMap<>();
	public final ObjectSet<EventEntry> entries = new ObjectSet<>();
	public final ObjectMap<Class<? extends WorldEventType>, EventEntry> classToEntry = new ObjectMap<>();
//	public static final
	
	
	public Customizer(){
		register(RaidEventType.class, RaidEventType::new);
		register(InterventionEventType.class, InterventionEventType::new);
		register(ReachWaveEvent.class, ReachWaveEvent::new);
	}
	
	public static Customizer customizer;
	
	public void register(Class<? extends WorldEventType> c, Func<String, ? extends WorldEventType> constructor){
		EventEntry e = new EventEntry(c, constructor);
		entries.add(e);
		classToEntry.put(c, e);
	}
	
	public EventEntry getEntry(Class<? extends WorldEventType> c){
		return classToEntry.get(c);
	}
	
	public String decidedName = "event-0";
	public String prefix = "custom" + (Vars.player == null ? "-" : ("-" + Vars.player.name + "-"));
	public Seq<Exception> lastException = new Seq<>();
	public WorldEventType context = null;
	
	public String name(){
		return prefix + decidedName;
	}
	
	public Customizer setContext(WorldEventType context){
		this.context = context;
		return this;
	}
	
	public WorldEventType getContext(){
		return context;
	}
	public class CustomParam implements Json.JsonSerializable{
		public final Class<? extends WorldEventType> includedClassType;
		public final Field field;
		public final String displayName;
		
		@Nullable public NumberParam numberParam = null;
		
		public boolean isNumber(){
			return numberParam != null || field.isAnnotationPresent(NumberParam.class);
		}
		
		public boolean needParser(){
			return field.isAnnotationPresent(Parserable.class);
		}
		
		public CustomParam(Class<? extends WorldEventType> includedClassType, Field field){
			this.includedClassType = includedClassType;
			this.field = field;
			displayName = Core.bundle.get("event.cus." + includedClassType.getSimpleName() + "-" + field.getName() + ".name");
			
			if(isNumber()){
				numberParam = field.getAnnotation(NumberParam.class);
			}
			
			inited.put(field, this);
		}
		
		public void buildTable(Table parent){
			ParserRegistry.getInterpreter(this).build(this, parent);
		}
		
		public Class<?> getType(){
			return field.getType();
		}
		
		public Object get(WorldEventType o){
			try{
				return field.get(o);
			}catch(IllegalAccessException e){
				lastException.add(e);
			}
			
			return null;
		}
		
		public Number getNumber(WorldEventType o){
			Object n = get(o);
			if(o != null && isNumber())return (Number)n;
			else return null;
		}
		
		public boolean getBool(WorldEventType o){
			Object n = get(o);
			if(o != null && n instanceof Boolean)return (Boolean)n;
			else return false;
		}
		
		public void set(Object o, WorldEventType type){
			try{
				field.set(type, o);
			}catch(IllegalAccessException e){
				lastException.add(e);
				try{
					field.set(WorldEventType.NULL, o);
				}catch(IllegalAccessException ex){
					ex.printStackTrace();
				}
			}
		}
		
		@Override
		public void write(Json json){
			json.writeField(getContext(), field.getName(), field.getName(), getType());
		}
		
		@Override
		public void read(Json json, JsonValue jsonData){
			json.readValue(getType(), jsonData);
		}
	}
	
	public class EventEntry{
		public final Class<? extends WorldEventType> type;
		public final Func<String, ? extends WorldEventType> constructor;
		public final Seq<CustomParam> params;
		
		public WorldEventType get(){
			return constructor.get(name());
		}
		
		public void buildTable(Table parent){
			params.each(p -> {
				p.buildTable(parent);
//				NewHorizon.debugLog("build: " + p.getType());
			});
		}
		
		public EventEntry(Class<? extends WorldEventType> type, Func<String, ? extends WorldEventType> constructor){
			this.type = type;
			this.constructor = constructor;
			params = new Seq<>();
			
			for(Field f : type.getFields()){
				if(f.isAnnotationPresent(Customizable.class)){
					if(inited.containsKey(f)){
						params.add(inited.get(f));
						
//						NewHorizon.debugLog(type.getSimpleName() + " | Registered Field: " + f.getName() + " as " + f.getType());
					}else{
						CustomParam param = new CustomParam(type, f);
						f.setAccessible(true);
						params.add(param);
						
//						NewHorizon.debugLog(type.getSimpleName() + " | Registered !New Field: " + f.getName() + " as " + f.getType());
					}
				}
			}
		}
	}
	
	public static Func<Number, CharSequence> getDisplay(String name){
		return NumberDisplay.valueOf(name).toDisplay;
	}
}
