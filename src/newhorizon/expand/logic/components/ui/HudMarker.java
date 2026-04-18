package newhorizon.expand.logic.components.ui;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import newhorizon.NewHorizon;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.CameraControlAction;
import newhorizon.expand.logic.components.action.InputLockAction;
import newhorizon.expand.logic.components.action.InputUnlockAction;
import newhorizon.util.func.NHInterp;
import newhorizon.util.graphic.DrawFunc;
import newhorizon.util.ui.DelaySlideBar;

import static mindustry.Vars.tilesize;
import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public class HudMarker extends Table {
    private static final Vec2 screenVec = new Vec2(), originVec = new Vec2();
    private float lifeTimer = 0;
    private float displayAlpha = 30f;

    private static final float padding = 0.05f;
    private static final float strokeInner = 3f, strokeOuter = 9f;
    private static final float iconSize = 80f;

    public Color markColor = Pal.accent;
    public Vec2 markPoint = new Vec2();

    public float delay = 3;
    public float duration = 5;
    public float radius = 24f;
    public float angle = 0f;

    public HudMarker() {
        touchable = Touchable.childrenOnly;
        fillParent = true;

        update(() -> {
            if (!Vars.state.isPaused()) lifeTimer += Time.delta;
            if (Vars.state.isMenu()) remove();
        });

        color.a = 0;
    }

    public HudMarker setMarkPosition(float x, float y) {
        this.markPoint.set(x, y);
        return this;
    }

    public HudMarker setDuration(float duration) {
        this.duration = duration - 1f;
        return this;
    }

    public HudMarker setRadius(float radius) {
        this.radius = radius;
        return this;
    }

    public HudMarker setAngle(float angle) {
        this.angle = angle;
        return this;
    }

    public HudMarker setMarkColor(Color markColor) {
        this.markColor = markColor;
        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        displayAlpha = Mathf.lerpDelta(displayAlpha, 0.1f, 5 * delta);
        if (completed()) removeMarker();
    }

    public void addMarker() {
        cutsceneUI.addMarker(this);
        actions(Actions.alpha(1, 0.45f, NHInterp.bounce5Out));
        setZIndex(0);
    }

    public void removeMarker() {
        actions(Actions.delay(delay), Actions.fadeOut(0.5f), Actions.remove());
    }

    @Override
    public boolean remove() {
        cutsceneUI.removeMarker(this);
        return super.remove();
    }

    public boolean completed() {
        return lifeTimer > duration;
    }

    public Table getDisplayStack() {
        return new Table(t -> {
            t.defaults().growX().fillY().padBottom(6f).pad(6f);
            t.add(new Stack(
                    new Table(table -> table.add(new DelaySlideBar(
                            () -> markColor,
                            () -> "     " + "World Event",
                            () -> Mathf.clamp(lifeTimer / duration)
                    )).padLeft(20f).height(40).expandX().fillX()),
                    new Table(table -> table.image(Core.atlas.find(NewHorizon.name("ADFSDS"))).color(markColor).size(54).pad(-8).expandX().left()),
                    new Table(table -> table.button(Icon.eyeSmall, Styles.clearNonei, () -> {
                        displayAlpha = 30f;
                        ActionBus bus = new ActionBus();
                        bus.addAll(
                                new InputLockAction(),
                                new CameraControlAction(){{
                                    duration = 90f;
                                    worldX = markPoint.x;
                                    worldY = markPoint.y;
                                }},
                                new InputUnlockAction()
                        );
                        cutscene.addSubActionBus(bus);
                    }).size(40).pad(-8).padRight(0).expandX().right())
            ));
        });
    }

    @Override
    public void draw() {
        super.draw();

        if (Vars.headless) return;
        Tmp.v1.set(Core.camera.project(markPoint.getX(), markPoint.getY()));

        originVec.set(Core.camera.project(markPoint.getX(), markPoint.getY()));
        screenVec.set(
                Mathf.clamp(originVec.x, width * padding, width * (1 - padding)),
                Mathf.clamp(originVec.y, height * padding, height * (1 - padding))
        );

        boolean outer = originVec.x < width * padding ||
                        originVec.y < height * padding ||
                        originVec.x > width * (1 - padding) ||
                        originVec.y > height * (1 - padding);

        drawOnWorld();
        if (outer) drawOnHud();
    }

    public void drawOnWorld() {
        drawCrossHair();
        drawProcessBar();
        drawArrow();
    }

    public void drawOnHud() {
        float angle = Angles.angle(width / 2, height / 2, originVec.x, originVec.y) - 90;
        Draw.rect(NHContent.pointerRegion, screenVec.x, screenVec.y, iconSize, iconSize, angle);
    }

    public void drawLineStroke(boolean outer, boolean center) {
        Lines.stroke((outer? strokeOuter: strokeInner) * getScale(), (outer? Pal.gray: markColor));
        Draw.alpha(color.a * Mathf.clamp(displayAlpha, center? 0.5f: 0.1f, 1f));
    }

    public float getScale() {
        return Mathf.clamp(Vars.renderer.getDisplayScale(), 0.5f, 2f);
    }

    public float getCenterSize() {
        return radius * Vars.renderer.getDisplayScale();
    }

    public void drawArrow(){
        float space = 6f;
        Tmp.v1.trns(angle + 180, getCenterSize() * 2f + 18 * getScale()).add(originVec);

        drawLineStroke(true, true);
        Fill.poly(Tmp.v1.x, Tmp.v1.y, 3, (8 + space) * getScale(), angle);
        drawLineStroke(false, true);
        Fill.poly(Tmp.v1.x, Tmp.v1.y, 3, 8 * getScale(), angle);
        Draw.color();
    }
    public void drawCrossHair() {
        drawLineStroke(true, false);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, originVec.y, originVec.x + getCenterSize() * i * 2, originVec.y);
            Lines.line(originVec.x, Math.max(0, i) * height, originVec.x, originVec.y + getCenterSize() * i * 2);
        }

        drawLineStroke(false, false);
        for (int i : Mathf.signs) {
            Lines.line(Math.max(0, i) * width, originVec.y, originVec.x + getCenterSize() * i * 2, originVec.y);
            Lines.line(originVec.x, Math.max(0, i) * height, originVec.x, originVec.y + getCenterSize() * i * 2);
        }
    }

    public void drawProcessBar() {
        drawLineStroke(true, true);
        Lines.circle(originVec.x, originVec.y, getCenterSize() * 2f);
        drawLineStroke(false, true);
        DrawFunc.circlePercent(originVec.x, originVec.y, getCenterSize() * 2f, Mathf.clamp(lifeTimer / duration), 0);
    }
}
