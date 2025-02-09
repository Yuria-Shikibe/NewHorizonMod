package newhorizon.expand.cutscene.action;

import arc.Core;
import arc.util.Time;
import mindustry.Vars;
import newhorizon.expand.cutscene.components.Action;

public class CameraResetAction extends Action {
    public CameraResetAction(float duration) {
        super(duration * Time.toSeconds);
    }

    public CameraResetAction(String[] args) {
        super(Float.parseFloat(args[0]) * Time.toSeconds);
    }

    @Override
    public void act() {
        Core.camera.position.lerpDelta(Vars.player, progress());
    }

    @Override
    public String phaseToString() {
        return "camera_reset";
    }
}
