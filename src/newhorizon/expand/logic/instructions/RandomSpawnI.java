package newhorizon.expand.logic.instructions;

import arc.math.Rand;
import arc.struct.Seq;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.Tile;

import static mindustry.Vars.spawner;

public class RandomSpawnI implements LExecutor.LInstruction {
    public LVar seed, x, y;

    public RandomSpawnI(LVar seed, LVar x, LVar y) {
        this.seed = seed;
        this.x = x;
        this.y = y;
    }

    public RandomSpawnI() {}

    @Override
    public void run(LExecutor exec) {
        Seq<Tile> spawns = spawner.getSpawns();
        if (spawns.isEmpty()) return;

        int s = seed.numi();
        Rand r = new Rand(s);

        Tile t = spawns.random(r);

        x.setnum(t.x * 8);
        y.setnum(t.y * 8);
    }
}
