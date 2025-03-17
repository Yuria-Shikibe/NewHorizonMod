package newhorizon.expand.logic.statements.cutscene.action;

import arc.scene.ui.layout.Table;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import newhorizon.content.NHContent;

public class CameraZoom extends LStatement {
    public String cutscene = "css", zoom = "1";

    public CameraZoom(String[] token){
        cutscene = token[1];
        zoom = token[2];
    }

    public CameraZoom() {}

    @Override
    public void build(Table table) {
        table.add(" Cutscene Name: ");
        fields(table, cutscene, str -> cutscene = str);
        table.add(" Zoom: ");
        fields(table, zoom, str -> zoom = str);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CameraZoomI(builder.var(cutscene), builder.var(zoom));
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
        builder.append("camerazoom");
        builder.append(" ");
        builder.append(cutscene);
        builder.append(" ");
        builder.append(zoom);
    }


    @SuppressWarnings("InnerClassMayBeStatic")
    public class CameraZoomI implements LExecutor.LInstruction {
        public int cutscene, zoom;
        public CameraZoomI(int cutscene, int zoom) {
            this.cutscene = cutscene;
            this.zoom = zoom;
        }

        @Override
        public void run(LExecutor exec) {
            String css = (String) exec.obj(cutscene);
            exec.setobj(cutscene, css + "camera_zoom" + " " + exec.numf(zoom) + "\n");
        }
    }
}
