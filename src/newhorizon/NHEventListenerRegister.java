package newhorizon;

import arc.Events;
import arc.struct.Seq;
import mindustry.game.EventType;

public class NHEventListenerRegister{
	public static final Seq<Runnable> afterLoad = new Seq<>();
	
	protected static boolean worldLoaded = false;
	
	public static void postAfterLoad(Runnable runnable){
		if(!worldLoaded)afterLoad.add(runnable);
	}
	
	public static void load(){
		Events.on(EventType.ResetEvent.class, e -> {
			NewHorizon.debugLog("Reset Event Triggered");
			
			NHGroups.clear();
			worldLoaded = false;
			afterLoad.clear();
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
			
			afterLoad.each(Runnable::run);
			afterLoad.clear();
			worldLoaded = true;
		});
	}
}
