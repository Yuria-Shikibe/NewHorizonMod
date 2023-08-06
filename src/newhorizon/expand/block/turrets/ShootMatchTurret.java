package newhorizon.expand.block.turrets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.type.Item;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class ShootMatchTurret extends ItemTurret{
	public float lifeRnd = 0;
	public IntMap<ShootPattern> shooterMap = new IntMap<>();
	
	/**
	 * Should invoke after {@link ItemTurret#ammo(Object...)}
	 *
	 * */
	public void shooter(Object... objects){
		ObjectMap<Item, ShootPattern> mapper = ObjectMap.of(objects);
		
		for(ObjectMap.Entry<Item, BulletType> entry : ammoTypes.entries()){
			shooterMap.put(entry.value.id, mapper.get(entry.key, shoot));
		}
	}
	
	public ShootMatchTurret(String name){
		super(name);
	}
	
	public class ShootMatchTurretBuild extends ItemTurretBuild{
		public ShootPattern getShooter(BulletType type){
			ShootPattern s = shooterMap.get(type.id);
			return s == null ? shoot : s;
		}
		
		@Override
		protected void shoot(BulletType type){
			float
					bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
					bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);
			
			ShootPattern shoot = getShooter(type);
			
			if(shoot.firstShotDelay > 0){
				chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
				type.chargeEffect.at(bulletX, bulletY, rotation);
			}
			
			shoot.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
				queuedBullets++;
				if(delay > 0f){
					Time.run(delay, () -> bullet(type, xOffset, yOffset, angle, mover));
				}else{
					bullet(type, xOffset, yOffset, angle, mover);
				}
			}, () -> barrelCounter++);
			
			if(consumeAmmoOnce){
				useAmmo();
			}
		}
		
		protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
			queuedBullets --;
			
			if(dead || (!consumeAmmoOnce && !hasAmmo())) return;
			
			float
					xSpread = Mathf.range(xRand),
					bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
					bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
					shootAngle = rotation + angleOffset + Mathf.range(inaccuracy + type.inaccuracy);
			
			float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y) / type.range, minRange / type.range, range() / type.range) : 1f;
			
			if(lifeRnd > 0)lifeScl += Mathf.range(lifeRnd);
			
			//TODO aimX / aimY for multi shot turrets?
			handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);
			
			(shootEffect == null ? type.shootEffect : shootEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
			(smokeEffect == null ? type.smokeEffect : smokeEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
			shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
			
			ammoUseEffect.at(
					x - Angles.trnsx(rotation, ammoEjectBack),
					y - Angles.trnsy(rotation, ammoEjectBack),
					rotation * Mathf.sign(xOffset)
			);
			
			if(shake > 0){
				Effect.shake(shake, shake, this);
			}
			
			curRecoil = 1f;
			if(recoils > 0){
				curRecoils[barrelCounter % recoils] = 1f;
			}
			heat = 1f;
			totalShots++;
			
			if(!consumeAmmoOnce){
				useAmmo();
			}
		}
	}
}
