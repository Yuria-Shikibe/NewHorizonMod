package newhorizon.expand.cutscene.action;

import mindustry.Vars;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class InputLockAction extends Action {
    public InputLockAction() {
        super(0f);
    }

    @Override
    public void begin() {
        cutsceneUI.controlOverride = true;
        ui.hudfrag.shown = false;
        control.input.config.forceHide();
    }

    @Override
    public String phaseToString() {
        return "input_lock";
    }
}
