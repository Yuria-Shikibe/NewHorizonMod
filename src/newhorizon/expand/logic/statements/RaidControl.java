package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.RaidControlI;

public class RaidControl extends LStatement {
    public String flag = "flag", timer = "event-timer", alertTime = "10", raidTime = "5", team = "@sharded",
            type = "0", count = "10", sourceX = "sx", sourceY = "sy", targetX = "tx", targetY = "ty", inaccuracy = "0";

    public RaidControl(String[] tokens) {
        flag = tokens[1];
        timer = tokens[2];
        alertTime = tokens[3];
        raidTime = tokens[4];
        team = tokens[5];
        type = tokens[6];
        count = tokens[7];
        sourceX = tokens[8];
        sourceY = tokens[9];
        targetX = tokens[10];
        targetY = tokens[11];
        inaccuracy = tokens[12];
    }

    public RaidControl() {

    }

    @Override
    public void build(Table table) {
        float width = 300f;
        table.table(t -> {
            t.add(" Using Objective Flag : ");
            fields(t, flag, str -> flag = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Using Objective Timer: ");
            fields(t, timer, str -> timer = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Alert Time: ");
            fields(t, alertTime, str -> alertTime = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Raid Time: ");
            fields(t, raidTime, str -> raidTime = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Source Team: ");
            fields(t, team, str -> team = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Raid Type: ");
            fields(t, type, str -> type = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Raid Count: ");
            fields(t, count, str -> count = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Raid Inaccuracy: ");
            fields(t, inaccuracy, str -> inaccuracy = str).width(width);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Source Position: ");
            fields(t, sourceX, str -> sourceX = str).width(120);
            t.add(" , ");
            fields(t, sourceY, str -> sourceY = str).width(120);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Target Position: ");
            fields(t, targetX, str -> targetX = str).width(120);
            t.add(" , ");
            fields(t, targetY, str -> targetY = str).width(120);
        }).left();
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RaidControlI(
                builder.var(flag),
                builder.var(timer),
                builder.var(alertTime),
                builder.var(raidTime),
                builder.var(team),
                builder.var(type),
                builder.var(count),
                builder.var(sourceX),
                builder.var(sourceY),
                builder.var(targetX),
                builder.var(targetY),
                builder.var(inaccuracy)
        );
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHContent.nhwproc;
    }

    public void write(StringBuilder builder) {
        builder.append("raidcontrol");
        builder.append(" ");
        builder.append(flag);
        builder.append(" ");
        builder.append(timer);
        builder.append(" ");
        builder.append(alertTime);
        builder.append(" ");
        builder.append(raidTime);
        builder.append(" ");
        builder.append(team);
        builder.append(" ");
        builder.append(type);
        builder.append(" ");
        builder.append(count);
        builder.append(" ");
        builder.append(sourceX);
        builder.append(" ");
        builder.append(sourceY);
        builder.append(" ");
        builder.append(targetX);
        builder.append(" ");
        builder.append(targetY);
        builder.append(" ");
        builder.append(inaccuracy);
    }
}
