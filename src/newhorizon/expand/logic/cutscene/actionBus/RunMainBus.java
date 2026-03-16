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

public class RunMainBus extends ActionLStatement {

    public RunMainBus(String[] token) {}

    public RunMainBus() {}

    @Override
    public String getLStatementName() {
        return "runmainbus";
    }

    @Override
    public LCategory category() {
        return NHContent.nhaction;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new RunMainBusI();
    }

    public class RunMainBusI extends ActionInstruction {
        @Override
        public void run(LExecutor exec) {
            cutscene.addMainActionBus(CutsceneControl.parseCode(exec.textBuffer.toString()));
        }
    }
}
