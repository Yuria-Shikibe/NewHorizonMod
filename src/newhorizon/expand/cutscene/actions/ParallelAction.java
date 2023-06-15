package newhorizon.expand.cutscene.actions;

import newhorizon.expand.cutscene.NHCSS_Action;

public class ParallelAction extends NHCSS_Action implements NHCSS_Action.ImportantAction{
	public NHCSS_Action[] actions;
	
	public ParallelAction(ActionBus bus, NHCSS_Action[] actions){
		super(bus);
		this.actions = actions;
		
		duration = 0;
		for(NHCSS_Action action : actions){
			action.setChild();
			
			duration = Math.max(action.duration, duration);
		}
	}
	
	public ParallelAction(ActionBus bus, NHCSS_Action delay, NHCSS_Action runnable){
		super(bus);
		actions = new NHCSS_Action[]{};
	}
	
	@Override
	public void act(){
		super.act();
		
		if(bus.skipping)for(NHCSS_Action action : actions){
			if(action instanceof ImportantAction && !action.done())action.act();
		}
	}
	
	@Override
	public void update(){
		for(NHCSS_Action action : actions){
			action.update();
		}
		
		super.update();
	}
	
	@Override
	public void setup(){
		super.setup();
		
		for(NHCSS_Action action : actions){
			action.setup();
		}
	}
}
