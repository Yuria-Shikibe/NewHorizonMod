package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class InfoFadeOut extends ActionLStatement {
    public String duration = "2";

    public InfoFadeOut(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public InfoFadeOut() {}

    @Override
    public String getLStatementName() {
        return "infofadeout";
    }

    @Override
    public void build(Table table) {
        buildRowTable(table, t -> {
            t.add(" Duration: ");
            fields(t, duration, str -> duration = str);
        });
    }

    @Override
    public LCategory category() {
        return NHLogic.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InfoFadeOutI(builder.var(duration));
    }

    public class InfoFadeOutI extends ActionInstruction {
        public LVar duration;

        public InfoFadeOutI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "info_fade_out");
            writeExec(exec, duration);
        }
    }
}
