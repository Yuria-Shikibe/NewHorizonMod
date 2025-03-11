package newhorizon.expand.logic.instructions;

import arc.math.Rand;
import arc.math.geom.Geometry;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.world.meta.BlockFlag;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.indexer;
import static mindustry.Vars.world;

public class RandomTargetI implements LExecutor.LInstruction {
    public int team, x, y;
    public int seed;
    public float w1, w2, w3, w4;

    public RandomTargetI(int team, int seed, int x, int y, float w1, float w2, float w3, float w4) {
        this.team = team;
        this.seed = seed;
        this.x = x;
        this.y = y;
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
    }

    public RandomTargetI() {}

    @Override
    public void run(LExecutor exec) {
        Team t = exec.team(team);
        if (t == null) return;

        int s = exec.numi(seed);
        Rand r = new Rand(s);
        float wx = r.random(0, world.unitWidth());
        float wy = r.random(0, world.unitHeight());

        AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.core);
        WeightedRandom.random(
                new WeightedOption(w1, () -> flag.set(BlockFlag.turret)),
                new WeightedOption(w2, () -> flag.set(BlockFlag.generator)),
                new WeightedOption(w3, () -> flag.set(BlockFlag.factory)),
                new WeightedOption(w4, () -> flag.set(BlockFlag.core))
        );
        Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(Team.get(team), flag.get()));
        if (b == null) b = t.core();
        if (b == null) return;
        exec.setnum(x, b.x);
        exec.setnum(y, b.y);
    }
}
