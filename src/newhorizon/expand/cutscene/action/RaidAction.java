package newhorizon.expand.cutscene.action;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Call;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.cutscene.components.Action;
import newhorizon.expand.cutscene.components.ActionControl;

public class RaidAction extends Action {
    public Team team;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0, inaccuracyRadius = 80;
    public int bulletType = 0;

    public RaidAction(String[] tokens) {
        super(0f);
        this.team = ActionControl.phaseTeam(tokens[0]);
        this.bulletType = Integer.parseInt(tokens[1]);
        this.sourceX = Float.parseFloat(tokens[2]);
        this.sourceY = Float.parseFloat(tokens[3]);
        this.targetX = Float.parseFloat(tokens[4]);
        this.targetY = Float.parseFloat(tokens[5]);
        this.inaccuracyRadius = Float.parseFloat(tokens[6]);
    }

    public RaidAction() {
        super(0);
    }

    public BulletType bulletType() {
        switch (bulletType) {
            default -> {
                return RaidBullets.raidBullet_1;
            }
        }
    }

    @Override
    public void end() {
        createBullet();
    }

    @Override
    public void skip() {
        end();
    }

    public void createBullet() {
        Tmp.v1.trns(Mathf.random(360f), Mathf.random(inaccuracyRadius));
        float dst = Mathf.dst(sourceX, sourceY, targetX, targetY);
        float ang = Angles.angle(sourceX, sourceY, targetX, targetY);
        float lifetimeScl = dst / (bulletType().speed * bulletType().lifetime);
        Call.createBullet(bulletType(), team, sourceX + Tmp.v1.x, sourceY + Tmp.v1.y, ang, -1, 1f, lifetimeScl);
    }
}
