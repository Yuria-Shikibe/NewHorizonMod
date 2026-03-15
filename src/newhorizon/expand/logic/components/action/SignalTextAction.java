package newhorizon.expand.logic.components.action;

import arc.flabel.FLabel;
import arc.util.Time;
import mindustry.gen.Sounds;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ActionControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class SignalTextAction extends Action {
    public String text;

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        text = ParseUtil.getNextString(tokens);
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
