package newhorizon.expand.logic;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;

/**
 * Base class for logic statements.
 */
public class NHLStatement extends LStatement {

    public NHLStatement(String[] tokens) {}

    public NHLStatement() {}

    public String getLStatementName() {
        return "statement";
    }

    @Override
    public void build(Table table) {}

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return null;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append(getLStatementName());
    }

    public void writeTokens(StringBuilder builder, String... tokens) {
        for (String token : tokens) {
            builder.append(" ").append(token);
        }
    }
}
