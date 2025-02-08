package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.math.geom.Vec2;
import newhorizon.expand.cutscene.components.Action;

public class CameraControlAction extends Action {
    public Vec2 target;
    public CameraControlAction(float duration, float x, float y) {
        super(duration);
        target = new Vec2(x, y);
    }

    public CameraControlAction(String[] args) {
        super(Float.parseFloat(args[0]));
        target = new Vec2(Float.parseFloat(args[1]), Float.parseFloat(args[2]));
    }

    @Override
    public void act() {
        Core.camera.position.lerpDelta(target, progress());
    }
}
