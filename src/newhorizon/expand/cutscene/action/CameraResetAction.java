package newhorizon.expand.cutscene.action;

import arc.Core;
import mindustry.Vars;
import newhorizon.expand.cutscene.components.Action;

public class CameraResetAction extends Action {
    public CameraResetAction(float duration) {
        super(duration);
    }

    @Override
    public void act() {
        Core.camera.position.lerpDelta(Vars.player, progress());
    }
}
