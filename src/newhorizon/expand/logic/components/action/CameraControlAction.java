package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraControlAction extends Action {
    public Vec2 target;

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        target = new Vec2(ParseUtil.getNextFloat(tokens), ParseUtil.getNextFloat(tokens));
    }

    @Override
    public void act() {
        if (headless) return;

        Tmp.v1.set(Core.camera.position).lerpDelta(target, progress());
        control.input.logicCamSpeed = 10f;
        control.input.logicCamPan = Tmp.v1;
    }
}
