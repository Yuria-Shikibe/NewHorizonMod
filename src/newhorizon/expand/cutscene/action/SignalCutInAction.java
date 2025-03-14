package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.util.func.NHInterp;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class SignalCutInAction extends Action {

    public SignalCutInAction() {
        super(30);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.textTable.actions(Actions.fadeIn(maxTimer / Time.toSeconds, NHInterp.bounce5Out));
    }

    @Override
    public String phaseToString() {
        return "signal_cut_in";
    }
}
