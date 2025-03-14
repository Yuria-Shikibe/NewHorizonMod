package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import mindustry.ui.Styles;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class InfoTextAction extends Action {
    public String text;
    public InfoTextAction(String text) {
        super(0f);
        this.text = text;
    }

    public InfoTextAction(String[] args) {
        super(0f);
        this.text = ActionControl.parseString(args[0]);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.infoLabel = new FLabel(text);
        cutsceneUI.infoLabel.setStyle(Styles.techLabel);
        cutsceneUI.infoTable.clear();
        cutsceneUI.infoTable.add(cutsceneUI.infoLabel);
    }

    @Override
    public String phaseToString() {
        return "info_text" + " " + "<" + text + ">";
    }
}
