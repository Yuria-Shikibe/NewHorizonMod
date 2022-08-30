package newhorizon;

import arc.Events;
import mindustry.game.EventType;

public class NHEventListenerRegister{
	public static void load(){
		Events.on(EventType.ResetEvent.class, e -> {
			NewHorizon.debugLog("Reset Event Triggered");
			
			NHGroups.clear();
		});
		
		Events.run(EventType.Trigger.draw, () -> {
			NHModCore.core.renderer.draw();
		});
		
		Events.run(EventType.Trigger.postDraw, () -> {
			NHModCore.core.renderer.draw();
		});
		
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NewHorizon.debugLog("WorldLoad Event Triggered");
			
			NHGroups.resize();
			NHModCore.core.initOnLoadWorld();
		});
	}
}
