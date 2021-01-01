package newhorizon.bullets;

import arc.func.Cons;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.entities.bullet.BulletType;
import newhorizon.func.PosLightning;

public class NHLightningBoltBulletType extends BulletType {
	public Cons<Position> hitAct = target -> {
		//Extra hit settings for bullets
	};
	
	public float lightningWidth = PosLightning.WIDTH;
	public int boltNum = 2;
	
	public NHLightningBoltBulletType(float damage){
		super(damage, 0.0001f);
		instantDisappear = true;
		scaleVelocity = true;
	}
	
	public NHLightningBoltBulletType(){
        this(1f);
    }

	@Override
	public void init(Bullet b){
        PosLightning.create(b, new Vec2().trns(b.rotation(), speed * b.lifetime()).add(b.x, b.y), b.team(), lightningColor, lightningWidth, boltNum, hitAct);
        super.init(b);
	}
	
	@Override
	public void hit(Bullet b){
		Tmp.v1.trns(b.rotation(), speed * b.lifetime()).add(b.x, b.y);
		hit(b, Tmp.v1.x, Tmp.v1.y);
	}
}














