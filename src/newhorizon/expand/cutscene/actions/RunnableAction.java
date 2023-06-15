package newhorizon.expand.cutscene.actions;

import newhorizon.expand.cutscene.NHCSS_Action;

public class RunnableAction extends NHCSS_Action implements NHCSS_Action.ImportantAction{
	public final Runnable runnable;
	
	public RunnableAction(ActionBus bus, Runnable runnable){
		super(bus);
		this.runnable = runnable;
	}
	
	@Override
	public void act(){
		super.act();
		
		if(runnable != null)runnable.run();
	}
}
