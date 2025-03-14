package newhorizon.expand.cutscene.action;

import arc.flabel.FLabel;
import mindustry.gen.Sounds;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

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
        Sounds.chatMessage.play();
        cutsceneUI.textLabel = new FLabel(text);
        cutsceneUI.textArea.clear();
        cutsceneUI.textArea.add(cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
    }

    @Override
    public String phaseToString() {
        return "signal_text" + " " + "<" + text + ">";
    }
}
