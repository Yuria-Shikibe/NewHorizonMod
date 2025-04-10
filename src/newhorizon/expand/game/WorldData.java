package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.io.SaveFileReader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static newhorizon.expand.game.NHWorldData.CURRENT_VER;

public class WorldData implements SaveFileReader.CustomChunk{
    public short version = 0;
    public float eventReloadSpeed = -1;
    public boolean jumpGateUsesCoreItems = true;
    public boolean applyEventTriggers = false;

    @Override
    public void write(DataOutput stream) throws IOException{
        stream.writeShort(CURRENT_VER);

        stream.writeFloat(eventReloadSpeed);
        stream.writeBoolean(jumpGateUsesCoreItems);
        stream.writeBoolean(applyEventTriggers);
    }

    @Override
    public void read(DataInput stream) throws IOException{
        version = stream.readShort();

        eventReloadSpeed = stream.readFloat();

        if(version > 0){
            jumpGateUsesCoreItems = stream.readBoolean();
            applyEventTriggers = stream.readBoolean();
        }

        version = CURRENT_VER;

        afterRead();
    }

    public void afterRead(){
        if(Vars.headless && (Float.isNaN(eventReloadSpeed) || eventReloadSpeed > 5.55f)){
            eventReloadSpeed = -1;
        }
    }
}
