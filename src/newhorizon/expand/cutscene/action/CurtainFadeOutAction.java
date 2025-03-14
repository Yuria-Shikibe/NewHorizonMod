package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
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

    public void end() {
        //ui.hudfrag.shown = true;
    }

    @Override
    public void skip() {
        if (headless) return;
        end();
    }

    @Override
    public String phaseToString() {
        return "curtain_fade_out";
    }
}
