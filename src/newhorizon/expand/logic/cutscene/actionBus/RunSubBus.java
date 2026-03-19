package newhorizon.expand.logic.cutscene.actionBus;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.CutsceneControl;

import static newhorizon.NHVars.cutscene;

public class RunSubBus extends ActionLStatement {

    public RunSubBus(String[] token) {}

    public RunSubBus() {}

    @Override
    public String getLStatementName() {
        return "runsubbus";
    }

    @Override
    public LCategory category() {
        return NHContent.nhcutscene;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RunSubBusI();
    }

    public class RunSubBusI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            cutscene.addSubActionBus(CutsceneControl.parseCode(exec.textBuffer.toString()));
        }
    }
}