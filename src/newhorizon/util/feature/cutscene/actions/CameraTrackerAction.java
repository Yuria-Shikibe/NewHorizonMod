package newhorizon.util.feature.cutscene.actions;

import arc.Core;
import arc.math.geom.Position;
import newhorizon.util.feature.cutscene.UIActions;

public class CameraTrackerAction extends CameraMoveAction{
	public Position trackTarget;
	
	@Override
	protected void update(float percent){
		if(!UIActions.disabled())Core.camera.position.lerp(trackTarget.getX(), trackTarget.getY(), 0.075f);
	}
}
