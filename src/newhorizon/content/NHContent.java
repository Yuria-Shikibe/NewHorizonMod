package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Texture;
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
	
	public static Texture
			smoothNoise, particleNoise, darkerNoise
			
			
			;
	
	public static CacheLayer
			quantum;
	
	public static TextureRegion
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade;
	
	public static TextureRegion //UI
		raid, objective, fleet;
	
	public static void loadModContent(){
		
		
		new NHContent().load();
	}
	
	public static void loadModContentLater(){
		CacheLayer.add(quantum = new CacheLayer.ShaderLayer(NHShaders.quantum){
			//			@Override
			//			public void end(){
			//				super.end();
			//				Draw.flush();
			//				Vars.renderer.blocks.floor.beginDraw();
			//			}
		});
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
		
		
		smoothNoise = loadTex("smooth-noise", t -> {
			t.setFilter(Texture.TextureFilter.linear);
			t.setWrap(Texture.TextureWrap.repeat);
		});
		
		particleNoise = loadTex("particle-noise", t -> {
			t.setFilter(Texture.TextureFilter.linear);
			t.setWrap(Texture.TextureWrap.repeat);
		});
		
		darkerNoise = loadTex("darker-noise", t -> {
			t.setFilter(Texture.TextureFilter.linear);
			t.setWrap(Texture.TextureWrap.repeat);
		});
//.png
//		NHLoader.loadSprite("tex");
		
		NHUpgradeDatas.all.each(UpgradeData::load);
		NHUpgradeDatas.all.each(UpgradeData::init);
	}
	
	Texture loadTex(String name, Cons<Texture> modifier){
		Texture tex = new Texture(NewHorizon.MOD.root.child("textures").child(name + (name.endsWith(".png") ? "" : ".png")));
		modifier.get(tex);
		
		
		return tex;
	}
}
