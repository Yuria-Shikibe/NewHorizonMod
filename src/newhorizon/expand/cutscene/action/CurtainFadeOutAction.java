package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeOutAction extends Action {
    public CurtainFadeOutAction() {
        super(120);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.targetOverlayAlpha = 0f;
    }
}
