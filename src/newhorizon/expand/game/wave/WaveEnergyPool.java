package newhorizon.expand.game.wave;

/**
 * This should work like a capacitor.
 *
 * */
public class WaveEnergyPool extends WaveEnergyReceiver{
	public float maxDump = 100;
	
	@Override
	public boolean hasOutput(){
		return true;
	}
	
	public float maxDump(){
		return maxDump * src.edelta();
	}
	
	@Override
	public float output(float require){
		float m = Math.min(require, maxDump());
		
		if(totalEnergy >= m){
			totalEnergy -= m;
		}else{
			m = totalEnergy;
			totalEnergy = 0;
		}
		
		return m;
	}
}
