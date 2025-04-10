package newhorizon.expand.game;

import mindustry.Vars;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NHWorldData{
	public static short CURRENT_VER = 1;

	public WorldData worldData = new WorldData();
	public WorldTileData worldTileData = new WorldTileData();
	public TeamPayloadData teamPayloadData = new TeamPayloadData();
	
	public NHWorldData(){
		SaveVersion.addCustomChunk("nh-world-data", worldData);
		SaveVersion.addCustomChunk("nh-world-tile-data", worldTileData);
		SaveVersion.addCustomChunk("nh-team-payload-data", teamPayloadData);
	}
}
