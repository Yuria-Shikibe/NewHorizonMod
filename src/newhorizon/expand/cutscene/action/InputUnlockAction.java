package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class InputUnlockAction extends Action {
    public InputUnlockAction() {
        super(0f);
    }

    @Override
    public void end() {
        cutsceneUI.controlOverride = false;
        ui.hudfrag.shown = true;
    }

    @Override
    public void skip(){
        end();
    }

    @Override
    public String phaseToString() {
        return "input_unlock";
    }
}
