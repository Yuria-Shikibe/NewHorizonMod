package newhorizon.expand.logic.cutscene.actionBus;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

//clear LExecutor text buffer, begin a new bus
public class InitBus extends ActionLStatement {
    public String actionBusName = "Action_Bus";

    public InitBus(String[] token) {
        actionBusName = ParseUtil.getFirstToken(token);
    }

    public InitBus() {}

    @Override
    public String getLStatementName() {
        return "initbus";
    }

    @Override
    public void build(Table table) {
        table.add(" Action Bus Name: ");
        fields(table, actionBusName, str -> actionBusName = str);
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, actionBusName);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InitBusI(builder.var(actionBusName));
    }

    public class InitBusI extends ActionInstruction {
        public LVar actionBusName;

        public InitBusI(LVar actionBusName) {
            this.actionBusName = actionBusName;
        }

        @Override
        public void run(LExecutor exec) {
            exec.textBuffer.setLength(0);
        }
    }
}
