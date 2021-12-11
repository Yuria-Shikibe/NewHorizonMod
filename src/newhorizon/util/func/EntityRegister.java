package newhorizon.util.func;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.block.defence.ShieldProjector;
import newhorizon.expand.block.special.JumpGate;
import newhorizon.expand.units.EnergyUnit;
import newhorizon.util.feature.cutscene.CutsceneEventEntity;

public class EntityRegister{
	private static final boolean debugging = true;
	private static final int startFrom = 100;
	
	public static final ObjectMap<Class<?>, ProvSet> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();
	
	static{
		EntityRegister.put(JumpGate.Spawner.class, JumpGate.Spawner::new);
		EntityRegister.put(HyperSpaceWarper.Carrier.class, HyperSpaceWarper.Carrier::new);
		EntityRegister.put(ShieldProjector.Projector.class, ShieldProjector.Projector::new);
		EntityRegister.put(CutsceneEventEntity.class, CutsceneEventEntity::new);
		EntityRegister.put(EnergyUnit.class, EnergyUnit::new);
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
		
		for(Class<?> c : key){
			classIdMap.put(c, EntityMapping.register(c.toString(), needIdClasses.get(c).prov));
		}
		
		if(debugging || Vars.headless){
			Log.info("//=============================================\\\\");
			classIdMap.each((c, i) -> {
				Log.info(i + "|" + c.getSimpleName());
			});
			Log.info("\\\\=============================================//");
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
