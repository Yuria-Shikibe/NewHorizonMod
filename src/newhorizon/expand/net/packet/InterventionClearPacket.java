package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.InterventionSync;
import newhorizon.expand.game.RaidLogic;

public class InterventionClearPacket extends Packet {
    public int syncSeed;
    private byte[] data = NODATA;

    public InterventionClearPacket() {
    }

    public InterventionClearPacket(int syncSeed) {
        this.syncSeed = syncSeed;
    }

    @Override
    public void write(Writes write) {
        write.i(syncSeed);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        syncSeed = READ.i();
    }

    @Override
    public void handleClient() {
        if (!RaidLogic.isRemoteClient()) return;
        if (syncSeed == 0) {
            InterventionSync.clearClientIntervention();
        } else {
            InterventionSync.clearClientIntervention(syncSeed);
        }
    }
}
