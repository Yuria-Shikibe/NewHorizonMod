package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import mindustry.ui.Styles;
import newhorizon.expand.cutscene.components.Action;

import static newhorizon.NHVars.cutsceneUI;

public class InfoTextAction extends Action {
    public String text;
    public InfoTextAction(String text) {
        super(0f);
        this.text = text;
    }

    @Override
    public void begin() {
        cutsceneUI.infoLabel = new FLabel(text);
        cutsceneUI.infoLabel.setStyle(Styles.techLabel);
        cutsceneUI.infoTable.clear();
        cutsceneUI.infoTable.add(cutsceneUI.infoLabel);
    }
}
