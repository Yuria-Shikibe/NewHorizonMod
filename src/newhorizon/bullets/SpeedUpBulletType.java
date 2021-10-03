package newhorizon.bullets;

import arc.math.Interp;
import arc.struct.FloatSeq;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;

public class SpeedUpBulletType extends BasicBulletType{
	public float velocityBegin = -1;
	public float velocityIncrease = 0;
	public float accelerateBegin = 0.1f;
	public float accelerateEnd = 0.6f;
	
	public Interp func = Interp.linear;
	
	public void disableAccel(){
		accelerateBegin = 10;
	}
	
	public SpeedUpBulletType(){
		super();
	}
	
	public SpeedUpBulletType(float speed, float damage, String bulletSprite) {
		super(speed, damage, bulletSprite);
	}
	
	public SpeedUpBulletType(float speed, float damage) {
		this(speed, damage, "bullet");
	}
	
	@Override
	public void init(){
		super.init();
		
		if(accelerateBegin > 1)return;
		
		if(velocityBegin < 0)velocityBegin = speed;
		
		FloatSeq speeds = new FloatSeq();
		for(float i = 0; i < 1; i += 0.05f){
			speeds.add(velocityBegin + func.apply(i) * velocityIncrease);
		}
		speed = speeds.sum() / speeds.size;
	}
	
	@Override
	public void update(Bullet b){
		if(accelerateBegin < 1 && b.drag == 0)b.vel.setLength(velocityBegin + func.apply(b.fin()) * velocityIncrease);
		super.update(b);
	}
}
