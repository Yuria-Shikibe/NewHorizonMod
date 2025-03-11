package newhorizon.expand.logic.instructions;

import mindustry.game.Team;
import mindustry.logic.LExecutor;
import newhorizon.expand.logic.ThreatLevel;

public class TeamThreatI implements LExecutor.LInstruction {
    public int team, threat;

    public TeamThreatI(int team, int threat) {
        this.team = team;
        this.threat = threat;
    }

    public TeamThreatI() {}

    @Override
    public void run(LExecutor exec) {
        Team t = exec.team(team);
        if (t == null) return;

        exec.setnum(threat, ThreatLevel.getTeamThreat(t));
    }
}
