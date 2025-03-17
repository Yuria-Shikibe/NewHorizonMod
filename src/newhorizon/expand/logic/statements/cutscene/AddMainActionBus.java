package newhorizon.expand.logic.statements.cutscene;

import arc.Core;
import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.expand.cutscene.components.ActionControl;

public class AddMainActionBus extends LStatement {
    public String cutscene = "css";

    public AddMainActionBus(String[] tokens){
        cutscene = tokens[1];
    }

    public AddMainActionBus() {}

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new AddMainCssBusI(builder.var(cutscene));
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("addmainbus");
        builder.append(" ");
        builder.append(cutscene);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class AddMainCssBusI implements LExecutor.LInstruction {
        public int cutscene;
        public AddMainCssBusI(int cutscene){
            this.cutscene = cutscene;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            Core.app.setClipboardText(css);
            NHVars.cutscene.addMainActionBus(ActionControl.parseCode(css, null));
        }
    }
}
