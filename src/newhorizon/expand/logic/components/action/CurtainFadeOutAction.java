package newhorizon.expand.logic.components.action;

import arc.util.Log;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeOutAction extends Action {
    @Override
    public String actionName() {
        return "curtain_fade_out";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;

        cutsceneUI.targetOverlayAlpha = 0f;
    }
}
