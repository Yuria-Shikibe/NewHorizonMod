package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.TeamThreatI;

public class TeamThreat extends LStatement {
    public String team = "@sharded", threat = "0";

    public TeamThreat(String[] tokens) {
        team = tokens[1];
        threat = tokens[2];
    }

    public TeamThreat() {
    }

    @Override
    public void build(Table table) {
        table.add(" Team: ");
        fields(table, team, str -> team = str);
        table.add(" Out Threat: ");
        fields(table, threat, str -> threat = str);
    }


    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new TeamThreatI(builder.var(team), builder.var(threat));
    }

    @Override
    public LCategory category() {
        return NHContent.nhwproc;
    }

    public void write(StringBuilder builder) {
        builder.append("teamthreat");
        builder.append(" ");
        builder.append(team);
        builder.append(" ");
        builder.append(threat);
    }
}
