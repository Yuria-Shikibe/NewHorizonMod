package newhorizon.expand.logic.deprecated.cutscene.action;

import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.logic.components.Action;
import newhorizon.util.func.NHInterp;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class SignalCutInAction extends Action {

    public SignalCutInAction() {
        super(30);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.textTable.actions(Actions.fadeIn(duration / Time.toSeconds, NHInterp.bounce5Out));
    }
}
