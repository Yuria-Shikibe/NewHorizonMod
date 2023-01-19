package newhorizon.util.graphic;

import arc.graphics.Color;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import newhorizon.NHSetting;

public class OptionalMultiEffect extends MultiEffect{
	
	public OptionalMultiEffect(Effect... effects){
		super(effects);
	}
	
	@Override
	public void init(){
		if(effects.length == 0)throw new IllegalArgumentException("The MultiEffect must contains at least one effect");
		super.init();
	}
	
	@Override
	public void create(float x, float y, float rotation, Color color, Object data){
		if(!shouldCreate()) return;
		
		if(NHSetting.enableDetails()){
			for(Effect effect : effects){
				effect.create(x, y, rotation, color, data);
			}
		}else effects[0].create(x, y, rotation, color, data);
	}
}
