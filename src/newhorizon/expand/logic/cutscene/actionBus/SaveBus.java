package newhorizon.expand.logic.cutscene.actionBus;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.CutsceneControl;

public class SaveBus extends ActionLStatement {
    public String actionBusName = "Action_Bus";

    public SaveBus(String[] token) {
        actionBusName = ParseUtil.getFirstToken(token);
    }

    public SaveBus() {}

    @Override
    public String getLStatementName() {
        return "savebus";
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
        return new SaveBusI(builder.var(actionBusName));
    }

    public class SaveBusI extends ActionInstruction {
        public LVar actionBusName;

        public SaveBusI(LVar actionBusName) {
            this.actionBusName = actionBusName;
        }

        @Override
        public void run(LExecutor exec) {
            CutsceneControl.saveActionBus(actionBusName.name, exec.textBuffer.toString());
        }
    }
}
