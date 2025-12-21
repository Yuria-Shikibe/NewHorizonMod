package newhorizon;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.scene.actions.RunnableAction;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.graphics.Pal;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.content.NHContent;
import newhorizon.content.NHShaders;

import static arc.Core.settings;
import static mindustry.Vars.ui;
import static newhorizon.util.ui.TableFunc.LEN;

public class NHSetting {
    public static final String
            START_LOG = "nh-hide-starting-log",
            EFFECT_DETAIL = "nh-effect-detail",

            GRAPHIC_GRAVITY_TRAP_ALPHA = "nh-gravity-trap-alpha",
            GRAPHIC_GRAVITY_TRAP_OUTLINE_ALPHA = "nh-gravity-trap-outline-alpha",

            OVERRIDE_UNIT_SHIELD = "nh-override-unit-shield",
            OVERRIDE_CORE_UNIT = "nh-override-core-unit",
            OVERRIDE_LOGISTIC = "nh-override-logistic",
            OVERRIDE_DRILL = "nh-override-drill",
            OVERRIDE_FACTORIES = "nh-override-factories",
            OVERRIDE_POWER = "nh-override-power",
            OVERRIDE_UNIT = "nh-override-unit",
            OVERRIDE_LOGIC = "nh-override-logic",
            OVERRIDE_ITEM = "nh-override-item",
            OVERRIDE_TURRET = "nh-override-turret",

            EVENT_RAID = "nh-event-raid",

            DEBUGGING = "nh-debugging",
            DEBUG_PANEL = "nh-debug-panel";

    public static boolean enableEffectDetail = true;

    public static boolean changed = false;

    public static OrderedMap<String, Seq<SettingKey<?>>> allSettings = new OrderedMap<>();

    public static String overrideStatus(){
        StringBuilder overrideStatus = new StringBuilder();
        allSettings.get("override").each(setting -> overrideStatus.append(setting.key).append(":").append(getBool(setting.key)).append("|"));
        return overrideStatus.toString();
    }

    public static void load() {
        allSettings.put("control", Seq.with(
                new BoolSetting(START_LOG, false, false),
                new BoolSetting(EFFECT_DETAIL, true, true)
        ));

        allSettings.put("graphic", Seq.with(
                new SliderSetting(GRAPHIC_GRAVITY_TRAP_ALPHA, 0.12f, 0.02f, 0.18f, 0.01f, false)
                        .onChange(value -> NHShaders.alphaInner = value),
                new SliderSetting(GRAPHIC_GRAVITY_TRAP_OUTLINE_ALPHA, 0.45f, 0.30f, 0.75f, 0.05f, false)
                        .onChange(value -> NHShaders.alphaOuter = value)
        ));

        allSettings.put("override", Seq.with(
                new BoolSetting(OVERRIDE_UNIT_SHIELD, false, true),
                new BoolSetting(OVERRIDE_CORE_UNIT, false, true),
                new BoolSetting(OVERRIDE_LOGISTIC, false, true),
                new BoolSetting(OVERRIDE_DRILL, false, true),
                new BoolSetting(OVERRIDE_FACTORIES, false, true),
                new BoolSetting(OVERRIDE_POWER, false, true),
                new BoolSetting(OVERRIDE_UNIT, false, true),
                new BoolSetting(OVERRIDE_LOGIC, false, true),
                new BoolSetting(OVERRIDE_ITEM, false, true),
                new BoolSetting(OVERRIDE_TURRET, false, true)
        ));

        allSettings.put("event", Seq.with(
                new BoolSetting(EVENT_RAID, false, true)
        ));

        allSettings.put("debug", Seq.with(
                new BoolSetting(DEBUGGING, false, true),
                new BoolSetting(DEBUG_PANEL, false, true)
        ));

        allSettings.each((name, seq) -> seq.each(SettingKey::setDefault));

        if (Vars.headless){
            NHSetting.allSettings.get("override").each(setting -> settings.put(setting.key, true));
            NHSetting.allSettings.get("event").each(setting -> settings.put(setting.key, true));
        }

        enableEffectDetail = getBool(EFFECT_DETAIL);
    }

    public static void loadUI() {
        ui.settings.addCategory("@mod.ui.nh-extra-menu", new TextureRegionDrawable(NHContent.icon), NHSetting::buildTable);
    }

    public static void update() {
        enableEffectDetail = getBool(EFFECT_DETAIL);
    }

    public static void buildTable(Table table) {
        table.pane(t -> allSettings.each((category, settings) -> {
            t.label(() -> Core.bundle.get("nh.setting." + category)).row();
            t.image().color(Pal.accent).size(0, 4).growX().pad(4f).row();
            settings.each(settingKey -> settingKey.buildTable(t));
        })).margin(LEN).get().setForceScroll(false, true);
    }

    public static void showDialog() {
        BaseDialog dialog = new BaseDialog("@nh.setting"){
            @Override
            public void hide() {
                super.hide();
                if (changed) ui.showConfirm("@mods.reloadexit", () -> Core.app.exit());
            }
        };
        buildTable(dialog.cont);
        dialog.addCloseButton();
        dialog.show();
    }

    public static boolean enableDetails() {
        return enableEffectDetail;
    }

    public static boolean getBool(String key) {
        return Core.settings.getBool(key);
    }

    public static boolean getBool(String key, boolean def) {
        return Core.settings.getBool(key, def);
    }

    public static float getFloat(String key) {
        return Core.settings.getFloat(key);
    }

    public static float getFloat(String key, float def) {
        return Core.settings.getFloat(key, def);
    }

    public static abstract class SettingKey<T> {
        public final String key;

        public boolean restart = false;
        public Cons<T> onChange = (T value) -> {};
        public T def;

        public SettingKey(String key) {
            this.key = key;
        }

        public void setDefault() {
            if (!Core.settings.has(key)) Core.settings.put(key, def);
        }

        public SettingKey<T> onChange(Cons<T> onChange) {
            this.onChange = onChange;
            return this;
        }

        public abstract T getValue();

        public abstract void buildTable(Table table);
    }

    public static class BoolSetting extends SettingKey<Boolean> {
        public BoolSetting(String key, boolean def, boolean restart) {
            super(key);
            this.def = def;
            this.restart = restart;
        }

        @Override
        public Boolean getValue() {
            return Core.settings.getBool(key);
        }

        @Override
        public void buildTable(Table table) {
            table.table(t -> {
                t.add(new CheckBox(Core.bundle.get("nh.setting." + key + ".name")){{
                    changed(() -> {
                        settings.put(key, isChecked());
                        onChange.get(isChecked());
                        if (restart) {
                            if (!changed) {
                                Dialog.setHideAction(() -> new RunnableAction() {{
                                    setRunnable(() -> ui.showConfirm("@mods.reloadexit", () -> Core.app.exit()));
                                }});
                            }
                            changed = true;
                        }
                    });
                    update(() -> setChecked(settings.getBool(key)));
                }}).padRight(6f).left();
                t.row().table(i -> {
                    i.left();
                    i.defaults().left();
                    i.add("[lightgray]" + Core.bundle.get("nh.setting." + key + ".desc") + "[]").padLeft(6f).wrap()
                            .width(Math.min(Core.graphics.getWidth() / 1.2f, 420f)).row();
                }).growX();
            }).growX().fillY().margin(8f).left().width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).row();
        }
    }

    public static class SliderSetting extends SettingKey<Float> {
        public float min = 0f, max = 1f, step = 0.01f;

        public SliderSetting(String key, float def, float min, float max, float step, boolean restart) {
            super(key);
            this.def = def;
            this.min = min;
            this.max = max;
            this.step = step;

            this.restart = restart;
        }

        public SliderSetting(String key, float def, boolean restart) {
            super(key);
            this.def = def;
            this.restart = restart;
        }

        @Override
        public Float getValue() {
            return Core.settings.getFloat(key);
        }

        @Override
        public void buildTable(Table table) {
            table.table(t -> {
                t.add(new Label(() -> Core.bundle.format("nh.setting." + key + ".name", getValue()))).padRight(6f).left();
                t.row().slider(min, max, step, def, f -> {
                   settings.put(key, f);
                   onChange.get(f);
                }).growX();
                t.row().table(i -> {
                    i.left();
                    i.defaults().left();
                    i.add("[lightgray]" + Core.bundle.get("nh.setting." + key + ".desc") + "[]").padLeft(6f).wrap()
                            .width(Math.min(Core.graphics.getWidth() / 1.2f, 420f)).row();
                }).growX();
            }).growX().fillY().margin(8f).left().width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).row();
        }
    }
}
