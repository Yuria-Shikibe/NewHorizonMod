package newhorizon.expand.units.probe;

import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import newhorizon.content.NHBullets;
import newhorizon.expand.logic.ThreatLevel;

import static mindustry.Vars.net;

public final class ProbeAirRaid {
    private ProbeAirRaid() {
    }

    public static void launch(ProbeScannerEntity scanner, Building target) {
        if (scanner == null || target == null || !target.isValid() || net.client()) return;

        int threat = Mathf.clamp(ThreatLevel.getTeamThreat(scanner.team), 1, 8);
        Rand random = new Rand((int) (Time.time + scanner.id() * 31L));
        int heavyMissiles = 2 + threat;
        for (int i = 0; i < heavyMissiles; i++) {
            spawn(NHBullets.airRaidMissile, scanner.team, target, random);
        }

        if (threat >= 4 && NHBullets.skyMissile != null) {
            for (int i = 0; i < threat / 2; i++) {
                spawn(NHBullets.skyMissile, scanner.team, target, random);
            }
        }
    }

    private static void spawn(UnitType type, mindustry.game.Team team, Building target, Rand random) {
        if (type == null) return;

        float angle = random.random(360f);
        float distance = random.random(100f, 220f);
        Unit missile = type.spawn(team,
                target.x + Mathf.cosDeg(angle) * distance,
                target.y + Mathf.sinDeg(angle) * distance,
                angle + 180f);
        if (missile != null && missile.controller() instanceof AIController ai) {
            ai.commandTarget(target);
        }
    }
}
