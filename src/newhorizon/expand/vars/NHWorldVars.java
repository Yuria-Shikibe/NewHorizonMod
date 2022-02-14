package newhorizon.expand.vars;

import mindustry.Vars;
import newhorizon.expand.entities.NHGroups;

public class NHWorldVars{
	public NHWorldVars(){
	
	}
	
	public void clear(){
		NHGroups.clear();
	}
	
	public void afterLoad(){
		Vars.world.getQuadBounds(NHGroups.gravityTraps.bounds);
	}
}
