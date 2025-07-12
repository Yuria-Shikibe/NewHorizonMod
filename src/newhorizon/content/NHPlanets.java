package newhorizon.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.GridBits;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.*;
import mindustry.maps.generators.BlankPlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Env;
import newhorizon.content.blocks.EnvironmentBlock;
import newhorizon.util.feature.ManhattanVoronoi;

public class NHPlanets {
    public static Planet midantha;

    public static void load() {
        midantha = new Planet("midantha", Planets.sun, 2){{
            sectors.add(new Sector(this, PlanetGrid.Ptile.empty));

            bloom = true;
            visible = true;
            accessible = true;
            hasAtmosphere = true;
            alwaysUnlocked = true;
            iconColor = NHColor.darkEnrColor;
            meshLoader = () -> new NHModMesh(
                    this, 5, 5, 0.3, 1.7, 1.2, 1.4, 1.1f,
                    NHColor.darkEnrFront.cpy().lerp(Color.white, 0.2f),
                    NHColor.darkEnrFront,
                    NHColor.darkEnrColor,
                    NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f).mul(1.05f),
                    Pal.darkestGray.cpy().mul(0.95f),
                    Pal.darkestGray.cpy().lerp(Color.white, 0.105f),
                    Pal.darkestGray.cpy().lerp(Pal.gray, 0.2f),
                    Pal.darkestGray
            );

            ruleSetter = r -> {
                r.waveTeam = Team.blue;
                r.placeRangeCheck = false;
                r.showSpawns = true;
                r.waveSpacing = 80 * Time.toSeconds;
                r.initialWaveSpacing = 8f * Time.toMinutes;
                r.hideBannedBlocks = true;

                Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
                teamRule.rtsAi = false;
                teamRule.unitBuildSpeedMultiplier = 5f;
                teamRule.buildSpeedMultiplier = 3f;
            };

            generator = new NHPlanetGenerator();

            cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 2, 0.15F, 0.14F, 5, Pal.darkerMetal.cpy().lerp(NHColor.darkEnrColor, 0.35f).a(0.55F), 2, 0.42F, 1.0F, 0.43F),
                    new HexSkyMesh(this, 3, 1.26F, 0.155F, 4, Pal.darkestGray.cpy().lerp(NHColor.darkEnrColor, 0.105f).a(0.75F), 6, 0.42F, 1.32F, 0.4F));

            iconColor = NHColor.darkEnrColor;

            landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
        }};
    }

    public static class NHModMesh extends HexMesh{
        public static float waterOffset = 0.05f;

        public NHModMesh(Planet planet, int divisions, double octaves, double persistence, double scl, double pow, double mag, float colorScale, Color... colors){
            super(planet, new HexMesher(){
                @Override
                public float getHeight(Vec3 position){
                    position = Tmp.v33.set(position).scl(4f);
                    float height = (Mathf.pow(Simplex.noise3d(123, 7, 0.5f, 1f/3f, position.x, position.y, position.z), 2.3f) + waterOffset) / (1f + waterOffset);
                    return Math.max(height, 0.05f);
                }

                @Override
                public void getColor(Vec3 position, Color out){
                    double height = Math.pow(Simplex.noise3d(1, octaves, persistence, scl, position.x, position.y, position.z), pow) * mag;
                    out.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]).mul(colorScale);
                }

                public Color getColor(Vec3 position){
                    double height = Math.pow(Simplex.noise3d(1, octaves, persistence, scl, position.x, position.y, position.z), pow) * mag;
                    return Tmp.c1.set(colors[Mathf.clamp((int)(height * colors.length), 0, colors.length - 1)]).mul(colorScale);
                }

            }, divisions, Shaders.unlit);
        }
    }

    public static class NHPlanetGenerator extends BlankPlanetGenerator {
        public static final Seq<Point2> points = new Seq<>();
        public static final Seq<Tile> path = new Seq<>();

        public static final Rand pointRand = new Rand();

        public static final int size = 350;
        @Override
        public int getSectorSize(Sector sector) {
            return size;
        }

        @Override
        protected void generate() {
            int chunkX = Core.settings.getInt("midantha-chunk-x", 0);
            int chunkY = Core.settings.getInt("midantha-chunk-y", 0);
            int seed = Point2.pack(chunkX, chunkY);
            rand.setSeed(seed);
            points.clear();

            tiles.eachTile(tile -> tile.setFloor(NHBlocks.conglomerateRock.asFloor()));
            //generate basalt (darker stone floor)
            pass((x, y) -> {
                if (noise(x + chunkX * size, y + chunkY * size, 7, 0.8f, 130f, 1f) > 0.52f) {
                    floor = Blocks.basalt;
                }
            });

            distort(10f, 12f);

            ManhattanVoronoi voronoi = new ManhattanVoronoi(width, height);
            for (float px = -0.25f; px <= 1.25f; px += 0.25f ) {
                for (float py = -0.25f; py <= 1.25f; py += 0.25f ) {
                    int sx = Mathf.round((px + chunkX) * 4);
                    int sy = Mathf.round((py + chunkY) * 4);
                    pointRand.setSeed(Point2.pack(sx, sy));
                    float rx = px + pointRand.random(-0.125f, 0.125f);
                    float ry = py + pointRand.random(-0.125f, 0.125f);
                    voronoi.points.add(rx, ry);

                    int rpx = (int) (rx * width);
                    int rpy = (int) (ry * height);
                    points.add(new Point2(rpx, rpy));
                }
            }

            for (Point2 p : points){
                Point2 closestPoint = p;
                int closest = size;
                for (Point2 p2 : points){
                    if (p == p2) continue;
                    if (Mathf.dstm(p.x, p.y, p2.x, p2.y) < closest){
                        closestPoint = p2;
                        closest = (int) Mathf.dstm(p.x, p.y, p2.x, p2.y);
                    }
                }
                if (p == closestPoint) continue;
                path.clear();
                Bresenham2.line(p.x, p.y, closestPoint.x, closestPoint.y, (x, y) -> {
                    if (Structs.inBounds(x, y, width, height)){
                        path.add(new Tile(x, y));
                    }
                });
                continualDraw(path, EnvironmentBlock.armorWall, 9, (x, y) -> !(tiles.get(x, y).floor() == EnvironmentBlock.metalFloorPlain));
            }

            pass((x, y) -> {
                if (!voronoi.getPointInThreshold(x, y, 50)){
                    floor = EnvironmentBlock.metalFloorPlain;
                    block = EnvironmentBlock.armorWall;
                }
            });


            grow(EnvironmentBlock.armorWall, Blocks.stone);

            pass((x, y) -> {
                if (block == Blocks.stone){
                    block = EnvironmentBlock.armorWall;
                }
            });

            pass((x, y) -> {
                int x1 = x - x % 3 + 30;
                int y1 = y - y % 3 + 30;

                if((x1 % 70 == 0 || y1 % 70 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.85f || Mathf.chance(0.035)){
                        floor = Blocks.metalFloor2;
                    }
                }

                //if((x % 50 == 0 || y % 50 == 0) && !floor.asFloor().isLiquid){
                //    if(noise(x, y, 5, 0.7f, 75f, 3f) > 0.8125f || Mathf.chance(0.075)){
                //        floor = NHBlocks.quantumFieldDisturbing;
                //    }
                //}
            });

            Schematics.placeLaunchLoadout(16, 16);

        }
        public void continualDraw(Seq<Tile> path, Block block, int rad, DrawBoolf b){
            GridBits used = new GridBits(tiles.width, tiles.height);

            for(Tile t : path){
                for(int x = -rad; x <= rad; x++){
                    for(int y = -rad; y <= rad; y++){
                        int wx = t.x + x, wy = t.y + y;
                        if(!used.get(wx, wy) && Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad)){
                            used.set(wx, wy);
                            if(b.get(wx, wy)){
                                Tile other = tiles.getn(wx, wy);
                                if(block instanceof Floor)other.setFloor(block.asFloor());
                                else other.setBlock(block);
                            }
                        }
                    }
                }
            }
        }

        public void blend(Block floor, Block around, Block ignore, float radius){
            float r2 = radius * radius;
            int cap = Mathf.ceil(radius);
            int max = tiles.width * tiles.height;
            Floor dest = around.asFloor();

            for(int i = 0; i < max; i++){
                Tile tile = tiles.geti(i);
                if(tile.floor() == floor || tile.block() == floor){
                    for(int cx = -cap; cx <= cap; cx++){
                        for(int cy = -cap; cy <= cap; cy++){
                            if(cx*cx + cy*cy <= r2){
                                Tile other = tiles.get(tile.x + cx, tile.y + cy);

                                if(other != null && other.floor() != floor && other.floor() != ignore){
                                    other.setFloor(dest);
                                }
                            }
                        }
                    }
                }
            }
        }

        public void draw(int cx, int cy, Block block, int rad, DrawBoolf b){
            for(int x = -rad; x <= rad; x++){
                for(int y = -rad; y <= rad; y++){
                    int wx = cx + x, wy = cy + y;
                    if(Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad) && b.get(wx, wy)){
                        Tile other = tiles.getn(wx, wy);
                        if(block instanceof Floor)other.setFloor(block.asFloor());
                        else other.setBlock(block);
                    }
                }
            }
        }

        public void grow(Block wall, Block target){
            pass((x, y) -> {
                if (block == wall){
                    for (Point2 p: Geometry.d8){
                        if (Structs.inBounds(x + p.x, y + p.y, width, height)){
                            if (tiles.get(x + p.x, y + p.y).block() != wall){
                                tiles.get(x + p.x, y + p.y).setBlock(target);
                            }
                        }
                    }
                }
            });
        }
    }

    public interface DrawBoolf{
        boolean get(int x, int y);
    }
}
