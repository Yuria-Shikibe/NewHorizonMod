package newhorizon.expand.block.environment;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.expand.block.editor.PlateFloorPlacer;
import newhorizon.util.graphic.SpriteUtil;

public class PlateFloor extends Floor {
    //[block size] [variant] [slice index]
    public String rawName;
    public int[] variantPairs = new int[16];
    public TextureRegion[][][] plateRegions = new TextureRegion[16][][];

    public PlateFloor(String name) {
        super(name);

        rawName = name;

        variants = 0;
        inEditor = false;

        drawEdgeIn = false;
        drawEdgeOut = false;
        saveData = true;
    }

    public void addVariant(int size, int variant) {
        variantPairs[(size - 1)] = variant;
    }

    public void loadBlocks() {
        for (int i = 0; i < variantPairs.length; i++) {
            if (variantPairs[i] == 0) continue;
            for (int j = 0; j < variantPairs[i]; j++) {
                new PlateFloorPlacer(rawName + "-" + (i + 1) + "-" + j, this, i + 1, j);
            }
        }
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < variantPairs.length; i++) {
            if (variantPairs[i] == 0) continue;
            plateRegions[i] = new TextureRegion[variantPairs[i]][];
            for (int j = 0; j < variantPairs[i]; j++) {
                plateRegions[i][j] = SpriteUtil.splitRegionArray(getRegion(i, j), 32, 32);
            }
        }
    }

    public TextureRegion getRegion(int size, int variant) {
        return Core.atlas.find(name + "-" + (size + 1) + "-" + variant);
    }

    public byte getSize(Tile tile) {
        return (byte) (tile.data & 0x0F);
    }

    public byte getType(Tile tile) {
        return (byte) ((tile.data >>> 4) & 0x0F);
    }

    @Override
    public void drawBase(Tile tile) {
        if (validTile(tile)) {
            Draw.rect(getDrawRegion(tile), tile.worldx(), tile.worldy());
        }else {
            super.drawBase(tile);
        }
    }

    public boolean validTile(Tile tile) {
        return variantPairs[getSize(tile)] > getType(tile);
    }

    public TextureRegion getDrawRegion(Tile tile) {
        if (plateRegions[getSize(tile)] == null || plateRegions[getSize(tile)].length <= getType(tile)) return region;
        TextureRegion[][] variants = plateRegions[getSize(tile)];
        if (variants[getType(tile)] == null || variants[getType(tile)].length <= tile.floorData) return region;
        return plateRegions[getSize(tile)][getType(tile)][tile.floorData];
    }
}
