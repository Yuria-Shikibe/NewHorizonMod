package newhorizon.content;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.util.Log;
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
import newhorizon.expand.block.distribution.transport.LogisticsBlock;
import newhorizon.expand.entities.UltFire;
import newhorizon.util.func.NHPixmap;
import newhorizon.util.graphic.FloatPlatformDrawer;

import java.io.IOException;

import static mindustry.Vars.renderer;

public class NHContent extends Content{
	public static final float GRAVITY_TRAP_LAYER = Layer.light + 2.472f; // Making it wried
	public static final float XEN_LAYER = Layer.block - 0.003f;
	public static final float ARMOR_LAYER = Layer.block - 0.012f;

	public static Fi scheDir;
	
	public static Schematic mLoadout, nhBaseLoadout;
	
	public static Texture smoothNoise, particleNoise, darkerNoise, armorTex/*, platingNoise*/;
	
	public static CacheLayer quantumLayer, armorLayer/*, platingLayer*/;
	
	public static TextureRegion
			crossRegion, sourceCenter, timeIcon, xenIcon,
			iconLevel, ammoInfo, arrowRegion, pointerRegion, icon, icon2, upgrade, upgrade2,
			linkArrow, activeBoost;
	
	public static TextureRegion //UI
		raid, objective, fleet, capture;
	
	public static Attribute quantum;
	
	public static void loadPriority(){
		new NHContent().load();
	}
	
	public static void loadBeforeContentLoad(){
		CacheLayer.add(quantumLayer = new CacheLayer.ShaderLayer(NHShaders.quantum){});
		CacheLayer.add(armorLayer = new CacheLayer.ShaderLayer(NHShaders.tiler){
			@Override
			public void begin(){
				renderer.blocks.floor.endc();
				renderer.effectBuffer.begin();
				Core.graphics.clear(Color.clear);
				renderer.blocks.floor.beginc();
			}
			
			@Override
			public void end(){
				renderer.blocks.floor.endc();
				renderer.effectBuffer.end();
				
				NHShaders.tiler.texture = armorTex;
				renderer.effectBuffer.blit(shader);
				
				renderer.blocks.floor.beginc();
			}
		});
		
		quantum = Attribute.add("quantum");
	}
	
	public static void loadLast(){
	
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
		
		Icon.icons.put("midantha", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("midantha"))));
		Icon.icons.put("nh", new TextureRegionDrawable(Core.atlas.find(NewHorizon.name("icon-2"))));
		UltFire.load();

		scheDir = NewHorizon.MOD.root.child("schematics-bases");


		try{
			NHSchematic.load();

			mLoadout = Schematics.read(scheDir.child("init-loadout" + ".msch"));
			nhBaseLoadout = Schematics.readBase64("bXNjaAF4nI3QTQuCMBgH8L+VQhlEBnWtD7AYpXePHaIvEB2mPeSgtpiK0afPilCww3bYDs9vzxtm8PsYKHEjTA9U7bSRT632Wpx1WcA/U54aeS+kVgC8q0jomqN3PPWwUFSx7OtZqWTBZH2RwbwdUPQoc5ZqQ/V/H+8zcLBqk4soMjJxHIfRmtdPFPI1/8iRLe3DremyTU0mlW5kuPlKr1s/MUKlWUO3/Ec79f9TB0O4737H9snHtsmBifVwgf1yA9vlvgAW2YvD");
		}catch(IOException e){
			Log.info(e);
		}
		
		crossRegion = Core.atlas.find("cross");
		sourceCenter = Core.atlas.find(NewHorizon.name("source-center"));
		timeIcon = Core.atlas.find(NewHorizon.name("time-icon"));
		xenIcon = Core.atlas.find(NewHorizon.name("xen-icon"));
		upgrade = Core.atlas.find(NewHorizon.name("upgrade"));
		upgrade2 = Core.atlas.find(NewHorizon.name("upgrade2"));
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
		activeBoost = Core.atlas.find(NewHorizon.name("active-boost"));

		LogisticsBlock.load();
		FloatPlatformDrawer.load();
		
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
		
		armorTex = loadTex("armor", t -> {
			t.setFilter(Texture.TextureFilter.nearest);
			t.setWrap(Texture.TextureWrap.repeat);
		});

	}
	
	Texture loadTex(String name, Cons<Texture> modifier){
		Texture tex = new Texture(NewHorizon.MOD.root.child("textures").child(name + (name.endsWith(".png") ? "" : ".png")));
		modifier.get(tex);
		
		return tex;
	}
}
