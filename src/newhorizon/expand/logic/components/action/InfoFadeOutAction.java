package newhorizon.expand.logic.components.action;

import arc.flabel.FLabel;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InfoFadeOutAction extends Action {
    @Override
    public String actionName() {
        return "info_fade_out";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless) return;

        cutsceneUI.infoTable.actions(Actions.fadeOut(duration / Time.toSeconds, Interp.pow2In));
    }

    public void end() {
        if (headless) return;

        cutsceneUI.infoLabel = new FLabel("");
        cutsceneUI.infoTable.clear();
        cutsceneUI.infoTable.add(cutsceneUI.infoLabel);
    }

    @Override
    public void skip() {
        if (headless) return;

        end();
        cutsceneUI.infoTable.actions(Actions.fadeOut(duration / Time.toSeconds, Interp.pow2In));
    }
}
