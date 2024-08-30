package newhorizon.expand.block.env;

import arc.graphics.g2d.Draw;
import arc.util.Tmp;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OverlayFloor;

public class HighAltitudeMarker extends OverlayFloor {
    public HighAltitudeMarker(String name) {
        super(name);

        variants = 1;
    }

    @Override
    public void drawBase(Tile tile){}

    @Override
    public int minimapColor(Tile tile){
        return tile.floor().mapColor.rgba();
    }
}
