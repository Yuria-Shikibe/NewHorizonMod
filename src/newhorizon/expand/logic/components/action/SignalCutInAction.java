package newhorizon.expand.logic.components.action;

import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.util.func.NHInterp;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class SignalCutInAction extends Action {
    @Override
    public String actionName() {
        return "signal_cut_in";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.textTable.actions(Actions.fadeIn(duration / Time.toSeconds, NHInterp.bounce5Out));
    }
}
