package newhorizon.expand.block.env;

import arc.graphics.g2d.Draw;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OverlayFloor;

public class ColorOverlayFloor extends OverlayFloor {
    public ColorOverlayFloor(String name) {
        super(name);
        useColor = true;
    }

    @Override
    public void drawBase(Tile tile) {
        Draw.alpha((tile.extraData << 24) / 255f);
        Draw.color(tile.extraData | 0xff);
        Draw.rect(this.region, tile.worldx(), tile.worldy());
        Draw.color();
        Draw.reset();
    }

    @Override
    public int minimapColor(Tile tile){
        return tile.extraData | 0xff;
    }
}
