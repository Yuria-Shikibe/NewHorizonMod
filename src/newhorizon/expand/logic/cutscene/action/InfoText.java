package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class InfoText extends ActionLStatement {
    public String duration = "2", text = "<Please Type Text Here>";

    public InfoText(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public InfoText() {}

    @Override
    public String getLStatementName() {
        return "infotext";
    }

    @Override
    public void build(Table table) {
        buildRowTable(table, t -> {
            t.add(" Duration: ");
            fields(t, duration, str -> duration = str);
        });

        buildRowTable(table, t -> fields(t, text, str -> text = str).width(0).growX().padLeft(3));
    }

    @Override
    public LCategory category() {
        return NHLogic.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration, text);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InfoTextI(builder.var(duration), builder.var(text));
    }

    public class InfoTextI extends ActionInstruction {
        public LVar duration, text;

        public InfoTextI(LVar duration, LVar text) {
            this.duration = duration;
            this.text = text;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "info_text");
            writeExec(exec, duration, text);
        }
    }
}
