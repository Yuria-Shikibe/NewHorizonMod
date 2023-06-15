package newhorizon.expand.cutscene.actions;

import arc.struct.Seq;
import newhorizon.expand.cutscene.NHCSS_Action;

//Just Be Simple, the schedule won't be long
public class QueueAction extends NHCSS_Action implements NHCSS_Action.ImportantAction{
	public Seq<NHCSS_Action> seq;
	
	protected float[] schedule;
	
	protected int lastIndex = 0; //Match the seq
	
	public QueueAction initActions(NHCSS_Action... actions){
		float sum = 0;
		
		seq = new Seq<>(actions.length);
		schedule = new float[actions.length + 1];
		schedule[0] = 0;
		
		int index = 0;
		for(NHCSS_Action action : actions){
			seq.add(action);
			action.setChild();
			sum += action.duration;
			schedule[++index] = sum;
		}
		
		setDuration(sum);
		
		return this;
	}
	
	public QueueAction(ActionBus bus){
		super(bus);
	}
	
	@Override
	public void update(){
		for(int i = 0; i < schedule.length - 1; i++){
			if(life > schedule[i] && life < schedule[i + 1]){
				if(lastIndex != i){
					NHCSS_Action action = seq.get(lastIndex);
					if(!action.done())action.act();//Act if undone
					lastIndex = i;
				}
				NHCSS_Action action = seq.get(i);
				action.life = life - schedule[i];
				action.update();
			}
		}
		
		super.update();
	}
	
	@Override
	public void act(){
		NHCSS_Action action = seq.peek();
		if(!action.done())action.act();
		
		if(bus.skipping)for(NHCSS_Action act : seq){
			if(act instanceof ImportantAction && !action.done())act.act();
		}
	}
}
