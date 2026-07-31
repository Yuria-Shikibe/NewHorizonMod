package newhorizon.expand.game;

import mindustry.io.SaveVersion;

public class NHWorldData {
    public static short CURRENT_VER = 2;

    public EventSaveData eventSaveData = new EventSaveData();
    public WorldData worldData = new WorldData(eventSaveData);
    public TeamPayloadData teamPayloadData = new TeamPayloadData();

    public NHWorldData() {
        SaveVersion.addCustomChunk("nh-world-data", worldData);
        SaveVersion.addCustomChunk("nh-team-payload-data", teamPayloadData);
    }
}
