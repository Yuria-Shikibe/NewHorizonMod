package newhorizon.feature.cutscene.actions;

import arc.Core;
import arc.math.geom.Position;

public class CameraTrackerAction extends CameraMoveAction{
	public Position trackTarget;
	
	@Override
	protected void update(float percent){
		Core.camera.position.lerp(trackTarget.getX(), trackTarget.getY(), 0.075f);
	}
}
