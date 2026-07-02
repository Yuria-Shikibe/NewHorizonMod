package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.net.Packet;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.logic.RaidBulletUtil;

import static mindustry.Vars.content;

public class RaidBulletPacket extends Packet {
    public int bulletId;
    public int teamId;
    public float x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY;
    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(bulletId);
        write.i(teamId);
        write.f(x);
        write.f(y);
        write.f(angle);
        write.f(damage);
        write.f(velocityScl);
        write.f(lifetimeScl);
        write.f(aimX);
        write.f(aimY);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        bulletId = READ.i();
        teamId = READ.i();
        x = READ.f();
        y = READ.f();
        angle = READ.f();
        damage = READ.f();
        velocityScl = READ.f();
        lifetimeScl = READ.f();
        aimX = READ.f();
        aimY = READ.f();
    }

    @Override
    public void handleClient() {
        if (RaidLogic.isLogicSide()) return;
        BulletType type = content.bullet(bulletId);
        if (type == null) return;
        Team team = Team.get((byte) teamId);
        if (team == null) return;
        RaidBulletUtil.createSynced(type, team, x, y, angle, damage, velocityScl, lifetimeScl, aimX, aimY);
    }
}
