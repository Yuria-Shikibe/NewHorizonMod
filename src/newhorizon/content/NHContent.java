package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.gen.Icon;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Layer;
import mindustry.world.meta.Attribute;
import newhorizon.NewHorizon;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHPixmap;

import java.io.IOException;

public class NHContent extends Content{
	public static final float GRAVITY_TRAP_LAYER = Layer.light + 2.472f; // Making it wried
	public static final float MATTER_STORM_LAYER = Layer.weather + 0.112f; // Making it wried
	public static final float EVENT_LAYER = Layer.weather + 5.12142f; // Making it wried
	
	public static Fi scheDir;
	
	public static Schematic mLoadout;
	
	public static Texture
			smoothNoise, particleNoise, darkerNoise
			
			
			;
	
	public static CacheLayer quantumLayer;
	
	public static TextureRegion
			crossRegion, sourceCenter,
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade,
			linkArrow;
	
	public static TextureRegion //UI
		raid, objective, fleet, capture;
	
	public static Attribute quantum;
	
	public static void loadPriority(){
		new NHContent().load();
	}
	
	public static void loadBeforeContentLoad(){
		CacheLayer.add(quantumLayer = new CacheLayer.ShaderLayer(NHShaders.quantum){});
		
		quantum = Attribute.add("quantum");
	}
	
	public static void loadLast(){
	
	}
	
	@Override
	public ContentType getContentType(){
		return ContentType.error;
	}
	
	public void process(){
//		NHPixmap.outLineAndAdd("sky-missile" + NHPixmap.PCD_SUFFIX, Core.atlas.find(NewHorizon.name("sky-missile")), NHPixmap.OUTLINE_COLOR, 4);
		NHPixmap.outLineAndAdd("ann-missile" + NHPixmap.PCD_SUFFIX, Core.atlas.find(NewHorizon.name("ann-missile")), NHPixmap.OUTLINE_COLOR, 4);
	}
	
	public void load(){
		if(Vars.headless)return;
		
		Icon.icons.put("midantha", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("midantha"))));
		UltFire.load();
//		process();
		
		scheDir = NewHorizon.MOD.root.child("schematics-bases");
		
		try{
			mLoadout = Schematics.read(scheDir.child("init-loadout" + ".msch"));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		crossRegion = Core.atlas.find("cross");
		sourceCenter = Core.atlas.find(NewHorizon.name("source-center"));
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
		capture = Core.atlas.find(NewHorizon.name("capture"));
		
		linkArrow = Core.atlas.find(NewHorizon.name("linked-arrow"));
		
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

	}
	
	Texture loadTex(String name, Cons<Texture> modifier){
		Texture tex = new Texture(NewHorizon.MOD.root.child("textures").child(name + (name.endsWith(".png") ? "" : ".png")));
		modifier.get(tex);
		
		return tex;
	}
}
