package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeInAction extends Action {
    public CurtainFadeInAction() {
        super(120);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.targetOverlayAlpha = 1f;
    }
}
