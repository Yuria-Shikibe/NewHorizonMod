package newhorizon.expand.entities;

import arc.func.Prov;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import newhorizon.NewHorizon;
import newhorizon.expand.eventsys.AutoEventTrigger;
import newhorizon.expand.units.AdaptedTimedKillUnit;
import newhorizon.expand.units.EnergyUnit;
import newhorizon.expand.units.PesterEntity;

public class EntityRegister{
	private static final int startFrom = 100;
	
	public static final ObjectMap<Class<?>, ProvSet> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();
	
	static{
		EntityRegister.put(EnergyUnit.class, EnergyUnit::new);
		EntityRegister.put(PesterEntity.class, PesterEntity::new);
		EntityRegister.put(AdaptedTimedKillUnit.class, AdaptedTimedKillUnit::new);
		EntityRegister.put(Spawner.class, Spawner::new);
		EntityRegister.put(Carrier.class, Carrier::new);
//		EntityRegister.put(ShieldProjector.Projector.class, ShieldProjector.Projector::new);
//		EntityRegister.put(CutsceneEventEntity.class, CutsceneEventEntity::new);
		EntityRegister.put(UltFire.class, UltFire::new);
		EntityRegister.put(AutoEventTrigger.class, AutoEventTrigger::new);
		EntityRegister.put(WorldEvent.class, WorldEvent::new);
//		EntityRegister.put(MatterStorm.AdaptedWeatherState.class, MatterStorm.AdaptedWeatherState::new);
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
		
		if(NewHorizon.DEBUGGING || Vars.headless){
			Log.info("//=============================================\\\\");
			classIdMap.each((c, i) -> Log.info(i + "|" + c.getSimpleName()));
			Log.info("\\\\=============================================//");
		}
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
