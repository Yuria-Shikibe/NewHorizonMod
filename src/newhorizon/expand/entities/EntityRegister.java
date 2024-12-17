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
import newhorizon.expand.units.EnergyUnitII;
import newhorizon.expand.units.unitEntity.NucleoidEntity;
import newhorizon.expand.units.unitEntity.PesterEntity;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.expand.weather.MatterStorm;

public class EntityRegister {

	private static final ObjectMap<Class<?>, ProvSet> needIdClasses = new ObjectMap<>();
	private static final ObjectMap<Class<?>, Integer> classIdMap = new ObjectMap<>();

	static {
		registerEntities();
	}

	/**
	 * Registers all entities with their corresponding provider.
	 */
	private static void registerEntities() {
		put(EnergyUnit.class, EnergyUnit::new);
		put(EnergyUnitII.class, EnergyUnitII::new);

		put(PesterEntity.class, PesterEntity::new);
		put(AdaptedTimedKillUnit.class, AdaptedTimedKillUnit::new);
		put(NucleoidEntity.class, NucleoidEntity::new);
		put(ProbeEntity.class, ProbeEntity::new);

		put(AutoEventTrigger.class, AutoEventTrigger::new);
		put(WorldEvent.class, WorldEvent::new);
		put(MatterStorm.AdaptedWeatherState.class, MatterStorm.AdaptedWeatherState::new);

		put(Spawner.class, Spawner::new);
		put(Carrier.class, Carrier::new);
		put(UltFire.class, UltFire::new);
		put(WarpRift.class, WarpRift::new);
	}

	/**
	 * Adds a class and its corresponding provider to the registration map.
	 */
	public static <T extends Entityc> void put(Class<T> c, Prov<T> prov) {
		needIdClasses.put(c, new ProvSet(prov));
	}

	/**
	 * Retrieves the ID of a registered class.
	 * Returns -1 if the class is not registered.
	 */
	public static <T extends Entityc> int getID(Class<T> c) {
		return classIdMap.get(c, -1);
	}

	/**
	 * Loads all registered entities and assigns them unique IDs.
	 */
	public static void load() {
		Seq<Class<?>> sortedKeys = needIdClasses.keys().toSeq().sortComparing(Class::getName);

		for (Class<?> c : sortedKeys) {
			int id = EntityMapping.register(c.getName(), needIdClasses.get(c).prov);
			classIdMap.put(c, id);
		}

		if (NewHorizon.DEBUGGING || Vars.headless) {
			logDebugInfo();
		}
	}

	/**
	 * Logs debug information about the registered classes and their IDs.
	 */
	private static void logDebugInfo() {
		Log.info("||=============================================||");
		classIdMap.each((c, id) -> NewHorizon.debugLog(id + " | " + c.getSimpleName()));
		Log.info("||=============================================||");
	}

	/**
	 * Wrapper class for entity providers with their corresponding name.
	 */
	public static class ProvSet {
		public final String name;
		public final Prov<?> prov;

		public ProvSet(Prov<?> prov) {
			this.name = prov.get().getClass().toString();
			this.prov = prov;
		}
	}
}

