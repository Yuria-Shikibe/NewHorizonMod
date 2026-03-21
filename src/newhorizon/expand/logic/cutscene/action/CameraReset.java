package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHContent;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class CameraReset extends ActionLStatement {
    public String duration = "2";

    public CameraReset(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
    }

    public CameraReset() {}

    @Override
    public String getLStatementName() {
        return "camerareset";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
    }

    @Override
    public LCategory category() {
        return NHContent.actionCameraControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CameraResetI(builder.var(duration));
    }

    public class CameraResetI extends ActionInstruction {
        public LVar duration;

        public CameraResetI(LVar duration) {
            this.duration = duration;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "camera_reset");
            writeExec(exec, duration);
        }
    }
}
