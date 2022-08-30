package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Drawc;
import mindustry.graphics.Shaders;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.block.defence.GravityTrap;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.entities.GravityTrapField;

import static arc.Core.graphics;
import static mindustry.Vars.control;

public class NHRenderer{
	public FrameBuffer mask, effect;
	
	public NHRenderer(){
		mask = effect = new FrameBuffer();
	}
	
	public void init(){
		mask.dispose();
		mask = null;
		mask = new FrameBuffer();
		
		effect.dispose();
		effect = null;
		effect = new FrameBuffer();
	}
	
	public void draw(){
		mask.resize(graphics.getWidth(), graphics.getHeight());
		
		drawGravityTrapField();
	}
	
	public void afterDraw(){
	}
	
	public void drawGravityTrapField(){
		Building building = Vars.control.input.config.getSelected();
		if(/*NHSetting.alwaysShowGravityTrapFields ||*/control.input.block instanceof GravityTrap || (building != null && (building.block instanceof GravityTrap || building.block instanceof HyperSpaceWarper))){
			Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
				mask.begin(Color.clear);
				GravityTrapField.drawAll();
				mask.end();
				mask.blit(NHShaders.gravityTrapShader);
				
				effect.begin(Color.clear);
				mask.blit(Shaders.screenspace);
				effect.end();
				effect.blit(NHShaders.outliner);
			});
		}
	}
	
	public void getTexture(Drawc drawc){
		Draw.proj(Core.camera.mat);
		drawc.draw();
	}
}
