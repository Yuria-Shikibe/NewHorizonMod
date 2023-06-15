package newhorizon.expand.eventsys.types;

import arc.Core;
import arc.math.Mathf;
import mindustry.Vars;
import newhorizon.expand.entities.WorldEvent;
import newhorizon.expand.eventsys.annotation.Customizable;
import newhorizon.expand.eventsys.annotation.NumberParam;
import newhorizon.expand.eventsys.annotation.Parserable;

public class ReachWaveEvent extends ObjectiveEventType{
	@Customizable @NumberParam
	public int targetWave = 10;
	
	@Customizable @Parserable(WorldEventType.class)
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
