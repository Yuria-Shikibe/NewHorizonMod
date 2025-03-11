package newhorizon.expand.logic.statements;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.instructions.RandomSpawnI;
import newhorizon.expand.logic.instructions.RandomTargetI;

public class RandomSpawn extends LStatement {
    public String seed = "0", x = "0", y = "0";

    public RandomSpawn(String[] tokens) {
        seed = tokens[1];
        x = tokens[2];
        y = tokens[3];
    }

    public RandomSpawn() {}

    @Override
    public void build(Table table) {
        rebuild(table);
    }

    void rebuild(Table table) {
        table.add(" Seed: ");
        fields(table, seed, str -> seed = str);
        table.add(" out: ");
        fields(table, x, str -> x = str);
        table.add(", ");
        fields(table, y, str -> y = str);
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RandomSpawnI(builder.var(seed), builder.var(x), builder.var(y));
    }

    @Override
    public LCategory category() {
        return NHContent.NH_CSS;
    }

    public void write(StringBuilder builder) {
        builder.append("randspawn");
        builder.append(" ");
        builder.append(seed);
        builder.append(" ");
        builder.append(x);
        builder.append(" ");
        builder.append(y);
    }
}
