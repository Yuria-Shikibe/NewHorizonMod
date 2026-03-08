package newhorizon.expand.logic;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;

/**
 * Base class for logic statements.
 */
public class NHLStatement extends LStatement {
    public static int tokenIndex = 0;
    public static int tokenLength = 0;

    public NHLStatement(String[] tokens) {

    }

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
}
