package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.RaidSync;
import newhorizon.expand.logic.components.action.EventRaidAction;

public class RaidAlertPacket extends Packet {
    private byte[] data = NODATA;

    public EventRaidAction action;

    public RaidAlertPacket() {
    }

    public RaidAlertPacket(EventRaidAction action) {
        this.action = action;
    }

    @Override
    public void write(Writes write) {
        RaidSync.writeAction(write, action);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        action = RaidSync.readAction(READ);
    }

    @Override
    public void handleClient() {
        if (RaidLogic.isLogicSide()) return;
        if (action != null) RaidSync.applyClientAction(action);
    }
}
