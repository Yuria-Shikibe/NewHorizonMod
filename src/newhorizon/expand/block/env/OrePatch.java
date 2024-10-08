package newhorizon.expand.block.env;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OverlayFloor;

import static mindustry.Vars.tilesize;

public class OrePatch extends OverlayFloor {
    public final int patchSize = 7;
    public Item itemDrop;
    public OrePatch(String name) {
        super(name);
        variants = 0;
    }

    @Override
    public void drawBase(Tile tile){
        if(checkAdjacent(tile)){
            Mathf.rand.setSeed(tile.pos());
            Draw.rect(region, tile.worldx() - tilesize * (patchSize / 2f - 0.5f), tile.worldy() - tilesize * (patchSize / 2f - 0.5f));
        }
    }

    public boolean checkAdjacent(Tile tile){
        for(int x = 0; x < patchSize; x++){
            for(int y = 0; y < patchSize; y++) {
                Tile other = Vars.world.tile(tile.x - x, tile.y - y);
                if (other == null || other.overlay() != this) {
                    return false;
                }
            }
        }
        return true;
    }
}
