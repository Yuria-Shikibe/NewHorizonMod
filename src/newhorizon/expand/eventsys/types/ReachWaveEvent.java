package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.math.Mathf;
import mindustry.Vars;
import newhorizon.expand.entities.WorldEvent;

public class ReachWaveEvent extends ObjectiveEventType{
	public int targetWave = 10;
	
	public WorldEventType toTrigger = WorldEventType.NULL;
	
	public ReachWaveEvent(String name){
		super(name);
		
		ratio = e -> Mathf.clamp(Vars.state.wave / (float)targetWave);
		action = e -> toTrigger.create();
		trigger = e -> Vars.state.wave >= targetWave;
		info = e -> Core.bundle.format("nh.cutscene.event.reach-waves", Vars.state.wave, targetWave);
	}
	
	@Override
	public void updateEvent(WorldEvent e){
		super.updateEvent(e);
		
		if(!Vars.state.rules.waves)e.remove();
	}
}
