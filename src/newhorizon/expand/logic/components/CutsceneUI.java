package newhorizon.expand.logic.components;

import arc.Core;
import arc.Events;
import arc.flabel.FLabel;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import newhorizon.NHUI;
import newhorizon.expand.logic.components.ui.HudMarker;
import newhorizon.expand.logic.components.ui.RaidMarker;
import newhorizon.util.annotation.HeadlessDisabled;

import static mindustry.Vars.headless;
import static newhorizon.NHRenderer.height;
import static newhorizon.NHRenderer.width;
import static newhorizon.NHVars.cutscene;
import static newhorizon.NHVars.cutsceneUI;

@HeadlessDisabled
public class CutsceneUI {
    public final float OVERLAY_SPEED = 0.0065f;
    public final Seq<HudMarker> markers = new Seq<>();
    public WidgetGroup root, overlay, curtain;
    public Table textTable, textArea, infoTable, skip, letterboxTextTable;
    public FLabel textLabel, infoLabel;
    public Label letterboxTextLabel;
    public boolean controlOverride = false;
    public boolean letterboxHudHidden = false;
    public Interp curtainInterp = Interp.pow2Out;
    public float curtainProgress = 0;
    public float targetOverlayAlpha;
    public float overlayAlphaShiftSpeed = OVERLAY_SPEED;
    public String letterboxFullText = "";
    public int letterboxVisibleChars = 0;
    public int letterboxTextAlign = Align.top;
    public boolean forceCameraZoom;
    public float forceCameraZoomScale = 1f;
    public float savedMinZoom = -1f, savedMaxZoom = -1f;

    public CutsceneUI() {
        if (headless) return;
        init();
        Events.on(EventType.WorldLoadEvent.class, e -> resetSave());
        Events.run(EventType.Trigger.preDraw, this::enforceLetterboxHudHidden);
    }

    public float curtainScl() {
        return Core.graphics.isPortrait() ? 0.22f : 0.1185f;
    }

    public float curtainTopScl() {
        return Core.graphics.isPortrait() ? 0.14f : 0.08f;
    }

    public float curtainBottomScl() {
        return Core.graphics.isPortrait() ? 0.28f : 0.16f;
    }

    public float textTableScl() {
        return Core.graphics.isPortrait() ? 0.22f : 0.1185f;
    }

    public float infoTableScl() {
        return Core.graphics.isPortrait() ? 0.4f : 0.2f;
    }

    public void init() {
        //cutscene root ui, the container of all cutscene ui
        buildRoot();
        //overlay ui, used to add some custom marks like signals.
        buildOverlay();
        //curtain ui, for cutscene curtain and fade in/fade out effect
        buildCurtain();
        //letterbox dialogue text over the thicker bottom bar
        buildLetterboxText();
        //text signal cut-in/cut-out ui, used for dialogs.
        buildTextTable();
        //COD styled info dialog.
        buildInfoTable();
        //skip button for skip current cutscene
        buildSkip();
        //lmao
        //buildKillStreak();
        //update the text table position according to the layout
        updatePosition();
        //build all cutscene ui, add the elements to root.
        buildCutsceneUI();
    }

    private void buildRoot() {
        root = new WidgetGroup() {{
            setFillParent(true);
            touchable = Touchable.childrenOnly;
        }};
    }

    private void buildOverlay() {
        overlay = new WidgetGroup() {{
            fillParent = true;
            touchable = Touchable.disabled;
        }};
    }

    private void buildCurtain() {
        curtain = new WidgetGroup() {
            {
                color.a = 1;
                fillParent = true;
                touchable = Touchable.disabled;
            }

            @Override
            public void draw() {
                super.draw();

                float progress = curtainInterp.apply(curtainProgress);
                float topH = height * curtainTopScl() * progress;
                float bottomH = height * curtainBottomScl() * progress;
                float barAlpha = Interp.pow3Out.apply(Mathf.curve(curtainProgress, 0, 0.75f));

                Draw.color(Color.black);
                Draw.alpha(barAlpha);
                Fill.quad(0, 0, 0, bottomH, width, bottomH, width, 0);
                Fill.quad(0, height, 0, height - topH, width, height - topH, width, height);
                Draw.reset();

                Draw.color(0, 0, 0, color.a);
                Fill.quad(0, 0, 0, height, width, height, width, 0);
            }
        };
    }

    private void buildLetterboxText() {
        letterboxTextLabel = new Label("");
        letterboxTextLabel.setAlignment(Align.left);
        letterboxTextLabel.setWrap(true);
        letterboxTextLabel.setColor(Color.white);

        letterboxTextTable = new Table() {{
            touchable = Touchable.disabled;
            visible(() -> Vars.state.isGame() && curtainProgress > 0.01f && letterboxFullText != null && !letterboxFullText.isEmpty());
            align(Align.topLeft);
            defaults().left();
            add(letterboxTextLabel).left().growX();
        }};

        Events.run(mindustry.game.EventType.Trigger.preDraw, this::applyForcedCameraZoom);
    }

    private void buildTextTable() {
        textTable = new Table(Tex.buttonEdge3) {{
            touchable(() -> Touchable.disabled);
            visible(() -> Vars.state.isGame());
            color.a = 0;

            if (headless) {
                textArea = new Table();
            } else {
                pane(Styles.smallPane, t -> {
                    textArea = t;
                    textArea.defaults().grow().pad(2f);
                    textArea.exited(() -> Core.scene.unfocus(textArea));
                    t.fillParent = true;
                }).grow();
            }
        }};
    }

    public void buildInfoTable() {
        infoTable = new Table(Tex.clear) {{
            touchable(() -> Touchable.disabled);
            visible(() -> Vars.state.isGame());
            color.a = 0;
        }};
    }

    private void buildSkip() {
        skip = new Table() {{
            margin(12f);
            visible(() -> !Vars.net.client() && isPlayingMainCutscene());
            setFillParent(true);
            touchable = Touchable.enabled;
            align(Align.topLeft);
            button("Skip Cutscene", Icon.play, () -> cutscene.mainBus.skip())
                    .marginLeft(8f).size(320, 50f).padTop(Vars.mobile ? 60 : 0);
        }};
    }

    private void updatePosition() {
        letterboxTextTable.update(this::updateLetterboxTextPosition);

        if (Vars.mobile) {
            textTable.update(() -> {
                textTable.setHeight(height * textTableScl());
                textTable.setWidth(width);
                textTable.setPosition(0, 0);
            });
            infoTable.update(() -> {
                infoTable.setHeight(height * infoTableScl());
                infoTable.setWidth(width);
                infoTable.setPosition(0, 0);
            });
        } else {
            textTable.update(() -> {
                textTable.setSize(Scl.scl(width * 0.65f), Scl.scl(height * 0.1f));
                textTable.setPosition((width - textTable.getWidth()) / 2, height * 0.14f);
            });
            infoTable.update(() -> {
                infoTable.setSize(Scl.scl(width * 0.25f), Scl.scl(height * 0.1f));
                infoTable.setPosition(width * 0.05f, height * 0.1f);
            });
        }
    }

    private void buildCutsceneUI() {
        if (!headless) {
            Vars.control.input.addLock(() -> controlOverride);
            Core.scene.root.addChildAt(0, root);
            root.addChild(overlay);
            root.addChild(curtain);
            root.addChild(letterboxTextTable);
            root.addChild(textTable);
            root.addChild(infoTable);
            root.addChild(skip);
            //root.addChild(killStreak);
        }
    }

    public boolean isPlayingMainCutscene() {
        return cutscene.mainBus != null && !cutscene.mainBus.complete();
    }

    public void reset() {
        if (headless) return;
        controlOverride = false;
        showHudAfterLetterbox();
        curtainProgress = 0;
        targetOverlayAlpha = 0;
        overlayAlphaShiftSpeed = OVERLAY_SPEED;
        letterboxTextAlign = Align.top;
        clearLetterboxText();
        clearForcedCameraZoom();

        overlay.clear();

        infoLabel = new FLabel("");
        infoTable.clear();
        infoTable.add(cutsceneUI.infoLabel);
        infoTable.actions(Actions.alpha(0));

        textLabel = new FLabel("");
        textArea.clear();
        textArea.add(cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
        textTable.actions(Actions.alpha(0));

        clearMarkers();
    }

    public void clearLetterboxText() {
        letterboxFullText = "";
        letterboxVisibleChars = 0;
        if (letterboxTextLabel != null) {
            letterboxTextLabel.setText("");
        }
    }

    public void hideHudForLetterbox() {
        if (headless) return;
        letterboxHudHidden = true;
        Vars.control.input.config.forceHide();
        enforceLetterboxHudHidden();
    }

    public void showHudAfterLetterbox() {
        if (headless) return;
        letterboxHudHidden = false;
        Vars.ui.hudfrag.shown = true;
    }

    private void enforceLetterboxHudHidden() {
        if (!headless && letterboxHudHidden) {
            Vars.ui.hudfrag.shown = false;
        }
    }

    public void setLetterboxTextAlign(int align) {
        letterboxTextAlign = align == 0 ? Align.top : align;
        updateLetterboxTextPosition();
    }

    public void setLetterboxText(String text, int visibleChars) {
        if (headless) return;
        letterboxFullText = text == null ? "" : text;
        letterboxVisibleChars = Mathf.clamp(visibleChars, 0, visibleLength(letterboxFullText));
        if (letterboxTextLabel != null) {
            letterboxTextLabel.setAlignment(Align.left);
            letterboxTextLabel.setText(takeVisibleChars(letterboxFullText, letterboxVisibleChars));
        }
        updateLetterboxTextPosition();
    }

    public void updateLetterboxTextPosition() {
        if (headless || letterboxTextTable == null) return;

        float progress = curtainInterp.apply(curtainProgress);
        float bottomH = height * curtainBottomScl() * progress;
        float padX = width * 0.06f;
        float padY = Math.max(8f, bottomH * 0.18f);
        float areaW = Math.max(1f, width - padX * 2f);
        float areaH = Math.max(1f, bottomH - padY * 2f);
        float areaX = padX;
        float areaY = padY;

        float textW = Math.min(areaW, Math.max(areaW * 0.72f, letterboxTextLabel == null ? areaW : letterboxTextLabel.getPrefWidth()));
        float textH = letterboxTextLabel == null ? areaH : Math.min(areaH, Math.max(letterboxTextLabel.getPrefHeight() + 8f, areaH * 0.45f));
        letterboxTextTable.setSize(textW, textH);

        float x;
        float y;
        if (Align.isLeft(letterboxTextAlign)) x = areaX;
        else if (Align.isRight(letterboxTextAlign)) x = areaX + areaW - textW;
        else x = areaX + (areaW - textW) / 2f;

        if (Align.isBottom(letterboxTextAlign)) y = areaY;
        else if (Align.isTop(letterboxTextAlign)) y = areaY + areaH - textH;
        else y = areaY + (areaH - textH) / 2f;

        letterboxTextTable.setPosition(x, y);
    }

    public void setForcedCameraZoom(float scale) {
        if (headless || Vars.renderer == null) return;
        if (savedMinZoom < 0f) {
            savedMinZoom = Vars.renderer.minZoom;
            savedMaxZoom = Vars.renderer.maxZoom;
        }
        forceCameraZoom = true;
        forceCameraZoomScale = Math.max(scale, 0.01f);
        Vars.renderer.minZoom = Math.min(Vars.renderer.minZoom, forceCameraZoomScale);
        Vars.renderer.maxZoom = Math.max(Vars.renderer.maxZoom, forceCameraZoomScale);
        applyForcedCameraZoom();
    }

    public void clearForcedCameraZoom() {
        forceCameraZoom = false;
        if (Vars.renderer != null && savedMinZoom >= 0f) {
            Vars.renderer.minZoom = savedMinZoom;
            Vars.renderer.maxZoom = savedMaxZoom;
        }
        savedMinZoom = -1f;
        savedMaxZoom = -1f;
    }

    private void applyForcedCameraZoom() {
        if (headless || !forceCameraZoom || Vars.renderer == null) return;

        float scale = forceCameraZoomScale;
        Vars.renderer.minZoom = Math.min(Vars.renderer.minZoom, scale);
        Vars.renderer.maxZoom = Math.max(Vars.renderer.maxZoom, scale);
        mindustry.Vars.control.input.logicCutscene = true;
        float span = Vars.renderer.maxZoom - Vars.renderer.minZoom;
        mindustry.Vars.control.input.logicCutsceneZoom = span <= 0.0001f ? 0f : (scale - Vars.renderer.minZoom) / span;

        Vars.renderer.camerascale = scale;
        Vars.renderer.targetscale = scale;
        Core.camera.width = Core.graphics.getWidth() / scale;
        Core.camera.height = Core.graphics.getHeight() / scale;
    }

    public static int visibleLength(String text) {
        if (text == null || text.isEmpty()) return 0;
        int visible = 0;
        for (int i = 0; i < text.length(); ) {
            char c = text.charAt(i);
            if (c == '[') {
                int close = text.indexOf(']', i + 1);
                if (close != -1) {
                    i = close + 1;
                    continue;
                }
            }
            visible++;
            i++;
        }
        return visible;
    }

    public static String takeVisibleChars(String text, int count) {
        if (text == null || text.isEmpty() || count <= 0) return "";
        if (count >= visibleLength(text)) return text;

        int visible = 0;
        int i = 0;
        while (i < text.length() && visible < count) {
            char c = text.charAt(i);
            if (c == '[') {
                int close = text.indexOf(']', i + 1);
                if (close != -1) {
                    i = close + 1;
                    continue;
                }
            }
            visible++;
            i++;
        }
        return text.substring(0, i);
    }

    public void clearMarkers() {
        clearMarkers(null);
    }

    public void clearMarkers(HudMarker.Kind kind) {
        for (int i = markers.size - 1; i >= 0; i--) {
            HudMarker marker = markers.get(i);
            if (kind != null && marker.kind != kind) continue;
            if (marker instanceof RaidMarker raidMarker) raidMarker.clearMinimapMarker();
            marker.clearActions();
            root.removeChild(marker);
            markers.remove(i);
        }
        if (NHUI.eventList != null) NHUI.rebuildEventList();
    }

    public void resetSave() {
        reset();
        NHUI.clearCustomProgressBars();
        curtain.color.a = 1;
    }

    public void update() {
        if (headless) return;
        curtain.color.a = Mathf.approachDelta(curtain.color.a, targetOverlayAlpha, overlayAlphaShiftSpeed);
    }

    public void clear() {
        if (headless) return;
        overlay.clear();
    }

    public void addMarker(HudMarker marker) {
        cutsceneUI.root.addChild(marker);
        markers.add(marker);

        NHUI.rebuildEventList();
    }

    public void removeMarker(HudMarker marker) {
        markers.remove(marker);
    }
}
