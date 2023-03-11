package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Drawc;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.block.defence.GravityWell;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.graphic.EffectDrawer;
import newhorizon.util.graphic.TextureStretchIn;

import static arc.Core.graphics;
import static mindustry.Vars.control;

public class NHRenderer implements Disposable{
	public static float width, height;
	public FrameBuffer mask, effect;
	public EffectDrawer effectDrawer;
	public TextureStretchIn textureStretchIn;
	
	public Rect viewport = new Rect();
	
	public Vec2 tmp = new Vec2(1, 0);
	
//	public Texture matterStorm = new Texture(1920, 1080);
	
	public NHRenderer(){
		mask = effect = new FrameBuffer();
		
		effectDrawer = EffectDrawer.drawer;
		textureStretchIn = new TextureStretchIn();
		textureStretchIn.load();
	}
	
	public void init(){
		mask.dispose();
		mask = null;
		mask = new FrameBuffer();
		
		effect.dispose();
		effect = null;
		effect = new FrameBuffer();
		
		textureStretchIn.clear();
	}
	
	public void draw(){
		Core.camera.bounds(viewport);
		width = graphics.getWidth();
		height = graphics.getHeight();
		
		drawGravityTrapField();
		
//		effect.resize(graphics.getWidth(), graphics.getHeight());
//
//		effect.begin(Color.clear);
//		NHShaders.matterStorm.primaryColor.set(Color.white);
//		NHShaders.matterStorm.applyDirection(tmp, 8f);
//		NHShaders.matterStorm.secondaryColor.set(Tmp.c1.set(Color.white).lerp(Color.white, Mathf.absin(8f, 0.4f)));
//		effect.end();
//
//		matterStorm = effect.getTexture();
//		matterStorm.bind(0);
//		NHShaders.matterStorm.bind();
//		NHShaders.matterStorm.apply();
//
//		matterStorm.setFilter(Texture.TextureFilter.linear);
//		matterStorm.setWrap(Texture.TextureWrap.repeat);
	}
	
	public void afterDraw(){
	}
	
	public void drawGravityTrapField(){
		mask.resize(graphics.getWidth(), graphics.getHeight());
		Building building = Vars.control.input.config.getSelected();
		if(/*NHSetting.alwaysShowGravityTrapFields ||*/control.input.block instanceof GravityWell || (building != null && (building.block instanceof GravityWell || building.block instanceof HyperSpaceWarper))){
			Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
				mask.begin(Color.clear);
				GravityTrapField.drawAll();
				mask.end();
				mask.blit(NHShaders.gravityTrapShader);
			});
		}
	}
	
	public void getTexture(Drawc drawc){
		Draw.proj(Core.camera.mat);
		drawc.draw();
	}
	
	/** Releases all resources of this object. */
	@Override
	public void dispose(){
		mask.dispose();
		effect.dispose();
		effectDrawer.dispose();
//		matterStorm.dispose();
		textureStretchIn.dispose();
	}
}
