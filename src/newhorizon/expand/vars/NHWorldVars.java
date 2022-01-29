package newhorizon.expand.vars;

import mindustry.Vars;
import newhorizon.expand.entities.NHGroups;

public class NHWorldVars{
	
	public transient int ix, iy;
	public transient int commandPos = -1;
	
	public NHWorldVars(){
	
	}
	
	public void clear(){
		NHGroups.clear();
		
		ix = iy = 0;
		commandPos = -1;
	}
	
	public void afterLoad(){
		Vars.world.getQuadBounds(NHGroups.gravityTraps.bounds);
	}
}
