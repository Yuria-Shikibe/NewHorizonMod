package newhorizon.expand.logic.wip;

import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.Tile;

import static mindustry.Vars.spawner;

public class NearestSpawnI implements LExecutor.LInstruction {
    public LVar targetX, targetY, x, y;

    public NearestSpawnI(LVar targetX, LVar targetY, LVar x, LVar y) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.x = x;
        this.y = y;
    }

    public NearestSpawnI() {
    }

    @Override
    public void run(LExecutor exec) {
        Seq<Tile> spawns = spawner.getSpawns();
        if (spawns.isEmpty()) return;

        float tx = targetX.numf();
        float ty = targetY.numf();

        Tile closest = spawns.first();
        float minDst = Mathf.dst2(closest.x, closest.y, tx, ty);

        for (int i = 1; i < spawns.size; i++) {
            Tile t = spawns.get(i);
            float dst = Mathf.dst2(t.x, t.y, tx, ty);
            if (dst < minDst) {
                minDst = dst;
                closest = t;
            }
        }

        x.setnum(closest.x);
        y.setnum(closest.y);
    }
}
