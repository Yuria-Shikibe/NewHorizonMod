package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.util.func.NHInterp;

import static newhorizon.NHVars.cutsceneUI;

public class SignalCutInAction extends Action {

    public SignalCutInAction(float duration) {
        super(duration);
    }

    @Override
    public void begin() {
        cutsceneUI.textTable.actions(Actions.fadeIn(maxTimer / Time.toSeconds, NHInterp.bounce5Out));
    }
}
