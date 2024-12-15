package newhorizon;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.math.geom.Rect;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Shaders;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;
import newhorizon.expand.block.defence.GravityWell;
import newhorizon.expand.block.defence.HyperSpaceWarper;
import newhorizon.expand.cutscene.NHCSS_Core;
import newhorizon.expand.entities.GravityTrapField;
import newhorizon.util.graphic.StatusRenderer;

import static arc.Core.graphics;
import static mindustry.Vars.control;
import static mindustry.Vars.renderer;

public class NHRenderer implements Disposable{
	public static float width, height;
	public FrameBuffer mask;
	public StatusRenderer statusRenderer;
	public Rect viewport = new Rect();

	public NHRenderer(){
		mask = new FrameBuffer();
		statusRenderer = new StatusRenderer();
	}
	
	public void init(){
		mask.dispose();
		mask = null;
		mask = new FrameBuffer();
	}
	
	public void draw(){
		Core.camera.bounds(viewport);
		width = graphics.getWidth();
		height = graphics.getHeight();
		NHCSS_Core.core.draw();
		
		drawGravityTrapField();
		
		
		
		statusRenderer.draw();

		renderer.effectBuffer.resize(graphics.getWidth(), graphics.getHeight());
		if(Vars.renderer.animateShields && Shaders.shield != null){
			Draw.drawRange(NHContent.XEN_LAYER, 0.0001f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
				renderer.effectBuffer.end();
				renderer.effectBuffer.blit(NHShaders.quantum);
			});
		}
	}
	
	public void drawGravityTrapField(){
		mask.resize(graphics.getWidth(), graphics.getHeight());
		Building building = Vars.control.input.config.getSelected();
		if(control.input.block instanceof GravityWell || (building != null && (building.block instanceof GravityWell || building.block instanceof HyperSpaceWarper))){
			Draw.draw(NHContent.GRAVITY_TRAP_LAYER, () -> {
				mask.begin(Color.clear);
				GravityTrapField.drawAll();
				mask.end();
				mask.blit(NHShaders.gravityTrapShader);
			});
		}
	}

	/** Releases all resources of this object. */
	@Override
	public void dispose(){
		mask.dispose();
	}
}
