package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.LineTargetI;

public class LineTarget extends LStatement {
    public String team = "@sharded", sourceX = "0", sourceY = "0", targetX = "0", targetY = "0", outX = "0", outY = "0";

    public LineTarget(String[] tokens) {
        team = tokens[1];
        sourceX = tokens[2];
        sourceY = tokens[3];
        targetX = tokens[4];
        targetY = tokens[5];
        outX = tokens[6];
        outY = tokens[7];
    }

    public LineTarget() {
    }

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Target Team: ");
            fields(t, team, str -> team = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Source Position: ");
            fields(t, sourceX, str -> sourceX = str);
            t.add(" , ");
            fields(t, sourceY, str -> sourceY = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" End Position: ");
            fields(t, targetX, str -> targetX = str);
            t.add(" , ");
            fields(t, targetY, str -> targetY = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Out Position: ");
            fields(t, outX, str -> outX = str);
            t.add(" , ");
            fields(t, outY, str -> outY = str);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new LineTargetI(builder.var(team), builder.var(sourceX), builder.var(sourceY), builder.var(targetX), builder.var(targetY), builder.var(outX), builder.var(outY));
    }

    @Override
    public LCategory category() {
        return NHContent.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("linetarget");
        builder.append(" ");
        builder.append(team);
        builder.append(" ");
        builder.append(sourceX);
        builder.append(" ");
        builder.append(sourceY);
        builder.append(" ");
        builder.append(targetX);
        builder.append(" ");
        builder.append(targetY);
        builder.append(" ");
        builder.append(outX);
        builder.append(" ");
        builder.append(outY);
    }
}
