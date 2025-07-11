package newhorizon.expand.logic.instructions;

import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.NHGroups;

public class GravityWellI implements LExecutor.LInstruction {
    public LVar x, y, out;

    public GravityWellI(LVar x, LVar y, LVar out) {
        this.x = x;
        this.y = y;
        this.out = out;
    }

    public GravityWellI() {
    }

    @Override
    public void run(LExecutor exec) {
        int wx = x.numi();
        int wy = y.numi();

        out.setbool(NHGroups.gravityTraps.any(wx, wy, 8, 8));
    }
}
