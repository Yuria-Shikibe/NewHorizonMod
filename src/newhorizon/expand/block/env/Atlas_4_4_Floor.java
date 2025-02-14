package newhorizon.expand.block.env;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import newhorizon.util.graphic.SpriteUtil;

import static newhorizon.util.graphic.SpriteUtil.*;

public class Atlas_4_4_Floor extends Floor {
    public TextureRegion[] splitRegions;
    public Seq<Floor> blendFloors = new Seq<>();

    public Atlas_4_4_Floor(String name) {
        super(name, 0);
    }

    @Override
    public void load(){
        super.load();
        splitRegions = SpriteUtil.splitRegionArray(Core.atlas.find(name + "-atlas"), 32, 32, 0, SpriteUtil.ATLAS_INDEX_4_4);
    }

    private void drawTile(Tile tile){
        int drawIndex = 0;

        for(int i = 0; i < orthogonalPos.length; i++){
            Point2 pos = orthogonalPos[i];
            if (checkTile(Vars.world.tile(tile.x + pos.x, tile.y + pos.y))){
                drawIndex += 1 << i;
            }
        }

        Draw.rect(splitRegions[drawIndex], tile.worldx(), tile.worldy());
    }

    public boolean checkTile(Tile tile){
        return tile != null && (tile.floor() == this || blendFloors.contains(tile.floor()));
    }


    @Override
    public void drawBase(Tile tile) {
        //if (baseFloor != null) baseFloor.drawBase(tile);
        drawTile(tile);
        Draw.alpha(1f);
        drawOverlay(tile);
    }
}
