package newhorizon;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.net.Net;
import newhorizon.expand.eventsys.EventHandler;
import newhorizon.expand.eventsys.WorldEventObjective;
import newhorizon.expand.packets.LongInfoMessageCallPacket;

public class NHRegister{
	public static final Seq<Runnable> afterLoad = new Seq<>();
	
	protected static boolean worldLoaded = false;
	
	public static void postAfterLoad(Runnable runnable){
		if(!worldLoaded)afterLoad.add(runnable);
	}
	
	static{
		Net.registerPacket(LongInfoMessageCallPacket::new);
	}
	
	private static void registerJsonClasses(){
	
	}
	
	public static boolean worldLoaded(){
		return worldLoaded;
	}
	
	public static void load(){
		Events.on(EventType.ResetEvent.class, e -> {
			NewHorizon.debugLog("Reset Event Triggered");
			
			NHGroups.clear();
			worldLoaded = false;
			afterLoad.clear();
			EventHandler.dispose();
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
			EventHandler.create();

			afterLoad.each(Runnable::run);
			afterLoad.clear();
			
			Core.app.post(() -> {
				Vars.state.rules.objectives.add(new WorldEventObjective());
				Core.app.post(() -> Core.app.post(() -> Core.app.post(() ->
					worldLoaded = true
				)));
			});
		});
		
		if(!Vars.headless)Events.on(EventType.StateChangeEvent.class, e -> {
			if(e.to == GameState.State.menu){
				worldLoaded = false;
			}
		});
	}
	
	
}
