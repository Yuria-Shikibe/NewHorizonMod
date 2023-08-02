package newhorizon.expand.cutscene;

import static arc.Core.bundle;


/**
 * @see arc.flabel.FConfig
 * */
public class CSSTexts{
	public static String getBundle(String name, int textID){
		
		return bundle.get("nh.cutscene." + name + ".dia-" + textID);
	}
	
	public static String endCommunicate(){
		return bundle.get("nh.cutscene.end-communication");
	}
	
	public static String jumpgateTriggered(){
		return bundle.get("nh.cutscene.jumpgate-triggered");
	}
	public static String powerSuppressors(){
		return bundle.get("nh.cutscene.power-suppressors");
	}
	
	public static String reinforcementsInbound(){
		return bundle.get("nh.cutscene.reinforcements-inbound");
	}
	public static String incomingRaid(){
		return bundle.get("nh.cutscene.incoming-raid");
	}
	public static String missionAccomplished(){
		return bundle.get("nh.cutscene.mission-accomplished");
	}
	
	public static String takingDamage_Heavy(){
		return bundle.get("nh.cutscene.damage-taking-heavy");
	}
	public static String standbyHyperspace(){
		return bundle.get("nh.cutscene.standby-hyperspace");
	}
}
