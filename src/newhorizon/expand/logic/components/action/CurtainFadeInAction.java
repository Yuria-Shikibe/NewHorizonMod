package newhorizon.expand.logic.components.action;

import arc.math.geom.Vec2;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeInAction extends Action {
    @Override
    public String actionName() {
        return "curtainfadein";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;

        cutsceneUI.targetOverlayAlpha = 1f;
    }
}
