package newhorizon.expand.logic.cutscene.letterbox;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class LetterboxIn extends ActionLStatement {
    public String duration = "1";

    public LetterboxIn(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public LetterboxIn() {
    }

    @Override
    public String getLStatementName() {
        return "letterboxin";
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
        return NHLogic.actionCurtainControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new LetterboxInI(builder.var(duration));
    }

    public class LetterboxInI extends ActionInstruction {
        public LVar duration;

        public LetterboxInI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "letterbox_in");
            writeExec(exec, duration);
        }
    }
}