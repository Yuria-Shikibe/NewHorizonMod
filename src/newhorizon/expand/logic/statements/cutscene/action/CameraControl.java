package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.*;
import newhorizon.content.NHContent;

public class CameraControl extends LStatement {
    public String cutscene = "css", duration = "5", camX = "camX", camY = "camY";

    public CameraControl(String[] token) {
        cutscene = token[1];
        duration = token[2];
        camX = token[3];
        camY = token[4];
    }

    public CameraControl() {
    }

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
        table.add(" Pan Time: ");
        fields(table, duration, str -> duration = str);
        table.add(" Pan Position: ");
        fields(table, camX, str -> camX = str);
        table.add(" , ");
        fields(table, camY, str -> camY = str);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CameraControlI(builder.var(cutscene), builder.var(duration), builder.var(camX), builder.var(camY));
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
        builder.append("cameracontrol");
        builder.append(" ");
        builder.append(cutscene);
        builder.append(" ");
        builder.append(duration);
        builder.append(" ");
        builder.append(camX);
        builder.append(" ");
        builder.append(camY);
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class CameraControlI implements LExecutor.LInstruction {
        public LVar cutscene, duration, camX, camY;

        public CameraControlI(LVar cutscene, LVar duration, LVar camX, LVar camY) {
            this.cutscene = cutscene;
            this.duration = duration;
            this.camX = camX;
            this.camY = camY;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) cutscene.obj();

            cutscene.setobj(css + "camera_control" + " " + duration.numf() + " " + camX.numf() + " " + camY.numf() + "\n");
        }
    }
}