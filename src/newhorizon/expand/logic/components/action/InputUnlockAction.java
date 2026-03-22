package newhorizon.expand.logic.components.action;

import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InputUnlockAction extends Action {
    @Override
    public String actionName() {
        return "input_unlock";
    }

    @Override
    public void end() {
        if (headless) return;

        cutsceneUI.controlOverride = false;
        control.input.logicCutscene = false;
    }

    @Override
    public void skip() {
        if (headless) return;

        end();
    }
}
