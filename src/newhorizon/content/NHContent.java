package newhorizon.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import newhorizon.NewHorizon;
import newhorizon.feature.UpgradeData;

public class NHContent extends Content{
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2;
	
	public static void initLoad(){
		new NHContent().load();
	}
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	public void load(){
		if(Vars.headless)return;
		
		arrowRegion = Core.atlas.find(NewHorizon.name("jump-gate-arrow"));
		ammoInfo = Core.atlas.find(NewHorizon.name("upgrade-info"));
		iconLevel = Core.atlas.find(NewHorizon.name("level-up"));
		pointerRegion = Core.atlas.find(NewHorizon.name("jump-gate-pointer"));
		icon = Core.atlas.find(NewHorizon.name("icon-white"));
		icon2 = Core.atlas.find(NewHorizon.name("icon-2"));
		
		for(UpgradeData data : NHUpgradeDatas.all){
			data.load();
			data.init();
		}
	}
}
