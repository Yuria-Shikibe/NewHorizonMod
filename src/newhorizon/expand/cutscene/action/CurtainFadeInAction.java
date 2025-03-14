package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeInAction extends Action {
    public CurtainFadeInAction() {
        super(120);
    }

    @Override
    public void begin() {
        if (headless) return;
        ui.hudfrag.shown = false;
        cutsceneUI.targetOverlayAlpha = 1f;
    }

    @Override
    public String phaseToString() {
        return "curtain_fade_in";
    }
}
