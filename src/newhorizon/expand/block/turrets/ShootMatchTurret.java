package newhorizon.expand.block.turrets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.type.Item;
import mindustry.world.blocks.defense.turrets.ItemTurret;

public class ShootMatchTurret extends ItemTurret{
	public ObjectMap<BulletType, ShootPattern> shooterMap = new ObjectMap<>();
	
	/**
	 * Should invoke after {@link ItemTurret#ammo(Object...)}
	 *
	 * */
	public void shooter(Object... objects){
		ObjectMap<Item, ShootPattern> mapper = ObjectMap.of(objects);
		
		for(ObjectMap.Entry<Item, BulletType> entry : ammoTypes.entries()){
			shooterMap.put(entry.value, mapper.get(entry.key, shoot));
		}
	}
	
	public ShootMatchTurret(String name){
		super(name);
	}
	
	public class ShootMatchTurretBuild extends ItemTurretBuild{
		@Override
		protected void shoot(BulletType type){
			float
					bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
					bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);
			
			ShootPattern shoot = shooterMap.get(type);
			
			if(shoot.firstShotDelay > 0){
				chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
				type.chargeEffect.at(bulletX, bulletY, rotation);
			}
			
			shoot.shoot(totalShots, (xOffset, yOffset, angle, delay, mover) -> {
				queuedBullets ++;
				if(delay > 0f){
					Time.run(delay, () -> bullet(type, xOffset, yOffset, angle, mover));
				}else{
					bullet(type, xOffset, yOffset, angle, mover);
				}
				totalShots ++;
			});
			
			if(consumeAmmoOnce){
				useAmmo();
			}
		}
	}
}
