package newhorizon.vars;

import arc.Events;
import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.world.Tile;
import newhorizon.func.NHSetting;
import newhorizon.interfaces.BeforeLoadc;
import newhorizon.interfaces.ServerInitc;


public class EventTriggers{
	public static void load(){
		Events.on(EventType.WorldLoadEvent.class, e -> {
			NHWorldVars.clear();
			NHCtrlVars.reset();
			
			for(Tile tile : Vars.world.tiles)NHWorldVars.intercepted.put(tile, new IntSeq(new int[Team.all.length]));
			
			for(BeforeLoadc c : NHWorldVars.advancedLoad){
				c.beforeLoad();
			}
			
			NHWorldVars.clearLast();
			NHWorldVars.worldLoaded = true;
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			NHSetting.log("Server Preload Run");
			for(ServerInitc c : NHWorldVars.serverLoad){
				c.loadAfterConnect();
			}
		});
		
//		Events.on(EventType.StateChangeEvent.class, e -> {
//			NHSetting.log("Event", "Server Preload Run");
//
//			if(NHWorldVars.worldLoaded){
//				NHSetting.log("Event", "Leaving World");
//				NHWorldVars.worldLoaded= false;
//			}
//		});
	}
}
