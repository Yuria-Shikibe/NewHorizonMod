package newhorizon.expand.logic.wip;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import newhorizon.expand.logic.RaidBulletUtil;

public class SpawnBulletI implements LExecutor.LInstruction {
    public LVar team, type, seed, count, sourceX, sourceY, targetX, targetY, inaccuracy;

    public SpawnBulletI(LVar team, LVar type, LVar seed, LVar count, LVar sourceX, LVar sourceY, LVar targetX, LVar targetY, LVar inaccuracy) {
        this.team = team;
        this.type = type;
        this.seed = seed;
        this.count = count;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.inaccuracy = inaccuracy;
    }

    @Override
    public void run(LExecutor exec) {
        if (!exec.privileged) return;
        Team t = team.team();
        if (t == null) return;
        int s = seed.numi();
        int tp = type.numi();

        int ct = count.numi();
        int sx = sourceX.numi();
        int sy = sourceY.numi();
        int tx = targetX.numi();
        int ty = targetY.numi();
        int inacc = inaccuracy.numi();

        Rand r = new Rand(s);
        for (int i = 0; i < ct; i++) {
            Tmp.v1.trns(r.random(360f), r.random(inacc));
            float dst = Mathf.dst(sx, sy, tx, ty);
            float ang = Angles.angle(sx, sy, tx, ty);
            RaidBulletUtil.spawn(bulletType(tp), t, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, -1, 1f, dst, tx, ty);
        }
    }

    public BulletType bulletType(int type) {
        return RaidBulletUtil.resolve(type);
    }
}
