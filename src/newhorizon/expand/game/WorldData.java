package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.io.SaveFileReader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static newhorizon.expand.game.NHWorldData.CURRENT_VER;

public class WorldData implements SaveFileReader.CustomChunk {
    public short version = 0;
    private final EventSaveData eventSaveData;
    public float eventReloadSpeed = -1;
    public boolean jumpGateUsesCoreItems = true;
    public boolean applyEventTriggers = false;

    public WorldData(EventSaveData eventSaveData) {
        this.eventSaveData = eventSaveData;
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        stream.writeShort(CURRENT_VER);

        stream.writeFloat(eventReloadSpeed);
        stream.writeBoolean(jumpGateUsesCoreItems);
        stream.writeBoolean(applyEventTriggers);
        if (eventSaveData != null) eventSaveData.writeSnapshot(stream);
        DefaultSpecialEvent.writeState(stream);
    }

    @Override
    public void read(DataInput stream) throws IOException {
        version = stream.readShort();

        eventReloadSpeed = stream.readFloat();

        if (version > 0) {
            jumpGateUsesCoreItems = stream.readBoolean();
            applyEventTriggers = stream.readBoolean();
        }

        if (version > 1 && eventSaveData != null) {
            eventSaveData.readSnapshot(stream);
        }

        if (version > 2) {
            DefaultSpecialEvent.readState(stream);
        }

        version = CURRENT_VER;

        afterRead();
    }

    @Override
    public void read(DataInput stream, int length) throws IOException {
        read(stream);
    }

    public void afterRead() {
        if (Vars.headless && (Float.isNaN(eventReloadSpeed) || eventReloadSpeed > 5.55f)) {
            eventReloadSpeed = -1;
        }
    }
}
