package newhorizon.expand.map;

import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Streams;
import arc.util.io.Writes;
import arc.util.serialization.Base64Coder;
import mindustry.content.Blocks;
import mindustry.game.Schematic.Stile;
import mindustry.io.TypeIO;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.expand.map.TerrainSchematic.SData;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static mindustry.Vars.content;
import static mindustry.Vars.world;

public class SchematicUtil {
    //new horizon schematic save, funny
    public static final byte[] header = {'n', 'h', 's', 's'};
    private static final Streams.OptimizedByteArrayOutputStream out = new Streams.OptimizedByteArrayOutputStream(1024);

    /** Converts a schematic to base64. Note that the result of this will always start with 'bXNjaAB'.*/
    public static String writeBase64(TerrainSchematic schematic){
        try{
            out.reset();
            write(schematic, out);
            return new String(Base64Coder.encode(out.getBuffer(), out.size()));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /** Loads a schematic from base64. May throw an exception. */
    public static TerrainSchematic readBase64(String schematic){
        try{
            return read(new ByteArrayInputStream(Base64Coder.decode(schematic.trim())));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void write(TerrainSchematic schematic, OutputStream output) throws IOException{
        output.write(header);

        try(DataOutputStream stream = new DataOutputStream(new DeflaterOutputStream(output))){
            stream.writeShort(schematic.width);
            stream.writeShort(schematic.height);

            stream.writeInt(schematic.tileData.size);
            for (SData data: schematic.tileData){
                stream.writeByte(data.data);
                stream.writeInt(Point2.pack(data.x, data.y));
            }

            stream.writeInt(schematic.floor.size);
            for (Stile floor: schematic.floor){
                stream.writeUTF(floor.block.name);
                stream.writeInt(Point2.pack(floor.x, floor.y));
            }

            stream.writeInt(schematic.overlay.size);
            for (Stile overlay: schematic.overlay){
                stream.writeUTF(overlay.block.name);
                stream.writeInt(Point2.pack(overlay.x, overlay.y));
            }

            stream.writeInt(schematic.block.size);
            for (Stile block: schematic.block){
                stream.writeUTF(block.block.name);
                stream.writeInt(Point2.pack(block.x, block.y));
                TypeIO.writeObject(Writes.get(stream), block.config);
                stream.writeByte(block.rotation);
            }
        }
    }

    public static TerrainSchematic read(InputStream input) throws IOException{
        for(byte b : header){
            if(input.read() != b){
                throw new IOException("Not New Horizon Schematic Save!");
            }
        }

        try(DataInputStream stream = new DataInputStream(new InflaterInputStream(input))){
            short width = stream.readShort(), height = stream.readShort();

            int tileDataSize = stream.readInt();
            Seq<SData> tileDataAll = new Seq<>(tileDataSize);
            for (int i = 0; i < tileDataSize; i ++){
                byte data = stream.readByte();
                int posPack = stream.readInt();
                SData tileData = new SData(data, Point2.x(posPack), Point2.y(posPack));
                tileDataAll.add(tileData);
            }

            int floorSize = stream.readInt();
            Seq<Stile> floorAll = new Seq<>(floorSize);
            for (int i = 0; i < floorSize; i ++){
                String name = stream.readUTF();
                int posPack = stream.readInt();
                Stile floor = new Stile(content.block(name), Point2.x(posPack), Point2.y(posPack), null, (byte) 0);
                floorAll.add(floor);
            }

            int overlaySize = stream.readInt();
            Seq<Stile> overlayAll = new Seq<>(floorSize);
            for (int i = 0; i < overlaySize; i ++){
                String name = stream.readUTF();
                int posPack = stream.readInt();
                Stile floor = new Stile(content.block(name), Point2.x(posPack), Point2.y(posPack), null, (byte) 0);
                overlayAll.add(floor);
            }

            int blockSize = stream.readInt();
            Seq<Stile> blockAll = new Seq<>(floorSize);
            for (int i = 0; i < blockSize; i ++){
                String name = stream.readUTF();
                int posPack = stream.readInt();
                Object config = TypeIO.readObject(Reads.get(stream));
                byte rotation = stream.readByte();
                Stile floor = new Stile(content.block(name), Point2.x(posPack), Point2.y(posPack), config, rotation);
                blockAll.add(floor);
            }

            return new TerrainSchematic(tileDataAll, floorAll, overlayAll, blockAll, width, height);
        }catch (IOException e){
            Log.info(e);
        }
        return null;
    }

    public static void placeTerrain(TerrainSchematic schem, int x, int y){
        int ox = x - schem.width/2, oy = y - schem.height/2;
        schem.tileData.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null) tile.data = st.data;
        });
        schem.floor.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null) tile.setFloor((Floor) st.block);
        });
        schem.overlay.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null) tile.setOverlay(st.block);
        });
        schem.block.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null) tile.setBlock(st.block);
        });
    }

    /*
    public static void placeTerrain(Schematic schem, int x, int y){
        int ox = x - schem.width/2, oy = y - schem.height/2;
        schem.tiles.each(st -> {
            Tile tile = world.tile(st.x + ox, st.y + oy);
            if (tile != null && st.block instanceof Floor){
                tile.setFloor((Floor) st.block);
            }
        });
    }

     */
}
