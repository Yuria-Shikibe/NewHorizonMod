package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.ArcRuntimeException;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.NewHorizon;

public class DataFloor extends Floor {
    public TextureRegion full;
    public TextureRegion[] splitRegion;
    public int maxSize;
    public DataFloor(String name) {
        super(name);
        variants = 1;
    }

    @Override
    public void load(){
        super.load();
        full = Core.atlas.find(name + "-full");
        splitRegion = splitRegion(full, 32, 32);
        maxSize = splitRegion.length;
        if (maxSize > 256) throw new ArcRuntimeException("Max Size for " + name + " > 256!");
    }

    public TextureRegion[] splitRegion(TextureRegion region, int tileWidth, int tileHeight){
        if(region.texture == null) return null;
        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int sw = width / tileWidth;
        int sh = height / tileHeight;

        int startX = x;
        TextureRegion[] tiles = new TextureRegion[sw * sh];
        for(int cy = 0; cy < sh; cy++, y += tileHeight){
            x = startX;
            for(int cx = 0; cx < sw; cx++, x += tileWidth){
                tiles[cx + cy * sw] = new TextureRegion(region.texture, x, y, tileWidth, tileHeight);
            }
        }

        return tiles;
    }

    private void drawTile(Tile tile){
        int data = tile.data;
        if (data < 0) data += 256;
        int index = Mathf.clamp(data, 0, splitRegion.length - 1);
        Draw.rect(splitRegion[index], tile.worldx(), tile.worldy());
    }

    @Override
    protected boolean doEdge(Tile tile, Tile otherTile, Floor other){
        return false;
    }

    @Override
    public void drawBase(Tile tile) {
        drawTile(tile);
    }

}
