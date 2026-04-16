package newhorizon.expand.logic.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.content.NHLogic;
import newhorizon.expand.logic.ActionLStatement;
import newhorizon.expand.logic.ParseUtil;

public class CameraZoom extends ActionLStatement {
    public String duration = "2", zoom = "1";

    public CameraZoom(String[] token) {
        ParseUtil.getFirstFloat(token);
        duration = ParseUtil.getNextToken(token);
        zoom = ParseUtil.getNextToken(token);
    }

    public CameraZoom() {}

    @Override
    public String getLStatementName() {
        return "camerazoom";
    }

    @Override
    public void build(Table table) {
        table.add(" Duration: ");
        fields(table, duration, str -> duration = str);
        table.row();
        table.add(" Zoom: ");
        fields(table, zoom, str -> zoom = str);
    }

    @Override
    public LCategory category() {
        return NHLogic.actionCameraControl;
    }

    @Override
    public void write(StringBuilder builder) {
        super.write(builder);
        writeTokens(builder, duration, zoom);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CameraZoomI(builder.var(duration), builder.var(zoom));
    }

    public class CameraZoomI extends ActionInstruction {
        public LVar duration, zoom;

        public CameraZoomI(LVar duration, LVar zoom) {
            this.duration = duration;
            this.zoom = zoom;
        }

        @Override
        public void run(LExecutor exec) {
            startExec(exec, "camera_zoom");
            writeExec(exec, duration, zoom);
        }
    }
}

