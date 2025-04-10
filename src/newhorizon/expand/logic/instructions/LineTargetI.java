package newhorizon.expand.logic.instructions;

import arc.math.geom.Bresenham2;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class LineTargetI implements LExecutor.LInstruction {
    public LVar team, sourceX, sourceY, targetX, targetY, outX, outY;

    public LineTargetI(LVar team, LVar sourceX, LVar sourceY, LVar targetX, LVar targetY, LVar outX, LVar outY) {
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
        Team t = team.team();
        if (t == null) return;

        int sx = sourceX.numi();
        int sy = sourceY.numi();
        int tx = targetX.numi();
        int ty = targetY.numi();


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

        outX.setnum(ox * 8);
        outY.setnum(oy * 8);
    }
}
