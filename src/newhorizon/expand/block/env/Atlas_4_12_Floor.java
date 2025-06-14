package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.util.graphic.SpriteUtil;

import static newhorizon.util.graphic.SpriteUtil.*;

public class Atlas_4_12_Floor extends Floor {
    public TextureRegion[] splitRegions;
    public Floor baseFloor;
    public boolean blendWater = false;

    public Atlas_4_12_Floor(String name) {
        super(name, 0);
    }

    public Atlas_4_12_Floor(String name, boolean blendWater) {
        super(name, 0);
        this.blendWater = blendWater;
    }

    @Override
    public void load() {
        super.load();
        splitRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 1, SpriteUtil.ATLAS_INDEX_4_12);
    }

    public void drawTile(Tile tile) {
        int drawIndex = 0;

        for (int i = 0; i < orthogonalPos.length; i++) {
            Point2 pos = orthogonalPos[i];
            if (checkTile(Vars.world.tile(tile.x + pos.x, tile.y + pos.y))) {
                drawIndex += 1 << i;
            }
        }

        for (int i = 0; i < diagonalPos.length; i++) {
            Point2[] posArray = diagonalPos[i];
            boolean out = true;
            for (Point2 pos : posArray) {
                if (!(checkTile(Vars.world.tile(tile.x + pos.x, tile.y + pos.y)))) {
                    out = false;
                    break;
                }
            }
            if (out) {
                drawIndex += 1 << i + 4;
            }
        }

        drawIndex = ATLAS_INDEX_4_12_MAP.get(drawIndex);

        Draw.rect(splitRegions[drawIndex], tile.worldx(), tile.worldy());
    }

    public boolean checkTile(Tile tile) {
        return tile != null && (tile.floor() == this || (blendWater && tile.floor().isLiquid));
    }

    @Override
    public void drawBase(Tile tile) {
        if (baseFloor != null) baseFloor.drawBase(tile);
        drawTile(tile);
        Draw.alpha(1f);
        drawOverlay(tile);
    }
}
