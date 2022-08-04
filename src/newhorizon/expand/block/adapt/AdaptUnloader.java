package newhorizon.expand.block.adapt;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import mindustry.world.blocks.storage.Unloader;

public class AdaptUnloader extends Unloader{
	public AdaptUnloader(String name){
		super(name);
	}
	
	public class AdaptUnloaderBuild extends UnloaderBuild{
		@Override
		public void draw(){
			Draw.rect(region, x, y, block.rotate ? rotdeg() : 0.0F);
			drawTeamTop();
			Draw.color(sortItem == null ? Color.clear : sortItem.color);
			Draw.rect(name + "-center", x, y);
			Draw.color();
		}
	}
}
