package newhorizon.expand.cutscene.components;

import arc.math.Mathf;
import arc.util.Time;
import newhorizon.util.struct.TimeQueue;

public abstract class Action implements TimeQueue.Timed{
	//lifeTimer timer
	public float lifeTimer = 0;
	//max time
	public float maxTimer = 0; //Ticks

	public Action(float duration){
		this.maxTimer = duration;
	}

	@Override
	public void update(){
		if(lifeTimer < maxTimer){
			lifeTimer += Time.delta;
			act();
		}
	}

	@Override
	public boolean complete() {return lifeTimer >= maxTimer;}

	@Override
	public void begin(){}

	@Override
	public void end(){}

	public void act(){}

	public void skip(){}

	public float progress(){return Mathf.clamp(lifeTimer / maxTimer);}

    public String phaseToString() {
        return "";
    }
}
