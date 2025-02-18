package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.StaticWall;
import newhorizon.util.graphic.SpriteUtil;

import static newhorizon.util.graphic.SpriteUtil.*;

public class Atlas_4_12_Wall extends StaticWall {
    public TextureRegion[] splitRegions;
    public Block baseBlock;

    public Atlas_4_12_Wall(String name) {
        super(name);
    }

    @Override
    public void load(){
        super.load();
        splitRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 1, ATLAS_INDEX_4_12);
    }

    private void drawTile(Tile tile){
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

        Draw.rect(splitRegions[drawIndex], tile.worldx(), tile.worldy());
    }

    public boolean checkTile(Tile tile){
        return tile != null && tile.block() == this;
    }

    @Override
    public void drawBase(Tile tile) {
        if (baseBlock != null) baseBlock.drawBase(tile);
        drawTile(tile);
        Draw.alpha(1f);
    }
}
