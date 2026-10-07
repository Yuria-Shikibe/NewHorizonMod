package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.Packet;
import newhorizon.expand.game.RaidLogic;
import newhorizon.expand.game.WeatherEventState;

public class WeatherEventAlertPacket extends Packet {
    public int storm;

    public WeatherEventAlertPacket() {
    }

    public WeatherEventAlertPacket(int storm) {
        this.storm = storm;
    }

    @Override
    public void write(Writes write) {
        write.b((byte) storm);
    }

    @Override
    public void read(Reads read, int length) {
        storm = read.b();
    }

    @Override
    public void handleClient() {
        if (RaidLogic.isRemoteClient() && (storm == 0 || storm == 1)) {
            WeatherEventState.showStormAlert(storm);
        }
    }
}
