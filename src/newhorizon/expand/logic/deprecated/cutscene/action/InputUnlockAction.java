package newhorizon.expand.logic.deprecated.cutscene.action;

import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.control;
import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class InputUnlockAction extends Action {
    public InputUnlockAction() {
        super(0f);
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
