package newhorizon.expand.logic.components.action;

import arc.util.Time;
import mindustry.game.Team;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.MarkStyle;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class MarkWorldAction extends Action {
    public int style;
    public Team team;
    public float worldX, worldY, markRadius, markTime;

    @Override
    public String actionName() {
        return "mark_world";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;
        style = ParseUtil.getNextInt(tokens);
        team = ParseUtil.getNextTeam(tokens);
        worldX = ParseUtil.getNextFloat(tokens);
        worldY = ParseUtil.getNextFloat(tokens);
        markRadius = ParseUtil.getNextFloat(tokens);
        markTime = ParseUtil.getNextFloat(tokens);
    }

    public MarkStyle getMarkStyle() {
        return switch (style) {
            case 1 -> MarkStyle.defaultNoLines;
            case 2 -> MarkStyle.defaultFixed;
            case 3 -> MarkStyle.signalShake;
            case 4 -> MarkStyle.iconRaid;
            default -> MarkStyle.defaultStyle;
        };
    }

    @Override
    public void end() {
        if (headless) return;

        cutsceneUI.mark(worldX, worldY, markRadius, markTime * Time.toSeconds, team.color, getMarkStyle());
    }

    @Override
    public void skip() {
        end();
    }
}
