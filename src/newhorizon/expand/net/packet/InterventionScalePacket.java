package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.InterventionState;

public class InterventionScalePacket extends Packet {
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
    public void handleClient() {
        InterventionState.setScale(scale);
    }
}
