package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;

public class UiShow extends LStatement {
    public String cutscene = "css";

    public UiShow(String[] token){
        cutscene = token[1];
    }

    public UiShow() {}

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new UIShowI(builder.var(cutscene));
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("uishow");
        builder.append(" ");
        builder.append(cutscene);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class UIShowI implements LExecutor.LInstruction {
        public int cutscene;
        public UIShowI(int cutscene){
            this.cutscene = cutscene;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            exec.setobj(cutscene, css + "ui_show" + "\n");
        }
    }
}
