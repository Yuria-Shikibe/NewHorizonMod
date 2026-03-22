package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class WarningIcon extends ActionLStatement {
    public String duration = "2", icon = "objective", team = "@sharded", text = "<INCOMING EVENT>";

    public WarningIcon(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
        icon = ParseUtil.getNextToken(token);
        team = ParseUtil.getNextToken(token);
        text = ParseUtil.getNextToken(token);
    }

    public WarningIcon() {}

    @Override
    public String getLStatementName() {
        return "warningicon";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
        table.row();
        table.add(" Team: ");
        fields(table, team, str -> team = str);
        table.row();
        table.add(" Icon: ");
        fields(table, icon, str -> icon = str);
        table.row();
        table.add(" Text: ");
        fields(table, text, str -> text = str);
    }

    @Override
    public LCategory category() {
        return NHContent.actionFlowControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration, team, icon, text);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new WarningIconI(builder.var(duration), builder.var(team), builder.var(icon), builder.var(text));
    }

    public class WarningIconI extends ActionInstruction {
        public LVar duration, icon, team, text;

        public WarningIconI(LVar duration, LVar icon, LVar team, LVar text) {
            this.duration = duration;
            this.icon = icon;
            this.team = team;
            this.text = text;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "wait");
            writeExec(exec, duration, icon, team, text);
        }
    }
}
