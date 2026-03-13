package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.NHLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;
import newhorizon.expand.logic.components.CutsceneControl;

import static mindustry.Vars.headless;
import static newhorizon.NHVars.cutsceneUI;

public class CurtainFadeIn extends NHLStatement {
    public String actionName = "Default Curtain Fade In", duration = "2";

    public CurtainFadeIn(String[] token) {
        actionName = ParseUtil.getFirstToken(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CurtainFadeIn() {}

    @Override
    public String getLStatementName() {
        return "curtainfadein";
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
        return new CurtainFadeInI(builder.var(actionName), builder.var(duration));
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class CurtainFadeInI implements LExecutor.LInstruction {
        public LVar actionName, duration;

        public CurtainFadeInI(LVar actionName, LVar duration) {
            this.actionName = actionName;
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            CutsceneControl.saveAction(actionName.name, String.valueOf(duration.numval));
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class CurtainFadeInAction extends Action {
        public float duration = 2f;

        public CurtainFadeInAction(float duration) {
            super(duration * Time.toSeconds);
        }

        public CurtainFadeInAction(String[] token) {
            super(Strings.parseFloat(ParseUtil.getFirstToken(token), 2f));
        }

        @Override
        public void begin() {
            if (headless) return;
            cutsceneUI.targetOverlayAlpha = 1f;
        }
    }
}
