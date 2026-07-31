package newhorizon.expand.logic.components.action;

import arc.math.Mathf;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.CutsceneUI;
import newhorizon.expand.logic.cutscene.letterbox.LetterboxText;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class LetterboxTextAction extends Action {
    public String text = "";
    public String align = "top";
    public int totalChars;

    @Override
    public String actionName() {
        return "letterbox_text";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        align = ParseUtil.getNextToken(tokens);
        text = LetterboxText.resolveLocalized(LetterboxText.decodeLogicToken(ParseUtil.getNextString(tokens)));
        totalChars = CutsceneUI.visibleLength(text);
    }

    @Override
    public void begin() {
        if (headless || cutsceneUI == null) return;
        cutsceneUI.setLetterboxTextAlign(LetterboxText.parseAlign(align));
        if (totalChars <= 0) {
            cutsceneUI.clearLetterboxText();
            return;
        }
        cutsceneUI.setLetterboxText(text, 0);
    }

    @Override
    public void act() {
        if (headless || cutsceneUI == null || totalChars <= 0) return;
        int visible = Math.min(totalChars, Mathf.ceil(progress() * totalChars));
        cutsceneUI.setLetterboxText(text, visible);
    }

    @Override
    public void end() {
        if (headless || cutsceneUI == null || totalChars <= 0) return;
        cutsceneUI.setLetterboxText(text, totalChars);
    }

    @Override
    public void skip() {
        end();
    }
}