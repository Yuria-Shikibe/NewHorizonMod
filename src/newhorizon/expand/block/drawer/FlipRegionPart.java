package newhorizon.expand.block.drawer;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.RegionPart;

public class FlipRegionPart extends RegionPart{
	
	public FlipRegionPart(String region){
		super(region);
	}
	
	@Override
	public void load(String name){
		String realName = this.name == null ? name + suffix : this.name;
		
		if(drawRegion){
			regions = new TextureRegion[]{Core.atlas.find(realName)};
			outlines = new TextureRegion[]{Core.atlas.find(realName + "-outline")};
		}
		
		heat = Core.atlas.find(realName + "-heat");
		for(DrawPart child : children){
			child.turretShading = turretShading;
			child.load(name);
		}
	}
}
