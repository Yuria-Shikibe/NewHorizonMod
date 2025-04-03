package newhorizon.expand.logic.instructions;

import mindustry.logic.LExecutor;
import newhorizon.NHGroups;

public class GravityWellI implements LExecutor.LInstruction {
    public int x, y, out;

    public GravityWellI(int x, int y, int out) {
        this.x = x;
        this.y = y;
        this.out = out;
    }

    public GravityWellI() {}

    @Override
    public void run(LExecutor exec) {
        int wx = exec.numi(x);
        int wy = exec.numi(y);

        exec.setbool(out, NHGroups.gravityTraps.any(wx, wy, 8, 8));
    }
}
