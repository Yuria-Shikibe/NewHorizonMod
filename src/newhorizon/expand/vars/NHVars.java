package newhorizon.expand.vars;

import arc.Events;
import arc.math.Rand;
import mindustry.world.Tile;
import newhorizon.expand.block.special.BeaconBlock;

public class NHVars{
	public static NHGameState state = new NHGameState();
	public static NHWorldVars world = new NHWorldVars();
	public static NHCtrlVars ctrl = new NHCtrlVars();
	
	public static Rand rand = new Rand();
	public static Tile tmpTile;
	
	
	public static void init(){
		Events.on(BeaconBlock.BeaconCapturedEvent.class, e -> {
			if(state.captureMod())state.beaconCaptureCore.updateBeacons();
		});
	}
	/** Called After {@link mindustry.game.EventType.WorldLoadEvent}*/
	public static void load(){
		state.load();
	}
	
	public static void update(){
		state.update();
	}
	
	public static void reset(){
		world = new NHWorldVars();
		state.exit();
		state = new NHGameState();
		ctrl = new NHCtrlVars();
	}
	
	public static void resetCtrl(){
		ctrl = new NHCtrlVars();
	}
	
	
}
