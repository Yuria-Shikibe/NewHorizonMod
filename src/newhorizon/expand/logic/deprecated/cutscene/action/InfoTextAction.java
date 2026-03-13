package newhorizon.expand.logic.deprecated.cutscene.action;

import arc.flabel.FLabel;
import mindustry.ui.Styles;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
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
}
