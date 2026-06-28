package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.net.NHCall;

public class RaidScaleRequestPacket extends Packet {
    public float scale;
    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.f(scale);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        scale = READ.f();
    }

    @Override
    public void handleServer(NetConnection con) {
        if (con.player == null || con.kicked || !con.player.admin) return;
        NHCall.applyRaidScale(scale, con.player);
    }
}
