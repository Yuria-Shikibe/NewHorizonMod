package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.Core;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import mindustry.type.UnitType;
import newhorizon.expand.units.probe.ProbeColonyManager;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public class ProbeProductionRequestPacket extends Packet {
    public int unitId;
    public short typeId;
    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(unitId);
        write.s(typeId);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        unitId = READ.i();
        typeId = READ.s();
    }

    @Override
    public void handleServer(NetConnection con) {
        if (con.player == null || con.kicked) return;

        Unit unit = Groups.unit.getByID(unitId);
        if (!(unit instanceof ProbeEntity factory) || factory.team != con.player.team()) return;

        UnitType type = Vars.content.getByID(ContentType.unit, typeId);
        if (!ProbeColonyManager.selectProduction(factory, type)) {
            con.player.sendMessage(Core.bundle.get(\u0022mod.ui.probe-production-denied\u0022));
        }
    }
}
