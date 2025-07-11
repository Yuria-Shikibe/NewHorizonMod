package newhorizon.expand.game;

import mindustry.io.SaveVersion;

public class NHWorldData {
    public static short CURRENT_VER = 1;

    public WorldData worldData = new WorldData();
    public TeamPayloadData teamPayloadData = new TeamPayloadData();

    public NHWorldData() {
        SaveVersion.addCustomChunk("nh-world-data", worldData);
        SaveVersion.addCustomChunk("nh-team-payload-data", teamPayloadData);
    }
}
