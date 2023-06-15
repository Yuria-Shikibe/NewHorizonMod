package newhorizon.expand.game.wave;

import mindustry.gen.Building;

public class WaveEnergyReceiver extends WaveEnergyState{
	public float maxAccept = 5;
	
	@Override
	public float getMaxEnergy(){
		return maxAccept;
	}
	
	@Override
	public float getAveEnergy(){
		return getCurrentEnergy();
	}
	
	@Override
	public float getCurrentEnergy(){
		return totalEnergy;
	}
	
	@Override
	public float getWavelength(){
		return Float.POSITIVE_INFINITY;
	}
	
	@Override
	public float getFrequency(){
		return 0;
	}
	
	@Override
	public float getRange(){
		return 0;
	}
	
	@Override
	public float expectInput(){
		return Math.min(maxAccept, 10000);
	}
	
	@Override
	public boolean hasOutput(){
		return false;
	}
	
	@Override
	public void input(Building src, float input){
		totalEnergy += input;
		totalEnergy = Math.min(totalEnergy, maxAccept);
	}
	
	@Override
	public float output(float require){
		return 0;
	}
}
