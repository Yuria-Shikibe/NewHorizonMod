package newhorizon.feature;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.TextureAtlas;
import mindustry.graphics.MultiPacker;
import mindustry.type.StatusEffect;

public class NHStatusEffect extends StatusEffect{
	public NHStatusEffect(String name){
		super(name);
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		if(fullIcon != null && fullIcon.found() && fullIcon instanceof TextureAtlas.AtlasRegion){
			packer.add(MultiPacker.PageType.main, name + "-full", Pixmaps.outline(Core.atlas.getPixmap(fullIcon), Color.valueOf("404049"), 3));
		}
	}
}
