package newhorizon.expand.map.planet;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.GridBits;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.content.Loadouts;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.blocks.environment.TallBlock;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.content.*;
import newhorizon.content.blocks.EnvironmentBlock;
import newhorizon.expand.map.SchematicUtil;

import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static newhorizon.content.NHPlanets.ceito;

public class MidanthaPlanet extends Planet {
    public static final int MidanthaEnv = 1 << 8;
    public MidanthaPlanet() {
        super("midantha-test", ceito, 1, 3);
        bloom = true;
        visible = true;
        accessible = true;
        hasAtmosphere = true;
        alwaysUnlocked = true;
        iconColor = NHColor.darkEnrColor;
        meshLoader = () -> new NHPlanets.NHModMesh(
            this, 5,
            5, 0.3, 1.7, 1.2, 1.4,
            1.1f,
            NHColor.darkEnrFront.cpy().lerp(Color.white, 0.2f),
            NHColor.darkEnrFront,
            NHColor.darkEnrColor,
            NHColor.darkEnrColor.cpy().lerp(Color.black, 0.2f).mul(1.05f),
            Pal.darkestGray.cpy().mul(0.95f),
            Pal.darkestGray.cpy().lerp(Color.white, 0.105f),
            Pal.darkestGray.cpy().lerp(Pal.gray, 0.2f),
            Pal.darkestGray,
            Pal.stoneGray.cpy().lerp(Pal.darkestGray, 0.5f),
            Pal.stoneGray.cpy().lerp(Pal.darkestGray, 0.6f)
        );

        clearSectorOnLose = true;
        allowWaveSimulation = true;
        allowLaunchSchematics = false;
        allowLaunchLoadout = false;

        ruleSetter = r -> {
            r.waveTeam = Team.malis;
            r.placeRangeCheck = false;
            r.showSpawns = true;
            r.waveSpacing = 80 * Time.toSeconds;
            r.initialWaveSpacing = 8f * Time.toMinutes;
            if(r.sector != null && r.sector.preset == null)r.winWave = 150;
            r.bannedUnits.add(NHUnitTypes.guardian);
            r.coreDestroyClear = true;
            r.hideBannedBlocks = true;
            r.dropZoneRadius = 64;

            r.bannedBlocks.addAll(Vars.content.blocks().copy().select(b -> {
                if(b instanceof SolidPump pump){
                    return pump.result == Liquids.water && pump.attribute == Attribute.water;
                }else return false;
            }));

            r.waves = true;
            r.showSpawns = true;
            r.onlyDepositCore = false;
            r.fog = false;

            Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
            teamRule.rtsAi = false;
            teamRule.unitBuildSpeedMultiplier = 5f;
            teamRule.blockDamageMultiplier = 1.25f;
            teamRule.buildSpeedMultiplier = 3f;
            teamRule.blockHealthMultiplier = 1.25f;
            teamRule = r.teams.get(r.waveTeam);
            teamRule.infiniteAmmo = teamRule.infiniteResources = true;
        };

        generator = new MidanthaPlanetGenerator();

        cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 2, 0.15f, 0.14f, 5, Pal.darkerMetal.cpy().lerp(NHColor.darkEnrColor, 0.35f).a(0.55f), 2, 0.42f, 1.0f, 0.43f),
            new HexSkyMesh(this, 3, 1.26f, 0.155f, 4, Pal.darkestGray.cpy().lerp(NHColor.darkEnrColor, 0.105f).a(0.75f), 6, 0.42f, 1.32f, 0.4f));

        defaultEnv = Env.terrestrial | Env.groundWater | Env.oxygen | MidanthaEnv;

        icon = "planet";
        iconColor = NHColor.darkEnrColor;

        landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
        atmosphereRadIn = 0.02f;
        atmosphereRadOut = 0.3f;
        startSector = 15;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class MidanthaPlanetGenerator extends PlanetGenerator {
        private final Block[] terrain = {NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGroundQuantum, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.metalGround, NHBlocks.quantumFieldDeep, NHBlocks.metalGround, NHBlocks.conglomerateRock, NHBlocks.quantumFieldDeep};
        private final Seq<Rect> tmpRects = new Seq<>();
        private final IntSeq chunks = new IntSeq();

        private int coreX, coreY, spawnX, spawnY;
        private float maxDst;

        private static final int
                DISABLED = -1,
                NO_GENERATION = 0,
                PREPARE_TO_GENERATE = 1,
                REGULAR_GENERATION = 2,
                INDUSTRY_GENERATION = 3,
                GENERATED = 4;

        @Override
        public boolean allowLanding(Sector sector){
            //i guess this can be replaced with some much more interesting rules
            return (sector.hasBase() || sector.near().contains(s -> s.hasBase() && s.isCaptured()));
        }

        @Override
        //no bases right now
        public void generateSector(Sector sector){}

        @Override
        public float getHeight(Vec3 position){
            return Mathf.pow(rawHeight(position), 3f) * 1.6f;
        }

        @Override
        public Color getColor(Vec3 position){
            return Pal.gray;
        }

        @Override
        public float getSizeScl(){
            return 2000 * 1.07f * 6f / 3.5f;
        }

        float rawHeight(Vec3 position){
            return Simplex.noise3d(seed, 8, 0.7f, 1f/ 0.9f, 10f + position.x, 10f + position.y, 10f + position.z);
        }

        Block getBlock(Vec3 position){
            float height = rawHeight(position);

            height *= 1.2f;
            height = Mathf.clamp(height);

            return terrain[Mathf.clamp((int)(height * terrain.length), 0, terrain.length - 1)];
        }

        @Override
        public void genTile(Vec3 position, TileGen tile){
            tile.floor = getBlock(position);
            tile.block = Blocks.air;
            if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 14) > 0.5f){
                tile.floor = Blocks.basalt;
            }
            if(Ridged.noise3d(seed + 2, position.x, position.y, position.z, 2, 14) > 0.4f){
                tile.floor = Blocks.basalt;
            }
            if(Ridged.noise3d(seed + 3, position.x, position.y, position.z, 2, 14) > 0.6f){
                tile.floor = Blocks.basalt;
            }
        }

        @Override
        protected void generate(){
            rand.setSeed(seed);

            distort(10f, 12f);
            distort(5f, 7f);

            float difficulty = sector == null ? rand.random(0.4f, 1f) : sector.threat;

            cells(4);

            float length = width/2.6f;
            Vec2 trns = Tmp.v1.trns(rand.random(360f), length);

            coreX = (int)(trns.x + width/2f);
            coreY = (int)(trns.y + height/2f);
            spawnX = (int)(-trns.x + width/2f);
            spawnY = (int)(-trns.y + height/2f);

            maxDst = Mathf.dst(width, height);

            erase(coreX, coreY, 22);
            erase(spawnX, spawnY, 15);

            //median(12, 0.6, NHBlocks.quantumField);

            blend(NHBlocks.quantumFieldDeep, NHBlocks.quantumField, 6);

            pass((x, y) -> {
                if(floor.asFloor().isDeep()){
                    float noise = noise(x + 342, y + 541, 7, 0.8f, 120f, 1.5f);
                    if(noise > 0.82f){
                        floor = NHBlocks.quantumField;
                    }
                }
            });

            inverseFloodFill(tiles.getn(coreX, coreY));

            erase(spawnX, spawnY, 6);

            //remove props near ores, they're too annoying
            pass((x, y) -> {
                if(ore.asFloor().wallOre || block.itemDrop != null || (block == Blocks.air && ore != Blocks.air)){
                    removeWall(x, y, 3, b -> b instanceof TallBlock);
                }
            });

            blend(NHBlocks.quantumFieldDisturbing, EnvironmentBlock.metalFloorGroove, 1);

            Seq<Tile> path = pathfind(coreX, coreY, spawnX, spawnY, tile -> (tile.solid() ? 50f : 0f), Astar.manhattan);

            Geometry.circle(spawnX, spawnY, 12, ((x, y) -> {
                Tile tile = tiles.get(x, y);
                if(tile != null && tile.floor().isLiquid){
                    tile.setFloor(NHBlocks.quantumField.asFloor());
                }
            }));

            continualDraw(path, NHBlocks.quantumField, 4, ((x0, y0) -> {
                Floor f = tiles.getn(x0, y0).floor();
                boolean b = f.isDeep();
                if(b && noise(x0, y0 * x0, 6, 0.7f, 25f, 3f) > 0.4125f){
                    rand.setSeed((long)x0 + y0 << 8);
                    if(rand.chance(0.22f))draw(x0, y0, NHBlocks.quantumField, 4, ((x1, y1) -> {
                        Floor f1 = tiles.getn(x1, y1).floor();
                        if(f1 == NHBlocks.quantumFieldDisturbing){
                            tiles.getn(x1, y1).setFloor(NHBlocks.metalGround.asFloor());
                            return false;
                        }
                        return f1.isDeep();
                    }));
                }

                else if(f == NHBlocks.quantumFieldDisturbing){
                    draw(x0, y0, NHBlocks.metalGround, 4, ((x1, y1) -> tiles.getn(x1, y1).floor() == NHBlocks.quantumFieldDisturbing));
                }

                return b;
            }));

            decoration(0.017f);

            setOrthogonalLine();

            setRectsOld(path, maxDst);

            setChunks();

            setDarkness();

            //setVent();

            pass((x, y) -> {
                boolean any = false;
                for (int rx = -1; rx <= 1; rx++) {
                    for (int ry = -1; ry <= 1; ry++) {
                        if (!(tiles.get(x+rx, y+ry) != null && tiles.get(x+rx, y+ry).block() == EnvironmentBlock.armorWall)) {
                            any = true;
                        }
                    }
                }
                if (!any && rand.chance(0.01f)){
                    for(int cx = -4; cx <= 4; cx++){
                        for(int cy = -4; cy <= 4; cy++){
                            int wx = cx + x, wy = cy + y;
                            if (tiles.getn(wx, wy) != null && rand.chance(0.75f) && tiles.getn(wx, wy).block() == EnvironmentBlock.armorWall) {
                                tiles.getn(wx, wy).setBlock(EnvironmentBlock.scarpWallSmall);
                            }
                        }
                    }
                }
            });

            for(Tile tile : tiles){
                if (tile.floor() == NHBlocks.metalGround) {
                    tile.setFloor(NHBlocks.conglomerateRock.asFloor());
                }
            }

            path = pathfind(coreX, coreY, spawnX, spawnY, tile -> (tile.solid() ? 150f : 0f) + maxDst - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);

            brush(path, 8);

            setSpawn();

            tiles.getn(spawnX, spawnY).setOverlay(Blocks.spawn);

            tiles.getn(coreX, coreY).setFloor(Blocks.coreZone.asFloor());

            setRule(difficulty);
        }

        private void setDarkness(){
            for(Tile tile : tiles){
                boolean any = world.getDarkness(tile.x, tile.y) > 0;
                for(int i = 0; i < 4 && !any; i++){
                    any = world.getDarkness(tile.x + Geometry.d4[i].x, tile.y + Geometry.d4[i].y) > 0;
                }

                if(any){
                    tile.setBlock(Blocks.stoneWall);
                }
            }
        }

        private void setChunks(){
            int cw = (width-1) / 24;
            int ch = (height-1) / 24;
            chunks.clear();
            chunks.setSize(cw * ch);
            for (int x = 0; x < cw; x++) {
                for (int y = 0; y < ch; y++) {
                    int count = 0;
                    if (checkSolid(x * 24, y * 24)) count++;
                    if (checkSolid(x * 24 + 23, y * 24)) count++;
                    if (checkSolid(x * 24, y * 24 + 23)) count++;
                    if (checkSolid(x * 24 + 23, y * 24 + 23)) count++;

                    if (count > 2) {
                        setChunk(x, y, NO_GENERATION);
                    }else {
                        setChunk(x, y, DISABLED);
                    }
                }
            }
            int validChunk = chunks.count(NO_GENERATION);
            int count = rand.random(validChunk / 5, validChunk / 3);
            int generateChunk = 0;
            while (generateChunk < count) {
                int x = rand.random(0, cw);
                int y = rand.random(0, ch);

                if (getChunk(x, y) == DISABLED) continue;
                if (getChunk(x, y) == NO_GENERATION){
                    float chance = 0.01f;
                    if (getChunk(x, y + 1) == PREPARE_TO_GENERATE) chance += 0.45f;
                    if (getChunk(x, y - 1) == PREPARE_TO_GENERATE) chance += 0.45f;
                    if (getChunk(x + 1, y) == PREPARE_TO_GENERATE) chance += 0.45f;
                    if (getChunk(x - 1, y) == PREPARE_TO_GENERATE) chance += 0.45f;

                    if (Mathf.chance(chance)){
                        setChunk(x, y, PREPARE_TO_GENERATE);
                        generateChunk ++;
                    }
                }
            }


            for (int x = 0; x < cw - 1; x++) {
                for (int y = 0; y < ch - 1; y++) {
                    if (rand.chance(0.2f) && getChunk(x, y) == PREPARE_TO_GENERATE && getChunk(x + 1, y) == PREPARE_TO_GENERATE && getChunk(x, y + 1) == PREPARE_TO_GENERATE && getChunk(x + 1, y + 1) == PREPARE_TO_GENERATE) {
                        int tx = x * 24;
                        int ty = y * 24;
                        for (int ctx = 0; ctx < 48; ctx++) {
                            for (int cty = 0; cty < 48; cty++) {
                                tiles.get(ctx + tx, cty + ty).setBlock(Blocks.air);
                            }
                        }
                        setChunk(x, y, GENERATED);
                        setChunk(x + 1, y, GENERATED);
                        setChunk(x, y + 1, GENERATED);
                        setChunk(x + 1, y + 1, GENERATED);

                        SchematicUtil.placeBuildLB(NHSchematic.ruin_2x2, tx, ty, Team.derelict);
                    }
                }
            }

            for (int x = 0; x < cw - 1; x++) {
                for (int y = 0; y < ch; y++) {
                    if (rand.chance(0.2f) && getChunk(x, y) == PREPARE_TO_GENERATE && getChunk(x + 1, y) == PREPARE_TO_GENERATE) {
                        int tx = x * 24;
                        int ty = y * 24;
                        for (int ctx = 0; ctx < 48; ctx++) {
                            for (int cty = 0; cty < 24; cty++) {
                                tiles.get(ctx + tx, cty + ty).setBlock(Blocks.air);
                            }
                        }
                        setChunk(x, y, GENERATED);
                        setChunk(x + 1, y, GENERATED);

                        SchematicUtil.placeBuildLB(NHSchematic.ruin_2x1, tx, ty, Team.derelict);
                    }
                }
            }

            for (int x = 0; x < cw; x++) {
                for (int y = 0; y < ch - 1; y++) {
                    if (rand.chance(0.67f) && getChunk(x, y) == PREPARE_TO_GENERATE && getChunk(x, y + 1) == PREPARE_TO_GENERATE) {
                        int tx = x * 24;
                        int ty = y * 24;
                        for (int ctx = 0; ctx < 24; ctx++) {
                            for (int cty = 0; cty < 48; cty++) {
                                tiles.get(ctx + tx, cty + ty).setBlock(Blocks.air);
                            }
                        }
                        setChunk(x, y, GENERATED);
                        setChunk(x, y + 1, GENERATED);

                        SchematicUtil.placeBuildLB(NHSchematic.ruin_1x2, tx, ty, Team.derelict);
                    }
                }
            }

            for (int x = 0; x < cw; x++) {
                for (int y = 0; y < ch; y++) {
                    if (getChunk(x, y) == PREPARE_TO_GENERATE) {
                        int tx = x * 24;
                        int ty = y * 24;
                        for (int ctx = 0; ctx < 24; ctx++) {
                            for (int cty = 0; cty < 24; cty++) {
                                tiles.get(ctx + tx, cty + ty).setBlock(Blocks.air);
                            }
                        }
                        setChunk(x, y, GENERATED);

                        SchematicUtil.placeBuildLB(NHSchematic.ruin_1x1, tx, ty, Team.derelict);
                    }
                }
            }
        }

        private boolean inChunks(int x, int y){
            return x >= 0 && x < (width-1) / 24 && y >= 0 && y < (height-1) / 24;
        }

        private int getChunk(int x, int y){
            if (inChunks(x, y)) return chunks.get(x + y * ((width-1) / 24));
            return DISABLED;
        }

        private void setChunk(int x, int y, int val){
            if (inChunks(x, y)) chunks.set(x + y * ((width-1) / 24), val);
        }

        private boolean checkSolid(int x, int y){
            return tiles.get(x, y) != null;
        }

        private void setVent(){
            int minVents = rand.random(22, 33);
            int ventCount = 0;

            over: while(ventCount < minVents){
                outer:
                for(Tile tile : tiles){
                    Floor floor = tile.floor();
                    if((floor == NHBlocks.metalGround) && rand.chance(0.002)){
                        int radius = 2;
                        for(int x = -radius; x <= radius; x++){
                            for(int y = -radius; y <= radius; y++){
                                Tile other = tiles.get(x + tile.x, y + tile.y);
                                if(other == null || (other.floor() != NHBlocks.metalGround) || other.block().solid){
                                    continue outer;
                                }
                            }
                        }

                        ventCount++;
                        for(Point2 pos : SteamVent.offsets){
                            Tile other = tiles.get(pos.x + tile.x + 1, pos.y + tile.y + 1);
                            other.setOverlay(Blocks.air);
                            other.setFloor(NHBlocks.metalVent.asFloor());
                        }
                        if(ventCount >= minVents)break over;
                    }
                }
            }
        }

        private void setOrthogonalLine(){
            pass((x, y) -> {
                int x1 = x - x % 3 + 30;
                int y1 = y - y % 3 + 30;

                if((x1 % 70 == 0 || y1 % 70 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.85f || Mathf.chance(0.035)){
                        floor = Blocks.metalFloor2;
                    }
                }

                if((x % 85 == 0 || y % 85 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x, y, 7, 0.67f, 55f, 3f) > 0.835f || Mathf.chance(0.175)){
                        floor = Blocks.metalFloor5;
                    }
                }

                if((x % 50 == 0 || y % 50 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x, y, 5, 0.7f, 75f, 3f) > 0.8125f || Mathf.chance(0.075)){
                        floor = NHBlocks.quantumFieldDisturbing;
                    }
                }
            });
        }

        private void setRectsOld(Seq<Tile> path, float maxd){
            while (tmpRects.size < 64){
                int rx = rand.random(20, width - 20);
                int ry = rand.random(20, height - 20);
                int w = rand.random(9, 21);
                int h = rand.random(9, 21);
                if (!tiles.get(rx, ry).solid() || !tiles.get(rx, ry).floor().isLiquid){
                    tmpRects.add(new Rect().setCentered(rx, ry, w, h));
                }
            }

            Rect coreRect = new Rect().setCentered(coreX, coreY, 45);

            for (int k = 0; k < 20; k++) {
                int idx = rand.random(tmpRects.size - 2);
                Rect r1 = tmpRects.get(idx);
                Rect r2 = tmpRects.get(idx + 1);

                r1.getCenter(Tmp.v1);
                r2.getCenter(Tmp.v2);

                path.clear();
                path = pathfind((int) Tmp.v1.x, (int) Tmp.v1.y, (int) Tmp.v2.x, (int) Tmp.v2.y, tile -> maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);

                continualDraw(path, Blocks.air, 7, (x, y) -> true, true);
                continualDraw(path, Blocks.metalFloor.asFloor(), 2, (x, y) -> rand.chance(0.95f));
            }

            for (int k = 0; k < 20; k++) {
                int idx = rand.random(tmpRects.size - 2);
                Rect r1 = tmpRects.get(idx);
                Rect r2 = tmpRects.get(idx + 1);

                r1.getCenter(Tmp.v1);
                r2.getCenter(Tmp.v2);

                path.clear();
                path = pathfind((int) Tmp.v1.x, (int) Tmp.v1.y, (int) Tmp.v2.x, (int) Tmp.v2.y, tile -> maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);

                continualDraw(path, EnvironmentBlock.armorWall, 3, (x, y) -> true);
            }

            for (int k = 0; k < 5; k++) {
                int idx = rand.random(tmpRects.size - 1);
                Rect r1 = tmpRects.get(idx);

                r1.getCenter(Tmp.v1);
                coreRect.getCenter(Tmp.v2);

                path.clear();
                path = pathfind((int) Tmp.v1.x, (int) Tmp.v1.y, (int) Tmp.v2.x, (int) Tmp.v2.y, tile -> maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);

                continualDraw(path, Blocks.air, 9, (x, y) -> true, true);
                continualDraw(path, Blocks.metalFloor.asFloor(), 3, (x, y) -> rand.chance(0.95f));
            }

            tmpRects.add(coreRect);

            /*
            for(int k = 0; k < tmpRects.size; k++){
                if (Mathf.chance(0.4f)) continue;
                Rect r = tmpRects.get(k);
                Tmp.r1.set(r).grow(1);
                if (tiles.get((int)(Tmp.r1.x), (int)(Tmp.r1.y)).solid()) continue;
                for(int i = 0; i < Tmp.r1.width; i++){
                    for(int j = 0; j < Tmp.r1.height; j++){
                        Tile tile = tiles.get((int)(i + Tmp.r1.x), (int)(j + Tmp.r1.y));
                        if(tile == null)continue;
                        if(i == 0 || i == (int)Tmp.r1.width - 1 || j == 0 || j == (int)Tmp.r1.height - 1){
                            if (rand.chance(0.75f)){
                                tile.setFloor(NHBlocks.metalGround.asFloor());
                            }
                            if (rand.chance(0.6f) && !tile.solid()){
                                tile.setBlock(Blocks.scrapWall);
                            }
                        }else {
                            tile.setBlock(Blocks.air);
                            tile.setFloor(EnvironmentBlock.metalFloorPlain);
                        }
                    }
                }
            }
             */

            tmpRects.clear();
        }

        private void setSpawn(){
            for (int x = -25; x <= 25; x++){
                for (int y = -25; y <= 25; y++){
                    Tile other = tiles.getn(coreX + x, coreY + y);
                    other.remove();
                }
            }
            Schematics.placeLoadout(NHContent.nhBaseLoadout, coreX, coreY);
            for (int x = -15; x <= 15; x++){
                for (int y = -15; y <= 15; y++){
                    Tile other = tiles.getn(coreX + x, coreY + y);
                    other.setFloor(EnvironmentBlock.metalFloorPlain);
                }
            }
            for (int x = -4; x <= 4; x++){
                for (int y = -4; y <= 4; y++){
                    Tile other = tiles.getn(coreX + x, coreY + y);
                    other.setFloor(EnvironmentBlock.metalFloorRidge);
                }
            }
            for (int x = -3; x <= 3; x++){
                for (int y = -3; y <= 3; y++){
                    Tile other = tiles.getn(coreX + x, coreY + y);
                    other.setFloor(Blocks.coreZone.asFloor());
                }
            }
        }

        private void setRule(float difficulty){
            state.rules.env = sector.planet.defaultEnv;
            state.rules.waves = true;
            state.rules.showSpawns = true;
            state.rules.onlyDepositCore = false;
            state.rules.fog = false;

            if(state.rules.sector.preset != null)return;

            state.rules.winWave = Mathf.round(150 * difficulty, 5);
            state.rules.weather.clear();
            state.rules.weather.add(new Weather.WeatherEntry(NHWeathers.quantumStorm, 3 * Time.toMinutes, 8 * Time.toMinutes, 0.25f * Time.toMinutes, 0.75f * Time.toMinutes));
            state.rules.spawns = NHOverride.generate(difficulty, new Rand(sector.id), false, false, false);
        }

        public void continualDraw(Seq<Tile> path, Block block, int rad, DrawBoolf b, boolean isBlock){
            GridBits used = new GridBits(tiles.width, tiles.height);

            for(Tile t : path){
                for(int x = -rad; x <= rad; x++){
                    for(int y = -rad; y <= rad; y++){
                        int wx = t.x + x, wy = t.y + y;
                        if(!used.get(wx, wy) && Structs.inBounds(wx, wy, width, height) && Mathf.within(x, y, rad)){
                            used.set(wx, wy);
                            if(b.get(wx, wy)){
                                Tile other = tiles.getn(wx, wy);
                                if (isBlock) other.setBlock(block);
                                else if(block instanceof Floor)other.setFloor(block.asFloor());
                                else other.setBlock(block);
                            }
                        }
                    }
                }
            }
        }

        public void continualDraw(Seq<Tile> path, Block block, int rad, DrawBoolf b){
            continualDraw(path, block, rad, b, false);
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
    }

    public interface DrawBoolf{
        boolean get(int x, int y);
    }
}
