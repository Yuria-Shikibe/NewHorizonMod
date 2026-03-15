package newhorizon.expand.logic.cutscene.actionBus;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.ActionControl;

import static newhorizon.NHVars.cutscene;

public class RunSubBus extends ActionLStatement {
    public String actionBusName = "Action_Bus";

    public RunSubBus(String[] token) {
        actionBusName = ParseUtil.getFirstToken(token);
    }

    public RunSubBus() {}

    @Override
    public String getLStatementName() {
        return "runsubbus";
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
        return new RunSubBusI(builder.var(actionBusName));
    }

    public class RunSubBusI extends ActionInstruction {
        public LVar actionBusName;

        public RunSubBusI(LVar actionBusName) {
            this.actionBusName = actionBusName;
        }

        @Override
        public void run(LExecutor exec) {
            cutscene.addSubActionBus(ActionControl.parseCode(exec.textBuffer.toString()));
        }
    }
}