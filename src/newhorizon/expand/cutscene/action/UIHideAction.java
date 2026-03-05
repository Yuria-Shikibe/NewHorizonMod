package newhorizon.expand.cutscene.action;

import newhorizon.expand.cutscene.components.Action;

import static mindustry.Vars.*;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class UIHideAction extends Action {
    public UIHideAction() {
        super(0f);
    }

    @Override
    public void begin() {
        if (headless) return;
        ui.hudfrag.shown = false;
        control.input.config.forceHide();
    }
}
