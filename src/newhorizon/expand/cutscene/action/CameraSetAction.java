package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.math.geom.Vec2;
import newhorizon.expand.cutscene.components.Action;

public class CameraSetAction extends Action {
    public Vec2 target;
    public CameraSetAction(float x, float y) {
        super(0);
        target = new Vec2(x, y);
    }

    public CameraSetAction(String[] args) {
        super(0);
        target = new Vec2(Float.parseFloat(args[0]), Float.parseFloat(args[1]));
    }

    @Override
    public void begin() {
        Core.camera.position.lerp(target, 1);
    }
}
