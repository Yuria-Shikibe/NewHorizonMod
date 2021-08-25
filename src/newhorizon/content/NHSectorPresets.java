package newhorizon.content;

import mindustry.ctype.ContentList;
import mindustry.gen.Icon;
import mindustry.type.Planet;
import mindustry.type.SectorPreset;

public class NHSectorPresets implements ContentList{
	public static SectorPreset
		hostileHQ, downpour, luminariOutpost, quantumCraters, ruinedWarehouse, shatteredRavine, deltaHQ;
	
	@Override
	public void load(){
		deltaHQ = new NHSectorPreset("delta-HQ", NHPlanets.midantha, 79){{
			addStartingItems = true;
			useAI = false;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		shatteredRavine = new NHSectorPreset("shattered-ravine", NHPlanets.midantha, 64){{
			addStartingItems = true;
			useAI = false;
			difficulty = 10;
			startWaveTimeMultiplier = 2.5f;
			captureWave = 30;
		}};
		
		ruinedWarehouse = new NHSectorPreset("ruined-warehouse", NHPlanets.midantha, 0){{
			addStartingItems = true;
			useAI = false;
			difficulty = 3;
			startWaveTimeMultiplier = 2.5f;
			captureWave = 40;
		}};
		
		hostileHQ = new NHSectorPreset("hostile-HQ", NHPlanets.midantha, 24){{
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
		
		downpour = new NHSectorPreset("downpour", NHPlanets.midantha, 55){{
			addStartingItems = true;
			captureWave = 80;
			difficulty = 5;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		luminariOutpost = new NHSectorPreset("luminari-outpost", NHPlanets.midantha, 102){{
			addStartingItems = true;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
		quantumCraters = new NHSectorPreset("quantum-craters", NHPlanets.midantha, 86){{
			addStartingItems = true;
			captureWave = 150;
			difficulty = 8;
			startWaveTimeMultiplier = 2.5f;
		}};
		
	}
	
	public static class NHSectorPreset extends mindustry.type.SectorPreset{
		
		public NHSectorPreset(String name, Planet planet, int sector){
			super(name, planet, sector);
		}
		
		@Override
		public boolean isHidden(){
			return false;
		}
	}
}
