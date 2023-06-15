package newhorizon.expand.game.wave;

import arc.math.Mathf;
import mindustry.gen.Building;
import newhorizon.expand.block.ancient.wave.WaveConveyor;
import newhorizon.util.struct.FloatCircularQueue;

/**Conveyors Only*/
public class WaveEnergyStack extends WaveEnergyPool{
	public FloatCircularQueue stacks;
	public float lastAverage;
	public float lastMax;
	public float lastMin;
	
	public float lastInput;
	public int length;
	
	public WaveEnergyStack(int length){
		this.length = length;
		
		//[0] -> Src Input
		//Bound out[length] -> Current Output;
//		Duct
		this.stacks = new FloatCircularQueue(length);
	}
	
	public float getAxis(float f){
		return Mathf.curve(f, lastMin, lastMax);
	}
	
	@Override
	public void update(){
		if(!stacks.isEmpty()){
			lastMax = Float.MIN_VALUE;
			lastMin = Float.MAX_VALUE;
			
			float sum = 0;
			for(float f : stacks){
				lastMax = Math.max(f, lastMax);
				lastMin = Math.min(f, lastMin);
				sum += f;
			}
			
			lastAverage = sum / stacks.size();
		}else{
			lastAverage = lastMax = lastMin = 0;
		}
	}
	
	@Override
	public boolean acceptInput(Building src){
		return super.acceptInput(src) && src instanceof WaveConveyor.WaveConveyorBuild && src.acceptItem(src, null);
	}
	
	@Override
	public void input(Building src, float input){
		totalEnergy = stacks.push(Math.min(input, maxAccept), 0);
	
		if(src instanceof WaveConveyor.WaveConveyorBuild)src.handleItem(src, null);
	}
	
	@Override
	public float getMaxEnergy(){
		return lastMax;
	}
	
	@Override
	public float getAveEnergy(){
		return lastAverage;
	}
	
	@Override
	public float getRange(){
		return lastMax - lastMin;
	}
}
