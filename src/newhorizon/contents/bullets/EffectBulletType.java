package newhorizon.contents.bullets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import newhorizon.NewHorizon;
import newhorizon.contents.effects.EffectTrail;

import static mindustry.Vars.tilesize;

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














