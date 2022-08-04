package newhorizon.util.graphic;

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
	public void render(EffectContainer e){
		if(NHSetting.enableDetails()){
			int index = 0;
			for(Effect f : effects){
				int i = ++index;
				e.scaled(f.lifetime, cont -> {
					cont.id = e.id + i;
					f.render(cont);
				});
				clip = Math.max(clip, f.clip);
			}
		}else{
			Effect f = effects[0];
			e.scaled(f.lifetime, cont -> {
				cont.id = e.id;
				f.render(cont);
			});
			clip = f.clip;
		}
	}
}
