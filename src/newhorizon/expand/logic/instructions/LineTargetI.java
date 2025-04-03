package newhorizon.expand.logic.instructions;

import arc.math.geom.Bresenham2;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.logic.LExecutor;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class LineTargetI implements LExecutor.LInstruction {
    public int team, sourceX, sourceY, targetX, targetY, outX, outY;

    public LineTargetI(int team, int sourceX, int sourceY, int targetX, int targetY, int outX, int outY) {
        this.team = team;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.outX = outX;
        this.outY = outY;
    }

    @Override
    public void run(LExecutor exec) {
        Team t = exec.team(team);
        if (t == null) return;

        int sx = exec.numi(sourceX);
        int sy = exec.numi(sourceY);
        int tx = exec.numi(targetX);
        int ty = exec.numi(targetY);


        Seq<Tile> tiles = new Seq<>();

        Bresenham2.line(sx, sy, tx, ty, (x, y) -> {
            if (world.tile(x, y) != null) {
                tiles.add(world.tile(x, y));
            }
        });

        int ox = -1, oy = -1;

        for (Tile tile : tiles) {
            if (tile.team() == t){
                ox = tile.x;
                oy = tile.y;
                break;
            }
        }

        exec.setnum(outX, ox * 8);
        exec.setnum(outY, oy * 8);
    }
}
