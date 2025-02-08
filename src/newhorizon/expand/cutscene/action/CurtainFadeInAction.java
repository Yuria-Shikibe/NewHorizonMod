package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeInAction extends Action {
    public CurtainFadeInAction() {
        super(120);
    }

    @Override
    public void begin() {
        cutsceneUI.targetOverlayAlpha = 1f;
    }
}
