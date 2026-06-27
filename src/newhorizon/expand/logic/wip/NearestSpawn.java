package newhorizon.expand.logic.wip;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHLogic;

public class NearestSpawn extends LStatement {
    public String targetX = "tx", targetY = "ty", x = "sx", y = "sy";

    public NearestSpawn(String[] tokens) {
        targetX = tokens[1];
        targetY = tokens[2];
        x = tokens[3];
        y = tokens[4];
    }

    public NearestSpawn() {
    }

    @Override
    public void build(Table table) {
        table.table(t -> {
            t.add(" Target: ");
            fields(t, targetX, str -> targetX = str);
            t.add(" , ");
            fields(t, targetY, str -> targetY = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Nearest Spawn Out: ");
            fields(t, x, str -> x = str);
            t.add(" , ");
            fields(t, y, str -> y = str);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new NearestSpawnI(builder.var(targetX), builder.var(targetY), builder.var(x), builder.var(y));
    }

    @Override
    public LCategory category() {
        return NHLogic.nhwproc;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("nearspawn");
        builder.append(" ");
        builder.append(targetX);
        builder.append(" ");
        builder.append(targetY);
        builder.append(" ");
        builder.append(x);
        builder.append(" ");
        builder.append(y);
    }
}
