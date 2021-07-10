package newhorizon.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import newhorizon.NewHorizon;
import newhorizon.feature.UpgradeData;

public class NHContent extends Content{
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion;
	
	public static void initLoad(){
		new NHContent().load();
	}
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	public void load(){
		arrowRegion = Core.atlas.find(NewHorizon.configName("jump-gate-arrow"));
		ammoInfo = Core.atlas.find(NewHorizon.configName("upgrade-info"));
		iconLevel = Core.atlas.find(NewHorizon.configName("level-up"));
		pointerRegion = Core.atlas.find(NewHorizon.configName("jump-gate-pointer"));
		
		for(UpgradeData data : NHUpgradeDatas.all){
			data.load();
			data.init();
		}
	}
}
