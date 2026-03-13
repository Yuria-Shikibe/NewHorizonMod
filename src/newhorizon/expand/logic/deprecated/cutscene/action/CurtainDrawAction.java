package newhorizon.expand.logic.deprecated.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class CurtainDrawAction extends Action {
    public CurtainDrawAction() {
        super(90);
    }

    @Override
    public void act() {
        if (headless) return;
        cutsceneUI.curtainProgress = Interp.linear.apply(progress());
    }
}
