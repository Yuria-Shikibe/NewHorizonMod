package newhorizon.vars;

import arc.Events;
import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.world.Tile;
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
			
		});
		
		Events.on(EventType.ClientPreConnectEvent.class, e -> {
			for(ServerInitc c : NHWorldVars.serverLoad){
				c.loadAfterConnect();
			}
		});
		
		Events.on(EventType.StateChangeEvent.class, e -> {
		
		});
	}
}
