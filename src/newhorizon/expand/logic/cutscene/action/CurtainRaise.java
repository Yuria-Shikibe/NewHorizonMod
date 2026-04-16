package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class CurtainRaise extends ActionLStatement {
    public String duration = "2";

    public CurtainRaise(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CurtainRaise() {}

    @Override
    public String getLStatementName() {
        return "curtainraise";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
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
        return new CurtainRaiseI(builder.var(duration));
    }

    public class CurtainRaiseI extends ActionInstruction {
        public LVar duration;

        public CurtainRaiseI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "curtain_raise");
            writeExec(exec, duration);
        }
    }
}
