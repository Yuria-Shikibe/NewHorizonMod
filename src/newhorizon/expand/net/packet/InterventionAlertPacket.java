package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.InterventionSync;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.logic.components.action.EventInterventionAction;

public class InterventionAlertPacket extends Packet {
    private byte[] data = NODATA;

    public EventInterventionAction action;

    public InterventionAlertPacket() {
    }

    public InterventionAlertPacket(EventInterventionAction action) {
        this.action = action;
    }

    @Override
    public void write(Writes write) {
        InterventionSync.writeAction(write, action);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        action = InterventionSync.readAction(READ);
    }

    @Override
    public void handleClient() {
        if (!RaidLogic.isRemoteClient()) return;
        if (action != null) InterventionSync.applyClientAction(action);
    }
}
