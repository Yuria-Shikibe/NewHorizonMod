package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutsceneUI;

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
