package newhorizon.expand.map;

import arc.func.*;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Structs;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.game.Schematics;
import mindustry.gen.Iconc;
import mindustry.graphics.g3d.PlanetGrid;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Sector;
import mindustry.ui.dialogs.PlanetDialog;
import mindustry.world.*;
import mindustry.world.blocks.environment.Floor;
import newhorizon.content.NHBlocks;
import newhorizon.content.blocks.EnvironmentBlock;

public class MidanthaPlanetGenerator extends PlanetGenerator {
    public static Interp interp = new Interp.Exp(2, 3);
    public static IntSet altitudes = new IntSet();

    static {
        PlanetDialog.debugSelect = true;
    }
    
    Block 
            qd = NHBlocks.quantumFieldDeep, qn = NHBlocks.quantumField,
            cb = EnvironmentBlock.conglomerateSparse, dc = EnvironmentBlock.darkConglomerateSparse,
            th = EnvironmentBlock.thoriumStoneSparse, ze = EnvironmentBlock.zetaCrystalFloor;

    Block[][] terrains = {
            {qd, qd, qd ,qd, qd},
            {qd, qd, qn ,qn, qn},
            {qn, qn, qn ,qn, qn},

            {cb, cb, qn, qn, qn, qn, cb, qn, cb},
            {cb, cb, qn, cb, cb, dc, dc, qn, cb},
            {cb, cb, th, th, cb, cb, cb, dc, cb},
            {dc, dc, th, th, dc, dc, cb, dc, cb},
            {cb, cb, dc, cb, cb, dc, dc, dc, cb},
            {cb, dc, dc, dc, cb, cb, cb, dc, dc},
            {cb, dc, dc, dc, dc, cb, cb, dc, dc},
            {dc, dc, qn, dc, dc, cb, cb, dc, dc},
            {dc, qn, qd, qn, dc, dc, cb, cb, dc},
            {dc, qn, qd, qn, th, dc, cb, cb, cb},
            {dc, dc, qn, dc, th, dc, cb, cb, cb},
            {dc, dc, dc, th, dc, dc, dc, dc, cb},
            {cb, cb, cb, th, dc, dc, dc, cb, dc},
            {cb, dc, cb, cb, th, th, dc, cb, dc},
            {dc, dc, dc, dc, th, th, qn, cb, cb},
            {cb, cb, dc, dc, th, cb, qn, dc, cb},
            {cb, cb, cb, dc, cb, cb, cb, cb, cb},
            {cb, cb, qn, qn, cb, cb, qn, qn, cb},

            {qn, qn, qn ,qn, qn},
            {qd, qd, qn ,qn, qd},
            {qd, qd, qd ,qd, qd},

    };

    public Color ammonia = Color.valueOf("262762");
    public Color cryonite = Color.valueOf("c5d7f0");
    public Color conglomerate = Color.valueOf("303044");

    public Color zetaFloor = Color.valueOf("e2bcb3");
    public Color thoriumFloor = Color.valueOf("403649");
    public Color silicarColor = Color.valueOf("4a4b53");

    public static float waterOffset = 0.81f;

    public float seaLevel = 0.42f;
    public float iceSheetLevel = 0.50f;
    public float snowLevel = 0.535f;

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
        float base = Interp.reverse.apply((getLatitude(position) / 90)) * 0.025f;

        float land = Simplex.noise3d(seed + 1465, 4, 0.32f, 0.45f, position.x, position.y, position.z) * scl + base;
        if (land > seaLevel) {
            
            float mountain = Interp.exp5Out.apply(Ridged.noise3d(seed + 4142, position.x, position.y, position.z, 4, 2.21f)) * 0.75f;
            float height = Math.max(mountain, land) * scl + base;
            float river = Ridged.noise3d(seed + 525, position.x + 12, position.y + 42, position.z + 92, 6, 1.22f) * 0.3f;
            if (river > 0) height -= river / 2f;
            return Math.max(seaLevel, height);
        }
        return seaLevel;
    }

    @Override
    public float getSizeScl() {
        return 2200;
    }

    public int getDensity(Vec3 position) {
        return Mathf.clamp((int) (Simplex.noise3d(321, 12, 0.42f, 8.7f, position.x, position.y, position.z) * 4f - 1f), 0, 3);
    }

    public Block getFloor(Vec3 position) {
        int size = terrains.length;
        float scl = Mathf.clamp((getRawNoise(position) * size), 0, size - 1) / size;
        int tSize = terrains[Mathf.round(scl * size)].length;
        float tScl = Mathf.clamp((getTerrainNoise(position) * tSize), 0, tSize - 1) / tSize;
        return terrains[Mathf.round(scl * size)][Mathf.round(tScl * tSize)];
    }

    @Override
    public void genTile(Vec3 position, TileGen tile) {
        tile.floor = getFloor(position);
        tile.block = tile.floor.asFloor().wall;

        int density = getDensity(position);
        if (tile.floor == EnvironmentBlock.conglomerateSparse){
            if (density == 1) tile.floor = EnvironmentBlock.conglomerate;
            if (density == 2) tile.floor = EnvironmentBlock.conglomerateDense;
        }
        if (tile.floor == EnvironmentBlock.darkConglomerateSparse){
            if (density == 1) tile.floor = EnvironmentBlock.darkConglomerate;
            if (density == 2) tile.floor = EnvironmentBlock.darkConglomerateDense;
        }
        if (tile.floor == EnvironmentBlock.thoriumStoneSparse){
            if (density == 1) tile.floor = EnvironmentBlock.thoriumStone;
            if (density == 2) tile.floor = EnvironmentBlock.thoriumStoneDense;
        }

        if(Ridged.noise3d(seed + 124, position.x, position.y, position.z, 4, 12.92f) > -0.45) tile.block = Blocks.air;
    }

    public float getRawHeight(Vec3 position){
        return (float) Math.pow(Interp.reverse.apply(Mathf.clamp(Math.abs(getRawNoise(position) - 0.645f) * 1.2f)) * 0.895f, 1.2f) + 0.15f;
    }

    public float getRawNoise(Vec3 position){
        return Simplex.noise3d(321, 12, 0.42f, 1.7f, position.x, position.y, position.z) * 1.4f;
    }

    public float getTerrainNoise(Vec3 position){
        return Simplex.noise3d(192, 4, 0.85f, 2.8f, position.x, position.y, position.z) * 1.1f;
    }

    public float getColorNoise(Vec3 position){
        return 1 + (Simplex.noise3d(1, 6, 0.72f, 0.2f, position.x, position.y, position.z) * 0.3f - 0.15f);
    }

    public void generate(Tiles tiles, Sector sec, WorldParams params){
        this.tiles = tiles;
        this.seed = params.seedOffset + baseSeed;
        this.sector = sec;
        this.width = tiles.width;
        this.height = tiles.height;
        this.rand.setSeed(sec.id + params.seedOffset + baseSeed);

        TileGen gen = new TileGen();

        //Vec3 lb = sector.rect.project(0, 0);
        //Vec3 tr = sector.rect.project(1, 1);

        //Vec3 lightDir = lb.sub(tr).nor();
        //Vec3 planeNormal = sector.tile.v.cpy().nor();

        Vec3 pos = new Vec3();

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                gen.reset();
                pos.set(sector.rect.project(x / (float)tiles.width, y / (float)tiles.height));
                genTile(pos, gen);
                Tile tile = new Tile(x, y, gen.floor, gen.overlay, gen.block);
                tiles.set(x, y, tile);
            }
        }

        generate(tiles, params);
    }

    //@Override
    //public void getColor(Vec3 position, Color out) {
    //    out.set(ammonia);
    //    float landLevel = getLand(position);
    //    float iceSheet = getIceSheet(position);
    //    if (landLevel > seaLevel) {
    //        out.set(conglomerate);
    //        //if (landLevel > snowLevel) out.set(cryonite);
    //        //if (getZeta(position) > 0.6) out.set(zetaFloor);
    //        //if (getThorium(position) > 0.6) out.set(thoriumFloor);
    //        //if (getSilicar(position) > 0.6) out.set(silicarColor);
    //    }
    //    if (iceSheet > iceSheetLevel) {
    //        out.set(conglomerate);
    //        if (iceSheet > snowLevel) out.set(cryonite);
    //    }
    //}

    //public Block getScaledSea(){}

    @Override
    public float getHeight(Vec3 position){
        //6, 5, 0.3, 1.7, 1.2, 1.4, 1.1f
        //position = Tmp.v33.set(position).scl(4f);
        float height = getRawHeight(position);
        return Math.max(height, waterOffset) - 0.2f;
    }

    @Override
    public void getColor(Vec3 position, Color out){
        out.set(getFloor(position).mapColor).mul(getColorNoise(position));
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

    @Override
    protected void generate() {
        distort(6, 12);
        median(3);

        scatter(EnvironmentBlock.conglomerateDense, EnvironmentBlock.conglomerate, 0.35f);
        scatter(EnvironmentBlock.conglomerate, EnvironmentBlock.conglomerateSparse, 0.4f);
        scatter(EnvironmentBlock.darkConglomerateDense, EnvironmentBlock.darkConglomerate, 0.35f);
        scatter(EnvironmentBlock.darkConglomerate, EnvironmentBlock.darkConglomerateSparse, 0.4f);
        scatter(EnvironmentBlock.thoriumStoneDense, EnvironmentBlock.thoriumStone, 0.35f);
        scatter(EnvironmentBlock.thoriumStone, EnvironmentBlock.thoriumStoneSparse, 0.4f);

        distort(4, 4);

        rand.setSeed(seed + sector.id);
        int shift = rand.random(20, 80);
        each((x, y) -> {
            Tile t = tiles.get(x, y);

            if (!t.solid() && !tiles.get(x, y).floor().asFloor().isLiquid){
                boolean baseChance = Ridged.noise2d(baseSeed + sector.id, x, y, 3, 0.012f) > 0.158f;

                boolean chanceBlock = (noise(x, y, 5, 0.7f, 15f, 3f) > 1.55f || Mathf.chance(0.125)) && baseChance;
                boolean chanceFloor = (noise(x, y, 5, 0.7f, 15f, 3f) > 1.17f || Mathf.chance(0.175)) && baseChance;
                boolean chanceLiquid = noise(x, y, 5, 0.7f, 15f, 3f) > 1.52f && baseChance;

                if (isOnLine(x, y, shift, 5)){
                    if (chanceFloor) t.setFloor(Blocks.metalTiles11.asFloor());
                }
                if (isOnLine(x, y, shift, 4) || isOnLine(x, y, shift, 3)){
                    //if (chanceBlock) t.setBlock(Blocks.metalWall3);
                    if (chanceFloor) t.setFloor(Blocks.metalTiles9.asFloor());
                }
                if (isOnLine(x, y, shift, 2)){
                    t.setBlock(Blocks.air);
                    if (chanceFloor) t.setFloor(Blocks.metalTiles11.asFloor());
                }
                if (isOnLine(x, y, shift, 0) || isOnLine(x, y, shift, 1)){
                    t.setBlock(Blocks.air);
                    if (chanceLiquid) t.setFloor(NHBlocks.quantumField.asFloor());
                }
            }
        });

        distort(5, 2);

        Vec2 trns = Tmp.v1.trns(rand.random(360f), width / 2.6f);
        int coreX = (int) (-trns.x + width / 2f), coreY = (int) (-trns.y + height / 2f);
        Schematics.placeLaunchLoadout(coreX, coreY);

    }

    private void drawLine(int rad, Block block) {
        rand.setSeed(seed + sector.id);
        int shift = rand.random(20, 80);

        each((x, y) -> {
            Floor f = tiles.get(x, y).floor().asFloor();
            if ((Math.abs(x % 100 + 1 - shift) < 2 || Math.abs(y % 100 + 1 - shift) < 2)) {
                drawPoint(x, y, rad, block);
            }
        });
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

    public boolean isOnLine(int x, int y, int s, int o){
        int spacing = 102;
        int n1 = (spacing + s + o) % spacing;
        int n2 = (spacing + s - o) % spacing;
        return x % spacing == n1 || x % spacing == n2 || y % spacing == n1 || y % spacing == n2;
    }

    public void drawPoint(int cx, int cy, int rad, Block block){
        drawPoint(cx, cy, rad, tile -> {
            if (block == Blocks.air) tile.setBlock(Blocks.air);
            else if (block instanceof Floor) tile.setFloor((Floor) block);
            else tile.setBlock(block);
        });
    }

    public void drawPoint(int cx, int cy, int rad, Cons<Tile> cons) {
        drawPoint(cx, cy, rad, tile -> true, cons);
    }

    public void drawPoint(int cx, int cy, int rad, Boolf<Tile> bool, Cons<Tile> cons) {
        for (int x = -rad; x <= rad; x++) {
            for (int y = -rad; y <= rad; y++) {
                int wx = cx + x, wy = cy + y;
                if (Structs.inBounds(wx, wy, width, height)) {
                    Tile tile = tiles.get(wx, wy);
                    if (bool.get(tile)) {
                        cons.get(tiles.getn(wx, wy));
                    }
                }
            }
        }
    }
}
