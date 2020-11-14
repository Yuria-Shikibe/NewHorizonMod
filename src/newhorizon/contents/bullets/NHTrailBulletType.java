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
	public int trailLength = 5;
	public float trailWidth = 3f;

	public NHTrailBulletType(float speed, float damage, String bulletSprite){
		super(speed, damage, bulletSprite);
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
		hitSound.at(b); 
		Effect.shake(despawnShake, despawnShake, b);
		hit(b);
	}
	
	@Override
	public void init(Bullet b) {
		super.init(b);
		b.data(new EffectTrail(14, 4.2f));
		EffectTrail t = (EffectTrail)b.data;
		t.clear();
	}

	@Override
	public void hit(Bullet b) {
		super.hit(b);
		if (!(b.data instanceof EffectTrail))return;
		EffectTrail t = (EffectTrail)b.data;
		t.disappear(trailColor);
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

		trail.length = (int)Mathf.floor(14 / Time.delta * 1.65f);
		trail.update(b.x, b.y, true);

		super.update(b);
	}
}














