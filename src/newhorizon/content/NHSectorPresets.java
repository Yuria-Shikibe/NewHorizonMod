package newhorizon.content;

import arc.Events;
import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.ctype.ContentList;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.SectorPreset;

public class NHSectorPresets implements ContentList{
	public static ObjectMap<SectorPreset, Cons<Sector>> captureMap = new ObjectMap<>(), loseMap = new ObjectMap<>();
	
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
			
			loseMap.put(this, NHSectorPresets::resetSector);
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
		
		Events.on(EventType.SectorCaptureEvent.class, e -> {
			if(captureMap.containsKey(e.sector.preset)){
				captureMap.get(e.sector.preset).get(e.sector);
			}
		});
		
		if(Vars.headless)return;
		
//		Events.on(EventType.SectorInvasionEvent.class, e -> {
//
//		});
//
		Events.on(EventType.SectorLoseEvent.class, e -> {
			Sector sector = e.sector;
			if(loseMap.containsKey(sector.preset)){
				loseMap.get(sector.preset).get(sector);
			}
		});
		
		Events.on(EventType.LoseEvent.class, e -> {
			if(!Vars.state.isGame() || !Vars.state.isCampaign())return;
			Sector sector = Vars.state.getSector();
			if(loseMap.containsKey(sector.preset)){
				loseMap.get(sector.preset).get(sector);
			}
		});
	}
	
	public static void resetSector(Sector sector){
		sector.save.delete();
		sector.save = null;
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
