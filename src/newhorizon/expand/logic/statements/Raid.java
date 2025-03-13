package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.LineTargetI;
import newhorizon.expand.logic.instructions.RaidI;

public class Raid extends LStatement {
    public String team = "@sharded", type = "0", seed = "0", count = "0", sourceX = "0", sourceY = "0", targetX = "0", targetY = "0", inaccuracy = "0";

    public Raid(String[] tokens) {
        team = tokens[1];
        type = tokens[2];
        seed = tokens[3];
        count = tokens[4];
        sourceX = tokens[5];
        sourceY = tokens[6];
        targetX = tokens[7];
        targetY = tokens[8];
        inaccuracy = tokens[9];
    }

    public Raid() {}

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Source Team: ");
            fields(t, team, str -> team = str);
            t.add(" Raid Type: ");
            fields(t, type, str -> type = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Rand Seed: ");
            fields(t, seed, str -> seed = str);
            t.add(" Raid Count: ");
            fields(t, count, str -> count = str);
            t.add(" Raid Inaccuracy: ");
            fields(t, inaccuracy, str -> inaccuracy = str);
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
            t.add(" Target Position: ");
            fields(t, targetX, str -> targetX = str);
            t.add(" , ");
            fields(t, targetY, str -> targetY = str);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RaidI(builder.var(team), builder.var(type), builder.var(seed), builder.var(count), builder.var(sourceX), builder.var(sourceY), builder.var(targetX), builder.var(targetY), builder.var(inaccuracy));
    }

    @Override
    public LCategory category() {
        return NHContent.nhcutscene;
    }

    public void write(StringBuilder builder) {
        builder.append("raid");
        builder.append(" ");
        builder.append(team);
        builder.append(" ");
        builder.append(type);
        builder.append(" ");
        builder.append(seed);
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
