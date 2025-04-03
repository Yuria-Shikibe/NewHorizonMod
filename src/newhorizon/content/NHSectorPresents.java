package newhorizon.content;

import mindustry.type.SectorPreset;

public class NHSectorPresents{
	public static SectorPreset abandonedOutpost;
	
	public static void load(){
		
		abandonedOutpost = new SectorPreset("abandoned-outpost", NHPlanets.midantha, 15){{
			captureWave = 40;
			difficulty = 4;
			
			alwaysUnlocked = true;
			
			rules = r -> {
				r.winWave = captureWave;
			};
		}};
	}
}
