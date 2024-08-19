package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

public class TiledFloor extends Floor {
    public TextureRegion full;
    public TextureRegion[][] largeSpilt;

    public TiledFloor(String name) {
        super(name);
        variants = 1;
    }

    @Override
    public void load(){
        super.load();

        full = Core.atlas.find(name + "-full");
        largeSpilt = new TextureRegion[48][4];
        for (int i = 0; i < 12; i++){
            largeSpilt = full.split(32, 32);
        }
    }

    private void drawTile(Tile tile){
        int tx = tile.x / 4 * 4;
        int ty = tile.y / 4 * 4;


        int index = Mathf.randomSeed(Point2.pack(tx, ty), 0, 11);
        int ix = index * 4 + tile.x - tx;
        int iy = 3 - (tile.y - ty);
        Draw.rect(largeSpilt[ix][iy], tile.worldx(), tile.worldy());
    }

    @Override
    public void drawBase(Tile tile) {
        drawTile(tile);
        Draw.alpha(1f);
        //drawEdges(tile);
        drawOverlay(tile);
    }
}
