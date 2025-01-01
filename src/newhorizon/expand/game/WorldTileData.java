package newhorizon.expand.game;

import arc.math.geom.Point2;
import arc.struct.ByteSeq;
import arc.struct.IntSeq;
import mindustry.io.SaveFileReader;
import mindustry.world.Tile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.world;

public class WorldTileData implements SaveFileReader.CustomChunk{
    public IntSeq packPos = new IntSeq();
    public ByteSeq tileByte = new ByteSeq();

    public void addTileData(int x, int y, byte data){
        packPos.add(Point2.pack(x, y));
        tileByte.add(data);
    }

    public void setupWorldTile(){
        for (int i = 0; i < packPos.size; i++){
            Tile tile = world.tile(packPos.get(i));
            if (tile != null){
                tile.data = tileByte.get(i);
                tile.recache();
            }
        }
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        if (packPos.size != tileByte.size) throw new IOException("size not match!");
        stream.writeInt(packPos.size);
        for (int i = 0; i < packPos.size; i++){
            stream.writeInt(packPos.get(i));
            stream.writeByte(tileByte.get(i));
        }
    }

    @Override
    public void read(DataInput stream) throws IOException {
        packPos.clear();
        tileByte.clear();
        int size = stream.readInt();
        for (int i = 0; i < size; i++){
            packPos.add(stream.readInt());
            tileByte.add(stream.readByte());
        }

        setupWorldTile();
    }
}
