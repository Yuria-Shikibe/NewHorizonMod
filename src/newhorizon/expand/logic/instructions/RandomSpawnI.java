package newhorizon.expand.logic.instructions;

import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;

public class RandomSpawnI implements LExecutor.LInstruction {
    public int seed, x, y;

    public RandomSpawnI(int seed, int x, int y) {
        this.seed = seed;
        this.x = x;
        this.y = y;
    }

    public RandomSpawnI() {}

    @Override
    public void run(LExecutor exec) {
        Seq<Tile> spawns = spawner.getSpawns();
        if (spawns.isEmpty()) return;

        int s = exec.numi(seed);
        Rand r = new Rand(s);

        Tile t = spawns.random(r);

        exec.setnum(x, t.x);
        exec.setnum(y, t.y);
    }
}
