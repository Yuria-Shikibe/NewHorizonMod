package newhorizon.expand.cutscene.components;

import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;

import static newhorizon.NHVars.cutsceneUI;

public class CutsceneControl {
	public boolean waiting = false;
	public float waitSpacing = 60;
	public float waitTimer = 0;

	public ActionBus mainBus;
	public Queue<ActionBus> waitingBuss = new Queue<>();
	public Seq<ActionBus> subBuses = new Seq<>();


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
		if (mainBus == null && !waiting && !waitingBuss.isEmpty()) {
			mainBus = waitingBuss.removeLast();
		}

		//update the sub action bus
		for(int i = 0; i < subBuses.size; i++){
			ActionBus bus = subBuses.get(i);
			if(bus.complete())subBuses.remove(i);
			bus.update();
		}

		cutsceneUI.update();
	}

	public void addMainActionBus(ActionBus bus) {
		if (mainBus == null) {
			mainBus = bus;
		}else {
			waitingBuss.add(bus);
		}
	}

	public void addSubActionBus(ActionBus bus) {
		subBuses.add(bus);
	}
}
