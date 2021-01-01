package newhorizon.bullets;

import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;

public class EffectBulletType extends BulletType {

	public EffectBulletType(float lifetime){
		super();
		this.lifetime = lifetime;
		despawnEffect = hitEffect = shootEffect = smokeEffect = trailEffect = Fx.none;
		absorbable = collides = collidesAir = collidesGround = collidesTeam = collidesTiles = false;
		hitSize = 0;
		speed = 0.1f;
		drag = 1f;
		drawSize = 120f;
    }
}














