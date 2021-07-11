package newhorizon.func;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.EntityMapping;

public class ClassIDIniter{
	public static boolean safe = true;
	private static final int startFrom = 100;
	
	public static final ObjectMap<Class<?>, Set> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();
	
	public static void put(Class<?> c, Set p){
		needIdClasses.put(c, p);
	}
	
	public static int getID(Class<?> c){return classIdMap.get(c);}
	
	public static void load(){
		Seq<Class<?>> key = needIdClasses.keys().toSeq().sortComparing(c -> c.toString().hashCode());
		ObjectMap<Class<?>, Set> copy = needIdClasses.copy();
		
		Log.info(key);
		
		needIdClasses.clear();
		for(Class<?> k : key){
			needIdClasses.put(k, copy.get(k));
		}
		
		
		for(Class<?> c : key){
			classIdMap.put(c, EntityMapping.register(c.toString(), needIdClasses.get(c).prov));
		}
		
		needIdClasses.clear();
	}
	
	public static class Set{
		public final String name;
		public final Prov<?> prov;
		
		public Set(String name, Prov<?> prov){
			this.name = name;
			this.prov = prov;
		}
		
		public Set(Prov<?> prov){
			this.name = prov.get().getClass().toString();
			this.prov = prov;
		}
	}
}
