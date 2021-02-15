package newhorizon.block.special;

import arc.graphics.g2d.TextureRegion;
import mindustry.entities.bullet.BulletType;
import mindustry.world.blocks.storage.CoreBlock;

public class DefenceCoreBlock extends CoreBlock{
	public float reloadTime;
	public float range;
	public int shots;
	public float inaccuracy;
	public float rotateSpeed = 5f;
	
	public BulletType shootType;
	
	public TextureRegion turretRegion;
	public TextureRegion heatRegion;
	
	public DefenceCoreBlock(String name){
		super(name);
		//outlineIcon = true;
	}
	
	@Override
	public void load(){
		super.load();
		//this.turretRegion = Core.atlas.find(this.name + "-turret");
		//this.heatRegion = Core.atlas.find(this.name + "-heat");
	}
	
	/*@Override
	protected TextureRegion[] icons() {
		return this.teamRegion.found() && this.minfo.mod == null ? new TextureRegion[]{this.region, this.teamRegions[Team.sharded.id], turretRegion} : new TextureRegion[]{this.region, turretRegion};
	}*/
	
	
	public class DefenceCoreBuild extends CoreBuild{
	
	}
	
}
