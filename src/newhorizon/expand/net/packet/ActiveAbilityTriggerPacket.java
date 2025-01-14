package newhorizon.expand.net.packet;

import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.TypeIO;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import mindustry.ui.dialogs.BaseDialog;
import newhorizon.expand.ability.active.ActiveAbility;
import newhorizon.expand.net.NHCall;

public class ActiveAbilityTriggerPacket extends Packet {
    private byte[] DATA = NODATA;

    public Unit unit;
    public int abilityId;


    @Override
    public void write(Writes WRITE) {
        TypeIO.writeUnit(WRITE, unit);
        WRITE.i(abilityId);
    }

    @Override
    public void read(Reads READ, int LENGTH) {
        DATA = READ.b(LENGTH);
    }

    @Override
    public void handled() {
        BAIS.setBytes(DATA);
        unit = TypeIO.readUnit(READ);
        abilityId = READ.i();
    }

    @Override
    public void handleClient(){
        if (unit != null && unit.abilities.length > abilityId) {
            Ability ability = unit.abilities[abilityId];
            if (ability instanceof ActiveAbility){
                ((ActiveAbility) ability).trigger(unit);
            }
        }
    }

    @Override
    public void handleServer(NetConnection con) {
        if(con.player == null || con.kicked) {return;}
        NHCall.triggerActiveAbility(unit, abilityId);
    }
}
