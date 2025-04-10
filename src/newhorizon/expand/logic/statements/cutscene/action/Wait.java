package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.*;
import newhorizon.content.NHContent;

public class Wait extends LStatement {
    public String cutscene = "css", time = "5";

    public Wait(String[] token){
        cutscene = token[1];
        time = token[2];
    }

    public Wait() {}

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
        table.add(" Wait Second: ");
        fields(table, time, str -> time = str);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new WaitI(builder.var(cutscene), builder.var(time));
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
        builder.append("waitaction");
        builder.append(" ");
        builder.append(cutscene);
        builder.append(" ");
        builder.append(time);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class WaitI implements LExecutor.LInstruction {
        public LVar cutscene, time;
        public WaitI(LVar cutscene, LVar time){
            this.cutscene = cutscene;
            this.time = time;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) cutscene.obj();
            cutscene.setobj(css + "wait" + " " + time.numf() + "\n");
        }
    }
}
