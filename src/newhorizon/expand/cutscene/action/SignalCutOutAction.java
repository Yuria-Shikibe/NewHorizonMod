package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import arc.math.Interp;
import arc.scene.actions.Actions;
import arc.util.Time;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class SignalCutOutAction extends Action {

    public SignalCutOutAction() {
        super(30);
    }

    @Override
    public void begin() {
        if (headless) return;
        cutsceneUI.textTable.actions(Actions.fadeOut(maxTimer / Time.toSeconds, Interp.pow2In));
    }

    public void end() {
        if (headless) return;
        cutsceneUI.textLabel = new FLabel("");
        cutsceneUI.textArea.clear();
        cutsceneUI.textArea.add(cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
    }

    @Override
    public void skip() {
        if (headless) return;
        end();
        cutsceneUI.textTable.actions(Actions.fadeOut(maxTimer / Time.toSeconds, Interp.pow2In));
    }

    @Override
    public String phaseToString() {
        return "signal_cut_out";
    }
}
