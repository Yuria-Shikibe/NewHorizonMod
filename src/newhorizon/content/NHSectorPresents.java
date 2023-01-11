package newhorizon.content;

import mindustry.type.SectorPreset;

public class NHSectorPresents{
	public static SectorPreset initialPlane;
	
	public static void load(){
		initialPlane = new SectorPreset("initialPlane", NHPlanets.midantha, 0){{
			captureWave = 50;
			difficulty = 4;
			
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
