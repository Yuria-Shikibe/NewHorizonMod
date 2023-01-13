package newhorizon.expand.bullets;

import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.type.UnitType;

public class MissileSpawner extends BulletType{
	public MissileSpawner(UnitType type){
			shootEffect = Fx.none;
			smokeEffect = Fx.none;
			hitShake = 1.0F;
			speed = 0.0F;
			keepVelocity = false;
			spawnUnit = type;
			instantDisappear = true;
			hitSound = despawnSound = Sounds.none;
	}
}
