package newhorizon.expand.logic.instructions;

import mindustry.game.Team;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.expand.logic.ThreatLevel;

public class TeamThreatI implements LExecutor.LInstruction {
    public LVar team, threat;

    public TeamThreatI(LVar team, LVar threat) {
        this.team = team;
        this.threat = threat;
    }

    public TeamThreatI() {}

    @Override
    public void run(LExecutor exec) {
        Team t = team.team();
        if (t == null) return;

        threat.setnum(ThreatLevel.getTeamThreat(t));
    }
}
