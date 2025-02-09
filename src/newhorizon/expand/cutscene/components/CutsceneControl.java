package newhorizon.expand.cutscene.components;

import arc.Events;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.game.EventType;

import static newhorizon.NHVars.cutsceneUI;

public class CutsceneControl {
	public boolean waiting = false;
	public float waitSpacing = 60;
	public float waitTimer = 0;

	public ActionBus mainBus;
	public Queue<ActionBus> waitingBuses = new Queue<>();
	public Seq<ActionBus> subBuses = new Seq<>();

	public CutsceneControl() {
		Events.on(EventType.WorldLoadEvent.class, event -> clear());
	}

	public void update(){
		//update the main action bus and remove completed action bus
		if(mainBus != null){
			mainBus.update();
			if(mainBus.complete()) {
				mainBus = null;
				waiting = true;
				cutsceneUI.reset();
			}
		}

		//increase timer when waiting
		if (waiting) {
			waitTimer += Time.delta;
			if (waitTimer >= waitSpacing) {
				waitTimer = 0;
				waiting = false;
			}
		}

		//change current main bus when waiting finished
		if (mainBus == null && !waiting && !waitingBuses.isEmpty()) {
			mainBus = waitingBuses.removeLast();
		}

		//update the sub action bus
		for(int i = 0; i < subBuses.size; i++){
			ActionBus bus = subBuses.get(i);
			if(bus.complete())subBuses.remove(i);
			bus.update();
		}

		cutsceneUI.update();
	}

	public void skipAll(){
		if (mainBus != null) mainBus.skip();
		if (waitingBuses != null && !waitingBuses.isEmpty()) waitingBuses.each(ActionBus::skip);
		if (subBuses != null && !subBuses.isEmpty()) subBuses.each(ActionBus::skip);
	}

	public void clear(){
		waiting = false;
		waitTimer = 0;
		mainBus = null;
		waitingBuses.clear();
		subBuses.clear();
	}

	public void addMainActionBus(ActionBus bus) {
		if (mainBus == null) {
			mainBus = bus;
		}else {
			waitingBuses.add(bus);
		}
	}

	public void addSubActionBus(ActionBus bus) {
		subBuses.add(bus);
	}
}
