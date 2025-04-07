package newhorizon.expand.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.io.TypeIO;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import newhorizon.expand.GravityWallSubstation;
import newhorizon.expand.net.NHCall;

public class ConfigGravityWallNodePacket extends Packet {
    private byte[] DATA = NODATA;

    public Building building;

    @Override
    public void write(Writes WRITE) {
        TypeIO.writeBuilding(WRITE, building);
    }

    @Override
    public void read(Reads READ, int LENGTH) {
        DATA = READ.b(LENGTH);
    }

    @Override
    public void handled() {
        BAIS.setBytes(DATA);
        building = TypeIO.readBuilding(READ);
    }

    @Override
    public void handleClient(){
        if (building != null && building instanceof GravityWallSubstation.GravityWallSubstationBuild gws) {
            gws.configLink();
        }
    }

    @Override
    public void handleServer(NetConnection con) {
        if(con.player == null || con.kicked) {return;}
        NHCall.reconnectGravityWallNode((GravityWallSubstation.GravityWallSubstationBuild) building);
    }
}
