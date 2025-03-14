package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class InputUnlockAction extends Action {
    public InputUnlockAction() {
        super(0f);
    }

    @Override
    public void end() {
        if (headless) return;
        cutsceneUI.controlOverride = false;
        ui.hudfrag.shown = true;
    }

    @Override
    public void skip(){
        if (headless) return;
        end();
    }

    @Override
    public String phaseToString() {
        return "input_unlock";
    }
}
