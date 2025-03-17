package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;
import static newhorizon.NHVars.cutsceneUI;

public class UIShowAction extends Action {
    public UIShowAction() {
        super(0f);
    }

    @Override
    public void end() {
        if (headless) return;
        ui.hudfrag.shown = true;
    }

    @Override
    public void skip(){
        if (headless) return;
        end();
    }
}
