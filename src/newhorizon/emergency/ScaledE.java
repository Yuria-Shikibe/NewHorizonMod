package newhorizon.emergency;

import arc.math.Interp;
import arc.math.Scaled;

@EmergencyRelpace(replaced = Scaled.class)
public interface ScaledE{
	float fin();
	default float fout(){
		return 1f - fin();
	}
	default float fout(Interp i){
		return i.apply(fout());
	}
	default float fout(float margin){
		float f = fin();
		if(f >= 1f - margin)return 1f - (f - (1f - margin)) / margin;
		else return 1f;
	}
	default float fin(Interp i){
		return i.apply(fin());
	}
	default float finpow(){
		return Interp.pow3Out.apply(fin());
	}
	default float fslope(){
		return (0.5f - Math.abs(fin() - 0.5f)) * 2f;
	}
}
