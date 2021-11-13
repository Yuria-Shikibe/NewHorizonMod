package newhorizon.util.feature.cutscene.actions;

import arc.math.Interp;
import arc.scene.actions.TemporalAction;
import mindustry.Vars;
import newhorizon.util.feature.cutscene.UIActions;

public class CameraZoomAction extends TemporalAction{
	public float fromScl, toScl;
	
	public CameraZoomAction(){
		setInterpolation(Interp.smooth);
	}
	
	@Override
	protected void begin(){
		if(!UIActions.disabled())fromScl = Vars.renderer.getDisplayScale();
	}
	
	@Override
	protected void update(float percent){
		if(!UIActions.disabled())Vars.renderer.setScale(fromScl + (toScl - fromScl) * percent);
	}
}
