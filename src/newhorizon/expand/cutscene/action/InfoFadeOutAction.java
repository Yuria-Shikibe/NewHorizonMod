package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class InfoFadeOutAction extends Action {
    public InfoFadeOutAction() {
        super(15);
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
