package newhorizon.content;

import mindustry.ctype.ContentList;
import mindustry.gen.Icon;
import mindustry.type.SectorPreset;

public class NHSectorPreset implements ContentList{
	public static SectorPreset
		hostileHQ, deltaOutpost, downpour, luminariOutpost, quantumCraters;
	
	@Override
	public void load(){
		hostileHQ = new SectorPreset("hostile-HQ", NHPlanets.midantha, 0){{
			addStartingItems = true;
			useAI = false;
			difficulty = 20;
			startWaveTimeMultiplier = 2.5f;
		}
			@Override
			public void loadIcon(){
				if(Icon.layers != null)uiIcon = fullIcon = Icon.layers.getRegion();
			}
		};
		
		deltaOutpost = new SectorPreset("delta-outpost", NHPlanets.midantha, 14){{
			addStartingItems = true;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		downpour = new SectorPreset("downpour", NHPlanets.midantha, 55){{
			addStartingItems = true;
			captureWave = 80;
			difficulty = 5;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		luminariOutpost = new SectorPreset("luminari-outpost", NHPlanets.midantha, 102){{
			addStartingItems = true;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		quantumCraters = new SectorPreset("quantum-craters", NHPlanets.midantha, 86){{
			addStartingItems = true;
			captureWave = 150;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
	}
}
