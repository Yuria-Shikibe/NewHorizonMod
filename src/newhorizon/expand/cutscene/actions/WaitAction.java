package newhorizon.expand.cutscene.actions;

import arc.func.Boolp;
import newhorizon.expand.cutscene.NHCSS_Action;

public class WaitAction extends NHCSS_Action{
	public final Boolp boolp;
	
	public WaitAction(ActionBus bus, Boolp boolp){
		super(bus);
		this.boolp = boolp;
	}
	
	public void update(){
		if(boolp.get()){
			life = duration;
			if(!done)act();
		}
	}
}
