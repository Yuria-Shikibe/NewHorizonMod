package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
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
