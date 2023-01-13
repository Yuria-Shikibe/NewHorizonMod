package newhorizon.expand.bullets;

import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;

public class SpeedUpBulletType extends BasicBulletType{
	public float velocityBegin = -1;
	public float velocityIncrease = 0;
	public float accelerateBegin = 0.1f;
	public float accelerateEnd = 0.6f;
	
	public Interp accelInterp = Interp.linear;
	
	public void disableAccel(){
		accelerateBegin = 10;
	}
	
	public SpeedUpBulletType(){
		super();
	}
	
	public SpeedUpBulletType(float velocityBegin, float velocityIncrease, Interp accelInterp, float damage, String bulletSprite){
		super(1, damage, bulletSprite);
		this.velocityBegin = velocityBegin;
		this.velocityIncrease = velocityIncrease;
		this.accelInterp = accelInterp;
	}
	
	public SpeedUpBulletType(float speed, float damage, String bulletSprite) {
		super(speed, damage, bulletSprite);
	}
	
	public SpeedUpBulletType(float speed, float damage) {
		this(speed, damage, "bullet");
	}
	
	public SpeedUpBulletType(float damage, String bulletSprite){
		super(1, damage, bulletSprite);
	}
	
	@Override
	public void init(){
		super.init();
		
		if(accelerateBegin > 1)return;
		
		if(velocityBegin < 0)velocityBegin = speed;
		
		boolean computeRange = rangeOverride < 0;
		
		FloatSeq speeds = new FloatSeq();
		for(float i = 0; i < 1; i += 0.05f){
			float s = velocityBegin + accelInterp.apply(Mathf.curve(i, accelerateBegin, accelerateEnd)) * velocityIncrease;
			speeds.add(s);
			if(computeRange)range += s * lifetime * 0.05f;
		}
		speed = speeds.sum() / speeds.size;
		
		if(computeRange)range += 1;
		
		super.init();
	}
	
	@Override
	public void update(Bullet b){
		if(accelerateBegin < 1)b.vel.setLength((velocityBegin + accelInterp.apply(Mathf.curve(b.fin(), accelerateBegin, accelerateEnd)) * velocityIncrease) * (drag != 0 ? (1 * Mathf.pow(b.drag, b.fin() * b.lifetime() / 6)) : 1));
		super.update(b);
	}
}
