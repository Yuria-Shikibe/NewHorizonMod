package newhorizon.expand.logic.components.action;

import arc.math.Interp;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainRaiseAction extends Action {
    @Override
    public String actionName() {
        return "curtain_raise";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void act() {
        if (headless) return;

        cutsceneUI.curtainProgress = Interp.linear.apply(Interp.reverse.apply(progress()));
    }

    @Override
    public void skip() {
        if (headless) return;

        cutsceneUI.curtainProgress = 0f;
    }
}
