package newhorizon.content;

import mindustry.type.SectorPreset;

public class NHSectorPresents{
	public static SectorPreset initialPlane, abandonedOutpost;
	
	public static void load(){
		abandonedOutpost = new SectorPreset("abandoned-outpost", NHPlanets.midantha, 15){{
			captureWave = 40;
			difficulty = 4;
			
			alwaysUnlocked = true;
			
			rules = r -> {
				r.winWave = captureWave;
			};
		}};
		
		initialPlane = new SectorPreset("initial-plane", NHPlanets.midantha, 0){{
			captureWave = 50;
			difficulty = 7;
			
			rules = r -> {
				r.tags.put(NHInbuiltEvents.applyKey, "true");
				r.winWave = captureWave;
//				NHRegister.postAfterLoad(() -> {
//					if(Vars.net.client())return;
//					WorldLabel l = Pools.obtain(WorldLabel.class, WorldLabel::create);
//					l.text(Core.bundle.get("nh.sector-hint.initialPlane.raider"));
//					l.set(307 * 8, 306 * 8);
//					l.add();
//				});
			};
		}};
	}
}
