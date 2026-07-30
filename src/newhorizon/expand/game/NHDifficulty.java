package newhorizon.expand.game;

import arc.math.Mathf;
import mindustry.game.Difficulty;
import mindustry.game.Team;
import mindustry.type.Planet;

import static mindustry.Vars.state;

/** Campaign difficulty helpers for NH-controlled spawns. */
public final class NHDifficulty {
    private NHDifficulty() {
    }

    /**
     * Difficulty is a campaign setting. Non-campaign games, including ordinary dedicated-server maps,
     * retain their existing event behavior.
     */
    public static boolean appliesToCurrentGame() {
        return state != null && state.isCampaign() && state.getPlanet() != null;
    }

    public static Difficulty current() {
        Planet planet = state == null ? null : state.getPlanet();
        if (planet == null || planet.campaignRules == null || planet.campaignRules.difficulty == null) {
            return Difficulty.normal;
        }
        return planet.campaignRules.difficulty;
    }

    public static int scaleEnemySpawnCount(Team team, int count) {
        if (count <= 0 || !appliesToCurrentGame() || team == null || team != state.rules.waveTeam) {
            return count;
        }
        return Math.max(1, Mathf.round(count * current().enemySpawnMultiplier));
    }
}
