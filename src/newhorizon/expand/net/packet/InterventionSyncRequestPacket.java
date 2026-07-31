package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.game.InterventionSync;

public class InterventionSyncRequestPacket extends Packet {
    @Override
    public void write(Writes write) {
    }

    @Override
    public void read(Reads read, int length) {
    }

    @Override
    public void handleServer(NetConnection con) {
        if (con.player == null || con.kicked) return;
        InterventionSync.pushStateTo(con.player);
    }
}
