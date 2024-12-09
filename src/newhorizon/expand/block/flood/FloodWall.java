package newhorizon.expand.block.flood;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Edges;
import newhorizon.util.graphic.DrawUtil;
import newhorizon.util.graphic.SpriteUtil;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class FloodWall extends FloodBase{
    public static final int[] index = new int[]{
        0, 4, 1, 5,
        12, 8, 13, 9,
        2, 6, 3, 7,
        14, 10, 15, 11
    };
    public TextureRegion[] atlas;
    public int regionSize = 0;

    public FloodWall(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        TextureRegion full = Core.atlas.find(name + "-atlas");
        atlas = SpriteUtil.splitRegionArray(full, full.width / 4, full.height / 4, 0, index);
        assert atlas != null;
        regionSize = atlas[0].width / tilesize;
    }

    public class FloodWallBuilding extends FloodBaseBuilding{
        public boolean[] checkTile = new boolean[8];
        public int[] drawIndex = new int[]{0, 0, 0, 0};

        @Override
        public void draw() {
            Draw.z(Layer.block + 0.01f * size);
            Draw.rect(atlas[drawIndex[0]], x - regionSize, y + regionSize);
            Draw.rect(atlas[drawIndex[1] + 4], x + regionSize, y + regionSize);
            Draw.rect(atlas[drawIndex[2] + 8], x + regionSize, y - regionSize);
            Draw.rect(atlas[drawIndex[3] + 12], x - regionSize, y - regionSize);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            for (int i = 0; i < 8; i++){
                boolean out = false;
                for (int j = 0; j < size/2; j++){
                    Point2 p = Edges.getEdges(size)[Mathf.mod(i * size/2 + j - (size-1)/2, 4 * size)];
                    Building b = world.build(tileX() + p.x, tileY() + p.y);
                    if (!(b instanceof FloodBuildingEntity && (b.block.size == size || b.block.size == size * 2))){
                        out = true;
                        break;
                    }
                }
                checkTile[i] = !out;
            }

            drawIndex[0] = Mathf.num(checkTile[4]) * 2 + Mathf.num(checkTile[3]);
            drawIndex[1] = Mathf.num(checkTile[2]) * 2 + Mathf.num(checkTile[1]);
            drawIndex[2] = Mathf.num(checkTile[0]) * 2 + Mathf.num(checkTile[7]);
            drawIndex[3] = Mathf.num(checkTile[6]) * 2 + Mathf.num(checkTile[5]);
        }
    }
}
