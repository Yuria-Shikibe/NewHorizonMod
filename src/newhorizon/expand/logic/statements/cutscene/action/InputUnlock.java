package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.*;
import newhorizon.content.NHContent;

public class InputUnlock extends LStatement {
    public String cutscene = "css";

    public InputUnlock(String[] token) {
        cutscene = token[1];
    }

    public InputUnlock() {
    }

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new InputUnlockI(builder.var(cutscene));
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
        builder.append("inputunlock");
        builder.append(" ");
        builder.append(cutscene);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class InputUnlockI implements LExecutor.LInstruction {
        public LVar cutscene;

        public InputUnlockI(LVar cutscene) {
            this.cutscene = cutscene;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) cutscene.obj();
            cutscene.setobj(css + "input_unlock" + "\n");
        }
    }
}