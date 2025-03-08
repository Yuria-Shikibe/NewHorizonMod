package newhorizon.expand.cutscene.action;

import arc.util.Time;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;
import newhorizon.expand.cutscene.components.ui.MarkStyle;

import static newhorizon.NHVars.cutsceneUI;

public class MarkWorldAction extends Action {
    public int style;
    public float x, y, radius, time;
    public MarkWorldAction(float x, float y, float radius, float time, int style) {
        super(0);
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.time = time;
        this.style = style;
    }

    public MarkWorldAction(String[] args) {
        super(0);
        x = Float.parseFloat(args[0]);
        y = Float.parseFloat(args[1]);
        radius = Float.parseFloat(args[2]);
        time = Float.parseFloat(args[3]);
        style = Integer.parseInt(args[4]);
    }

    public MarkWorldAction(String[] tokens, Building source) {
        super(0);
        x = ActionControl.parseFloat(tokens[0], source);
        y = ActionControl.parseFloat(tokens[1], source);
        radius = ActionControl.parseFloat(tokens[2], source);
        time = ActionControl.parseFloat(tokens[3], source);
        style = Integer.parseInt(tokens[4]);
    }

    public MarkStyle getMarkStyle() {
        return switch (style) {
            case 1 -> MarkStyle.defaultNoLines;
            case 2 -> MarkStyle.fixed;
            case 3 -> MarkStyle.shake;
            default -> MarkStyle.defaultStyle;
        };
    }

    @Override
    public void end() {
        cutsceneUI.mark(x, y, radius, time * Time.toSeconds, Pal.accent, getMarkStyle(), () -> false);
    }

    @Override
    public void skip() {
        end();
    }
}
