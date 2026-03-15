package newhorizon.expand.logic.components.action;

import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;

public class UIShowAction extends Action {
    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void end() {
        if (headless) return;

        ui.hudfrag.shown = true;
    }

    @Override
    public void skip() {
        if (headless) return;

        end();
    }
}
