package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class CameraControl extends ActionLStatement {
    public String duration = "5", cameraX = "0", cameraY = "0";

    public CameraControl(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
        cameraX = ParseUtil.getNextToken(token);
        cameraY = ParseUtil.getNextToken(token);
    }

    public CameraControl() {}

    @Override
    public String getLStatementName() {
        return "cameracontrol";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
        table.row();
        table.add(" Camera Pos: < X: ");
        fields(table, cameraX, str -> cameraX = str);
        table.add(" , Y: ");
        fields(table, cameraY, str -> cameraY = str);
        table.add(" > ");
    }

    @Override
    public LCategory category() {
        return NHLogic.actionCameraControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration, cameraX, cameraY);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CameraControlI(builder.var(duration), builder.var(cameraX), builder.var(cameraY));
    }

    public class CameraControlI extends ActionInstruction {
        public LVar duration, cameraX, cameraY;

        public CameraControlI(LVar duration, LVar cameraX, LVar cameraY) {
            this.duration = duration;
            this.cameraX = cameraX;
            this.cameraY = cameraY;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "camera_control");
            writeExec(exec, duration, cameraX, cameraY);
        }
    }
}
