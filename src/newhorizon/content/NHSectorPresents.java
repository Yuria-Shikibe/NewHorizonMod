package newhorizon.content;

import arc.Events;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.type.SectorPreset;
import mindustry.world.blocks.campaign.LandingPad;

public class NHSectorPresents {
    public static SectorPreset primaryBase;
    private static boolean campaignEventsRegistered;

    public static void load() {
        primaryBase = new SectorPreset("primary-base", NHPlanets.midantha, 1) {{
            alwaysUnlocked = true;
            showHidden = true;
            addStartingItems = true;
            captureWave = 2;
            difficulty = 1;
        }};

        NHPlanets.midantha.startSector = primaryBase.sector.id;
        registerCampaignEvents();
    }

    public static void applyPrimaryBaseStartingLoadout(Rules rules) {
        if (primaryBase == null || primaryBase.generator.map == null) return;

        if (rules.sector == primaryBase.sector && !primaryBase.sector.info.wasCaptured
                && primaryBase.sector.info.attempts == 0) {
            rules.loadout = primaryBase.generator.map.rules().loadout;
        }
    }

    private static void registerCampaignEvents() {
        if (campaignEventsRegistered) return;
        campaignEventsRegistered = true;

        Events.run(EventType.Trigger.update, NHSectorPresents::updatePrimaryBaseLandingPads);
    }

    private static void updatePrimaryBaseLandingPads() {
        if (Vars.state == null || !Vars.state.isGame() || !Vars.state.isCampaign() || Vars.net.client()) return;
        if (primaryBase == null || primaryBase.sector == null
                || Vars.state.rules.sector != primaryBase.sector
                || primaryBase.sector.info.wasCaptured) return;

        Vars.state.rules.defaultTeam.data().getBuildings(Blocks.landingPad).each(building -> {
            if (!(building instanceof LandingPad.LandingPadBuild pad)) return;
            if (pad.config == null || pad.arriving != null || pad.cooldown > 0f
                    || pad.efficiency <= 0f || pad.items.total() > 0) return;

            Call.landingPadLanded(pad.tile);
        });
    }
}
