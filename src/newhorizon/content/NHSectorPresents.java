package newhorizon.content;

import arc.Core;
import arc.Events;
import mindustry.content.Planets;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.game.Rules;
import mindustry.gen.Call;
import mindustry.type.SectorPreset;
import mindustry.world.blocks.campaign.LandingPad;

public class NHSectorPresents {
    public static SectorPreset primaryBase;
    public static SectorPreset landingPoint;
    public static SectorPreset relicAccess;
    public static SectorPreset streamStation;
    public static SectorPreset edgeZone;
    public static SectorPreset crossBranch;
    public static SectorPreset interferedArray;
    public static SectorPreset hollow;
    public static SectorPreset frozenPeaks;
    private static boolean campaignEventsRegistered;
    private static boolean landingPointTransitionPending;

    public static void load() {
        primaryBase = new SectorPreset("primary-base", NHPlanets.midantha, 1) {{
            alwaysUnlocked = true;
            showHidden = true;
            addStartingItems = true;
            captureWave = 2;
            difficulty = 1;
        }};

        landingPoint = new SectorPreset("landing-point", "new-horizon-LandingPoint", NHPlanets.midantha, 37) {{
            showHidden = true;
            captureWave = 20;
            difficulty = 1;
        }};

        relicAccess = new SectorPreset("relic-access", "new-horizon-RelicAccess", NHPlanets.midantha, 38) {{
            showHidden = true;
            captureWave = 25;
            difficulty = 2;
        }};

        streamStation = new SectorPreset("stream-station", "new-horizon-StreamStation", NHPlanets.midantha, 56) {{
            showHidden = true;
            captureWave = 25;
            difficulty = 2;
        }};

        edgeZone = new SectorPreset("edge-zone", "new-horizon-EdgeZone", NHPlanets.midantha, 39) {{
            showHidden = true;
            captureWave = 0;
            difficulty = 2;
        }};

        crossBranch = new SectorPreset("cross-branch", "new-horizon-CrossBranch", NHPlanets.midantha, 40) {{
            showHidden = true;
            captureWave = 25;
            difficulty = 3;
        }};

        interferedArray = new SectorPreset("interfered-array", "new-horizon-InterferedArray", NHPlanets.midantha, 23) {{
            showHidden = true;
            captureWave = 30;
            difficulty = 3;
        }};

        hollow = new SectorPreset("hollow", "new-horizon-Hollow", NHPlanets.midantha, 22) {{
            showHidden = true;
            captureWave = 30;
            difficulty = 3;
        }};

        frozenPeaks = new SectorPreset("frozen-peaks", "new-horizon-FrozenPeaks", Planets.serpulo, 131) {{
            showHidden = true;
            captureWave = 40;
            difficulty = 4;
        }};

        unlockPrimaryBase();
        registerCampaignEvents();
    }

    public static void applyPrimaryBaseStartingLoadout(Rules rules) {
        if (primaryBase == null || primaryBase.generator.map == null) return;

        if (rules.sector == primaryBase.sector && !primaryBase.sector.info.wasCaptured
                && primaryBase.sector.info.attempts == 0) {
            rules.loadout = primaryBase.generator.map.rules().loadout;
        }
    }

    public static void applyCampaignMapRules(Rules rules) {
        applyMapRules(landingPoint, rules);
        applyMapRules(relicAccess, rules);
        applyMapRules(streamStation, rules);
        applyMapRules(edgeZone, rules);
        applyMapRules(crossBranch, rules);
        applyMapRules(interferedArray, rules);
        applyMapRules(hollow, rules);
    }

    private static void applyMapRules(SectorPreset preset, Rules rules) {
        if (preset == null || preset.generator.map == null || rules.sector != preset.sector) return;

        preset.generator.map.rules(rules);
        rules.sector = preset.sector;
        rules.planet = preset.planet;
    }

    private static void registerCampaignEvents() {
        if (campaignEventsRegistered) return;
        campaignEventsRegistered = true;

        Events.run(EventType.Trigger.update, NHSectorPresents::updatePrimaryBaseLandingPads);
        Events.run(EventType.Trigger.update, NHSectorPresents::resetAbandonedPrimaryBase);
        Events.run(EventType.Trigger.update, NHSectorPresents::finishLandingPointTransition);
        Events.on(EventType.GameOverEvent.class, event -> {
            if (Vars.state.isCampaign() && Vars.state.rules.sector == primaryBase.sector) {
                resetPrimaryBase();
            }
        });
        Events.on(EventType.SectorLoseEvent.class, event -> {
            if (event.sector == primaryBase.sector) {
                resetPrimaryBase();
            }
        });
    }

    public static void launchLandingPointFromPrimaryBase() {
        if (landingPointTransitionPending || Vars.headless || Vars.net.client()
                || primaryBase == null || landingPoint == null) return;

        landingPointTransitionPending = true;
        Core.app.post(() -> {
            if (!Vars.state.isCampaign() || Vars.state.rules.sector != primaryBase.sector) {
                landingPointTransitionPending = false;
                return;
            }

            Vars.control.playSector(primaryBase.sector, landingPoint.sector);
        });
    }

    private static void deletePrimaryBaseSave() {
        if (primaryBase.sector.save != null) {
            primaryBase.sector.save.delete();
            primaryBase.sector.save = null;
        }

        lockPrimaryBase();
        Core.settings.manualSave();
    }

    private static void resetPrimaryBase() {
        if (primaryBase == null || primaryBase.sector == null || primaryBase.sector.info.wasCaptured) return;

        if (primaryBase.sector.save != null) {
            primaryBase.sector.save.delete();
            primaryBase.sector.save = null;
        }

        primaryBase.sector.clearInfo();
        Core.settings.manualSave();
    }

    public static void unlockPrimaryBase() {
        if (primaryBase == null || primaryBase.sector == null) return;

        primaryBase.alwaysUnlocked = true;
        primaryBase.requireUnlock = true;
        NHPlanets.midantha.startSector = primaryBase.sector.id;
    }

    public static void lockPrimaryBase() {
        if (primaryBase == null || primaryBase.sector == null || landingPoint == null) return;

        primaryBase.alwaysUnlocked = false;
        primaryBase.requireUnlock = false;
        primaryBase.clearUnlock();
        NHPlanets.midantha.startSector = landingPoint.sector.id;
    }

    private static void finishLandingPointTransition() {
        if (!landingPointTransitionPending || !Vars.state.isCampaign()
                || Vars.state.rules.sector != landingPoint.sector) return;

        landingPointTransitionPending = false;
        deletePrimaryBaseSave();
    }

    private static void resetAbandonedPrimaryBase() {
        if (primaryBase == null || primaryBase.sector == null || primaryBase.sector.info.wasCaptured
                || primaryBase.sector.isBeingPlayed() || primaryBase.sector.info.hasCore) return;

        resetPrimaryBase();
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
