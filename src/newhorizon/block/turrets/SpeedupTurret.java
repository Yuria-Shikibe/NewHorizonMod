package newhorizon.block.turrets;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import newhorizon.content.NHColor;

public class SpeedupTurret extends PowerTurret{
	public float maxSpeedupScl = 0.5f;
	public float speedupPerShoot = 0.075f;
	public float slowDownReloadTime = 45f;
	
	public SpeedupTurret(String name){
		super(name);
	}
	
	@Override
	public void setBars(){
		super.setBars();
		bars.add("liquid", 
			(SpeedupTurretBuild entity) -> new Bar(
				() -> "Speed Up:",
				() -> NHColor.lightSky,
				() -> entity.speedupScl / maxSpeedupScl
			)
		);
	}
	
	public class SpeedupTurretBuild extends PowerTurretBuild{
		public float speedupScl = 0f;
		public float slowDownReload = 0f;
		
		
		@Override
		public void updateTile(){
			super.updateTile();
			if(slowDownReload >= 1f){
				slowDownReload -= Time.delta;
			}else speedupScl = Mathf.lerpDelta(speedupScl, 0f, 0.05f);
		}
		
		@Override
		protected void updateShooting(){
            if(reload >= reloadTime){
                BulletType type = peekAmmo();

                shoot(type);

                reload = 0f;
            }else{
                reload += (1 + speedupScl) * delta() * peekAmmo().reloadMultiplier * baseReloadSpeed();
            }
        }
				
		@Override
		protected void shoot(BulletType type){
			super.shoot(type);
			
			slowDownReload = slowDownReloadTime;
			if(speedupScl < maxSpeedupScl){
				speedupScl += speedupPerShoot;
			}else speedupScl = maxSpeedupScl;
		}
		
	}
}













