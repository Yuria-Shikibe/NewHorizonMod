package newhorizon.util.ui.dialog;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.game.CampaignRules;
import mindustry.game.Difficulty;
import mindustry.gen.Call;
import mindustry.gen.Tex;
import mindustry.type.Planet;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CampaignRulesDialog;
import newhorizon.expand.game.NHDefaultEventSettings;

/** Adds NH's per-planet event switch to the vanilla campaign difficulty dialog. */
public class NHCampaignRulesDialog extends CampaignRulesDialog {
    private Planet selectedPlanet;
    private boolean defaultEventsChanged;

    public NHCampaignRulesDialog() {
        hidden(() -> {
            if (selectedPlanet == null) return;
            selectedPlanet.saveRules();
            if (Vars.state.isGame() && Vars.state.isCampaign() && Vars.state.getPlanet() == selectedPlanet) {
                selectedPlanet.campaignRules.apply(selectedPlanet, Vars.state.rules);
                if (defaultEventsChanged) NHDefaultEventSettings.applyToCurrentWorld(selectedPlanet);
                Call.setRules(Vars.state.rules);
            }
        });
        onResize(() -> rebuild());
    }

    @Override
    public void show(Planet planet) {
        selectedPlanet = planet;
        defaultEventsChanged = false;
        rebuild();
        show();
    }

    private void rebuild() {
        if (selectedPlanet == null) return;
        CampaignRules rules = selectedPlanet.campaignRules;
        cont.clear();
        cont.top().pane(inner -> {
            inner.top().left().defaults().fillX().left().pad(5f);
            inner.table(Tex.button, t -> {
                t.margin(10f);
                var group = new arc.scene.ui.ButtonGroup<>();
                t.defaults().size(140f, 50f);
                for (Difficulty diff : Difficulty.all) {
                    t.button(diff.localized(), Styles.flatTogglet, () -> rules.difficulty = diff)
                            .group(group).checked(b -> rules.difficulty == diff).tooltip(diff.info());
                    if (Core.graphics.isPortrait() && diff.ordinal() % 2 == 1) t.row();
                }
            }).left().fill(false).expand(false, false).row();

            if (selectedPlanet.allowSectorInvasion) {
                check(inner, "@rules.invasions", b -> rules.sectorInvasion = b, () -> rules.sectorInvasion);
            }
            check(inner, "@rules.fog", b -> rules.fog = b, () -> rules.fog);
            check(inner, "@rules.hidespawns", b -> rules.hideSpawns = b, () -> rules.hideSpawns);
            check(inner, "@rules.randomwaveai", b -> rules.randomWaveAI = b, () -> rules.randomWaveAI);
            check(inner, "@rules.pauseDisabled", b -> rules.pauseDisabled = b, () -> rules.pauseDisabled);
            if (selectedPlanet.showRtsAIRule) {
                check(inner, "@rules.rtsai.campaign", b -> rules.rtsAI = b, () -> rules.rtsAI);
            }
            if (!selectedPlanet.clearSectorOnLose) {
                check(inner, "@rules.clearsectoronloss", b -> rules.clearSectorOnLose = b, () -> rules.clearSectorOnLose);
            }
            check(inner, "@nh.campaign-rules.default-events", enabled -> {
                NHDefaultEventSettings.setEnabled(selectedPlanet, enabled);
                defaultEventsChanged = true;
            }, () -> NHDefaultEventSettings.enabled(selectedPlanet));
        }).growY();
    }

    private void check(Table table, String text, arc.func.Boolc listener, arc.func.Boolp provider) {
        String infoText = text.substring(1) + ".info";
        var cell = table.check(text, listener).checked(provider.get()).update(a -> a.setDisabled(false));
        if (Core.bundle.has(infoText)) cell.tooltip(text + ".info");
        cell.get().left();
        table.row();
    }
}
