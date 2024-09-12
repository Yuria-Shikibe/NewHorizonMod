package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.util.Log;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;
import newhorizon.util.graphic.DrawUtil;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.util.graphic.SpriteUtil.splitRegionArray;

public class TileWall extends Wall {
    public TextureRegion full;
    public TextureRegion[] splitRegion;

    public Block smallWall;
    public Block largeWall;

    public TileWall(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        full = Core.atlas.find(name + "-full");
        splitRegion = splitRegionArray(full, 64, 64);
    }

    public class TileWallBuild extends WallBuild{
        //well i prefer north/east/south/west clockwise but this suck
        public int[] indexDirection = new int[4];
        public boolean[] cornerCheck = new boolean[4];

        public Point2[] point2;

        @Override
        public void created() {
            super.created();
            point2 = Edges.getEdges(size);
        }

        public void updateSideIndex(){
            for (int i = 0; i < 4; i++){
                int index = 0;
                for (int j = 0; j < size; j++){
                    Tile t = world.tile(tileX() + point2[i * size + j].x, tileY() + point2[i * size + j].y);
                    if (t != null && t.build != null){
                        if ((smallWall != null && t.build.block == smallWall) ||
                            (largeWall != null && t.build.block == largeWall)) {
                            index += 1 << j;
                        }
                    }
                }
                indexDirection[i] = index;
            }
        }

        public void updateCorner(){
            float topX = x + size * tilesize / 2f + 0.5f;
            float topY = y + size * tilesize / 2f + 0.5f;
            float botX = x - size * tilesize / 2f - 0.5f;
            float botY = y - size * tilesize / 2f - 0.5f;
            if (check(topX, topY) && check(topX - tilesize, topY) && check(topX, topY - tilesize)) cornerCheck[0] = true;
            if (check(botX, topY) && check(botX + tilesize, topY) && check(botX, topY - tilesize)) cornerCheck[1] = true;
            if (check(botX, botY) && check(botX + tilesize, botY) && check(botX, botY + tilesize)) cornerCheck[2] = true;
            if (check(topX, botY) && check(topX - tilesize, botY) && check(topX, botY + tilesize)) cornerCheck[3] = true;
        }

        private boolean check(float x, float y){
            return world.buildWorld(x, y) != null && (world.buildWorld(x, y).block == smallWall || world.buildWorld(x, y).block == largeWall);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateSideIndex();
            updateCorner();
        }

        @Override
        public void draw() {
            int sideSize = Mathf.pow(2, size);
            for (int i = 0; i < 4; i++){
                int splitIndex = i * sideSize + indexDirection[i];
                Draw.rect(splitRegion[splitIndex], x, y);
            }
            for (int i = 0; i < 4; i++){
                if (cornerCheck[i]){
                    Draw.rect(splitRegion[4 * sideSize + i], x, y);
                }
            }
        }
    }
}
