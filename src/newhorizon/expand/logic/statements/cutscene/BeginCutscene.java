package newhorizon.expand.logic.statements.cutscene;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;

public class BeginCutscene extends LStatement {
    public String cutscene = "css";

    public BeginCutscene(String[] tokens){
        cutscene = tokens[1];
    }

    public BeginCutscene() {}

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
        return new BeginCutsceneI(builder.var(cutscene));
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("begincutscene");
        builder.append(" ");
        builder.append(cutscene);
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class BeginCutsceneI implements LExecutor.LInstruction {
        public int cutscene;
        public BeginCutsceneI(int cutscene){
            this.cutscene = cutscene;
        }

        @Override
        public void run(LExecutor exec) {
            exec.setobj(cutscene, "");
        }
    }
}
