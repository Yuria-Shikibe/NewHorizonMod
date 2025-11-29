package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import mindustry.world.Tile;
import mindustry.world.blocks.TileBitmask;
import mindustry.world.blocks.environment.Floor;
import newhorizon.NewHorizon;
import newhorizon.util.graphic.SpriteUtil;

public class TiledFloor extends Floor {
    public TextureRegion[][] spilt;

    public String tileName = "plating-floor";

    public int splitTileSize = 4;
    public int splitVariants = 12;

    public boolean useTiles = true;

    boolean splitLoaded = false;

    public TiledFloor(String name) {
        super(name);
    }

    public TiledFloor(String name, int sSize, int sVar) {
        super(name);
        splitTileSize = sSize;
        splitVariants = sVar;
    }

    @Override
    public void load() {
        super.load();

        var full = Core.atlas.find(NewHorizon.name(tileName));

        if(autotile){
            var tiled = Core.atlas.find(name + "-tiled");
            if (tiled.height == 128) autotileRegions = SpriteUtil.splitRegionArray(tiled, 32, 32);
            if (tiled.height == 136) autotileRegions = SpriteUtil.splitRegionArray(tiled, 32, 32, 1);

            if(autotileVariants > 1) {
                autotileVariantRegions = new TextureRegion[autotileVariants][];
                for (int i = 0; i < autotileVariants; i++) {
                    var tiledVariant = Core.atlas.find(name + "-tiled-" + (i + 1));
                    if (tiled.height == 128) autotileVariantRegions[i] = SpriteUtil.splitRegionArray(tiledVariant, 32, 32);
                    else if (tiled.height == 136) autotileVariantRegions[i] = SpriteUtil.splitRegionArray(tiledVariant, 32, 32, 1);
                    else Log.err("Failed to load tile " + name + "-tiled-" + (i + 1) + ": " + tiledVariant.width + "x" + tiledVariant.height);
                }
            }
        }

        if (!useTiles) return;

        int pw = splitTileSize * splitVariants * 32;
        int ph = splitTileSize * 32;
        if (full.width == pw && full.height == ph) {
            spilt = new TextureRegion[splitTileSize * splitVariants][splitTileSize];
            for (int i = 0; i < splitVariants; i++) {
                spilt = full.split(32, 32);
            }
            splitLoaded = true;
        }else {
            Log.err("Failed to load tile " + tileName + "with size " + pw + "x" + ph, ". tiled disable.");
        }

    }

    private void drawTile(Tile tile) {
        int tx = tile.x / splitTileSize * splitTileSize;
        int ty = tile.y / splitTileSize * splitTileSize;

        int index = Mathf.randomSeed(Point2.pack(tx, ty), 0, splitVariants - 1);
        int ix = index * splitTileSize + tile.x - tx;
        int iy = splitTileSize - (tile.y - ty) - 1;
        Draw.rect(spilt[ix][iy], tile.worldx(), tile.worldy());
    }

    @Override
    public void drawBase(Tile tile) {
        if (useTiles && splitLoaded) drawTile(tile);
        super.drawBase(tile);
    }
}