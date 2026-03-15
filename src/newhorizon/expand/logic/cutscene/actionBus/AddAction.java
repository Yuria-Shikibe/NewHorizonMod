package newhorizon.expand.logic.cutscene.actionBus;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.Vars;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.CutsceneControl;

public class AddAction extends ActionLStatement {
    public String actionName = "Action_Bus";

    public AddAction(String[] token) {
        actionName = ParseUtil.getFirstToken(token);
    }

    public AddAction() {}

    @Override
    public String getLStatementName() {
        return "addaction";
    }

    @Override
    public void build(Table table) {
        table.add(" Action Name: ");
        fields(table, actionName, str -> actionName = str);
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, actionName);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new AddActionI(builder.var(actionName));
    }

    public class AddActionI extends ActionInstruction {
        public LVar actionName;

        public AddActionI(LVar actionName) {
            this.actionName = actionName;
        }

        @Override
        public void run(LExecutor exec) {
            String actionCode = CutsceneControl.getAction(actionName.name);
            if (actionCode.isEmpty()) Log.err("Failed to add action " + actionName);
            exec.textBuffer.append(actionCode).append("\n");
        }
    }
}
