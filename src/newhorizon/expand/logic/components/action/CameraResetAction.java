package newhorizon.expand.logic.components.action;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static mindustry.Vars.player;

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

        Tmp.v1.set(Core.camera.position).lerpDelta(player, progress());
        control.input.logicCamSpeed = 1000f;
        control.input.logicCamPan.set(Tmp.v1);
    }

    @Override
    public void end() {
        skip();
    }

    @Override
    public void skip() {
        control.input.logicCutscene = false;
    }
}
