package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraResetAction extends Action {
    @Override
    public String actionName() {
        return "camera_reset";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void act() {
        if (headless) return;

        Tmp.v1.set(Core.camera.position).lerpDelta(Vars.player, progress());
        control.input.logicCamSpeed = 1000f;
        control.input.logicCamPan = Tmp.v1;
    }
}
