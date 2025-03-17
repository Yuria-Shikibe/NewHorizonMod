package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.math.geom.Vec2;
import mindustry.gen.Building;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

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

    public CameraSetAction(String[] tokens, Building source) {
        super(0);
        target = new Vec2(ActionControl.parseFloat(tokens[0], source), ActionControl.parseFloat(tokens[1], source));
    }

    @Override
    public void begin() {
        if (headless) return;
        control.input.logicCamSpeed = 100f;
        control.input.logicCamPan = target;
    }
}
