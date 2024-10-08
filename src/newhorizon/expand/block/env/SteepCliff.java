package newhorizon.expand.block.env;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Tmp;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.world;

public class SteepCliff extends Floor {
    public TextureRegion[][] cliff;

    public SteepCliff(String name){
        super(name);
        variants = 1;
        noSideBlend = true;

        placeableOn = false;
    }

    @Override
    public void load() {
        super.load();
        cliff = variantRegions[0].split(32, 32);
    }

    @Override
    public void drawBase(Tile tile){
        Draw.rect(cliff[getTileIndex(tile)][1], tile.worldx(), tile.worldy());
    }

    private byte getTileIndex(Tile tile){
        byte index = 0;
        if (world.floor(tile.x, tile.y + 1) == this) index += 1;
        if (world.floor(tile.x + 1, tile.y) == this) index += 2;
        if (world.floor(tile.x, tile.y - 1) == this) index += 4;
        if (world.floor(tile.x - 1, tile.y) == this) index += 8;
        return index;
    }

    @Override
    public int minimapColor(Tile tile){
        return Tmp.c1.set(tile.floor().mapColor).mul(1.2f).rgba();
    }
}
