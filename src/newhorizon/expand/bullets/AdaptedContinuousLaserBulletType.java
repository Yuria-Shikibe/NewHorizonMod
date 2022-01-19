package newhorizon.expand.bullets;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import mindustry.entities.Damage;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Layer;
import newhorizon.util.feature.PosLightning;

public class AdaptedContinuousLaserBulletType extends ContinuousLaserBulletType{
	public float lightningEffectDelta = 4f;
	
	public AdaptedContinuousLaserBulletType(float damage){
		super(damage);
	}
	
	protected AdaptedContinuousLaserBulletType(){
		this(0);
	}
	
	@Override
	public void draw(Bullet b){
		super.draw(b);
		float i = 2f;
		Draw.z(Layer.effect);
		for(Color c : colors){
			Draw.color(c);
			Fill.circle(b.x, b.y, width * i / 2f * Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f));
			i -= 0.25f;
		}
	}
	
	@Override
	public void update(Bullet b){
		super.update(b);
		if(b.timer(2, lightningEffectDelta)) PosLightning.createEffect(b, Damage.findLaserLength(b, length) * Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f) * lenscales[2] * 0.9f, b.rotation(), hitColor, 2, PosLightning.WIDTH);
	}
}
