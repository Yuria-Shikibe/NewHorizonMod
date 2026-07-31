package newhorizon.expand.logic.cutscene.letterbox;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class LetterboxOut extends ActionLStatement {
    public String duration = "1";

    public LetterboxOut(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public LetterboxOut() {
    }

    @Override
    public String getLStatementName() {
        return "letterboxout";
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
        return new LetterboxOutI(builder.var(duration));
    }

    public class LetterboxOutI extends ActionInstruction {
        public LVar duration;

        public LetterboxOutI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "letterbox_out");
            writeExec(exec, duration);
        }
    }
}