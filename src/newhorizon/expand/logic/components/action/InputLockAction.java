package newhorizon.expand.logic.components.action;

import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InputLockAction extends Action {
    @Override
    public String actionName() {
        return "input_lock";
    }

    @Override
    public void begin() {
        if (headless) return;

        cutsceneUI.controlOverride = true;
        control.input.config.forceHide();
    }
}
