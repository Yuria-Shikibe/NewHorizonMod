package newhorizon.content;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.FloatSeq;
import arc.struct.GridBits;
import arc.struct.Seq;
import arc.struct.ShortSeq;
import arc.util.*;
import arc.util.noise.Simplex;
import arc.util.pooling.Pool;
import mindustry.Vars;
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
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Env;
import newhorizon.content.blocks.EnvironmentBlock;
import newhorizon.util.feature.ManhattanVoronoi;

import static mindustry.Vars.content;

public class NHPlanets {
    protected static final ShortSeq ints1 = new ShortSeq(), ints2 = new ShortSeq();

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
            return 300 * 3;
        }

        @Override
        protected void generate() {
            /*
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


             */

            for (int x = 0; x < 6; x++) {
                for (int y = 0; y < 6; y++) {
                    Tiles ts = generateChunk(x - 6, y);
                    int finalX = x;
                    int finalY = y;
                    ts.each((tx, ty) -> {
                        tiles.get(finalX * 150 + tx, finalY * 150 + ty).setFloor(ts.get(tx, ty).floor());
                        if (tiles.get(finalX * 150 + tx, finalY * 150 + ty).block() == Blocks.air){
                            tiles.get(finalX * 150 + tx, finalY * 150 + ty).setBlock(ts.get(tx, ty).block());
                        }
                    });
                }
            }

            pass((x, y) -> {
                int x1 = x - x % 3 + 30;
                int y1 = y - y % 3 + 30;

                if((x1 % 75 == 0 || y1 % 75 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.90f || Mathf.chance(0.095)){
                        if (floor == Blocks.basalt || floor == NHBlocks.conglomerateRock){
                            floor = Blocks.metalFloor2;
                        }
                    }
                }

                if((x % 100 == 0 || y % 100 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x, y, 5, 0.7f, 75f, 3f) > 0.88f || Mathf.chance(0.085)){
                        if (floor == Blocks.basalt || floor == NHBlocks.conglomerateRock){
                            floor = NHBlocks.quantumFieldDisturbing;
                        }
                    }
                }

                if((x % 300 <= 8 || x % 300 >= 291 ||y % 300 <= 8 || y % 300 >= 291)){
                    tiles.get(x, y).setBlock(NHBlocks.metalWall);
                }
            });

            Schematics.placeLaunchLoadout(122, 122);

            pass((x, y) -> {
                if(!nearWall(x, y) && (floor == Blocks.basalt || floor == NHBlocks.conglomerateRock)){
                    if(noise(x + 150, y + x * 1.25f + 100, 6, 0.5f, 125f, 1.5f) > 1.1f) ore = Blocks.oreTitanium;
                    if(noise(x + 999, y + 600 - x, 6, 0.5f, 95f, 1.5f) > 1.09f) ore = Blocks.oreTungsten;
                }
            });

            pass((x, y) -> {
                if(floor == Blocks.darkPanel3 && rand.chance(0.8f)){
                    ore = NHBlocks.oreZeta;
                }
            });

            pass((x, y) -> {
                if(floor == Blocks.metalFloor3){
                    floor = NHBlocks.metalVent;
                }
            });
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

    public static final Seq<Point2> points = new Seq<>();
    public static final Seq<Tile> path = new Seq<>();
    public static final Rand pointRand = new Rand();
    public static final int chunkSize = 150;

    //lb, rb. rt, lt
    public static final Point2 tmp0 = new Point2(), tmp1 = new Point2(), tmp2 = new Point2(), tmp3 = new Point2();
    public static Tiles generateChunk(int chunkX, int chunkY){
        Tiles tiles = new Tiles(chunkSize, chunkSize);
        tiles.fill();

        ManhattanVoronoi voronoi = new ManhattanVoronoi(chunkSize, chunkSize);
        points.clear();

        //6*6 grid, center 2x2 as actual map, others for voronoi use\
        float shift = 0.4f;
        for (int px = -2; px <= 3; px++) {
            for (int py = -2; py <= 3; py++) {
                int sx = px + chunkX;
                int sy = py + chunkY;
                pointRand.setSeed(Point2.pack(sx, sy));
                float rx = px + pointRand.random(-shift, shift);
                float ry = py + pointRand.random(-shift, shift);
                voronoi.points.add(rx, ry);

                int rpx = (int) (rx * chunkSize);
                int rpy = (int) (ry * chunkSize);
                points.add(new Point2(rpx, rpy));
            }
        }

        //base terrain
        tiles.eachTile(tile -> tile.setFloor(NHBlocks.conglomerateRock.asFloor()));
        //darker terrain
        tiles.eachTile(tile -> {
            if (noise(tile.x + chunkX * chunkSize, tile.y + chunkY * chunkSize, 7, 0.8f, 130f, 1f) > 0.52f) {
                tile.setFloor(Blocks.basalt.asFloor());
            }
        });
        //deep liquid
        tiles.eachTile(tile -> {
            if (voronoi.getPointInThreshold(tile.x, tile.y, 45f)) {
                if(noise(tile.x + 342 + chunkX * chunkSize, tile.y + 541 + chunkY * chunkSize, 4, 0.62f, 120f, 1.6f) > 0.96f){
                    tile.setFloor(NHBlocks.quantumFieldDeep.asFloor());
                }
            }
        });

        distort(tiles, 3, 2f);

        //liquid margin
        blend(tiles, NHBlocks.quantumFieldDeep, NHBlocks.quantumField, 3);

        //use percent for distance, not actual manhattan but this do look cool
        tiles.each((x, y) -> {
            if (!voronoi.getPointInPercentThreshold(x, y, 0.3f)){
                tiles.get(x, y).setBlock(NHBlocks.metalWall);
            }
        });

        //randomize
        distort(tiles, 3f, 2f);

        //check for paths, remove
        tiles.each((x, y) -> {
            if (voronoi.getPointInThreshold(x, y, 72f)){
                tiles.get(x, y).setAir();
            }
        });

        //margin
        tiles.each((x, y) -> {
            if (!voronoi.getPointInThreshold(x, y, 70f)){
                tiles.get(x, y).setFloor(Blocks.metalFloor.asFloor());
            }
        });

        //some connections
        getPath(tiles);
        continualDraw(tiles, path, Blocks.metalFloor, 13, (x, y) -> noise(x + 321, y + 151, 5, 0.7f, 75f, 3f) > 1.23f || Mathf.chance(0.025));
        continualDraw(tiles, path, NHBlocks.metalWall, 9, (x, y) -> noise(x + 321, y + 151, 5, 0.7f, 75f, 3f) > 1.23f || Mathf.chance(0.025));
        continualDraw(tiles, path, EnvironmentBlock.metalFloorRidge, 7, (x, y) -> noise(x + 321, y + 151, 5, 0.7f, 75f, 3f) > 1.23f || Mathf.chance(0.025));
        continualDraw(tiles, path, EnvironmentBlock.metalFloorGroove, 5, (x, y) -> noise(x + 321, y + 151, 5, 0.7f, 75f, 3f) > 1.23f || Mathf.chance(0.025));
        continualDraw(tiles, path, EnvironmentBlock.metalFloorRidge, 2, (x, y) -> noise(x + 321, y + 151, 5, 0.7f, 75f, 3f) > 1.23f || Mathf.chance(0.025));
        path.each(t -> erase(tiles, t.x, t.y, 6));

        //inner cleanup
        tiles.each((x, y) -> {
            if (!voronoi.getPointInThreshold(x, y, 85f)){
                tiles.get(x, y).setAir();
                tiles.get(x, y).setFloor(EnvironmentBlock.metalFloorPlain.asFloor());
            }
        });

        //vents, zeta ores
        tiles.each((x, y) -> {
            if (!voronoi.getPointInThreshold(x, y, 85f)){
                if (x % 15 == 0 && y % 15 == 0){
                    if (voronoi.getPointInThreshold(x + 15, y, 85f)) return;
                    if (voronoi.getPointInThreshold(x + 15, y + 15, 85f)) return;
                    if (voronoi.getPointInThreshold(x, y + 15, 85f)) return;
                    if (noise(x - 1234, y - 938, 6, 0.58f, 15f, 1.5f) > 0.75f){
                        pointRand.setSeed(Point2.pack(x + chunkX * chunkSize, y + chunkX * chunkSize));
                        boolean ore = pointRand.chance(0.5f);
                        boolean o1 = pointRand.chance(0.75f), o2 = pointRand.chance(0.75f), o3 = pointRand.chance(0.75f), o4 = pointRand.chance(0.75f);
                        boolean f1 = pointRand.chance(0.5f), f2 = pointRand.chance(0.5f), f3 = pointRand.chance(0.5f), f4 = pointRand.chance(0.5f);

                        if (ore){
                            for (int tx = 0; tx < 6; tx++){
                                for (int ty = 0; ty < 6; ty++){
                                    int rx = x + tx, ry = y + ty;
                                    setFloor(tiles, rx + 1, ry + 1, EnvironmentBlock.metalFloorGrooveDeep);
                                    setFloor(tiles, rx + 9, ry + 1, EnvironmentBlock.metalFloorGrooveDeep);
                                    setFloor(tiles, rx + 1, ry + 9, EnvironmentBlock.metalFloorGrooveDeep);
                                    setFloor(tiles, rx + 9, ry + 9, EnvironmentBlock.metalFloorGrooveDeep);
                                }
                            }

                            for (int tx = 0; tx < 4; tx++){
                                for (int ty = 0; ty < 4; ty++){
                                    int rx = x + tx, ry = y + ty;
                                    if (o1) setFloor(tiles, rx + 2, ry + 2, Blocks.darkPanel3);
                                    if (o2) setFloor(tiles, rx + 2, ry + 10, Blocks.darkPanel3);
                                    if (o3) setFloor(tiles, rx + 10, ry + 2, Blocks.darkPanel3);
                                    if (o4) setFloor(tiles, rx + 10, ry + 10, Blocks.darkPanel3);
                                }
                            }
                        }else {
                            for (int tx = 0; tx < 5; tx++){
                                for (int ty = 0; ty < 5; ty++){
                                    int rx = x + tx, ry = y + ty;
                                    setFloor(tiles, rx + 2, ry + 2, EnvironmentBlock.metalFloorRidgeHigh);
                                    setFloor(tiles, rx + 9, ry + 2, EnvironmentBlock.metalFloorRidgeHigh);
                                    setFloor(tiles, rx + 2, ry + 9, EnvironmentBlock.metalFloorRidgeHigh);
                                    setFloor(tiles, rx + 9, ry + 9, EnvironmentBlock.metalFloorRidgeHigh);
                                }
                            }

                            for (int tx = 0; tx < 3; tx++){
                                for (int ty = 0; ty < 3; ty++){
                                    int rx = x + tx, ry = y + ty;
                                    if (f1) setFloor(tiles, rx + 3, ry + 3, Blocks.metalFloor3);
                                    if (f2) setFloor(tiles, rx + 3, ry + 10, Blocks.metalFloor3);
                                    if (f3) setFloor(tiles, rx + 10, ry + 3, Blocks.metalFloor3);
                                    if (f4) setFloor(tiles, rx + 10, ry + 10, Blocks.metalFloor3);
                                }
                            }
                        }
                    }
                }
            }
        });

        tiles.each((x, y) -> {
            if (tiles.get(x, y).floor() == EnvironmentBlock.metalFloorPlain){
                if (x % 15 == 0 || y % 15 == 0) tiles.get(x, y).setFloor(EnvironmentBlock.metalFloorGroove);
            }

            if (tiles.get(x, y).floor() == Blocks.metalFloor){
                if ((x + y) % 50 <= 3 || (x + y) % 50 >= 47) tiles.get(x, y).setAir();
                if (Math.abs(x - y) % 50 <= 3) tiles.get(x, y).setAir();
            }
        });

        return tiles;
    }

    public static void setFloor(Tiles tiles, int x, int y, Block block){
        if (Structs.inBounds(x, y, tiles.width, tiles.height)){
            tiles.get(x, y).setFloor(block.asFloor());
        }
    }

    public static void setOverlay(Tiles tiles, int x, int y, Block block){
        if (Structs.inBounds(x, y, tiles.width, tiles.height)){
            tiles.get(x, y).setOverlay(block);
        }
    }

    public static void erase(Tiles tiles, int cx, int cy, int rad){
        for(int x = -rad; x <= rad; x++){
            for(int y = -rad; y <= rad; y++){
                int wx = cx + x, wy = cy + y;
                if(Structs.inBounds(wx, wy, tiles.width, tiles.height) && Mathf.within(x, y, rad)){
                    Tile other = tiles.getn(wx, wy);
                    other.setBlock(Blocks.air);
                }
            }
        }
    }

    protected static float noise(float x, float y, double octaves, double falloff, double scl, double mag){
        return Simplex.noise2d(0, octaves, falloff, 1f / scl, x, y) * (float)mag;
    }

    protected static float noise(float x, float y, double scl, double mag){
        return noise(x, y, 1, 1, scl, mag);
    }

    public static void getPath(Tiles tiles){
        //some connections
        path.clear();
        for (Point2 p : points){
            Point2 closestPoint = p;
            int closest = chunkSize * 2;
            for (Point2 p2 : points){
                if (p == p2) continue;
                if (Mathf.dstm(p.x, p.y, p2.x, p2.y) < closest){
                    closestPoint = p2;
                    closest = (int) Mathf.dstm(p.x, p.y, p2.x, p2.y);
                }
            }
            if (p == closestPoint) continue;
            if ((p.x - closestPoint.x) > (p.y - closestPoint.y)){
                Bresenham2.line(p.x, p.y, p.x, closestPoint.y, (x, y) -> {
                    if (Structs.inBounds(x, y, chunkSize, chunkSize)){
                        path.add(tiles.get(x, y));
                    }
                });
                Bresenham2.line(closestPoint.x, closestPoint.y, p.x, closestPoint.y, (x, y) -> {
                    if (Structs.inBounds(x, y, chunkSize, chunkSize)){
                        path.add(tiles.get(x, y));
                    }
                });
            }else {
                Bresenham2.line(p.x, p.y, closestPoint.x, p.y, (x, y) -> {
                    if (Structs.inBounds(x, y, chunkSize, chunkSize)){
                        path.add(tiles.get(x, y));
                    }
                });
                Bresenham2.line(closestPoint.x, closestPoint.y, closestPoint.x, p.y, (x, y) -> {
                    if (Structs.inBounds(x, y, chunkSize, chunkSize)){
                        path.add(tiles.get(x, y));
                    }
                });
            }
        }
    }

    public static void distort(Tiles tiles, float scl, float mag){
        short[] blocks = new short[tiles.width * tiles.height];
        short[] floors = new short[blocks.length];

        tiles.each((x, y) -> {
            int idx = y*tiles.width + x;
            float cx = x + noise(x - 155f, y - 200f, scl, mag) - mag / 2f, cy = y + noise(x + 155f, y + 155f, scl, mag) - mag / 2f;
            Tile other = tiles.getn(Mathf.clamp((int)cx, 0, tiles.width-1), Mathf.clamp((int)cy, 0, tiles.height-1));
            blocks[idx] = other.block().id;
            floors[idx] = other.floor().id;
        });

        for(int i = 0; i < blocks.length; i++){
            Tile tile = tiles.geti(i);
            tile.setFloor(Vars.content.block(floors[i]).asFloor());
            tile.setBlock(Vars.content.block(blocks[i]));
        }
    }

    public static void median(Tiles tiles, int radius, double percentile, @Nullable Block targetFloor){
        short[] blocks = new short[tiles.width * tiles.height];
        short[] floors = new short[blocks.length];

        tiles.each((x, y) -> {
            if(targetFloor != null && tiles.getn(x, y).floor() != targetFloor) return;

            ints1.clear();
            ints2.clear();
            Geometry.circle(x, y, tiles.width, tiles.height, radius, (cx, cy) -> {
                ints1.add(tiles.getn(cx, cy).floorID());
                ints2.add(tiles.getn(cx, cy).blockID());
            });
            ints1.sort();
            ints2.sort();

            floors[x + y * tiles.width] = ints1.get(Mathf.clamp((int)(ints1.size * percentile), 0, ints1.size - 1));
            blocks[x + y * tiles.width] = ints2.get(Mathf.clamp((int)(ints2.size * percentile), 0, ints2.size - 1));
        });

        tiles.eachTile(tile -> {
            if(targetFloor != null && tile.floor() != targetFloor) return;

            tile.setBlock(content.block(blocks[tile.x + tile.y * tiles.width]));
            tile.setFloor(content.block(floors[tile.x + tile.y * tiles.width]).asFloor());
        });
    }

    public static void blend(Tiles tiles, Block floor, Block around, float radius){
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

                            if(other != null && other.floor() != floor){
                                other.setFloor(dest);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void continualDraw(Tiles tiles, Seq<Tile> path, Block block, int rad, DrawBoolf b){
        GridBits used = new GridBits(tiles.width, tiles.height);

        for(Tile t : path){
            for(int x = -rad; x <= rad; x++){
                for(int y = -rad; y <= rad; y++){
                    int wx = t.x + x, wy = t.y + y;
                    if(!used.get(wx, wy) && Structs.inBounds(wx, wy, tiles.width, tiles.height) && Mathf.within(x, y, rad)){
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

}
