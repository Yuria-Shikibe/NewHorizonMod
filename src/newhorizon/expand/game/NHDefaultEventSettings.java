package newhorizon.expand.game;

import arc.Core;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.gen.Call;
import mindustry.type.Planet;
import newhorizon.content.NHPlanets;
import newhorizon.expand.net.NHCall;

/** Per-planet campaign setting for NH's automatically scheduled events. */
public final class NHDefaultEventSettings {
    private static final String settingPrefix = "nh-default-events-";

    private NHDefaultEventSettings() {
    }

    public static boolean enabled(@Nullable Planet planet) {
        return planet == null || Core.settings.getBool(key(planet), enabledByDefault(planet));
    }

    public static boolean enabledForCurrentGame() {
        if (Vars.state == null || !Vars.state.isCampaign()) return true;
        return enabled(Vars.state.getPlanet());
    }

    public static void setEnabled(Planet planet, boolean enabled) {
        Core.settings.put(key(planet), enabled);
    }

    public static void applyToCurrentWorld(Planet planet) {
        if (Vars.state == null || !Vars.state.isGame() || !Vars.state.isCampaign()
                || Vars.state.getPlanet() != planet || Vars.net.client()) return;

        boolean enabled = enabled(planet);
        NHCall.applyRaidScale(enabled ? 1f : 0f, null);
        NHCall.applyInterventionScale(enabled ? 1f : 0f, null);
        SpecialEventState.setEnabled(enabled);
        Call.setRules(Vars.state.rules);
    }

    public static boolean enabledByDefault(Planet planet) {
        if (planet == Planets.serpulo || planet == Planets.erekir) return false;
        if (planet == NHPlanets.midantha || planet == NHPlanets.ringWorld) return true;
        return true;
    }

    private static String key(Planet planet) {
        return settingPrefix + planet.name;
    }
}
