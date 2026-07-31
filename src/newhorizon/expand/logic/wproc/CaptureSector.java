package newhorizon.expand.logic.wproc;

import arc.scene.ui.layout.Table;
import mindustry.gen.Call;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;

/** Immediately captures the active campaign sector through Mindustry's normal capture flow. */
public class CaptureSector extends LStatement {

    public CaptureSector(String[] tokens) {
    }

    public CaptureSector() {
    }

    @Override
    public void build(Table table) {
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LCategory category() {
        return LCategory.world;
    }

    @Override
    public void write(StringBuilder builder) {
        builder.append("capturesector");
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new CaptureSectorInstruction();
    }

    public static class CaptureSectorInstruction implements LExecutor.LInstruction {
        @Override
        public void run(LExecutor exec) {
            Call.sectorCapture();
        }
    }
}
