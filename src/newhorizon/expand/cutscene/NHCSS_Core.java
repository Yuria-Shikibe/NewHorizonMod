package newhorizon.expand.cutscene;

import arc.struct.ObjectMap;
import mindustry.gen.Building;

public class NHCSS_Core{
	public static NHCSS_Core core = new NHCSS_Core();
	
	public static void loadRegisters(){
//		Events.on(EventType.BuildDamageEvent);
	}
	
	public ObjectMap<String, Runnable> updaters = new ObjectMap<>();
	public ObjectMap<String, Runnable> initers = new ObjectMap<>();
	
	public enum Trigger{
		enemyCoreDestruction;
		
		Building monitorTarget;
		int wave;
	}
}
