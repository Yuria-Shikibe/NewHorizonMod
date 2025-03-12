package newhorizon.expand.logic.instructions;

import arc.math.Rand;
import arc.struct.Seq;
import mindustry.logic.LExecutor;
import mindustry.world.Tile;
import newhorizon.NHGroups;

import static mindustry.Vars.spawner;

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
