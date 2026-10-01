package newhorizon.expand.logic.components.action;

import arc.graphics.Color;
import arc.util.Time;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.components.ui.HudMarker;
import mindustry.game.Team;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.ui.MarkStyle;

import static mindustry.Vars.headless;

public class MarkWorldAction extends Action {
    public int style;
    public Team team;
    public float worldX, worldY, markRadius, markTime;
    private HudMarker marker;

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
        markTime = ParseUtil.getNextFloat(tokens) * Time.toSeconds;
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
        if (headless || marker == null) return;
        marker.removeMarkerNow();
        marker = null;
    }

    @Override
    public void begin() {
        if (headless) return;
        Color color = team == null ? mindustry.graphics.Pal.accent : team.color;
        marker = new HudMarker()
                .setMarkPosition(worldX, worldY)
                .setRadius(markRadius)
                .setDuration(Math.max(markTime, 1f))
                .setStyle(getMarkStyle())
                .setMarkColor(color)
                .setIcon(NHContent.objective);
        marker.addMarker();
    }

    @Override
    public void skip() {
        end();
    }
}
