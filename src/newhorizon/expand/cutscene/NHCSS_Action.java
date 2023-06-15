package newhorizon.expand.cutscene;

import arc.Core;
import arc.math.Mathf;
import arc.util.ArcRuntimeException;
import arc.util.Time;
import newhorizon.util.struct.TimeQueue;

public abstract class NHCSS_Action implements TimeQueue.Timed{
	protected static ActionBus contextBus = null;
	
	public static void skip(ActionBus bus){
		while(!bus.queue.isEmpty()){
			NHCSS_Action action = bus.queue.removeLast();
			if(action instanceof ImportantAction)action.act();
		}
	}
	
	public static void beginCreateAction(ActionBus bus){
		Core.app.post(() -> {
			if(contextBus == bus)throw new ArcRuntimeException("Context Should Be Removed After Creating Action Queue!");
		});
		
		contextBus = bus;
	}
	
	public static void endCreateAction(){
		contextBus = null;
	}
	
	public static ActionBus getContext(){
		return contextBus;
	}
	
	public NHCSS_Action(ActionBus bus){
		this.bus = bus;
	}
	
	@Override
	public float getDuration(){
		return duration;
	}
	
	public final ActionBus bus;
	
	protected boolean isChild = false;
	
	protected boolean done = false;
	
	public float life = 0;
	
	public float duration = 0; //Ticks
	
	public NHCSS_Action setDuration(float life){
		duration = life;
		return this;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean done(){
		return done;
	}
	
	public void setChild(){
		isChild = true;
	}
	
	
	public void update(){
		if(life >= duration){
			life = duration;
			if(!done)act();
		}else life += Time.delta;
	}
	
	public void next(){
		if(isChild)return;
		
		if(!bus.queue.isEmpty()){
			bus.current = bus.queue.removeLast();
			bus.current.setup();
		}else bus.current = null;
	}
	
	public void act(){
		done = true;
		if(!bus.skipping)next();
	}
	
	public void setup(){}
	
	@Override
	public String toString(){
		return "On Bus: " + bus.hashCode() + " | " + getClass().getSimpleName() + "-" + hashCode();
	}
	
	public float progress(){
		return Mathf.clamp(life / duration);
	}
	
	public interface ImportantAction{
	
	}
	
	public static class ActionBus extends TimeQueue<NHCSS_Action>{
		public boolean skipping = false;
		
		public void skip(){
			skipping = true;
			
			if(current != null && current instanceof ImportantAction){
				current.act();
			}
			
			while(!queue.isEmpty()){
				NHCSS_Action action = queue.removeLast();
				if(action instanceof ImportantAction)action.act();
			}
			
			skipping = false;
			current = null;
			queue.clear();
			
			Core.app.post(() -> {
				if(NHCSS_UI.duringCurtain)NHCSS_UI.withdrawCurtain();
				NHCSS_UI.cameraOverride = false;
				NHCSS_UI.controlOverride = false;
			});
		}
	}
}
