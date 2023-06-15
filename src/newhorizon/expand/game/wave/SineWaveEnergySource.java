package newhorizon.expand.game.wave;

import arc.math.Mathf;
import mindustry.gen.Building;

public class SineWaveEnergySource extends WaveEnergyState{
	public float amplitude = 1;
	
	public float phaseOffset = 0;
	
	//Should be applied to src's progress
	public float angularVelocity = 1;
	
	public SineWaveEnergySource init(float amplitude, float phaseOffset, float angularVelocity){
		this.angularVelocity = angularVelocity;
		this.phaseOffset = phaseOffset;
		this.amplitude = amplitude;
		
		return this;
	}
	
	public SineWaveEnergySource init(float amplitude, float angularVelocity){
		this.angularVelocity = angularVelocity;
		this.amplitude = amplitude;
		
		return this;
	}
	
	@Override public float getMaxEnergy(){return getAmp();}
	
	@Override public float getAveEnergy(){return getAmp() / Mathf.sqrt2;}
	
	public float getAmp(){return amplitude * (constantOutput ? 1 : src.efficiency());}
	
	@Override public float getCurrentEnergy(){return totalEnergy;}
	
	@Override
	public float getAngularVelocity(){
		return angularVelocity;
	}
	
	@Override public float getRange(){return getAmp();}
	@Override public float expectInput(){return -1;}
	@Override public boolean hasOutput(){return true;}
	@Override public void input(Building src, float input){}
	
	@Override
	public float output(float require){
		if(totalEnergy >= require){
			totalEnergy -= require;
			return require;
		}else{
			float m = totalEnergy;
			totalEnergy = 0;
			return m;
		}
	}
	
	@Override
	public void update(){
		totalEnergy = Math.abs(Mathf.sin(src.totalProgress() + phaseOffset)) * getAmp();
		
		super.update();
	}
}
