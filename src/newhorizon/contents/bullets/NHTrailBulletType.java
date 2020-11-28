package newhorizon.contents.bullets;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.entities.*;
import mindustry.entities.bullet.BasicBulletType;
import newhorizon.contents.effects.EffectTrail;

public class NHTrailBulletType extends BasicBulletType {
	public int trailLength = 12;
	public float trailWidth = 3.7f;
	public float trailDrawsize = 200f;

	public NHTrailBulletType(float speed, float damage, String bulletSprite){
		super(speed, damage, bulletSprite);
		this.trailDrawsize = Math.max(trailDrawsize, 2.5f * trailLength * speed);
    }
    
	public NHTrailBulletType(float speed, float damage){
		this(speed, damage, "bullet");
    }

	public NHTrailBulletType(){
		this(1f, 1f, "bullet");
    }
    
    @Override
	public void despawned(Bullet b){
		despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
		Effect.shake(despawnShake, despawnShake, b);
		hit(b);
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		b.data(new EffectTrail(trailLength, trailWidth, trailDrawsize));
		EffectTrail t = (EffectTrail)b.data;
		t.clear();
	}

	@Override
	public void hit(Bullet b) {
		super.hit(b);
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail t = (EffectTrail)b.data;
		t.disappear(trailColor, trailWidth);
	}

	@Override
	public void draw(Bullet b) {
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail t = (EffectTrail)b.data;
		t.draw(trailColor);
		super.draw(b);
	}

	@Override
	public void update(Bullet b) {
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail trail = (EffectTrail)b.data;
		if(b.timer(0, Time.delta))trail.update(b.x, b.y, false);
		super.update(b);
	}
}














