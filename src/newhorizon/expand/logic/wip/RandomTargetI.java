package newhorizon.expand.logic.wip;

import arc.math.Rand;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import newhorizon.expand.game.DefaultIntervention;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class RandomTargetI implements LExecutor.LInstruction {
    public LVar team, x, y, seed, w1, w2, w3, w4, avoidGravity;

    public RandomTargetI(LVar team, LVar seed, LVar x, LVar y, LVar w1, LVar w2, LVar w3, LVar w4) {
        this(team, seed, x, y, w1, w2, w3, w4, null);
    }

    public RandomTargetI(LVar team, LVar seed, LVar x, LVar y, LVar w1, LVar w2, LVar w3, LVar w4, LVar avoidGravity) {
        this.team = team;
        this.seed = seed;
        this.x = x;
        this.y = y;
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
        this.avoidGravity = avoidGravity;
    }

    public RandomTargetI() {
    }

    @Override
    public void run(LExecutor exec) {
        if (!exec.privileged) return;

        Team wave = state.rules.waveTeam;
        Team player = state.rules.defaultTeam;
        if (wave == null || player == null) return;

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

        boolean avoid = avoidGravity == null || avoidGravity.bool();
        Building target = DefaultIntervention.findClosestBuilding(wave, player, flag.get(), wx, wy, avoid);
        Building core = player.core();
        if (target == null && core != null && (!avoid || DefaultIntervention.isTargetAvailable(core.tileX(), core.tileY()))) {
            target = core;
        }
        if (target != null) {
            x.setnum(target.tileX());
            y.setnum(target.tileY());
            return;
        }
        if (!avoid) return;

        target = DefaultIntervention.findClosestBuilding(wave, player, flag.get(), wx, wy, false);
        if (target == null) target = core;
        float tileX = target == null ? wx / tilesize : target.tileX();
        float tileY = target == null ? wy / tilesize : target.tileY();
        Tile spawn = DefaultIntervention.findNearestEnemySpawn(wave, tileX, tileY);
        if (spawn == null) return;

        x.setnum(spawn.x);
        y.setnum(spawn.y);
    }
}
