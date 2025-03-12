package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.GravityWellI;
import newhorizon.expand.logic.instructions.RandomSpawnI;

public class GravityWell extends LStatement {
    public String x = "0", y = "0", out = "0";

    public GravityWell(String[] tokens) {
        x = tokens[1];
        y = tokens[2];
        out = tokens[3];
    }

    public GravityWell() {}

    @Override
    public void build(Table table) {
        rebuild(table);
    }

    void rebuild(Table table) {
        table.add(" Pos: ");
        fields(table, x, str -> x = str);
        table.add(", ");
        fields(table, y, str -> y = str);
        table.add(" Is Gravity: ");
        fields(table, out, str -> out = str);
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new GravityWellI(builder.var(x), builder.var(y), builder.var(out));
    }

    @Override
    public LCategory category() {
        return NHContent.NH_CSS;
    }

    public void write(StringBuilder builder) {
        builder.append("gravitywell");
        builder.append(" ");
        builder.append(x);
        builder.append(" ");
        builder.append(y);
        builder.append(" ");
        builder.append(out);
    }
}
