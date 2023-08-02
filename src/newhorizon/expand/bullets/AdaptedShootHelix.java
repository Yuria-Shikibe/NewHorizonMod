package newhorizon.expand.bullets;

import arc.math.Mathf;
import arc.util.Nullable;
import mindustry.entities.pattern.ShootHelix;

public class AdaptedShootHelix extends ShootHelix{
	public boolean flip = false;
	public float rotSpeedOffset = 0;
	public float rotSpeedBegin = 1;
	
	@Override
	public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
		for(int i = 0; i < shots; i++){
			if(flip){
				for(int sign : Mathf.signs){
					int finalI = i;
					handler.shoot(0, 0, 0, firstShotDelay + shotDelay * i,
							b -> b.moveRelative(0f, Mathf.sin(b.time * (rotSpeedOffset * finalI + rotSpeedBegin) + offset * ((float)finalI / shots), scl, mag * sign)));
				}
			}else{
				int finalI = i;
				handler.shoot(0, 0, 0, firstShotDelay + shotDelay * i,
						b -> b.moveRelative(0f, Mathf.sin(b.time * (rotSpeedOffset * finalI + rotSpeedBegin) + offset * ((float)finalI / shots), scl, mag)));
			}
		}
	}
}
