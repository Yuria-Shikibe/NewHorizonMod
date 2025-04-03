package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.world.Tile;
import newhorizon.NewHorizon;
import newhorizon.util.graphic.SpriteUtil;

import static newhorizon.util.graphic.SpriteUtil.*;

public class MaskFloor extends Atlas_4_12_Floor {
    public MaskFloor(String name) {
        super(name);
    }

    @Override
    public void load(){
        super.load();
        splitRegions = SpriteUtil.splitRegionArray(Core.atlas.find(NewHorizon.name("mask-atlas")), 32, 32, 1, SpriteUtil.ATLAS_INDEX_4_12);
    }

    @Override
    public void drawTile(Tile tile){
        int drawIndex = 0;

        for(int i = 0; i < orthogonalPos.length; i++){
            Point2 pos = orthogonalPos[i];
            if (checkTile(Vars.world.tile(tile.x + pos.x, tile.y + pos.y))){
                drawIndex += 1 << i;
            }
        }

        for(int i = 0; i < diagonalPos.length; i++){
            Point2[] posArray = diagonalPos[i];
            boolean out = true;
            for (Point2 pos : posArray) {
                if (!(checkTile(Vars.world.tile(tile.x + pos.x, tile.y + pos.y)))) {
                    out = false;
                    break;
                }
            }
            if (out){
                drawIndex += 1 << i + 4;
            }
        }

        drawIndex = ATLAS_INDEX_4_12_MAP.get(drawIndex);

        Draw.rect(region, tile.worldx(), tile.worldy());
        Draw.blend(Blending.additive);
        Draw.rect(splitRegions[drawIndex], tile.worldx(), tile.worldy());
        Draw.blend();
    }

    public boolean checkTile(Tile tile){
        return tile != null && tile.floor() instanceof MaskFloor;
    }
}
