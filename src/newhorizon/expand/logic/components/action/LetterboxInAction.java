package newhorizon.expand.logic.components.action;

import arc.math.Mathf;
import arc.util.Time;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class LetterboxInAction extends Action {
    public float startProgress;

    @Override
    public String actionName() {
        return "letterbox_in";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
    }

    @Override
    public void begin() {
        if (headless || cutsceneUI == null) return;
        startProgress = cutsceneUI.curtainProgress;
    }

    @Override
    public void act() {
        if (headless || cutsceneUI == null) return;
        cutsceneUI.curtainProgress = Mathf.lerp(startProgress, 1f, progress());
    }

    @Override
    public void end() {
        if (headless || cutsceneUI == null) return;
        cutsceneUI.curtainProgress = 1f;
    }

    @Override
    public void skip() {
        end();
    }
}