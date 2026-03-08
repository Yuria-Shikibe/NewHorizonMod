package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import mindustry.gen.Sounds;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

/**
 * @deprecated This class is deprecated. Use logic statements instead.
 */
@Deprecated
public class SignalTextAction extends Action {
    public String text;

    public SignalTextAction(String text) {
        super(0f);
        this.text = text;
    }

    public SignalTextAction(String[] args) {
        super(0f);
        this.text = ActionControl.parseString(args[0]);
    }

    @Override
    public void begin() {
        if (headless) return;
        Sounds.uiChat.play();
        cutsceneUI.textLabel = new FLabel(text);
        cutsceneUI.textArea.clear();
        cutsceneUI.textArea.add(cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
    }
}
