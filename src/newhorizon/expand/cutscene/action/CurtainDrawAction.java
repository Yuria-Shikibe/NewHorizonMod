package newhorizon.expand.cutscene.action;

import arc.math.Interp;
import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.*;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainDrawAction extends Action {
    public CurtainDrawAction(float duration) {
        super(duration);
    }

    @Override
    public void begin() {
        ui.hudfrag.shown = false;
    }

    @Override
    public void act() {
        cutsceneUI.curtainProgress = Interp.linear.apply(progress());
    }
}
