package newhorizon.expand.block.commandable;

import arc.audio.Sound;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;

public class AirRaider extends CommandableAttackerBlock{
	public float shootSpread = 38f;
	
	public Effect shootEffect = Fx.none;
	public Effect smokeEffect = Fx.none;
	public Effect triggeredEffect = Fx.none;
	
	public Sound shootSound = Sounds.artillery;
	public float shake = 4f;
	
	protected int totalShots = 0;
	
	public float velocityRnd = 0.015f;
	public float inaccuracy = 3f;
	
	public float cooldownSpeed = 0.075f;
	
	protected Vec2 tr = new Vec2();
	
	public AirRaider(String name){
		super(name);
		
		reloadTime = 600f;
		range = 1000f;
		spread = 40f;
		prepareDelay = 90f;
		
		unloadable = false;
	}
	
	@Override
	public void setStats(){
		super.setStats();
	}
	
	public class AirRaiderBuild extends CommandableAttackerBlockBuild{
		@Override
		public void shoot(Vec2 target){
			super.shoot(target);
			
			shoot.shoot(totalShots, (xOffset, yOffset, angle, delay, mover) -> {
				if(delay > 0f){
					Time.run(delay, () -> bullet(xOffset, yOffset, angle, mover));
				}else{
					bullet(xOffset, yOffset, angle, mover);
				}
				totalShots++;
			});
		}
		
		protected void bullet(float xOffset, float yOffset, float angleOffset, Mover mover){
			if(!isValid())return;
			
			tr.setToRandomDirection().scl(shootSpread);
			Tmp.v5.setToRandomDirection().scl(spread).add(lastConfirmedTarget);
			
			float
					aimAngle = angleTo(lastConfirmedTarget),
					bulletX = x + Angles.trnsx(aimAngle, xOffset, yOffset) + Mathf.range(shootSpread),
					bulletY = y + Angles.trnsy(aimAngle, xOffset, yOffset) + Mathf.range(shootSpread),
					lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, Tmp.v5.x, Tmp.v5.y) / bullet.range) : 1f,
					angle = aimAngle + Mathf.range(inaccuracy);
			
			Bullet shootBullet = bullet.create(self(), team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, Tmp.v5.x, Tmp.v5.y);
			
			Effect.shake(shake, shake, self());
			
//			ejectEffect.at(x, y, angle * Mathf.sign(this.x));
			bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
			bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
			
			Effect fshootEffect = shootEffect == Fx.none ? bullet.shootEffect : shootEffect;
			Effect fsmokeEffect = smokeEffect == Fx.none ? bullet.smokeEffect : smokeEffect;
			
			fshootEffect.at(x + tr.x, y + tr.y, rotation);
			fsmokeEffect.at(x + tr.x, y + tr.y, rotation);
			shootSound.at(x + tr.x, y + tr.y, Mathf.random(0.9f, 1.1f));
		}
	}
}
