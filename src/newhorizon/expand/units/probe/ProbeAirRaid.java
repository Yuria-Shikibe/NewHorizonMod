package newhorizon.expand.units.probe;

import arc.util.Time;
import mindustry.gen.Building;
import mindustry.game.Team;
import newhorizon.expand.game.DefaultRaid;

import static mindustry.Vars.state;
import static mindustry.Vars.net;

public final class ProbeAirRaid {
    private ProbeAirRaid() {
    }

    public static void launch(ProbeScannerEntity scanner, Building target) {
        if (scanner == null || target == null || !target.isValid() || net.client()) return;

        Team friendly = state == null || state.rules == null ? null : state.rules.defaultTeam;
        Team threatTeam = scanner.team == friendly ? scanner.team : target.team;
        DefaultRaid.launchProbeRaid(scanner.team, threatTeam, scanner.x, scanner.y, target,
                (int) (Time.time + scanner.id() * 31L));
    }
}
