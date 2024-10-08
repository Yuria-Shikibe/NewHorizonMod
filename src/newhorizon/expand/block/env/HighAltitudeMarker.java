package newhorizon.expand.block.env;

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
