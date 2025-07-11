package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.RandomTargetI;

public class RandomTarget extends LStatement {
    public String team = "@sharded", seed = "0", x = "tx", y = "ty", turretW = "0", generatorW = "0", factoryW = "0", coreW = "1";

    public RandomTarget(String[] tokens) {
        team = tokens[1];
        seed = tokens[2];
        x = tokens[3];
        y = tokens[4];
        turretW = tokens[5];
        generatorW = tokens[6];
        factoryW = tokens[7];
        coreW = tokens[8];
    }

    public RandomTarget() {
    }

    @Override
    public void build(Table table) {
        rebuild(table);
    }

    void rebuild(Table table) {
        table.table(t -> {
            t.add(" Team: ");
            fields(t, team, str -> team = str);
            t.add(" Seed: ");
            fields(t, seed, str -> seed = str);
            t.add(" out: ");
            fields(t, x, str -> x = str);
            t.add(", ");
            fields(t, y, str -> y = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Turret Weight: ");
            fields(t, turretW, str -> turretW = str);
            t.add(" Generator Weight: ");
            fields(t, generatorW, str -> generatorW = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add(" Factory Weight: ");
            fields(t, factoryW, str -> factoryW = str);
            t.add(" Core Weight: ");
            fields(t, coreW, str -> coreW = str);
        }).left();
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RandomTargetI(builder.var(team), builder.var(seed), builder.var(x), builder.var(y), builder.var(turretW), builder.var(generatorW), builder.var(factoryW), builder.var(coreW));
    }

    @Override
    public LCategory category() {
        return NHContent.nhwproc;
    }

    public void write(StringBuilder builder) {
        builder.append("randtarget");
        builder.append(" ");
        builder.append(team);
        builder.append(" ");
        builder.append(seed);
        builder.append(" ");
        builder.append(x);
        builder.append(" ");
        builder.append(y);
        builder.append(" ");
        builder.append(turretW);
        builder.append(" ");
        builder.append(generatorW);
        builder.append(" ");
        builder.append(factoryW);
        builder.append(" ");
        builder.append(coreW);
    }
}
