package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class InputUnlock extends ActionLStatement {

    public InputUnlock(String[] token) {}

    public InputUnlock() {}

    @Override
    public String getLStatementName() {
        return "inputunlock";
    }

    @Override
    public LCategory category() {
        return NHContent.actionInputControl;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InputUnlockI();
    }

    public class InputUnlockI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            startExec(exec, "input_unlock");
            endExec(exec);
        }
    }
}
