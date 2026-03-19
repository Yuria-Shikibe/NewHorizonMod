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
import newhorizon.expand.logic.components.CutsceneControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeOut extends ActionLStatement {
    public String duration = "2";

    public CurtainFadeOut(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CurtainFadeOut() {}

    @Override
    public String getLStatementName() {
        return "curtainfadeout";
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
        return new CurtainFadeOutI(builder.var(duration));
    }

    public class CurtainFadeOutI extends ActionInstruction {
        public LVar duration;

        public CurtainFadeOutI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            exec.textBuffer.append("curtain_fade_out").append(" ").append(duration.numf());
            exec.textBuffer.append("\n");
        }
    }
}
