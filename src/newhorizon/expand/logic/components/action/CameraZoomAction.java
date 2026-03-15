package newhorizon.expand.logic.components.action;

import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;

public class CameraZoomAction extends Action {
    public float zoom = 1f;

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        zoom = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;

        control.input.logicCutsceneZoom = zoom;
    }
}
