package newhorizon;

import arc.Core;
import arc.func.Boolp;
import arc.func.Floatp;
import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Scaling;
import arc.util.Strings;
import mindustry.editor.MapEditorDialog;
import mindustry.game.Gamemode;
import mindustry.game.MapObjectives;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHContent;
import newhorizon.content.NHDatabaseEntries;
import newhorizon.content.NHLogic;
import newhorizon.expand.game.DefaultRaidStrength;
import newhorizon.expand.game.RaidState;
import newhorizon.util.ui.DelayCollapser;
import newhorizon.util.ui.DelaySlideBar;
import newhorizon.util.ui.ObjectiveSign;
import newhorizon.util.ui.dialog.NHWorldSettingDialog;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.underline;
import static newhorizon.NHVars.cutsceneUI;

public class NHUI {
    public static final float maxWidth = 65f * 5f + 4f;
    public static Table HUD_overlay, HUD_waves, HUD_statustable, HUD_status;
    public static Table itemInv;
    public static WidgetGroup HUD_waves_editor;
    public static Element infoTable;

    public static Table objectiveList, eventList;

    public static NHWorldSettingDialog nhWorldSettingDialog;

    public static void init() {

        nhWorldSettingDialog = new NHWorldSettingDialog();

        try {
            getReferences();
            rebuildSkipButton();
            preProcess();
            buildListTable();
            postProcess();
        } catch (Exception e) {
            Log.err(e);
        }

        try {
            BaseDialog menu = Reflect.get(MapEditorDialog.class, ui.editor, "menu");
            menu.cont.row().button("@mod.ui.nh-extra-menu", new TextureRegionDrawable(NHContent.icon), 30,
                    () -> nhWorldSettingDialog.show()).padTop(1f).size(180f * 2 + 10f, 60f);
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void getReferences() {
        HUD_overlay = ui.hudGroup.find("overlaymarker");
        HUD_waves_editor = HUD_overlay.find("waves/editor");
        HUD_waves = HUD_waves_editor.find("waves");
        HUD_statustable = HUD_waves.find("statustable");
        HUD_status = HUD_statustable.find("status");

        itemInv = ui.hudGroup.find("inventory");
    }

    public static void rebuildSkipButton() {
        ImageButton skip = HUD_statustable.find("skip");

        skip.setStyle(new ImageButton.ImageButtonStyle() {{
            over = Tex.buttonSelectTrans;
            down = Tex.whitePane;
            up = Tex.pane;
            imageUp = Icon.play;
            disabled = Tex.paneRight;
            imageDisabledColor = Color.clear;
            imageUpColor = Color.white;
        }});

        skip.addChild(new Table(underline) {{
            touchable = Touchable.disabled;
            setSize(skip.getWidth(), skip.getHeight());
        }}.visible(() -> !(state.rules.waves && state.rules.waveSending && ((net.server() || player.admin) || !net.active()) && state.enemies == 0 && !spawner.isSpawning())));
    }

    public static void preProcess() {
        infoTable = HUD_waves.find("infotable");
        infoTable.remove();
    }

    public static void postProcess() {
        HUD_waves.add(infoTable).width(maxWidth).left();
    }

    public static void buildListTable() {
        HUD_waves.row().add(new Table(Tex.buttonEdge4, t -> {
            Table infoT = new Table();
            infoT.touchable = Touchable.childrenOnly;

            objectiveList = new Table();
            eventList = new Table();

            rebuildEventList();

            ImageButton b = new ImageButton(Icon.downOpen, Styles.clearNonei);
            b.clicked(() -> {
                if (b.isChecked()) {
                    infoT.clear();
                    infoT.table().padTop(4);
                    ScrollPane pane = infoT.pane(Styles.smallPane, i -> {
                        if (showRaidStrengthHud()) {
                            i.add(getRaidStrengthTable()).growX().row();
                        }
                        i.table(p -> {
                            p.align(Align.topLeft).defaults().growX().fillY().row();
                            state.rules.objectives.each(mapObjective -> {
                                Boolp shown = () -> mapObjective.qualified() && !mapObjective.hidden && hasObjectiveUi(mapObjective);
                                DelayCollapser col = new DelayCollapser(getObjectiveTable(mapObjective), !shown.get());
                                col.setCollapsed(true, () -> !shown.get());
                                p.add(col).row();
                            });
                        }).growX().row();
                        i.add(eventList).growX().row();
                    }).grow().maxHeight(Core.graphics.getHeight() / 2f).get();
                    pane.name = "pane";
                    pane.setFadeScrollBars(true);
                    pane.setForceScroll(false, true);
                    infoT.exited(() -> Core.scene.unfocus(infoT));
                } else {
                    Core.scene.unfocus(infoT);
                }
            });

            b.update(() -> {
                if (state.isMenu()) b.setChecked(false);
            });

            t.table(bl -> {
                bl.table(table -> {
                    table.label(NHUI::getDisplayObjectiveCount).labelAlign(Align.left).maxWidth(maxWidth - 40).pad(2, 16, 4, 0).row();
                    table.label(NHUI::getDisplayEventCount).labelAlign(Align.left).maxWidth(maxWidth - 40).pad(4, 16, 2, 0).row();
                }).growX().height(50).marginLeft(10f);
                bl.add(b).size(50).padLeft(10f);
            }).growX().fillY().margin(4f).padBottom(4f);

            t.row().collapser(infoT, true, b::isChecked).growX().get().setDuration(0.1f);
        })).left().margin(10f).growX().row();
    }

    public static void rebuildEventList() {
        eventList.clear();
        eventList.align(Align.topLeft).defaults().growX().fillY().row();
        for (var eventHudMarker : cutsceneUI.markers) {
            Boolp shown = eventHudMarker::completed;
            DelayCollapser col = new DelayCollapser(eventHudMarker.getDisplayStack(), shown.get());
            col.setCollapsed(true, shown);
            eventList.add(col).row();
        }
    }

    public static String getDisplayObjectiveCount() {
        int activeCount = 0;
        for (var mapObjective : state.rules.objectives) {
            if (mapObjective.qualified() && !mapObjective.hidden) {
                activeCount++;
            }
        }
        return activeCount == 0 ? Core.bundle.get("mod.ui.no-objective") : Core.bundle.format("mod.ui.objective-count", activeCount);
    }

    public static String getDisplayEventCount() {
        int eventCount = cutsceneUI.markers.size;
        return eventCount == 0 ? Core.bundle.get("mod.ui.no-event") : Core.bundle.format("mod.ui.event-count", eventCount);
    }

    public static boolean showRaidStrengthHud() {
        return RaidState.enabled()
                && state.isGame()
                && state.rules.mode() != Gamemode.sandbox
                && state.rules.mode() != Gamemode.pvp
                && !NHLogic.hasCustomRaidLogic();
    }

    public static Table getRaidStrengthTable() {
        return new Table(t -> {
            t.defaults().growX().fillY().padBottom(6f).pad(6f);
            t.add(new Stack(
                    new Table(table -> table.add(new DelaySlideBar(
                            () -> Pal.accent,
                            () -> "     " + getRaidStrengthBarText(),
                            NHUI::getRaidStrengthProgress
                    )).padLeft(20f).height(40).expandX().fillX()),
                    new Table(table -> table.button(new TextureRegionDrawable(NHContent.danger), Styles.clearNonei, () -> {
                        ui.content.show(NHDatabaseEntries.raidThreat);
                    }).size(54).pad(-8).expandX().left())
            ));
        });
    }

    public static CharSequence getRaidStrengthBarText() {
        Team player = state.rules.defaultTeam;
        if (player == null) return Core.bundle.get("mod.ui.raid-strength-bar-empty");

        float strength = DefaultRaidStrength.evaluate(player);
        int tier = displayThreatTier(DefaultRaidStrength.toTier(player));
        float next = DefaultRaidStrength.nextTierMin(tier);
        String strengthText = Strings.fixed(strength, 0);

        if (next < 0f) {
            return Core.bundle.format("mod.ui.raid-strength-bar-max", strengthText, tier);
        }
        return Core.bundle.format("mod.ui.raid-strength-bar", strengthText, Strings.fixed(next, 0), tier);
    }

    public static int displayThreatTier(int tier) {
        return Math.min(Math.max(tier, 1), DefaultRaidStrength.maxTier());
    }

    public static float getRaidStrengthProgress() {
        Team player = state.rules.defaultTeam;
        if (player == null) return 0f;

        float strength = DefaultRaidStrength.evaluate(player);
        int tier = DefaultRaidStrength.toTier(player);
        float next = DefaultRaidStrength.nextTierMin(tier);
        if (next < 0f) return 1f;

        float base = DefaultRaidStrength.tierMin(tier);
        float range = next - base;
        if (range <= 0f) return 1f;
        return Mathf.clamp((strength - base) / range);
    }

    public static CharSequence objectiveDisplayText(MapObjectives.MapObjective obj) {
        String text = obj.text();
        if (text == null || text.isEmpty()) return null;
        return text.replace('\n', ' ');
    }

    public static boolean hasObjectiveUi(MapObjectives.MapObjective obj) {
        return buildObjectiveStack(obj) != null;
    }

    public static Stack buildObjectiveStack(MapObjectives.MapObjective e) {
        Prov<CharSequence> labelProv = () -> objectiveDisplayText(e);

        if (e instanceof MapObjectives.ResearchObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.content.fullIcon, () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.ProduceObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.content.fullIcon, () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.ItemObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.item.fullIcon, () -> state.rules.defaultTeam.items().get(obj.item), () -> obj.amount, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.CoreItemObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.item.fullIcon, () -> state.stats.coreItemCount.get(obj.item), () -> obj.amount, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.BuildCountObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.block.fullIcon, () -> state.stats.placedBlockCount.get(obj.block, 0), () -> obj.count, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.UnitCountObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.unit.fullIcon, () -> state.rules.defaultTeam.data().countType(obj.unit), () -> obj.count, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.DestroyUnitsObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(Icon.units.getRegion(), () -> state.stats.enemyUnitsDestroyed, () -> obj.count, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.TimerObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            Floatp countup = () -> Reflect.get(obj, "countup");
            Floatp realTime = () -> obj.duration * state.rules.objectiveTimerMultiplier;
            return objectiveTable(Icon.refresh.getRegion(), () -> (int) countup.get(), () -> (int) realTime.get(), labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.DestroyBlockObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.block.fullIcon, () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.DestroyBlocksObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(obj.block.fullIcon, obj::progress, () -> obj.positions.length, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.CommandModeObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(Icon.units.getRegion(), () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.FlagObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(Icon.info.getRegion(), () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (e instanceof MapObjectives.DestroyCoreObjective obj) {
            if (objectiveDisplayText(obj) == null) return null;
            return objectiveTable(Icon.effect.getRegion(), () -> Mathf.num(obj.isCompleted()), () -> 1, labelProv, obj::isCompleted, false);
        }

        if (objectiveDisplayText(e) != null) {
            return objectiveTable(Icon.info.getRegion(), () -> 0, () -> 1, () -> objectiveDisplayText(e), () -> false, false);
        }

        return null;
    }

    public static Table getObjectiveTable(MapObjectives.MapObjective e) {
        return new Table(t -> {
            t.defaults().growX().fillY().padBottom(6f).pad(6f);
            Stack stack = buildObjectiveStack(e);
            if (stack != null) t.add(stack);
        });
    }

    public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked, boolean text) {
        return new Stack(
                new Table(table -> table.add(new DelaySlideBar(
                        () -> Pal.accent,
                        () -> "            " + info.get() + (text ? (value.get() + "/" + target.get()) : ""),
                        () -> Mathf.clamp((float) value.get() / target.get())
                )).padLeft(20f).height(40).expandX().fillX()),
                new Table(image -> image.image(region).scaling(Scaling.fit).size(32).padTop(4f).padBottom(4f).padLeft(56f).padRight(8)).left(),
                new Table(table -> table.add(new ObjectiveSign(Pal.gray, Pal.accent, 2, 4, 5, checked)).size(40).expandX().left())
        );
    }

    public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked) {
        return objectiveTable(region, value, target, info, checked, true);
    }
}
