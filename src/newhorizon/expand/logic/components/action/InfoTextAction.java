package newhorizon.expand.logic.components.action;

import arc.flabel.FLabel;
import arc.util.Time;
import mindustry.ui.Styles;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InfoTextAction extends Action {
    public String text;

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        text = ParseUtil.getNextString(tokens);
    }

    @Override
    public void begin() {
        if (headless) return;

        cutsceneUI.infoLabel = new FLabel(text);
        cutsceneUI.infoLabel.setStyle(Styles.techLabel);
        cutsceneUI.infoTable.clear();
        cutsceneUI.infoTable.add(cutsceneUI.infoLabel);
    }
}
