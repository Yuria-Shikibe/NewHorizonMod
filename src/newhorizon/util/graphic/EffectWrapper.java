package newhorizon.util.graphic;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;

public class EffectWrapper extends Effect{
	public Effect effect = Fx.none;
	public Color color = Color.white.cpy();
	public float rot = -1;
	public boolean rotModifier = false;
	
	public EffectWrapper(){
	}
	
	
	public EffectWrapper(Effect effect, Color color){
		this.effect = effect;
		this.color = color;
	}
	
	public EffectWrapper(Effect effect, Color color, float rot){
		this.effect = effect;
		this.color = color;
		this.rot = rot;
	}
	
	
	
	public static EffectWrapper wrap(Effect effect, Color color){
		return new EffectWrapper(effect, color);
	}
	public static EffectWrapper wrap(Effect effect, Color color, float rot){
		return new EffectWrapper(effect, color, rot);
	}
	public static EffectWrapper wrap(Effect effect, float rot, boolean rotModifier){
		return new EffectWrapper(effect, Color.white, rot).setRotModifier(rotModifier);
	}
	
	public EffectWrapper setRotModifier(boolean rotModifier){
		this.rotModifier = rotModifier;
		return this;
	}
	
	@Override
	public void init(){
		effect.init();
		clip = effect.clip;
		lifetime = effect.lifetime;
	}
	
	@Override
	public void render(EffectContainer e){
	}
	
	@Override
	public void create(float x, float y, float rotation, Color color, Object data){
		effect.create(x, y, rot > 0 ? rotModifier ? rot + rotation : rot : rotation, this.color, data);
	}
	
}
