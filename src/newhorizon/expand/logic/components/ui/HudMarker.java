package newhorizon.expand.logic.components.ui;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.components.ActionBus;
import newhorizon.expand.logic.components.action.CameraControlAction;
import newhorizon.expand.logic.components.action.InputLockAction;
import newhorizon.expand.logic.components.action.InputUnlockAction;
import newhorizon.util.func.NHInterp;
import newhorizon.util.ui.DelaySlideBar;

import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

public class HudMarker extends Table {
    public enum Kind {
        RAID, INTERVENTION, SPECIAL, OTHER
    }

    public static final class UnitPreview {
        public final UnitType type;
        public final int count;
        public final Seq<StatusEffect> statuses = new Seq<>();
        public final Seq<ItemStack> items = new Seq<>();
        public Block payload;

        public UnitPreview(UnitType type, int count) {
            this.type = type;
            this.count = count;
        }

        public UnitPreview status(StatusEffect effect) {
            if (effect != null && effect != StatusEffects.none) statuses.addUnique(effect);
            return this;
        }

        public UnitPreview item(ItemStack stack) {
            if (stack != null && stack.item != null && stack.amount > 0) items.add(stack);
            return this;
        }

        public UnitPreview payload(Block block) {
            payload = block;
            return this;
        }
    }

    protected static final Vec2 screenVec = new Vec2(), originVec = new Vec2();
    protected static final float padding = 0.05f;
    protected static final float strokeInner = 3f, strokeOuter = 9f;
    protected static final float iconSize = 80f;
    public Kind kind = Kind.OTHER;
    public int syncSeed;
    public Color markColor = Pal.accent;
    public Vec2 markPoint = new Vec2();
    public TextureRegion icon = NHContent.icon2;
    public float delay = 3;
    public float duration = 5;
    public float radius = 24f;
    public float angle = 0f;
    protected float lifeTimer = 0;
    protected float displayAlpha = 30f;
    protected Prov<Float> lifeTimerProv;
    protected boolean removing;
    public final Seq<UnitPreview> unitPreviews = new Seq<>();

    public HudMarker() {
        touchable = Touchable.childrenOnly;
        fillParent = true;

        update(() -> {
            if (lifeTimerProv == null && !Vars.state.isPaused()) lifeTimer += Time.delta;
            if (Vars.state.isMenu()) remove();
        });

        color.a = 0;
    }

    public HudMarker bindLifeTimer(Prov<Float> prov) {
        lifeTimerProv = prov;
        return this;
    }

    protected float elapsed() {
        return lifeTimerProv != null ? lifeTimerProv.get() : lifeTimer;
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

    public HudMarker setKind(Kind kind) {
        this.kind = kind == null ? Kind.OTHER : kind;
        return this;
    }

    public HudMarker setSyncSeed(int syncSeed) {
        this.syncSeed = syncSeed;
        return this;
    }

    public HudMarker setIcon(TextureRegion icon) {
        this.icon = icon;
        return this;
    }

    public HudMarker setUnitPreviews(Seq<UnitPreview> previews) {
        unitPreviews.clear();
        if (previews != null) unitPreviews.addAll(previews);
        return this;
    }

    public HudMarker addUnitPreview(UnitType type, int count) {
        return addUnitPreview(new UnitPreview(type, count));
    }

    public HudMarker addUnitPreview(UnitPreview preview) {
        if (preview != null && preview.type != null && preview.count > 0) unitPreviews.add(preview);
        return this;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        displayAlpha = Mathf.lerpDelta(displayAlpha, 0.1f, 5 * delta);
        if (completed()) removeMarker();
    }

    public void addMarker() {
        removing = false;
        cutsceneUI.addMarker(this);
        actions(Actions.alpha(1, 0.45f, NHInterp.bounce5Out));
        setZIndex(0);
    }

    public void removeMarker() {
        if (removing) return;
        removing = true;
        clearActions();
        actions(Actions.fadeOut(0.35f), Actions.remove());
    }

    public void removeMarkerNow() {
        removing = true;
        clearActions();
        cutsceneUI.removeMarker(this);
        remove();
    }

    @Override
    public boolean remove() {
        cutsceneUI.removeMarker(this);
        return super.remove();
    }

    public boolean completed() {
        return elapsed() > duration;
    }

    public Prov<String> displayText() {
        return () -> "World Event";
    }

    public Table getDisplayStack() {
        return new Table(t -> {
            t.defaults().growX().fillY().padBottom(6f).pad(6f);
            t.add(new Stack(
                    new Table(table -> table.add(new DelaySlideBar(
                            () -> markColor,
                            () -> "     " + displayText().get(),
                            () -> Mathf.clamp(elapsed() / duration)
                    )).padLeft(20f).height(40).expandX().fillX()),
                    new Table(table -> table.image(icon).color(markColor).size(54).pad(-8).expandX().left()),
                    new Table(table -> table.table(buttons -> {
                        buttons.button(Icon.eyeSmall, Styles.clearNonei, this::focusCamera).size(40).pad(-8);
                        if (kind == Kind.INTERVENTION || kind == Kind.SPECIAL) {
                            buttons.button(Icon.info, Styles.clearNonei, this::showUnitPreview).size(40).pad(-8).padLeft(2f);
                        }
                    }).expandX().right())
            ));
        });
    }

    protected void focusCamera() {
        displayAlpha = 30f;
        float cx = markPoint.x;
        float cy = markPoint.y;
        ActionBus bus = new ActionBus();
        bus.addAll(
                new InputLockAction(),
                new CameraControlAction() {{
                    duration = 90f;
                    worldX = cx;
                    worldY = cy;
                }},
                new InputUnlockAction()
        );
        cutscene.addSubActionBus(bus);
    }

    protected void showUnitPreview() {
        float dialogW = Core.graphics.getWidth() / 3f;
        float rowH = 52f;

        BaseDialog dialog = new BaseDialog("@nh.cutscene.event.fleet-composition");
        dialog.cont.pane(p -> {
            p.top().left();
            p.defaults().width(dialogW - 24f).left();
            if (unitPreviews.isEmpty()) {
                p.add("@nh.cutscene.event.fleet-composition-empty").color(Pal.lightishGray).pad(16f).width(dialogW - 24f);
                return;
            }
            for (UnitPreview preview : unitPreviews) {
                if (preview == null || preview.type == null) continue;
                UnitType type = preview.type;
                p.button(b -> {
                    b.setBackground(Tex.pane);
                    b.left().defaults().pad(0f);
                    b.table(unit -> {
                        unit.left();
                        unit.image(type.uiIcon).size(40f).scaling(Scaling.fit).pad(6f);
                        unit.add(type.localizedName).padLeft(4f).padRight(6f).left().growX().wrap().labelAlign(Align.left);
                    }).growY().width((dialogW - 24f) * 0.42f).left();

                    b.table(buffs -> {
                        buffs.left();
                        if (preview.statuses.isEmpty()) {
                            buffs.add("-").color(Pal.lightishGray).pad(4f);
                        } else {
                            for (StatusEffect effect : preview.statuses) {
                                if (effect == null || effect == StatusEffects.none) continue;
                                buffs.image(effect.uiIcon).size(28f).scaling(Scaling.fit).pad(2f);
                            }
                        }
                    }).growY().width((dialogW - 24f) * 0.22f).left().padLeft(4f);

                    b.table(cargo -> {
                        cargo.left();
                        boolean any = false;
                        for (ItemStack stack : preview.items) {
                            if (stack == null || stack.item == null || stack.amount <= 0) continue;
                            any = true;
                            cargo.table(slot -> {
                                slot.image(stack.item.uiIcon).size(24f).scaling(Scaling.fit);
                                slot.add(String.valueOf(stack.amount)).fontScale(0.85f).padLeft(2f);
                            }).pad(2f);
                        }
                        if (preview.payload != null) {
                            any = true;
                            cargo.image(preview.payload.uiIcon).size(28f).scaling(Scaling.fit).pad(2f);
                        }
                        if (!any) cargo.add("-").color(Pal.lightishGray).pad(4f);
                    }).growY().width((dialogW - 24f) * 0.22f).left().padLeft(4f);

                    b.add("x" + preview.count).color(markColor).padLeft(4f).padRight(8f).expandX().right();
                }, Styles.flatt, () -> Vars.ui.content.show(type)).height(rowH).padBottom(4f).row();
            }
        }).width(dialogW).maxHeight(Core.graphics.getHeight() * 0.55f);
        dialog.addCloseButton();
        Core.scene.setScrollFocus(null);
        dialog.show();
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
        drawLineStroke(false, true);
        Draw.rect(NHContent.pointerRegion, screenVec.x, screenVec.y, iconSize, iconSize, angle);
    }

    public void drawLineStroke(boolean outer, boolean center) {
        Lines.stroke((outer ? strokeOuter : strokeInner) * getScale(), (outer ? Pal.gray : markColor));
        Draw.alpha(color.a * Mathf.clamp(displayAlpha, center ? 0.5f : 0.1f, 1f));
    }

    public float getScale() {
        return Mathf.clamp(Vars.renderer.getDisplayScale(), 0.5f, 2f);
    }

    public float getCenterSize() {
        return radius * Vars.renderer.getDisplayScale();
    }

    public void drawArrow() {
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
    }
}
