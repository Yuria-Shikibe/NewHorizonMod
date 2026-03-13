package newhorizon.expand.logic.deprecated.cutscene.action;

import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
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
