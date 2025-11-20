package newhorizon.expand.map;

import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.gen.Iconc;
import mindustry.graphics.g3d.PlanetGrid;
import mindustry.maps.generators.BlankPlanetGenerator;
import mindustry.type.Sector;

public class MidanthaPlanetGenerator extends BlankPlanetGenerator {
    public static final PlanetGrid grid = PlanetGrid.create(0);
    public static final Vec3[] corners = new Vec3[grid.tiles.length];
    public static final int[][] edges = new int[grid.edges.length][2];

    static {
        for (PlanetGrid.Ptile tile : grid.tiles) {
            corners[tile.id] = tile.v;
        }
        for (PlanetGrid.Edge edge : grid.edges) {
            edges[edge.id][0] = edge.tiles[0].id;
            edges[edge.id][1] = edge.tiles[1].id;
        }
    }

    public Color ammonia = Color.valueOf("1e2538");
    public Color cryonite = Color.valueOf("c5d7f0");
    public Color conglomerate = Color.valueOf("303044");

    public Color zetaFloor = Color.valueOf("e2bcb3");

    public float seaLevel = 0.42f;
    public float snowLevel = 0.48f;

    public MidanthaPlanetGenerator() {

    }

    @SuppressWarnings("all")
    //-90 ~ 90 deg
    public float getLatitude(Vec3 position) {
        return Math.abs(Mathf.atan2(position.y, Mathf.sqrt(Mathf.sqr(position.x) + Mathf.sqr(position.z))) * Mathf.radDeg - 90);
        //float yaw = Mathf.atan2(position.x, position.z) * Mathf.radDeg;
    }

    public float getIceSheet(Vec3 position) {
        float scl = Interp.exp10.apply(Mathf.clamp(getLatitude(position) / 90));
        return Math.max(seaLevel, Simplex.noise3d(seed + 4923, 7, 0.6f, 0.42f, position.x, position.y, position.z) * scl / 1.1f);
    }

    public float getLand(Vec3 position) {
        float scl = Interp.reverse.apply(Interp.exp5.apply(Mathf.clamp(getLatitude(position) / 90)));
        float base = Interp.reverse.apply((getLatitude(position) / 90)) * 0.045f;
        float height = Simplex.noise3d(seed + 1465, 8, 0.7f, 0.45f, position.x, position.y, position.z) * scl + base;
        float river = getRiver(position);
        if (river > -0.2f) height -= river / 2.3f;

        return Math.max(seaLevel, height);
    }

    public float getRiver(Vec3 position) {
        return Ridged.noise3d(seed + 525, position.x + 12, position.y + 42, position.z + 92, 3, 1.92f);
    }

    public float getZeta(Vec3 position) {
        return Ridged.noise3d(seed + 9125, position.x + 12, position.y + 42, position.z + 92, 4, 3.92f);
    }

    public float getThorium(Vec3 position) {
        return Ridged.noise3d(seed + 3525, position.x + 12, position.y + 42, position.z + 92, 4, 3.92f);
    }



    @Override
    public void getColor(Vec3 position, Color out) {
        out.set(ammonia);
        float landLevel = getLand(position);
        float iceSheet = getIceSheet(position);
        if (landLevel > seaLevel) {
            float lerp = Mathf.curve(landLevel, seaLevel, seaLevel + 0.025f);
            out.lerp(conglomerate, lerp);
            if (landLevel > snowLevel) {
                out.lerp(cryonite, Mathf.curve(landLevel, snowLevel, snowLevel + 0.025f) * lerp);
            }
            if (getZeta(position) > 0.5) {
                out.lerp(zetaFloor, Mathf.curve(getZeta(position), 0.5f, 0.5f + 0.1f) * lerp);
            }
        }
        if (iceSheet > seaLevel) {
            float lerp = Mathf.curve(iceSheet, seaLevel, seaLevel + 0.025f);
            out.lerp(conglomerate, lerp);
            if (iceSheet > snowLevel) {
                out.lerp(cryonite, Mathf.curve(iceSheet, snowLevel, snowLevel + 0.025f) * lerp);
            }
        }
    }

    @Override
    public float getHeight(Vec3 position) {
        return Math.max(seaLevel, Math.max(getIceSheet(position), getLand(position)));
    }

    @Override
    public void getEmissiveColor(Vec3 position, Color out) {
        getColor(position, out);
        out.a(0.25f);
    }

    @Override
    public boolean isEmissive() {
        return true;
    }

    @Override
    public void getLockedText(Sector hovered, StringBuilder out) {
        if (!isLandSector(hovered)) {
            out.append("[gray]").append(Iconc.cancel).append(" ").append("LANDLESS");
        }else {
            super.getLockedText(hovered, out);
        }
    }

    @Override
    public boolean allowAcceleratorLanding(Sector sector) {
        return super.allowAcceleratorLanding(sector) && isLandSector(sector);
    }

    @Override
    public boolean allowLanding(Sector sector) {
        return super.allowLanding(sector) && isLandSector(sector);
    }

    public boolean isLandSector(Sector sector) {
        if (sector == null) return true;
        int land = 0;
        if (getHeight(sector.tile.v) > seaLevel) land++;
        for (PlanetGrid.Corner corner: sector.tile.corners){
            if (getHeight(corner.v) > seaLevel) land += 5;
        }
        return land > 5;
    }

    /*
    public static Vec3 tmp = new Vec3();
    public static final Seq<Point2> points = new Seq<>();
    public static final Seq<Tile> path = new Seq<>();

    public static final int chunkSize = 150;
    public static final int size = 3;

    @Override
    public int getSectorSize(Sector sector) {
        return chunkSize * size;
    }


    @Override
    protected void generate() {
        int startX = Mathf.random(-20, 20);
        int startY = Mathf.random(-20, 20);

        pass((x, y) -> {
            if (x < 5f || y < 5f || x > chunkSize * size - 6f || y > chunkSize * size - 6f) {
                block = EnvironmentBlock.armorWall;
            }
        });

        distort(12, 6);
        median(5);


        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Tiles ts = NHPlanets.generateChunk(x + startX, y + startY);
                int finalX = x;
                int finalY = y;
                ts.each((tx, ty) -> {
                    tiles.get(finalX * 150 + tx, finalY * 150 + ty).setFloor(ts.get(tx, ty).floor());
                    if (tiles.get(finalX * 150 + tx, finalY * 150 + ty).block() == Blocks.air) {
                        tiles.get(finalX * 150 + tx, finalY * 150 + ty).setBlock(ts.get(tx, ty).block());
                    }
                });
            }
        }

        rand.setSeed(Point2.pack(startX, startY));
        Vec2 trns = Tmp.v1.trns(rand.random(360f), width / 2.6f);
        int spawnX = (int) (trns.x + width / 2f), spawnY = (int) (trns.y + height / 2f),
                coreX = (int) (-trns.x + width / 2f), coreY = (int) (-trns.y + height / 2f);

        erase(spawnX, spawnY, 12);
        erase(coreX, coreY, 12);

        path.clear();
        path.add(pathfind(spawnX, spawnY, coreX, coreY, tile -> (tile.solid() ? 50f : 0f), Astar.manhattan));

        tiles.eachTile(tile -> {
            if (tile.floor() == Blocks.carbonStone) {
                float noise = noise(tile.x + 150, tile.y + 100 + tile.x / 0.8f, 4, 0.5f, 65f, 1.5f);
                if (noise > 0.9f) tile.setOverlay(Blocks.oreTitanium);
                if (noise < 0.5f) tile.setOverlay(Blocks.oreTungsten);
            }
        });

        NHPlanets.removeOreNear(tiles, NHBlocks.quantumField, 4);
        NHPlanets.removeOreNear(tiles, NHBlocks.quantumFieldDeep, 4);
        NHPlanets.removeOreNear(tiles, NHBlocks.conglomerateRock, 5);
        NHPlanets.removeOreNear(tiles, Blocks.metalFloor, 4);
        NHPlanets.removeOreNear(tiles, Blocks.basalt, 5);

        pass((x, y) -> {
            if (floor == Blocks.shale || floor == Blocks.carbonStone) return;
            int x1 = x - x % 3 + 30;
            int y1 = y - y % 3 + 30;

            if ((x1 % 75 == 0 || y1 % 75 == 0) && !floor.asFloor().isLiquid) {
                if (noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.90f || Mathf.chance(0.095)) {
                    if (floor == Blocks.basalt || floor == NHBlocks.conglomerateRock) {
                        floor = Blocks.metalFloor2;
                    }
                }
            }

            if ((x % 100 == 0 || y % 100 == 0) && !floor.asFloor().isLiquid) {
                if (noise(x, y, 5, 0.7f, 75f, 3f) > 0.88f || Mathf.chance(0.085)) {
                    if (floor == Blocks.basalt || floor == NHBlocks.conglomerateRock) {
                        floor = NHBlocks.quantumFieldDisturbing;
                    }
                }
            }

            if ((x % 300 <= 8 || x % 300 >= 291 || y % 300 <= 8 || y % 300 >= 291)) {
                tiles.get(x, y).setBlock(NHBlocks.metalWall);
            }
        });

        pass((x, y) -> {
            if (floor == Blocks.darkPanel3 && rand.chance(0.8f)) {
                ore = EnvironmentBlock.oreZeta;
            }
        });

        pass((x, y) -> {
            if (floor == Blocks.darkPanel1 && rand.chance(0.8f)) {
                ore = EnvironmentBlock.oreSilicon;
            }
        });

        pass((x, y) -> {
            if (floor == Blocks.metalFloor3) {
                floor = NHBlocks.metalVent;
            }
        });

        erase(coreX, coreY, 15);
        erase(spawnX, spawnY, 15);
        tiles.getn(spawnX, spawnY).setOverlay(Blocks.spawn);
        Schematics.placeLaunchLoadout(coreX, coreY);
    }

     */

    /*
    public void draw(int cx, int cy, Block block, int rad, NHPlanets.DrawBoolf b) {
        for (int x = -rad; x <= rad; x++) {
            for (int y = -rad; y <= rad; y++) {
                int wx = cx + x, wy = cy + y;
                if (Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad) && b.get(wx, wy)) {
                    Tile other = tiles.getn(wx, wy);
                    if (block instanceof Floor) other.setFloor(block.asFloor());
                    else other.setBlock(block);
                }
            }
        }
    }

    public void grow(Block wall, Block target) {
        pass((x, y) -> {
            if (block == wall) {
                for (Point2 p : Geometry.d8) {
                    if (Structs.inBounds(x + p.x, y + p.y, width, height)) {
                        if (tiles.get(x + p.x, y + p.y).block() != wall) {
                            tiles.get(x + p.x, y + p.y).setBlock(target);
                        }
                    }
                }
            }
        });
    }

     */
}
