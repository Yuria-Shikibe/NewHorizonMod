package newhorizon.expand.logic.statements.cutscene;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.NHVars;
import newhorizon.content.NHContent;
import newhorizon.expand.cutscene.components.ActionControl;

public class AddSubActionBus extends LStatement {
    public String cutscene = "css";

    public AddSubActionBus(String[] tokens){
        cutscene = tokens[1];
    }

    public AddSubActionBus() {}

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
        return new AddSubActionBusI(builder.var(cutscene));
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("addsubbus");
        builder.append(" ");
        builder.append(cutscene);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class AddSubActionBusI implements LExecutor.LInstruction {
        public int cutscene;
        public AddSubActionBusI(int cutscene){
            this.cutscene = cutscene;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            NHVars.cutscene.addSubActionBus(ActionControl.parseCode(css, null));
        }
    }
}
