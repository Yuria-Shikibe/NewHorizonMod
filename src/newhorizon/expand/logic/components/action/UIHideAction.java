package newhorizon.expand.logic.components.action;

import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.*;

public class UIHideAction extends Action {
    @Override
    public String actionName() {
        return "ui_hide";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;

        ui.hudfrag.shown = false;
        control.input.config.forceHide();
    }
}
