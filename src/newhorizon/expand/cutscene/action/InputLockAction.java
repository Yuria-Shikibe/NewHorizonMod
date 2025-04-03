package newhorizon.expand.cutscene.action;

import arc.Core;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InputLockAction extends Action {
    public InputLockAction() {
        super(0f);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.controlOverride = true;
        control.input.logicCamPan = Core.camera.position;
        control.input.logicCutscene = true;
        control.input.config.forceHide();
    }

    @Override
    public String phaseToString() {
        return "input_lock";
    }
}
