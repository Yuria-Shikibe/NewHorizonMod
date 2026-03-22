package newhorizon.expand.logic.cutscene.action;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;

public class UIShow extends ActionLStatement {

    public UIShow(String[] token) {}

    public UIShow() {}

    @Override
    public String getLStatementName() {
        return "uishow";
    }

    @Override
    public LCategory category() {
        return NHContent.actionInputControl;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new UIShowI();
    }

    public class UIShowI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            startExec(exec, "ui_show");
            endExec(exec);
        }
    }
}
