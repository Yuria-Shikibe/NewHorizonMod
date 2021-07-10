package newhorizon.func;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.EntityMapping;

import java.util.Comparator;

public class ClassIDIniter{
	public static boolean safe = true;
	private static final int startFrom = 100;
	
	public static final ObjectMap<Class<?>, Prov<?>> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();
	
	public static int getID(Class<?> c){return classIdMap.get(c);}
	
	public static void load(){
		Seq<Class<?>> key = needIdClasses.keys().toSeq().sort(Comparator.comparingInt(c -> c.toString().hashCode()));
		ObjectMap<Class<?>, Prov<?>> copy = needIdClasses.copy();
		
		Log.info(key);
		
		needIdClasses.clear();
		for(Class<?> k : key){
			needIdClasses.put(k, copy.get(k));
		}
		
		Prov<?>[] map = EntityMapping.idMap;
		
		for(Class<?> c : key){
			classIdMap.put(c, EntityMapping.register(c.toString(), needIdClasses.get(c)));
		}
		
		for(Class<?> c : needIdClasses.keys()){
			Log.info(c + " | " + needIdClasses.get(c).equals(map[classIdMap.get(c)]) + " | " + classIdMap.get(c));
		}
		
		needIdClasses.clear();
	}
}
