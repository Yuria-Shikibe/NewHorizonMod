package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.WeatherEventState;

public class WeatherEventSyncPacket extends Packet {
    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        WeatherEventState.writeSync(write);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        WeatherEventState.applySync(READ);
    }
}
