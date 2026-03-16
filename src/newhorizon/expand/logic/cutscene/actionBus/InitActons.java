package newhorizon.expand.logic.cutscene.actionBus;

import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;

//clear LExecutor text buffer, begin a new bus
public class InitActons extends ActionLStatement {

    public InitActons(String[] token) {
    }

    public InitActons() {}

    @Override
    public String getLStatementName() {
        return "initactions";
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InitBusI();
    }

    public class InitBusI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            exec.textBuffer.setLength(0);
        }
    }
}
