package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.RaidSync;

public class RaidClearPacket extends Packet {
    @Override
    public void write(Writes write) {
    }

    @Override
    public void read(Reads read, int length) {
    }

    @Override
    public void handleClient() {
        if (RaidLogic.isLogicSide()) return;
        RaidSync.clearClientRaid();
    }
}
