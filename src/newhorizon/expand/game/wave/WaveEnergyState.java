package newhorizon.expand.game.wave;

import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import mindustry.gen.Building;

/**
 * Scale:
 * Extremely Huge: 10000
 * Huge: 5000
 * Large: 2000
 * Medium: 1000
 * Small: 500
 * Tiny: 100
 *
 * */
public abstract class WaveEnergyState{
	public Building src;
	public float totalEnergy = 0;
	public float decayScl = 1;
	public boolean constantOutput = false;
	
	//Maximum Energy
	public abstract float getMaxEnergy();
	
	//Average Energy
	public abstract float getAveEnergy();
	
	public abstract float getCurrentEnergy();
	
	public float getWavelength(){
		return Mathf.PI2 / getAngularVelocity();
	}
	
	public float getFrequency(){
		return getAngularVelocity() / Mathf.PI2;
	}
	
	public float getAngularVelocity(){
		return 1;
	}
	
	public abstract float getRange();
	
	public abstract float expectInput();
	
	public boolean acceptInput(Building src){
		return expectInput() > 0;
	}
	
	public abstract boolean hasOutput();
	
	public abstract void input(Building src, float input);
	
	public abstract float output(float require);
	
	public void update(){
	
	}
	
	public void updateTrans(){
		if(hasOutput()){
			src.proximity().each(b -> {
				if(b instanceof WaveEnergyBuilding){
					WaveEnergyState ee = ((WaveEnergyBuilding)b).getWave();
					
					if(ee.acceptInput(src))ee.input(src, output(ee.expectInput() * decayScl));
				}
			});
		}
	};
	
	public void init(Building src){
		this.src = src;
	};
	
	public void display(Table table){
	
	}
}
