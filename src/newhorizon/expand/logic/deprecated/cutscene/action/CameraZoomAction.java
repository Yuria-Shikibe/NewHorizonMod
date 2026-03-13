package newhorizon.expand.logic.deprecated.cutscene.action;

import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
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
