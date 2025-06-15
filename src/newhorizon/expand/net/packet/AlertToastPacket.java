package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.net.NHCall;
import newhorizon.util.ui.NHUIFunc;

import static newhorizon.expand.net.NHCall.getDrawable;
import static newhorizon.expand.net.NHCall.getSound;

public class AlertToastPacket extends Packet {
    public int iconID, soundID;
    public String text;

    private byte[] DATA = NODATA;

    @Override
    public void write(Writes WRITE) {
        WRITE.str(text);
        WRITE.i(iconID);
        WRITE.i(soundID);
    }

    @Override
    public void read(Reads READ, int LENGTH) {
        DATA = READ.b(LENGTH);
    }

    @Override
    public void handled() {
        BAIS.setBytes(DATA);
        text = READ.str();
        iconID = READ.i();
        soundID = READ.i();
    }

    @Override
    public void handleClient() {
        NHUIFunc.showToast(getDrawable(iconID), text, getSound(soundID));
    }

    @Override
    public void handleServer(NetConnection con) {
        if (con.player == null || con.kicked) {
            return;
        }
        NHCall.alertToastTable(iconID, soundID, text);
    }
}
