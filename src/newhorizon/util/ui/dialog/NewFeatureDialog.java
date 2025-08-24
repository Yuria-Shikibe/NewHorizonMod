package newhorizon.util.ui.dialog;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Scaling;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.ContentInfoDialog;
import newhorizon.content.NHContent;
import newhorizon.content.blocks.*;
import newhorizon.util.ui.FeatureLog;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.NewHorizon.MOD;
import static newhorizon.util.ui.TableFunc.LEN;
import static newhorizon.util.ui.TableFunc.OFFSET;

public class NewFeatureDialog extends BaseDialog {
    public NewFeatureDialog() {
        super(Core.bundle.get("nh.new-feature.title"));

        shown(this::build);
        onResize(this::build);

        addCloseListener();
    }

    public static FeatureLog[] getUpdateContent() {
        Seq<FeatureLog> updates = Seq.with(
                new FeatureLog(0, FeatureLog.featureType.IMPORTANT, NHContent.icon2),
                new FeatureLog(0, FeatureLog.featureType.FEATURE, NHContent.objective),
                new FeatureLog(1, FeatureLog.featureType.FEATURE, NHContent.fleet),
                new FeatureLog(2, FeatureLog.featureType.FEATURE, Core.atlas.find("new-horizon-incursion")),
                new FeatureLog(3, FeatureLog.featureType.FEATURE, NHContent.capture),
                new FeatureLog(4, FeatureLog.featureType.FEATURE, NHContent.raid),

                new FeatureLog(0, FeatureLog.featureType.CONTENT, NHContent.objective),
                new FeatureLog(1, FeatureLog.featureType.CONTENT, NHContent.objective),
                new FeatureLog(2, FeatureLog.featureType.CONTENT, NHContent.objective),
                new FeatureLog(3, FeatureLog.featureType.CONTENT, NHContent.objective),

                new FeatureLog(1, FeatureLog.featureType.IMPORTANT, NHContent.pointerRegion)
        );

        ModuleBlock.modules.each(module -> updates.add(new FeatureLog(module)));

        updates.add(new FeatureLog(CraftingBlock.stampingFacility));
        updates.add(new FeatureLog(CraftingBlock.processorPrinter));
        updates.add(new FeatureLog(CraftingBlock.crucibleFoundry));
        updates.add(new FeatureLog(CraftingBlock.crystallizer));
        updates.add(new FeatureLog(CraftingBlock.surgeRefactor));
        updates.add(new FeatureLog(CraftingBlock.fabricSynthesizer));
        updates.add(new FeatureLog(CraftingBlock.processorEncoder));
        updates.add(new FeatureLog(CraftingBlock.irdryonMixer));
        updates.add(new FeatureLog(CraftingBlock.multipleSteelFactory));
        updates.add(new FeatureLog(CraftingBlock.irayrondFactory));
        updates.add(new FeatureLog(CraftingBlock.setonFactory));
        updates.add(new FeatureLog(CraftingBlock.upgradeSortFactory));
        updates.add(new FeatureLog(CraftingBlock.ancimembraneConcentrator));
        updates.add(new FeatureLog(CraftingBlock.factory0));
        updates.add(new FeatureLog(CraftingBlock.factory1));
        updates.add(new FeatureLog(CraftingBlock.factory2));
        updates.add(new FeatureLog(CraftingBlock.factory3));
        updates.add(new FeatureLog(CraftingBlock.factory4));
        updates.add(new FeatureLog(CraftingBlock.factory5));
        updates.add(new FeatureLog(CraftingBlock.factory6));

        updates.add(new FeatureLog(DistributionBlock.conveyor));
        updates.add(new FeatureLog(DistributionBlock.logisticsJunction));
        updates.add(new FeatureLog(DistributionBlock.logisticsDirectionalRouter));
        updates.add(new FeatureLog(DistributionBlock.logisticsDirectionalMerger));
        updates.add(new FeatureLog(DistributionBlock.logisticsDirectionalGate));
        updates.add(new FeatureLog(DistributionBlock.logisticsOmniGate));
        updates.add(new FeatureLog(DistributionBlock.logisticsOmniSorter));
        updates.add(new FeatureLog(DistributionBlock.logisticsOmniBlocker));
        updates.add(new FeatureLog(DistributionBlock.conveyorBridge));
        updates.add(new FeatureLog(DistributionBlock.conveyorBridgeExtend));
        updates.add(new FeatureLog(DistributionBlock.conveyorUnloader));
        updates.add(new FeatureLog(DistributionBlock.rapidUnloader));

        updates.add(new FeatureLog(LiquidBlock.turboPumpSmall));
        updates.add(new FeatureLog(LiquidBlock.turboPump));
        updates.add(new FeatureLog(LiquidBlock.standardLiquidStorage));
        updates.add(new FeatureLog(LiquidBlock.heavyLiquidStorage));

        updates.add(new FeatureLog(LogicBlock.iconDisplaySmall));
        updates.add(new FeatureLog(LogicBlock.iconDisplay));
        updates.add(new FeatureLog(LogicBlock.characterDisplaySmall));
        updates.add(new FeatureLog(LogicBlock.characterDisplay));

        updates.add(new FeatureLog(PayloadBlock.payloadRail));
        updates.add(new FeatureLog(PayloadBlock.payloadRouter));

        updates.add(new FeatureLog(PowerBlock.gravityTrapMidantha));
        updates.add(new FeatureLog(PowerBlock.gravityTrapSerpulo));
        updates.add(new FeatureLog(PowerBlock.gravityTrapErekir));
        updates.add(new FeatureLog(PowerBlock.gravityTrapSmall));
        updates.add(new FeatureLog(PowerBlock.gravityTrap));
        updates.add(new FeatureLog(PowerBlock.armorBattery));
        updates.add(new FeatureLog(PowerBlock.armorBatteryLarge));
        updates.add(new FeatureLog(PowerBlock.armorBatteryHuge));
        updates.add(new FeatureLog(PowerBlock.zetaGenerator));
        updates.add(new FeatureLog(PowerBlock.anodeFusionReactor));
        updates.add(new FeatureLog(PowerBlock.cathodeFusionReactor));
        updates.add(new FeatureLog(PowerBlock.thermoReactor));

        updates.add(new FeatureLog(SpecialBlock.standardStorage));
        updates.add(new FeatureLog(SpecialBlock.heavyStorage));
        updates.add(new FeatureLog(SpecialBlock.juniorModuleBeacon));
        updates.add(new FeatureLog(SpecialBlock.seniorModuleBeacon));

        updates.add(new FeatureLog(DefenseBlock.standardForceProjector));
        updates.add(new FeatureLog(DefenseBlock.largeShieldGenerator));
        updates.add(new FeatureLog(DefenseBlock.standardRegenProjector));
        updates.add(new FeatureLog(DefenseBlock.heavyRegenProjector));

        return updates.toArray(FeatureLog.class);
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

    private void buildMainChangelog(Table main) {
        main.pane(table -> {
            table.align(Align.topLeft);
            table.add(MOD.meta.version + ": ").row();
            table.image().height(OFFSET / 3).growX().color(Pal.accent).row();
            table.add(Core.bundle.get("mod.ui.update-log")).left();
        }).growX().fillY().padBottom(LEN).row();
        main.image().growX().height(4).pad(6).color(Color.lightGray).row();
    }

    private void buildFeatureLog(Table main) {
        main.pane(t -> {
            for (FeatureLog feature : getUpdateContent()) {
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
                            i.add(feature.getLocalizedDescription()).padLeft(LEN).left().growX().wrap();
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
