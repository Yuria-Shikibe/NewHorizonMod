package newhorizon.bullets;

import arc.math.Mathf;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import newhorizon.interfaces.Curve;

public class SpeedUpBulletType extends BasicBulletType{
	public float velocityEnd = -1;
	public float accelerateBegin = 0.1f;
	public float accelerateEnd = 0.6f;
	
	public Curve<Bullet> accelCurve;
	
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
		if(accelCurve == null)accelCurve = b -> Mathf.curve(b.fin(), accelerateBegin, accelerateEnd);
		if(!(velocityEnd <= speed)){
			speed = (speed + velocityEnd) / 2f;
		}
	}
	
	@Override
	public void update(Bullet b){
		if(!(velocityEnd <= speed))b.vel.setLength(2 * speed - velocityEnd + accelCurve.get(b) * velocityEnd);
		super.update(b);
	}
}
