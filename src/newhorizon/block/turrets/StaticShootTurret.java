package newhorizon.block.turrets;

import mindustry.world.blocks.defense.turrets.ItemTurret;

public class StaticShootTurret extends ItemTurret{
	public StaticShootTurret(String name){
		super(name);
	}
	
	public class StaticShootTurretBuild extends ItemTurretBuild{
		@Override
		public boolean shouldTurn() {
			return !this.charging && !this.isShooting();
		}
	}
}
