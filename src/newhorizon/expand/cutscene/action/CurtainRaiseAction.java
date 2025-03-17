package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainRaiseAction extends Action {
    public CurtainRaiseAction() {
        super(90);
    }

    @Override
    public void act() {
        if (headless) return;
        cutsceneUI.curtainProgress = Interp.linear.apply(Interp.reverse.apply(progress()));
    }

    @Override
    public void skip() {
        if (headless) return;
        cutsceneUI.curtainProgress = 0f;
    }
}
