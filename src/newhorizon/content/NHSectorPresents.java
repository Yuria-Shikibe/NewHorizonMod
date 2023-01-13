package newhorizon.content;

import arc.Core;
import mindustry.type.SectorPreset;

public class NHSectorPresents{
	public static SectorPreset initialPlane;
	
	public static void load(){
		initialPlane = new SectorPreset("initialPlane", NHPlanets.midantha, 0){{
			captureWave = 50;
			difficulty = 4;
			
			rules = r -> {
				r.tags.put(NHInbuiltEvents.applyKey, "true");
				Core.app.post(() -> {
					r.winWave = captureWave;
				});
			};
		}};
	}
}
