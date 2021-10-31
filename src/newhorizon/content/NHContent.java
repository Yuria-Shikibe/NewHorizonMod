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
	
	public static TextureRegion //UI
		raid, objective;
	
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
		
		raid = Core.atlas.find(NewHorizon.name("raid"));
		objective = Core.atlas.find(NewHorizon.name("objective"));
		
		NHUpgradeDatas.all.each(UpgradeData::load);
		NHUpgradeDatas.all.each(UpgradeData::init);
	}
}
