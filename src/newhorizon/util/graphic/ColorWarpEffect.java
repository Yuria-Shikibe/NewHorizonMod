package newhorizon.util.graphic;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;

public class ColorWarpEffect extends Effect{
	public Effect effect = Fx.none;
	public Color color = Color.white.cpy();
	
	public ColorWarpEffect(){
	}
	
	public ColorWarpEffect(Effect effect, Color color){
		this.effect = effect;
		this.color = color;
	}
	
	public static ColorWarpEffect wrap(Effect effect, Color color){
		return new ColorWarpEffect(effect, color);
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
		effect.create(x, y, rotation, this.color, data);
	}
}
