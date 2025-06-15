package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.logic.DefaultRaid;
import newhorizon.expand.net.NHCall;

public class WarnHUDPacket extends Packet {
    public String name;
    public float time;
    public float range;
    public float sourceX;
    public float sourceY;
    public float targetX;
    public float targetY;

    public int alertSound;
    private byte[] DATA = NODATA;

    @Override
    public void write(Writes WRITE) {
        WRITE.str(name);
        WRITE.f(time);
        WRITE.f(range);
        WRITE.f(sourceX);
        WRITE.f(sourceY);
        WRITE.f(targetX);
        WRITE.f(targetY);
        WRITE.f(alertSound);
    }

    @Override
    public void read(Reads READ, int LENGTH) {
        DATA = READ.b(LENGTH);
    }

    @Override
    public void handled() {
        BAIS.setBytes(DATA);
        name = READ.str();
        time = READ.f();
        range = READ.f();
        sourceX = READ.f();
        sourceY = READ.f();
        targetX = READ.f();
        targetY = READ.f();
    }

    @Override
    public void handleClient() {
        DefaultRaid.clientAlertHud(name, time, range, sourceX, sourceY, targetX, targetY);
    }

    @Override
    public void handleServer(NetConnection con) {
        if (con.player == null || con.kicked) {
            return;
        }
        NHCall.warnHudPacket(name, time, range, sourceX, sourceY, targetX, targetY);
    }
}
