package newhorizon.expand.logic.components.ui;

import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.core.UI;
import mindustry.graphics.Pal;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;

public class RaidMarker extends HudMarker {

    private static final float LINE_BOOST = 1.35f;
    private static final float BREATHE_SPEED = 5f;
    private static final float BREATHE_AMP = 0.12f;
    private static final float OUTER_RING_SCL = 1.15f;
    private static final float TRI_WAVE_SCL = 0.28f;
    private static final float TRI_WOBBLE_SCL = 0.5f;
    private static final float TRI_MIN_ORBIT_SCL = 0.2f;
    private static final float TRI_MAX_ORBIT_SCL = 1.2f;
    private static final float TRI_END_WOBBLE_SCL = 0.55f;

    @Override
    public void drawOnWorld() {
        drawCrossHair();
        drawEntanglementCharge();
        drawProcessBar();
        drawArrow();
    }

    public Prov<String> displayText() {
        return () -> "ETA: " + UI.formatTime(Mathf.maxZero(duration - lifeTimer));
    }

    @Override
    public void drawLineStroke(boolean outer, boolean center) {
        Lines.stroke((outer ? strokeOuter : strokeInner) * getScale() * LINE_BOOST, outer ? Pal.gray : markColor);
        Draw.alpha(color.a * Mathf.clamp(displayAlpha, center ? 0.5f : 0.1f, 1f));
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
        return getCenterSize() * 2f * breatheScl();
    }

    private float crosshairRadius() {
        return ringRadius() * OUTER_RING_SCL;
    }

    private float breatheScl() {
        return 1f + Mathf.absin(Time.time, BREATHE_SPEED, BREATHE_AMP);
    }

    private float thick(float stroke) {
        return stroke * LINE_BOOST * getScale();
    }

    private Color chargeColor() {
        return Tmp.c1.set(markColor).lerp(Color.white, Mathf.absin(4f, 0.3f));
    }

    private float chargeAlpha() {
        return color.a * Mathf.clamp(displayAlpha, 0.5f, 1f);
    }

    private void drawEntanglementCharge() {
        float fin = Mathf.clamp(lifeTimer / duration);
        float fout = 1f - fin;

        float fadeS = Mathf.curve(fout, 0.0225f, 0.06f);
        float fade = Mathf.curve(fout, 0, 0.025f) * NHInterp.bounce5In.apply(fadeS);
        float str = thick(2.2f * Math.max(fade, TRI_MIN_ORBIT_SCL));
        Color charge = chargeColor();
        float alpha = chargeAlpha();

        drawTriangleAim(fout, str, charge, alpha);
        drawTargetBurst(fin, fout, str, charge, alpha);
        Draw.color();
    }

    private void drawTriangleAim(float fout, float str, Color charge, float alpha) {
        float fadeS2 = Mathf.curve(fout, 0.09f, 0.185f);
        float wave = lifeTimer * TRI_WAVE_SCL;
        float spread = ringRadius();
        float orbitMin = spread * TRI_MIN_ORBIT_SCL;
        float orbitMax = spread * TRI_MAX_ORBIT_SCL;
        float orbitBase = Mathf.lerp(orbitMin, orbitMax, fout);
        float orbitPulse = 1f + Mathf.absin(wave, 9f, 0.06f);
        float orbitDist = Mathf.clamp(orbitBase * orbitPulse, orbitMin, orbitMax);
        float wobbleFade = Mathf.lerp(TRI_END_WOBBLE_SCL, 1f, fout);
        float wobbleAmp = spread * 0.045f * TRI_WOBBLE_SCL * wobbleFade;
        float orbitAngle = wave / 14f
                + DrawFunc.rotator_120(DrawFunc.cycle(wave, 20, 720f), 0.2f) * fadeS2
                + Mathf.sin(wave, 19, 14f) * (0.25f + 0.5f * fout);

        Tmp.v2.trns(wave / 22f, Mathf.sin(wave, 30f, 18f) * wobbleAmp, Mathf.cos(wave + 177f, 17f, 16f) * wobbleAmp);
        Tmp.v3.set(Mathf.sin(wave, 30, 12f) * wobbleAmp, Mathf.sin(wave + Mathf.PI * 0.3f, 43, 10f) * wobbleAmp);
        Tmp.v4.trns(orbitAngle, orbitDist);

        float addRot = (-DrawFunc.rotator_120(DrawFunc.cycle(wave, 45, 860f), 0.24f) + Mathf.absin(33f, 45f)) * fadeS2 + wave * 14f;
        drawTriangleAimNode(Tmp.v4, Tmp.v2, Tmp.v3, orbitMin, orbitMax, fout, str, addRot, charge, alpha);
    }

    private void drawTriangleAimNode(float offsetX, float offsetY, float wobbleAX, float wobbleAY, float wobbleBX, float wobbleBY,
                                     float orbitMin, float orbitMax, float fout, float str, float addRot, Color charge, float alpha) {
        Tmp.v5.set(offsetX, offsetY).add(originVec).add(wobbleAX, wobbleAY).add(wobbleBX, wobbleBY);
        clampOrbit(Tmp.v5, orbitMin, orbitMax);

        Draw.color(charge, alpha);
        Lines.stroke(str, charge);
        Lines.poly(Tmp.v5.x, Tmp.v5.y, 3, (50f + 80f * Mathf.lerp(TRI_MIN_ORBIT_SCL, 1f, fout)) * getScale(), addRot);
        Lines.line(Tmp.v5.x, Tmp.v5.y, originVec.x, originVec.y);
        Fill.circle(Tmp.v5.x, Tmp.v5.y, Lines.getStroke() * 1.8f);
        Lines.stroke(1f);
    }

    private void clampOrbit(Vec2 pos, float orbitMin, float orbitMax) {
        float dx = pos.x - originVec.x, dy = pos.y - originVec.y;
        float dist = Mathf.len(dx, dy);
        if (dist <= 0.001f) {
            pos.set(originVec).add(orbitMin, 0f);
            return;
        }
        float clamped = Mathf.clamp(dist, orbitMin, orbitMax);
        if (clamped != dist) {
            pos.set(originVec).add(dx / dist * clamped, dy / dist * clamped);
        }
    }

    private void drawTriangleAimNode(Vec2 offset, Vec2 wobbleA, Vec2 wobbleB,
                                     float orbitMin, float orbitMax, float fout, float str, float addRot, Color charge, float alpha) {
        drawTriangleAimNode(offset.x, offset.y, wobbleA.x, wobbleA.y, wobbleB.x, wobbleB.y, orbitMin, orbitMax, fout, str, addRot, charge, alpha);
    }

    private void drawTargetBurst(float fin, float fout, float str, Color charge, float alpha) {
        Tmp.v5.set(originVec).add(Mathf.sin(Time.time, 36, 12) * fout, Mathf.cos(Time.time, 36, 12) * fout);

        Fill.circle(originVec.x, originVec.y, thick(2.8f) * breatheScl());
        Draw.color(Color.black, alpha);
        Fill.circle(originVec.x, originVec.y, thick(2.1f) * breatheScl());

        Draw.color(charge, alpha);
        Lines.stroke(thick(1.2f), charge);
        for (int i : Mathf.signs) {
            float d = 220f * i * fout * getScale() + 2f * i;
            float phi = Mathf.absin(8 + i * 2f, 12f) * fout;
            Lines.lineAngle(originVec.x + d + 1f * i, originVec.y + phi, 90 - i * 90, (682 + i * 75) + 220 * fin);
            Lines.lineAngleCenter(originVec.x + d, originVec.y + phi, 45, (188 + i * 20) * fout + 80);
        }

        Lines.stroke(1f);
    }

    @Override
    public void drawArrow() {
        float fout = 1f - Mathf.clamp(lifeTimer / duration);
        float beamLen = Mathf.dst(originVec.x, originVec.y, width / 2f, height / 2f) + getCenterSize() * 4f;
        Tmp.v5.trns(angle + 180, beamLen).add(originVec);

        Color charge = chargeColor();
        float alpha = chargeAlpha();
        float str = thick((1.4f + Mathf.absin(10f, 0.35f)) * fout);

        Draw.color(charge, alpha * 0.85f);
        Lines.stroke(str * 1.5f, Color.black);
        Lines.line(Tmp.v5.x, Tmp.v5.y, originVec.x, originVec.y);
        Lines.stroke(str, charge);
        Lines.line(Tmp.v5.x, Tmp.v5.y, originVec.x, originVec.y);
        DrawFunc.basicLaser(Tmp.v5.x, Tmp.v5.y, originVec.x, originVec.y, str * 0.55f);
        Lines.stroke(1f);
        Draw.color();
    }

    @Override
    public void drawProcessBar() {
        float fin = Mathf.clamp(lifeTimer / duration);
        float fout = 1f - fin;
        float breath = breatheScl();
        float ring = getCenterSize() * 2f * breath;
        Color charge = chargeColor();
        float alpha = chargeAlpha();
        float iconScl = 96f * getScale() * breath * (1f + 0.05f * Mathf.absin(8f, 1f) * fout);

        drawLineStroke(true, true);
        Lines.circle(originVec.x, originVec.y, ring * OUTER_RING_SCL);
        drawLineStroke(false, true);
        Lines.circle(originVec.x, originVec.y, ring);

        Draw.color(charge, alpha * 0.35f);
        Lines.stroke(thick(1.1f), charge);
        DrawFunc.circlePercent(originVec.x, originVec.y, ring * 1.3f, fin * 0.85f, Time.time / 3f);

        Draw.color(charge, alpha);
        DrawFunc.circlePercent(originVec.x, originVec.y, ring, fin, Time.time / 2f);
        DrawFunc.circlePercent(originVec.x, originVec.y, ring * 0.72f, fin * 1.035f, -Time.time / 2f + 90f);

        Draw.color(Color.black, alpha * 0.75f);
        Fill.circle(originVec.x, originVec.y, iconScl * 0.42f);
        Draw.color(charge, alpha);
        Draw.rect(icon, originVec.x, originVec.y, iconScl, iconScl);

        Lines.stroke(1f);
        Draw.color();
    }
}
