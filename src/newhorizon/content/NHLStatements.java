package newhorizon.content;

import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Building;
import mindustry.gen.LogicIO;
import mindustry.logic.*;
import mindustry.world.meta.BlockFlag;
import newhorizon.util.func.WeightedRandom;
import newhorizon.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.indexer;
import static mindustry.Vars.world;

public class NHLStatements {
    public static class RandomTarget extends LStatement {
        public String team = "@sharded", seed = "0", x = "0", y = "0", turretW = "0", generatorW = "0", factoryW = "0", coreW = "1";

        public RandomTarget(String[] tokens){
            team = tokens[1];
            seed = tokens[2];
            x = tokens[3];
            y = tokens[4];
            turretW = tokens[5];
            generatorW = tokens[6];
            factoryW = tokens[7];
            coreW = tokens[8];
        }

        public RandomTarget(){}

        @Override
        public void build(Table table){
            rebuild(table);
        }

        void rebuild(Table table){
            table.table(t -> {
                t.add(" Team: ");
                fields(t, team, str -> team = str);
                t.add(" Seed: ");
                fields(t, seed, str -> seed = str);
                t.add(" out: ");
                fields(t, x, str -> x = str);
                t.add(", ");
                fields(t, y, str -> y = str);
            }).left();

            table.row();

            table.table(t -> {
                t.add(" Turret Weight: ");
                fields(t, turretW, str -> turretW = str);
                t.add(" Generator Weight: ");
                fields(t, generatorW, str -> generatorW = str);
            }).left();

            table.row();

            table.table(t -> {
                t.add(" Factory Weight: ");
                fields(t, factoryW, str -> factoryW = str);
                t.add(" Core Weight: ");
                fields(t, coreW, str -> coreW = str);
            }).left();
        }

        @Override
        public boolean privileged(){
            return true;
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            return new RandomTargetI(builder.var(team), builder.var(seed), builder.var(x), builder.var(y), builder.var(turretW), builder.var(generatorW), builder.var(factoryW), builder.var(coreW));
        }

        @Override
        public LCategory category(){
            return NHContent.NH_CSS;
        }

        public void write(StringBuilder builder){
            builder.append("randtarget");
            builder.append(" ");
            builder.append(team);
            builder.append(" ");
            builder.append(seed);
            builder.append(" ");
            builder.append(x);
            builder.append(" ");
            builder.append(y);
            builder.append(" ");
            builder.append(turretW);
            builder.append(" ");
            builder.append(generatorW);
            builder.append(" ");
            builder.append(factoryW);
            builder.append(" ");
            builder.append(coreW);
        }
    }

    public static class RandomTargetI implements LExecutor.LInstruction {
        public int team, x, y;
        public int seed;
        public float w1, w2, w3, w4;

        public RandomTargetI(int team, int seed, int x, int y, float w1, float w2, float w3, float w4){
            this.team = team;
            this.seed = seed;
            this.x = x;
            this.y = y;
            this.w1 = w1;
            this.w2 = w2;
            this.w3 = w3;
            this.w4 = w4;
        }

        public RandomTargetI(){}

        @Override
        public void run(LExecutor exec){
            Team t = exec.team(team);
            if(t == null) return;

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
}
