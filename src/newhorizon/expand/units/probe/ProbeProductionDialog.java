package newhorizon.expand.units.probe;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.expand.net.NHCall;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public final class ProbeProductionDialog {
    private static boolean open;

    private ProbeProductionDialog() {
    }

    public static void show(ProbeEntity factory) {
        if (open || Vars.headless || Vars.player == null || Vars.ui == null
                || factory == null || !factory.isValid() || factory.team != Vars.player.team()) return;

        open = true;
        Core.app.post(() -> {
            if (!factory.isValid()) {
                open = false;
                return;
            }

            var options = ProbeProductionPlan.options(factory);
            BaseDialog dialog = new BaseDialog(\u0022@mod.ui.probe-production-title\u0022);
            dialog.addCloseButton();
            dialog.hidden(() -> open = false);

            if (options.isEmpty()) {
                dialog.cont.add(\u0022@mod.ui.probe-production-empty\u0022).pad(20f);
            } else {
                dialog.cont.pane(Styles.noBarPane, table -> buildOptions(dialog, factory, options, table))
                        .width(Math.min(Core.graphics.getWidth() * 0.8f, 520f))
                        .height(Math.min(options.size * 54f, Core.graphics.getHeight() * 0.7f));
            }

            dialog.show();
        });
    }

    private static void buildOptions(BaseDialog dialog, ProbeEntity factory, arc.struct.Seq<UnitType> options, Table table) {
        table.top();
        for (UnitType type : options) {
            table.button(button -> {
                if (type.uiIcon != null) button.image(type.uiIcon).size(36f).padRight(8f);
                button.add(type.localizedName).left();
            }, Styles.cleart, () -> {
                boolean selected = NHCall.selectProbeProduction(factory, type);
                if (!selected && !Vars.net.client()) {
                    Vars.ui.showInfoToast(Core.bundle.get(\u0022mod.ui.probe-production-denied\u0022), 2f);
                }
                dialog.hide();
            }).growX().height(50f).pad(2f).row();
        }
    }
}
