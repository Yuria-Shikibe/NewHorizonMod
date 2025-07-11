package newhorizon.expand.logic.instructions;

import arc.math.Rand;
import arc.math.geom.Geometry;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.meta.BlockFlag;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.indexer;
import static mindustry.Vars.world;

public class RandomTargetI implements LExecutor.LInstruction {
    public LVar team, x, y, seed, w1, w2, w3, w4;

    public RandomTargetI(LVar team, LVar seed, LVar x, LVar y, LVar w1, LVar w2, LVar w3, LVar w4) {
        this.team = team;
        this.seed = seed;
        this.x = x;
        this.y = y;
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
    }

    public RandomTargetI() {
    }

    @Override
    public void run(LExecutor exec) {
        Team t = team.team();
        if (t == null) return;

        int s = seed.numi();
        Rand r = new Rand(s);
        float wx = r.random(0, world.unitWidth());
        float wy = r.random(0, world.unitHeight());

        AtomicReference<BlockFlag> flag = new AtomicReference<>(BlockFlag.core);
        WeightedRandom.random(
                new WeightedOption(w1.numf(), () -> flag.set(BlockFlag.turret)),
                new WeightedOption(w2.numf(), () -> flag.set(BlockFlag.generator)),
                new WeightedOption(w3.numf(), () -> flag.set(BlockFlag.factory)),
                new WeightedOption(w4.numf(), () -> flag.set(BlockFlag.core))
        );
        Building b = Geometry.findClosest(wx, wy, indexer.getEnemy(t, flag.get()));
        if (b == null) b = t.core();
        if (b == null) return;
        x.setnum(b.tileX());
        y.setnum(b.tileY());
    }
}
