package newhorizon.expand.map.planet;

import arc.struct.ByteSeq;
import newhorizon.expand.map.TerrainSchematic;

public class TileDataSchematic{
    public ByteSeq tileData;
    public int width, height;

    public TileDataSchematic(int width, int height, ByteSeq tileData){
        this.width = width;
        this.height = height;
        this.tileData = tileData;
    }
}