package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraResetAction extends Action {
    public CameraResetAction(float duration) {
        super(duration * Time.toSeconds);
    }

    public CameraResetAction(String[] args) {
        super(Float.parseFloat(args[0]) * Time.toSeconds);
    }

    @Override
    public void act() {
        if (headless) return;
        Tmp.v1.set(Core.camera.position).lerpDelta(Vars.player, progress());
        control.input.logicCamSpeed = 1000f;
        control.input.logicCamPan = Tmp.v1;
    }

    @Override
    public String phaseToString() {
        return "camera_reset";
    }
}
