package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;

import static newhorizon.NHVars.cutsceneUI;

public class InfoFadeOutAction extends Action {
    public InfoFadeOutAction(float duration) {
        super(duration);
    }

    @Override
    public void begin() {
        cutsceneUI.infoTable.actions(Actions.fadeOut(maxTimer / Time.toSeconds, Interp.pow2In));
    }

    public void end() {
        cutsceneUI.infoLabel = new FLabel("");
        cutsceneUI.infoTable.clear();
        cutsceneUI.infoTable.add(cutsceneUI.infoLabel);
    }

    @Override
    public void skip() {
        end();
        cutsceneUI.infoTable.actions(Actions.fadeOut(maxTimer / Time.toSeconds, Interp.pow2In));
    }
}
