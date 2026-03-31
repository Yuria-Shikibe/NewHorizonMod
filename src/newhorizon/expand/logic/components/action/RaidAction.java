package newhorizon.expand.logic.components.action;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Call;
import newhorizon.content.NHBullets;
import newhorizon.content.bullets.RaidBullets;
import newhorizon.expand.logic.ParseUtil;
import newhorizon.expand.logic.components.Action;

public class RaidAction extends Action {


    public String raidType = "PRESET_RAID_0";

    public String warningIcon = "raid";
    public String warningSound = "alarm";
    public String warningText = "default_raid_text";

    public Team team;
    public int bulletType = 0, bulletCount = 5;
    public float sourceX = 0, sourceY = 0, targetX = 0, targetY = 0;

    @Override
    public String actionName() {
        return "raid";
    }

    @Override
    public void parseTokens(String[] tokens) {
        duration = ParseUtil.getFirstFloat(tokens) * Time.toSeconds;

        team = ParseUtil.getNextTeam(tokens);

        bulletType = ParseUtil.getNextInt(tokens);
        bulletCount = ParseUtil.getNextInt(tokens);

        sourceX = ParseUtil.getNextFloat(tokens);
        sourceY = ParseUtil.getNextFloat(tokens);
        targetX = ParseUtil.getNextFloat(tokens);
        targetY = ParseUtil.getNextFloat(tokens);
    }

    public BulletType bulletType() {
        return switch (bulletType) {
            case 1 -> RaidBullets.raidBullet_1;
            case 2 -> RaidBullets.raidBullet_2;
            case 3 -> RaidBullets.raidBullet_3;
            case 4 -> RaidBullets.raidBullet_4;
            case 5 -> RaidBullets.raidBullet_5;
            case 6 -> RaidBullets.raidBullet_6;
            case 7 -> RaidBullets.raidBullet_7;
            case 8 -> RaidBullets.raidBullet_8;
            default -> NHBullets.railGun1;
        };
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
        Tmp.v1.trns(Mathf.random(360f), 1);
        float dst = Mathf.dst(sourceX, sourceY, targetX, targetY);
        float ang = Angles.angle(sourceX, sourceY, targetX, targetY);
        float lifetimeScl = dst / (bulletType().speed * bulletType().lifetime);
        Call.createBullet(bulletType(), team, sourceX + Tmp.v1.x, sourceY + Tmp.v1.y, ang, -1, 1f, lifetimeScl);
    }
}
