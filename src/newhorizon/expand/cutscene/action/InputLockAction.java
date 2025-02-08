package newhorizon.expand.cutscene.action;

import mindustry.Vars;
import newhorizon.expand.cutscene.components.Action;

import static newhorizon.NHVars.cutsceneUI;

public class InputLockAction extends Action {
    public InputLockAction() {
        super(0f);
    }

    @Override
    public void begin() {
        cutsceneUI.controlOverride = true;
        Vars.control.input.config.forceHide();
    }

    @Override
    public String phaseToString() {
        return "input_lock";
    }
}
