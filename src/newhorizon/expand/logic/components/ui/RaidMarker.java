package newhorizon.expand.logic.components.ui;

import arc.Core;
import arc.func.Prov;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.core.UI;
import newhorizon.util.graphic.DrawFunc;

public class RaidMarker extends HudMarker{

    @Override
    public void drawOnWorld() {
        drawCrossHair();
        drawProcessBar();
        drawArrow();
    }

    public Prov<String> displayText() {
        return () -> "ETA: " + UI.formatTime(Mathf.maxZero(duration - lifeTimer));
    }

    @Override
    public void drawArrow(){
        float space = 6f;
        Tmp.v1.trns(angle + 180, getCenterSize() * 2f + 18 * getScale()).add(originVec);

        drawLineStroke(true, true);
        Fill.poly(Tmp.v1.x, Tmp.v1.y, 3, (8 + space) * getScale(), angle);
        drawLineStroke(false, true);
        Fill.poly(Tmp.v1.x, Tmp.v1.y, 3, 8 * getScale(), angle);
        Draw.color();
    }

    @Override
    public void drawProcessBar() {
        drawLineStroke(true, true);
        Lines.circle(originVec.x, originVec.y, getCenterSize() * 2f);
        drawLineStroke(false, true);
        DrawFunc.circlePercent(originVec.x, originVec.y, getCenterSize() * 2f, Mathf.clamp(lifeTimer / duration), 0);
        Draw.rect(icon, originVec.x, originVec.y, 96 * getScale(), 96 * getScale());
    }
}
