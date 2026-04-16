package newhorizon.expand.logic.cutscene.action;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;

public class UIHide extends ActionLStatement {

    public UIHide(String[] token) {}

    public UIHide() {}

    @Override
    public String getLStatementName() {
        return "uihide";
    }

    @Override
    public LCategory category() {
        return NHLogic.actionInputControl;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new UIHideI();
    }

    public class UIHideI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            startExec(exec, "ui_hide");
            endExec(exec);
        }
    }
}
