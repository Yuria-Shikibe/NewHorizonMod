package newhorizon.util.func;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;

public class EntityRegister{
	public static boolean safe = true;
	private static final int startFrom = 100;
	
	public static final ObjectMap<Class<?>, ProvSet> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();
	
	static{
		EntityRegister.put(CutsceneEventEntity.class, CutsceneEventEntity::new);
	}
	
	public static <T extends Entityc> void put(Class<T> c, ProvSet p){
		needIdClasses.put(c, p);
	}
	
	public static <T extends Entityc> void put(Class<T> c, Prov<T> prov){
		put(c, new ProvSet(prov));
	}
	
	public static <T extends Entityc> int getID(Class<T> c){return classIdMap.get(c);}
	
	public static void load(){
		Seq<Class<?>> key = needIdClasses.keys().toSeq().sortComparing(c -> c.toString().hashCode());
		
		Log.info(key);
		
		for(Class<?> c : key){
			classIdMap.put(c, EntityMapping.register(c.toString(), needIdClasses.get(c).prov));
		}
		
		needIdClasses.clear();
	}
	
	public static class ProvSet{
		public final String name;
		public final Prov<?> prov;
		
		public ProvSet(String name, Prov<?> prov){
			this.name = name;
			this.prov = prov;
		}
		
		public ProvSet(Prov<?> prov){
			this.name = prov.get().getClass().toString();
			this.prov = prov;
		}
	}
}
