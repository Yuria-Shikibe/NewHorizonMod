package newhorizon.util.feature.cutscene.events;

import arc.Core;
import mindustry.Vars;
import newhorizon.util.feature.cutscene.CutsceneEvent;

public class ReachWaveObjective extends ObjectiveEvent{
	public int targetWave = 10;
	public CutsceneEvent toTrigger = CutsceneEvent.NULL_EVENT;
	
	public ReachWaveObjective(String name){
		super(name);
		action = e -> toTrigger.setup();
		trigger = e -> Vars.state.wave >= targetWave;
		info = e -> Core.bundle.format("nh.cutscene.event.reach-waves", Vars.state.wave, targetWave);
		exist = e -> Vars.state.rules.waves;
	}
}
