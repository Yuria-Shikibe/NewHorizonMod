package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraZoomAction extends Action {
    public float zoom = 1f;
    public CameraZoomAction(String[] tokens) {
        super(60);
        zoom = Float.parseFloat(tokens[0]);
    }

    public CameraZoomAction() {
        super(0);
    }

    @Override
    public void begin() {
        if (headless) return;
        control.input.logicCutsceneZoom = zoom;
    }
}
