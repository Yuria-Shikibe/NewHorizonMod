package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static newhorizon.NHVars.cutsceneUI;

public class InputUnlockAction extends Action {
    public InputUnlockAction() {
        super(0f);
    }

    @Override
    public void end() {
        cutsceneUI.controlOverride = true;
    }
    public void skip(){
        end();
    }
}
