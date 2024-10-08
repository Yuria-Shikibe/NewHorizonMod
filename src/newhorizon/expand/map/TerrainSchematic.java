package newhorizon.expand.map;

import arc.struct.Seq;
import mindustry.content.Blocks;
import mindustry.game.Schematic.Stile;
import mindustry.gen.Building;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class TerrainSchematic {
    public Seq<SData> tileData;
    public Seq<Stile> floor;
    public Seq<Stile> overlay;
    public Seq<Stile> block;

    public int width, height;



    public TerrainSchematic(Seq<SData> tileData, Seq<Stile> floor, Seq<Stile> overlay, Seq<Stile> block, int width, int height){
        this.tileData = tileData;
        this.floor = floor;
        this.overlay = overlay;
        this.block = block;

        this.width = width;
        this.height = height;
    }

    public TerrainSchematic(int startX, int startY, int endX, int endY){
        width = endX - startX + 1;
        height = endY - startY + 1;

        Seq<Building> buildTmp = new Seq<>();
        Seq<SData> tileDataTmp = new Seq<>();
        Seq<Stile> floorTmp = new Seq<>();
        Seq<Stile> overlayTmp = new Seq<>();
        Seq<Stile> blockTmp = new Seq<>();

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                Tile tile = world.tile(x + startX, y + startY);
                if (tile == null) break;
                tileDataTmp.add(new SData(tile.data, x, y));
                if (tile.floor() != Blocks.air){floorTmp.add(new Stile(tile.floor(), x, y, null, (byte) 0));}
                if (tile.overlay() != Blocks.air){overlayTmp.add(new Stile(tile.overlay(), x, y, null, (byte) 0));}
                if (tile.block().isStatic() || tile.block() == Blocks.air){blockTmp.add(new Stile(tile.block(), x, y, null, (byte) 0));}
            }
        }

        for (Building building : buildTmp) {
            blockTmp.add(new Stile(building.block, building.tileX(), building.tileY(), building.config(), (byte) building.rotation));
        }

        tileData = tileDataTmp;
        floor = floorTmp;
        overlay = overlayTmp;
        block = blockTmp;
    }

    @Override
    public String toString() {
        return "data size:" + tileData.size + "first: " + tileData.firstOpt().data +
            ", floor size =" + floor.size + "first: " + floor.firstOpt().block.name +
            ", overlay size =" + overlay.size + "first: " + overlay.firstOpt().block.name +
            ", block size =" + block.size + "first: " + block.firstOpt().block.name +
            ", width=" + width +
            ", height=" + height;
    }

    public static class SData{
        public short x, y;
        public byte data;

        public SData(byte data, int x, int y){
            this.data = data;
            this.x = (short)x;
            this.y = (short)y;
        }

        //pooling only
        public SData(){
            data = 0;
        }
    }
}
