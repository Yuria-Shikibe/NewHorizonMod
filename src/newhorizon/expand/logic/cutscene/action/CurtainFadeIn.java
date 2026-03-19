package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.CutsceneControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeIn extends ActionLStatement {
    public String duration = "2";

    public CurtainFadeIn(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CurtainFadeIn() {}

    @Override
    public String getLStatementName() {
        return "curtainfadein";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CurtainFadeInI(builder.var(duration));
    }

    public class CurtainFadeInI extends ActionInstruction {
        public LVar duration;

        public CurtainFadeInI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            exec.textBuffer.append("curtain_fade_in").append(" ").append(duration.numf());
            exec.textBuffer.append("\n");
        }
    }
}
