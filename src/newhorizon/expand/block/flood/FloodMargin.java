package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import newhorizon.util.graphic.SpriteUtil;

public class FloodMargin extends FloodWall{
    public TextureRegion[] atlas;

    public FloodMargin(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        TextureRegion full = Core.atlas.find(name + "-atlas");
        atlas = SpriteUtil.splitRegionArray(full, 32, 32, 1, SpriteUtil.ATLAS_INDEX_4_4);
    }

    public class FloodMarginBuilding extends FloodWallBuilding{
        public int drawIndex = 0;

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            drawIndex = 0;
            if (check(tile.x, tile.y + 1)) drawIndex += 1;
            if (check(tile.x + 1, tile.y)) drawIndex += 2;
            if (check(tile.x, tile.y - 1)) drawIndex += 4;
            if (check(tile.x - 1, tile.y)) drawIndex += 8;
        }

        public boolean check(int x, int y){
            Building building = Vars.world.build(x, y);
            return (building != null && building.block == this.block);
        }

        @Override
        public void draw() {
            Draw.z(Layer.block);
            Draw.rect(atlas[drawIndex], x, y);
        }
    }
}
