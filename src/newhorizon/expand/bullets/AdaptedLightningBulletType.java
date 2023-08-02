package newhorizon.expand.bullets;

import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Lightning;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;

public class AdaptedLightningBulletType extends BulletType{
	public AdaptedLightningBulletType(){
		damage = 1f;
		speed = 0f;
		lifetime = 1;
		shootEffect = hitEffect = despawnEffect = Fx.none;
		despawnHit = false;
		keepVelocity = false;
		hittable = false;
		//for stats
		status = StatusEffects.shocked;
	}
	
	@Override
	protected float calculateRange(){
		return (lightningLength + lightningLengthRand/2f) * 6f;
	}
	
	@Override
	public float estimateDPS(){
		return super.estimateDPS() * Math.max(lightningLength / 10f, 1);
	}
	
	@Override
	public void draw(Bullet b){
	}
	
	@Override
	public void init(Bullet b){
		Lightning.create(b, lightningColor, damage, b.x, b.y, b.rotation(), lightningLength + Mathf.random(lightningLengthRand));
	}
	
	
}
