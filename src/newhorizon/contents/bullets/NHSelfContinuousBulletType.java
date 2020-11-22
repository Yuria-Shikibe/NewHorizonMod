package mindustry.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class NHSelfContinuousBulletType extends ContinuousLaserBulletType{
	public float continuousTime = 60f;
    
    //Buildings Only
    
    public NHSelfContinuousBulletType(float damage){
        super(0.001f, damage);
        despawnEffect = Fx.none;
        hitSize = 4;
        drawSize = 420f;
        lifetime = 20f;
        keepVelocity = false;
        collides = false;
        pierce = true;
        hittable = false;
        absorbable = false;
    }

    protected NHSelfContinuousBulletType(){
        this(0);
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, length*2f);
    }

	@Override
    public void init(Bullet b){
        super.init(b);
        b.data(0f);
    }

    @Override
	public void update(Bullet b){
		if(!(b.data instanceof Float))return;
		Float data = (Float)b.data();
		if(b.owner instanceof Building){
			Building owner = b.owner;
			Tmp.v1.trns(owner.rotation, owner.block.size * tilesize / 2f);
			b.rotation(owner.rotation);
			b.set(owner.x + Tmp.v1.x, owner.y + Tmp.v1.y);
			
			float conTime = data.floatValue();
			if(conTime < continuousTime){
				b.time(0);
				b.data(conTime + Time.delta);
			}
		}else b.time(b.lifetime);
        //damage every 5 ticks
        super.update(b);
    }

}
