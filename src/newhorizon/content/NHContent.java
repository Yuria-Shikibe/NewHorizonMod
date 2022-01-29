package newhorizon.content;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.feature.UpgradeData;
import newhorizon.util.func.NHPixmap;

public class NHContent extends Content{
	public static final float GRAVITY_TRAP_LAYER = Layer.light + 2.472f; // Making it wried
	public static final float MATTER_STORM_LAYER = Layer.weather + 0.112f; // Making it wried
	
	public static CacheLayer
			quantum;
	
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade;
	
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
	
	public void process(){
		NHPixmap.outLineAndAdd("ann-missile" + NHPixmap.PCD_SUFFIX, Core.atlas.find(NewHorizon.name("ann-missile")), NHPixmap.OUTLINE_COLOR, 4);
	}
	
	public void load(){
		if(Vars.headless)return;
		
		UltFire.load();
		if(NHPixmap.isDebugging())process();
		
		upgrade = Core.atlas.find(NewHorizon.name("upgrade"));
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
