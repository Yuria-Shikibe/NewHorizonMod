package newhorizon.feature;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.TextureAtlas;
import mindustry.graphics.MultiPacker;
import mindustry.type.StatusEffect;
import newhorizon.func.NHUnitOutline;

public class NHStatusEffect extends StatusEffect{
	public Color textureColor = null;
	
	public NHStatusEffect(String name){
		super(name);
	}
	
	@Override
	public void createIcons(MultiPacker packer){
		if((fullIcon != null && fullIcon.found() && fullIcon instanceof TextureAtlas.AtlasRegion)){
			if(textureColor != null){
				packer.add(MultiPacker.PageType.main, name + "-full", NHUnitOutline.fillColor(Core.atlas.getPixmap(fullIcon), textureColor).outline(Color.valueOf("404049"), 3));
			}else{
				packer.add(MultiPacker.PageType.main, name + "-full", Pixmaps.outline(Core.atlas.getPixmap(fullIcon), Color.valueOf("404049"), 3));
			}
		}
	}
}
