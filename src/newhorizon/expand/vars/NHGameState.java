package newhorizon.expand.vars;

import arc.util.Log;
import arc.util.serialization.Jval;
import mindustry.Vars;
import newhorizon.NewHorizon;
import newhorizon.util.feature.BeaconCaptureCore;

import static newhorizon.util.Tool_JsonHandler.*;

public class NHGameState{
	public boolean jumpGateUseCoreItems = false;
	public boolean jumpGateEnemyCheatEnabled = true;
	public boolean mode_beaconCapture = false;
	public boolean beaconUsePolygonField = false, unitHoldField = true;
	public BeaconCaptureCore beaconCaptureCore = null;
	
	public NHGameState(){
	}
	
	public boolean captureMod(){
		return beaconCaptureCore != null && mode_beaconCapture;
	}
	
	public void exit(){
		if(beaconCaptureCore != null)beaconCaptureCore.exit();
	}
	
	public void update(){
		if(captureMod())beaconCaptureCore.update();
	}
	
	/** Called After {@link mindustry.game.EventType.WorldLoadEvent}*/
	public void load(){
		Jval jval;
		
		if(Vars.state.map.tags.containsKey(ALL_SETTINGS))jval = Jval.read(Vars.state.map.tags.get(ALL_SETTINGS));
		else jval = Jval.newObject();
		
		setContext(jval);
		jumpGateUseCoreItems = getBool_Context(JUMP_GATE_USE_CORE_ITEMS, false);
		jumpGateEnemyCheatEnabled = getBool_Context(JUMP_GATE_CHEAT_ENABLED, true);
		mode_beaconCapture = getBool_Context(BEACON_ENABLE, false);
		
		if(Vars.state.isCampaign() && Vars.state.rules.sector.isCaptured())mode_beaconCapture = false;
		
		if(mode_beaconCapture){
			beaconUsePolygonField = getBool_Context(BEACON_FIELD_POLYGON, false);
			unitHoldField = getBool_Context(BEACON_UNIT_FIELD, true);
			
			beaconCaptureCore = new BeaconCaptureCore();
			beaconCaptureCore.updateBeacons();
			
			beaconCaptureCore.winScore = getInt_Context(BEACON_CAPTURE_SCORE, 100_000);
		}
		
		endContext();
		
//		if(mode_beaconCapture)Vars.state.rules.pvp = true;
		
		if(NewHorizon.DEBUGGING){
			Log.info("State:");
			Log.info("jumpGateUseCoreItems: " + jumpGateUseCoreItems);
			Log.info("jumpGateEnemyCheatEnabled: " + jumpGateEnemyCheatEnabled);
		}
	}
}
