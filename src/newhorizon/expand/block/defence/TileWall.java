package newhorizon.expand.block.defence;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.Wall;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;
import static newhorizon.util.graphic.SpriteUtil.splitRegionArray;


//this cause render issues and dont use this for now
public class TileWall extends Wall {
    public TextureRegion full;
    public TextureRegion[] splitRegion;

    public TileWall smallWall;
    public TileWall largeWall;

    public TileWall(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        full = Core.atlas.find(name + "-full");
        splitRegion = splitRegionArray(full, size * 32, size * 32, 1);
    }

    public class TileWallBuild extends WallBuild{
        //well i prefer north/east/south/west clockwise but this suck
        public int[] indexDirection = new int[4];
        public boolean[] cornerCheck = new boolean[4];
        public TileWallBuild[] cornerBuild = new TileWallBuild[4];
        public int variantIndex;

        public Point2[] point2;

        @Override
        public void created() {
            super.created();
            point2 = Edges.getEdges(size);
            Mathf.rand.setSeed(id);
            variantIndex = Mathf.random(0, 3);
        }



        @Override
        public void onRemoved() {
            for (TileWallBuild building: cornerBuild){
                if (building != null){
                    building.updateCornerBuild();
                }
            }
            super.onRemoved();
        }

        public void updateCornerBuild(){
            float topX = x + size * tilesize / 2f + 4f;
            float topY = y + size * tilesize / 2f + 4f;
            float botX = x - size * tilesize / 2f - 4f;
            float botY = y - size * tilesize / 2f - 4f;

            if (check(topX, topY)){cornerBuild[0] = (TileWallBuild) world.buildWorld(topX, topY);}
            if (check(botX, topY)){cornerBuild[1] = (TileWallBuild) world.buildWorld(botX, topY);}
            if (check(botX, botY)){cornerBuild[2] = (TileWallBuild) world.buildWorld(botX, botY);}
            if (check(topX, botY)){cornerBuild[3] = (TileWallBuild) world.buildWorld(topX, botY);}

            updateCorner();
        }


        public void updateSideIndex(){
            for (int i = 0; i < 4; i++){
                int index = 0;
                for (int j = 0; j < size; j++){
                    if (checkInt(tileX() + point2[i * size + j].x, tileY() + point2[i * size + j].y)){
                        index += 1 << j;
                    }
                }
                indexDirection[i] = index;
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            for (Building building: cornerBuild){
                if (building != null){
                    Fill.circle(building.x, building.y, 3);
                }
            }
        }

        public void updateCorner(){
            float topX = x + size * tilesize / 2f + 4f;
            float topY = y + size * tilesize / 2f + 4f;
            float botX = x - size * tilesize / 2f - 4f;
            float botY = y - size * tilesize / 2f - 4f;

            cornerCheck[0] = (check(topX, topY) && check(topX - tilesize, topY) && check(topX, topY - tilesize));
            cornerCheck[1] = (check(botX, topY) && check(botX + tilesize, topY) && check(botX, topY - tilesize));
            cornerCheck[2] = (check(botX, botY) && check(botX + tilesize, botY) && check(botX, botY + tilesize));
            cornerCheck[3] = (check(topX, botY) && check(topX - tilesize, botY) && check(topX, botY + tilesize));
        }

        private boolean check(float x, float y){
            Tile checkTile = world.tileWorld(x, y);
            return checkTile != null && checkTile.build != null && (
                smallWall != null && checkTile.build.block() == smallWall ||
                largeWall != null && checkTile.build.block() == largeWall);
        }

        private boolean checkInt(int x, int y){
            Tile checkTile = world.tile(x, y);
            return checkTile != null && checkTile.build != null && (
                smallWall != null && checkTile.build.block() == smallWall ||
                largeWall != null && checkTile.build.block() == largeWall);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateSideIndex();
            updateCornerBuild();
            for (TileWallBuild other : cornerBuild) {
                if (other != null){
                    other.updateCornerBuild();
                }
            }
        }

        @Override
        public void draw() {
            int sideSize = Mathf.pow(2, size);

            for (int i = 0; i < 4; i++){
                int splitIndex = i * sideSize + indexDirection[i];
                Draw.rect(splitRegion[splitIndex], x, y);
            }
            for (int i = 0; i < 4; i++){
                int rot = i * 90;
                if (cornerCheck[i]){
                    Draw.rect(splitRegion[4 * sideSize + 1], x, y, rot);
                }
            }
            Draw.z(Layer.effect);
            for (int i = 0; i < 4; i++){
                int splitIndex = 4 * sideSize + 4 + indexDirection[i];
                int rot = i * 90;
                Draw.rect(splitRegion[splitIndex], x, y, rot);
            }
            Draw.reset();
        }


    }
}
