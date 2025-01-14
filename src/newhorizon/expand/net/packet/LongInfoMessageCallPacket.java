package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.io.TypeIO;
import mindustry.net.Packet;
import mindustry.ui.dialogs.BaseDialog;

public class LongInfoMessageCallPacket extends Packet{
    private byte[] DATA;
    public String message;

    public LongInfoMessageCallPacket() {
        this.DATA = NODATA;
    }

    public void write(Writes WRITE) {
        TypeIO.writeString(WRITE, this.message);
    }

    public void read(Reads READ, int LENGTH) {
        this.DATA = READ.b(LENGTH);
    }

    public void handled() {
        BAIS.setBytes(this.DATA);
        this.message = TypeIO.readString(READ);
    }

    public void handleClient() {
        new BaseDialog("@message"){{
           addCloseButton();
           cont.margin(6f);
           cont.pane(t -> {
               t.add(message).expandX().fillY();
           }).grow();
        }}.show();
    }
}