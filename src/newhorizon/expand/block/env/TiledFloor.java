package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.NewHorizon;

public class TiledFloor extends Floor {
    public TextureRegion[] cover;
    public TextureRegion[][][] coverSplit;

    public String[] coverRegionName = {"plating-cover", "metal-cover"};
    public int[] coverSize = {2, 4};
    public int[] coverVariants = {24, 12};

    public TiledFloor(String name) {
        super(name);
        variants = 1;
    }

    @Override
    public void load(){
        super.load();

        cover = new TextureRegion[coverRegionName.length];
        coverSplit = new TextureRegion[coverRegionName.length][][];
        for (int i = 0; i < coverRegionName.length; i++){
            cover[i] = Core.atlas.find(NewHorizon.name(coverRegionName[i]));
        }
        for (int i = 0; i < cover.length; i++){
            coverSplit[i] = cover[i].split(32, 32);
        }
    }

    private void drawTile(Tile tile){
        for (int i = 0; i < coverSplit.length; i++){
            int bx = tile.x / coverSize[i] * coverSize[i], by = tile.y / coverSize[i] * coverSize[i];
            int baseIndex = Mathf.randomSeed(Point2.pack(bx, by), 0, coverVariants[i] - 1);
            int bix = baseIndex * coverSize[i] + tile.x - bx, biy = coverSize[i] - 1 - (tile.y - by);
            Draw.rect(coverSplit[i][bix][biy], tile.worldx(), tile.worldy());
        }
    }

    @Override
    protected boolean doEdge(Tile tile, Tile otherTile, Floor other){
        return false;
    }

    @Override
    public void drawBase(Tile tile) {
        Draw.rect(region, tile.worldx(), tile.worldy());
        drawTile(tile);
        Draw.alpha(1f);
        //drawEdges(tile);
        drawOverlay(tile);
    }
}
