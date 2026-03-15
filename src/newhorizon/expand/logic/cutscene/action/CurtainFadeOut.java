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
    public String actionName = "Default_Curtain_Fade_Out", duration = "2";

    public CurtainFadeOut(String[] token) {
        actionName = ParseUtil.getFirstToken(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CurtainFadeOut() {}

    @Override
    public String getLStatementName() {
        return "curtainfadeout";
    }

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ").width(120f);
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
        return new CurtainFadeOutI(builder.var(actionName), builder.var(duration));
    }

    public class CurtainFadeOutI extends ActionInstruction {
        public LVar actionName, duration;

        public CurtainFadeOutI(LVar actionName, LVar duration) {
            this.actionName = actionName;
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            CutsceneControl.saveAction(actionName.name, String.valueOf(duration.numval));
        }
    }
}
