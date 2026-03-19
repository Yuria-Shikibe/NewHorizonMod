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

public class GetActions extends ActionLStatement {
    public String actionBusName = "Action_Bus";

    public GetActions(String[] token) {
        actionBusName = ParseUtil.getFirstToken(token);
    }

    public GetActions() {
    }

    @Override
    public String getLStatementName() {
        return "getactions";
    }

    @Override
    public void build(Table table) {
        table.add(" Action Bus Name: ");
        fields(table, actionBusName, str -> actionBusName = str).width(0f).growX().padRight(3);
    }

    @Override
    public LCategory category() {
        return NHContent.nhcutscene;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, actionBusName);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new GetBusI(builder.var(actionBusName));
    }

    public class GetBusI extends ActionInstruction {
        public LVar actionBusName;

        public GetBusI(LVar actionBusName) {
            this.actionBusName = actionBusName;
        }

        @Override
        public void run(LExecutor exec) {
            String actionBus = CutsceneControl.getActionBus(actionBusName.name);
            exec.textBuffer.setLength(0);
            exec.textBuffer.append(actionBus);
        }
    }
}