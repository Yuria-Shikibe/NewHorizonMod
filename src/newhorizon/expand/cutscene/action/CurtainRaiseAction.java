package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainRaiseAction extends Action {
    public CurtainRaiseAction(float duration) {
        super(duration);
    }

    @Override
    public void act() {
        //cutsceneUI.curtain.color.a = Interp.linear.apply(Interp.reverse.apply(progress()));
        cutsceneUI.curtainProgress = Interp.linear.apply(Interp.reverse.apply(progress()));
    }

    @Override
    public void end() {
        ui.hudfrag.shown = true;
        cutsceneUI.controlOverride = false;
    }

    @Override
    public void skip() {
        cutsceneUI.curtainProgress = 0f;
        end();
    }
}
