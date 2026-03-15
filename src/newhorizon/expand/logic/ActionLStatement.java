package newhorizon.expand.logic;

import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.logic.LAssembler;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.expand.logic.components.Action;

/**
 * Base class for logic statements.
 */
public class ActionLStatement extends LStatement {

    public ActionLStatement(String[] tokens) {}

    public ActionLStatement() {}

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

    @SuppressWarnings("InnerClassMayBeStatic")
    public class ActionInstruction implements LExecutor.LInstruction {
        public ActionInstruction() {}

        @Override
        public void run(LExecutor exec) {}
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class BasicAction extends Action {
        public BasicAction(float duration) {
            super(duration * Time.toSeconds);
        }
    }
}
