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
import newhorizon.expand.logic.components.CutsceneControl;

import static newhorizon.NHVars.cutscene;

public class RunMainBus extends ActionLStatement {
    public String actionBusName = "Action_Bus";

    public RunMainBus(String[] token) {
        actionBusName = ParseUtil.getFirstToken(token);
    }

    public RunMainBus() {}

    @Override
    public String getLStatementName() {
        return "runmainbus";
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
        return new RunMainBusI(builder.var(actionBusName));
    }

    public class RunMainBusI extends ActionInstruction {
        public LVar actionBusName;

        public RunMainBusI(LVar actionBusName) {
            this.actionBusName = actionBusName;
        }

        @Override
        public void run(LExecutor exec) {
            cutscene.addMainActionBus(ActionControl.parseCode(exec.textBuffer.toString()));
        }
    }
}
