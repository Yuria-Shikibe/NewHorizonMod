package newhorizon.util.ui.dialog;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import newhorizon.util.ui.FeatureLog;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.NewHorizon.MOD;
import static newhorizon.NewHorizon.getUpdateContent;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NewFeatureDialog extends BaseDialog {
    public NewFeatureDialog() {
        super(Core.bundle.get("nh.new-feature.title"));

        shown(this::build);
        onResize(this::build);

        addCloseListener();
    }

    public void build() {
        cont.clear();
        cont.pane(main -> {
            main.top();
            buildMainChangelog(main);
            buildFeatureLog(main);
        }).grow().padLeft(LEN).padRight(LEN).padTop(LEN).row();

        cont.button("@back", Icon.left, Styles.cleart, this::hide).growX().height(LEN).bottom().growX().height(LEN).padTop(OFFSET).padLeft(LEN).padRight(LEN);
    }

    private void buildMainChangelog(Table main){
        main.pane(table -> {
            table.align(Align.topLeft);
            table.add(MOD.meta.version + ": ").row();
            table.image().height(OFFSET / 3).growX().color(Pal.accent).row();
            table.add(Core.bundle.get("mod.ui.update-log")).left();
        }).growX().fillY().padBottom(LEN).row();
        main.image().growX().height(4).pad(6).color(Color.lightGray).row();
    }

    private void buildFeatureLog(Table main){
        main.pane(t -> {
            for (FeatureLog feature: getUpdateContent()) {
                Table importantLabel = new Table(table -> {
                    Label label = new Label("[ancient]<<IMPORTANT UPDATE>>");
                    label.setStyle(Styles.techLabel);
                    label.setFontScale(1.25f);
                    table.add(label).row();
                });
                Table info = new Table(Tex.pane, table -> {
                    if (feature.important) {
                        table.background(Tex.whitePane);
                        table.color.set(Pal.accent);
                    }

                    table.image(feature.icon).size(120).scaling(Scaling.bounded);
                    table.pane(i -> {
                        i.top();

                        if (feature.important) {
                            i.add(importantLabel).pad(2f, 2f, 4f, 2f).row();
                        }

                        i.add(feature.type.localizedName + " [accent]" + feature.getLocalizedTitle() + "[]").left().row();
                        i.image().growX().height(OFFSET / 3).pad(OFFSET / 3).color(Color.lightGray).row();

                        if (feature.description != null) {
                            i.add(Core.bundle.get("nh.new-feature.description")).left().row();
                            i.add(feature.getLocalizedDescription()).padLeft(LEN).left().wrap();
                        }

                        if (feature.modifier != null) i.table(i1 -> {
                            NHUIFunc.show(i1, feature.content);
                            feature.modifier.get(i);
                        }).grow().left().row();

                    }).grow().padLeft(OFFSET).top();
                    table.button(Icon.info, Styles.cleari, LEN, () -> {
                        ContentInfoDialog dialog = new ContentInfoDialog();
                        dialog.show(feature.content);
                    }).growY().width(LEN).padLeft(OFFSET).disabled(b -> feature.content == null);
                });

                t.add(info).grow().row();
            }
        }).growX().top().row();
    }
}
