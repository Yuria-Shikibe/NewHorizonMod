package newhorizon.expand.cutscene.actions;

import arc.Core;
import arc.math.Interp;
import arc.math.geom.Vec2;
import arc.util.pooling.Pool;
import mindustry.Vars;
import newhorizon.expand.cutscene.NHCSS_Action;
import newhorizon.util.annotation.HeadlessDisabled;

@HeadlessDisabled
public class CameraAction extends NHCSS_Action implements Pool.Poolable{
	public final Vec2 cameraTarget = new Vec2();
	public final Vec2 cameraSource = new Vec2();
	public float cameraTargetScl = -1;
	public Interp panInterp = Interp.smoother;
	
	public CameraAction(ActionBus bus){
		super(bus);
	}
	
	@Override
	public void setup(){
		super.setup();
		
		if(cameraTarget.isNaN()){
			cameraTarget.set(Core.camera.position);
		}
		
		cameraSource.set(Core.camera.position);
		if(cameraTargetScl < 0)cameraTargetScl = Vars.renderer.getScale();
		Vars.renderer.setScale(cameraTargetScl);
	}
	
	@Override
	public void update(){
		super.update();
		
		Core.camera.position.set(cameraSource.lerp(cameraTarget, cameraProgress()));
	}
	
	@Override
	public void act(){
		super.act();
		
//		CSSActions.cameraPool.free(this);
	}
	
	public float cameraProgress(){
		return panInterp.apply(progress());
	}
	
	/** Resets the object for reuse. Object references should be nulled and fields may be set to default values. */
	@Override
	public void reset(){
		cameraTarget.setZero();
		cameraSource.setZero();
		isChild = false;
		life = 0;
		cameraTargetScl = -1;
		panInterp = Interp.smoother;
	}
}
