package newhorizon.content;

import mindustry.ctype.ContentList;
import mindustry.type.SectorPreset;

public class NHSectorPreset implements ContentList{
	public static SectorPreset
		outpost;
	
	@Override
	public void load(){
		outpost = new SectorPreset("outpost", NHPlanets.midantha, 0){{
			alwaysUnlocked = true;
			addStartingItems = true;
			difficulty = 10;
			startWaveTimeMultiplier = 2.5f;
		}};
	}
}
