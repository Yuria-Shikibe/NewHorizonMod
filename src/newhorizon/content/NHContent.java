package newhorizon.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.graphics.CacheLayer;
import newhorizon.NewHorizon;
import newhorizon.util.feature.UpgradeData;

public class NHContent extends Content{
	public static CacheLayer
			quantum;
	
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2;
	
	public static TextureRegion //UI
		raid, objective, fleet;
	
	public static void loadModContent(){
		CacheLayer.add(quantum = new CacheLayer.ShaderLayer(NHShaders.quantum){
//			@Override
//			public void end(){
//				super.end();
//				Draw.flush();
//				Vars.renderer.blocks.floor.beginDraw();
//			}
		});
		
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
		fleet = Core.atlas.find(NewHorizon.name("fleet"));
		
		NHUpgradeDatas.all.each(UpgradeData::load);
		NHUpgradeDatas.all.each(UpgradeData::init);
	}
}
