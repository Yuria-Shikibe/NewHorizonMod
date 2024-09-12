package newhorizon.expand.block.env;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

public class DataFloor extends AtlasFloor {
    public DataFloor(String name) {
        super(name);
    }

    @Override
    protected boolean doEdge(Tile tile, Tile otherTile, Floor other){
        return false;
    }

    @Override
    public void drawBase(Tile tile) {
        int data = tile.data;
        if (data < 0) data += 256;
        int index = Mathf.clamp(data, 0, splitRegion.length - 1);
        Draw.rect(splitRegion[index], tile.worldx(), tile.worldy());
    }
}
