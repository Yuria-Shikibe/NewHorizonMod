package newhorizon.expand.logic.components.ui;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.core.UI;
import mindustry.graphics.Pal;
import newhorizon.content.NHContent;
import newhorizon.util.graphic.DrawFunc;

public class RaidMarker extends HudMarker {

    private static final float LINE_BOOST = 1.35f;
    private static final float BREATHE_SPEED = 4f;
    private static final float BREATHE_AMP = 0.04f;
    private static final float END_FADE_TIME = 3f;
    private static final float RING_SCL = 1.65f;
    private static final float OUTER_RING_SCL = 1.1f;
    private static final float OUTER_ARC_SCL = 1.18f;
    private static final float ARROW_OUTER_PAD = 16f;
    private static final float ARROW_SPACING = 40f;

    @Override
    public void drawOnWorld() {
        drawCrossHair();
        drawProcessBar();
        drawArrow();
    }

    public Prov<String> displayText() {
        return () -> "ETA: " + UI.formatTime(Mathf.maxZero(duration - lifeTimer));
    }

    public float progress() {
        return Mathf.clamp(lifeTimer / duration);
    }

    public float arrowScale() {
        return Interp.pow3Out.apply(Mathf.curve(1 - progress(), 0, 0.05f));
    }

    @Override
    public void drawLineStroke(boolean outer, boolean center) {
        Lines.stroke((outer ? strokeOuter : strokeInner) * getScale() * LINE_BOOST, outer ? Pal.gray : markColor);
        Draw.alpha(color.a * Mathf.clamp(displayAlpha, center ? 0.5f : 0.1f, 1f) * endFade());
    }

    @Override
    public void drawCrossHair() {
        float outer = crosshairRadius();

        drawLineStroke(true, false);
        for (int i : Mathf.signs) {
            Lines.line(i > 0 ? 0 : width, originVec.y, originVec.x - outer * i, originVec.y);
            Lines.line(originVec.x, i > 0 ? 0 : height, originVec.x, originVec.y - outer * i);
        }

        drawLineStroke(false, false);
        for (int i : Mathf.signs) {
            Lines.line(i > 0 ? 0 : width, originVec.y, originVec.x - outer * i, originVec.y);
            Lines.line(originVec.x, i > 0 ? 0 : height, originVec.x, originVec.y - outer * i);
        }
    }

    private float ringRadius() {
        return getCenterSize() * RING_SCL * breatheScl();
    }

    private float crosshairRadius() {
        return ringRadius() * OUTER_RING_SCL;
    }

    private float endFade() {
        float remain = Mathf.maxZero(duration - lifeTimer);
        if (remain >= END_FADE_TIME) return 1f;
        return Interp.pow2In.apply(Mathf.clamp(remain / END_FADE_TIME));
    }

    private float breatheScl() {
        float fade = endFade();
        return 1f + Mathf.absin(Time.time, BREATHE_SPEED, BREATHE_AMP * fade);
    }

    private Color chargeColor() {
        return Tmp.c1.set(markColor).lerp(Color.white, Mathf.absin(4f, 0.15f * endFade()));
    }

    private float chargeAlpha() {
        return color.a * Mathf.clamp(displayAlpha, 0.5f, 1f) * endFade();
    }

    @Override
    public void drawProcessBar() {
        float fin = progress();
        float fout = 1f - fin;
        float f = Interp.pow3Out.apply(Mathf.curve(1 - fin, 0, 0.01f));
        float breath = breatheScl();
        float ring = getCenterSize() * RING_SCL * breath;
        Color charge = chargeColor();
        float alpha = chargeAlpha();
        float scl = getScale();
        float iconScl = 96f * scl * breath * (1f + 0.03f * Mathf.absin(8f, 1f) * fout * endFade());
        float iconScale = iconScl / Math.max(Math.max(icon.width, icon.height), 1f);
        float iconW = icon.width * iconScale;
        float iconH = icon.height * iconScale;

        Draw.blend(Blending.additive);
        Draw.color(markColor, Color.white, 0.075f);
        Draw.alpha(alpha * 0.65f);

        drawLineStroke(true, true);
        Lines.circle(originVec.x, originVec.y, ring * OUTER_RING_SCL);
        drawLineStroke(false, true);
        Lines.circle(originVec.x, originVec.y, ring);

        float pulse = 0.035f * endFade();
        float arcFin = fin * endFade();
        Lines.stroke(5f * f * scl, markColor);
        Lines.circle(originVec.x, originVec.y, getCenterSize() * (1 + Mathf.absin(4f, pulse)));

        Draw.color(charge, alpha);
        DrawFunc.circlePercent(originVec.x, originVec.y, getCenterSize() * 0.875f, arcFin, 0);
        DrawFunc.circlePercent(originVec.x, originVec.y, ring * OUTER_ARC_SCL, arcFin, Time.time / 2.5f);
        DrawFunc.circlePercent(originVec.x, originVec.y, ring, arcFin, -Time.time / 2.5f);

        Draw.color(Color.black, alpha * 0.75f);
        Fill.circle(originVec.x, originVec.y, Math.max(iconW, iconH) * 0.42f);
        Draw.color(charge, alpha);
        Draw.rect(icon, originVec.x, originVec.y, iconW, iconH);

        Draw.reset();
        Draw.blend();
        Lines.stroke(1f);
    }

    private float outerRingWorldRadius() {
        return radius * RING_SCL * breatheScl() * OUTER_RING_SCL;
    }

    private void projectAlongSource(float worldDist, Vec2 out) {
        Tmp.v2.trns(angle + 180, worldDist).add(markPoint);
        out.set(Core.camera.project(Tmp.v2.x, Tmp.v2.y));
    }

    @Override
    public void drawArrow() {
        float f = arrowScale();
        float alpha = chargeAlpha();
        float scl = getScale();
        float outerWorld = outerRingWorldRadius();

        Draw.color(markColor, Color.white, 0.075f);
        Draw.blend(Blending.additive);
        Draw.alpha(alpha * 0.65f);

        for (int i = 0; i < 4; i++) {
            float s = (1 - ((Time.time + 25 * i) % 100) / 100) * f * scl * 1.75f;
            projectAlongSource(outerWorld + ARROW_OUTER_PAD + ARROW_SPACING * i, Tmp.v1);
            Draw.rect(NHContent.arrowRegion, Tmp.v1.x, Tmp.v1.y, NHContent.arrowRegion.width * s, NHContent.arrowRegion.height * s, angle - 90);
        }

        Draw.blend();
        Draw.reset();
    }
}
