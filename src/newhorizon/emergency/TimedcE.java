package newhorizon.emergency;

import mindustry.gen.Entityc;
import mindustry.gen.Timedc;

@EmergencyRelpace(replaced = Timedc.class)
public interface TimedcE extends Entityc, ScaledE{
	float fin();
	
	float time();
	
	void time(float var1);
	
	float lifetime();
	
	void lifetime(float var1);
}
