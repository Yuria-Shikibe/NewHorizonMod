package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.ArcRuntimeException;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static newhorizon.util.graphic.SpriteUtil.splitRegionArray;

public class AtlasFloor extends Floor {
    public TextureRegion full;
    public TextureRegion[] splitRegion;
    public int maxSize;
    public AtlasFloor(String name) {
        super(name);
        variants = 1;
    }

    @Override
    public void load(){
        super.load();
        full = Core.atlas.find(name + "-full");
        splitRegion = splitRegionArray(full, 32, 32);
        if (splitRegion != null) maxSize = splitRegion.length;
        if (maxSize > 256) throw new ArcRuntimeException("Max Size for " + name + " > 256!");
    }



    @Override
    public void drawBase(Tile tile) {
        Mathf.rand.setSeed(tile.pos());
        Draw.rect(splitRegion[Mathf.randomSeed(tile.pos(), 0, Math.max(0, maxSize - 1))], tile.worldx(), tile.worldy());

        Draw.alpha(1f);
        drawEdges(tile);
        drawOverlay(tile);
    }
}
