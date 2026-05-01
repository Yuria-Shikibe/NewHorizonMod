package newhorizon.expand.logic.cutscene.action;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;

public class InputLock extends ActionLStatement {
    public InputLock(String[] token) {}

    public InputLock() {}

    @Override
    public String getLStatementName() {
        return "inputlock";
    }

    @Override
    public LCategory category() {
        return NHLogic.actionInputControl;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InputLockI();
    }

    public class InputLockI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            startExec(exec, "input_lock");
            endExec(exec);
        }
    }
}
