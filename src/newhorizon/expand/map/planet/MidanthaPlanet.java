package newhorizon.expand.map.planet;

import arc.func.Intc2;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.content.*;
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
            Pal.darkestGray
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
                if(b instanceof SolidPump){
                    SolidPump pump = (SolidPump)b;
                    return pump.result == Liquids.water && pump.attribute == Attribute.water;
                }else return false;
            }));

            //r.env = r.sector.planet.defaultEnv;
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

        icon = "midantha";
        iconColor = Color.white;

        landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
        atmosphereRadIn = 0.02f;
        atmosphereRadOut = 0.3f;
        startSector = 15;
    }

    public class MidanthaPlanetGenerator extends PlanetGenerator {
        //planet
        public float heightScl = 0.9f, octaves = 8, persistence = 0.7f, heightPow = 3f, heightMult = 1.6f;
        //chunk size
        public static final int cSize = 16;
        //inner num after limit area
        public int border, borderSize, cStart, cStep;
        public Chunk[][] chunks;

        //inner variable
        public final Seq<Rect> chunkRects = new Seq<>();
        public Seq<Point2> tmpRivers = new Seq<>();


        public boolean allowLanding(Sector sector){
            return (sector.hasBase() || sector.near().contains(s -> s.hasBase() && s.isCaptured()))/* && NHPlanets.midantha.unlocked()*/;
        }

        @Override
        public void generateSector(Sector sector){
            //no bases right now
        }

        @Override
        public float getHeight(Vec3 position){
            return Mathf.pow(rawHeight(position), heightPow) * heightMult;
        }

        @Override
        public Color getColor(Vec3 position){
            return Pal.gray;
        }

        @Override
        public float getSizeScl(){
            return 350;
        }

        @Override
        public int getSectorSize(Sector sector){
            return 512;
        }

        float rawHeight(Vec3 position){
            return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 10f + position.x, 10f + position.y, 10f + position.z);
        }

        float rawTemp(Vec3 position){
            return position.dst(0, 0, 1)*2.2f - Simplex.noise3d(seed, 8, 0.54f, 1.4f, 10f + position.x, 10f + position.y, 10f + position.z) * 2.9f;
        }

        @Override
        public void genTile(Vec3 position, TileGen tile){
            tile.floor = Blocks.air;
            tile.block = Blocks.air;
        }

        @Override
        protected void generate(){
            rand.setSeed(seed + sector.id);
            setBorder();
            for (int cx = 0; cx < cStep; cx++){
                for (int cy = 0; cy < cStep; cy++){
                    int cwx = cStart + cx * cSize,
                        cwy = cStart + cy * cSize;

                    SchematicUtil.placeTerrainLB(NHSchematic.TEST_CHUNK_BLACK, cwx, cwy);
                }
            }
            setRects();
            setRiver();

            setSpawnAndCore();
            setRule();
        }

        //set the map border and limit map area, and set border variables
        private void setBorder(){
            //FUCK YOU ANUKE THE FUCKED POLY BORDER
            //alright lets worldCreated the area for that shit border
            for (int i = 0; i < width/2; i++){
                if (world.getDarkness(i, i) == 0 &&
                    world.getDarkness(width - 1 - i, i) == 0 &&
                    world.getDarkness(i, height - 1 - i) == 0 &&
                    world.getDarkness(width - 1 - i, height - 1 - i) == 0){
                    border = i;
                    Log.info(border);
                    break;
                }
            }

            borderSize = width - border * 2;
            //worldCreated the map
            cStart = (borderSize % cSize) / 2 + border;
            cStep = borderSize / cSize;
            chunks = new Chunk[cStep][cStep];
            for (int i = 0; i < cStep; i++){
                for (int j = 0; j < cStep; j++){
                    chunks[i][j] = new Chunk();
                }
            }

            state.rules.limitMapArea = true;
            state.rules.limitX = state.rules.limitY = border;
            state.rules.limitWidth = state.rules.limitHeight = borderSize;
        }

        private void setRiver(){
            for (Point2 p: tmpRivers){
                chunks[p.x][p.y].river = true;
            }

            for (int x = 0; x < cStep; x++){
                for (int y = 0; y < cStep; y++){
                    if (chunks[x][y].river){
                        int idx = 0;
                        if (isRiverChunk(x, y + 1)) idx += 1;
                        if (isRiverChunk(x + 1, y)) idx += 2;
                        if (isRiverChunk(x, y - 1)) idx += 4;
                        if (isRiverChunk(x - 1, y)) idx += 8;

                        if (idx > 0){
                            SchematicUtil.placeTerrainLB(NHSchematic.QUANTUM_RIVER, getChunkLBxy(x), getChunkLBxy(y), idx * cSize, 0, cSize, cSize);
                            SchematicUtil.placeBuildLB(NHSchematic.QUANTUM_RIVER_BUILD[idx], getChunkLBxy(x), getChunkLBxy(y), Team.derelict);
                        }
                    }
                }
            }

        }

        private boolean isRiverChunk(int x, int y){
            if (x < 0 || y < 0 || x >= cStep || y >= cStep) return true;
            return chunks[x][y].river;
        }

        private int getChunkLBxy(int index){
            return cStart + index * cSize;
        }

        private void setRects(){
            chunkRects.clear();
            tmpRivers.clear();
            chunkRects.add(new Rect(0, 0, cStep-1, cStep-1));

            for(int i = 0; i < 25; i++){
                Rect largest = new Rect();
                for (Rect rect: chunkRects){
                    if (rect.area() > largest.area()) largest = rect;
                }
                spiltRect(largest);
            }
        }

        private void spiltRect(Rect rect){
            boolean verticalSplit = Mathf.chance(rect.width / (rect.width + rect.height));
            if (verticalSplit && rect.width > 3){
                int xSplit = rand.random(1, (int)rect.width - 1);
                chunkRects.remove(rect);
                for (int y = 0; y < rect.height + 1; y++){
                    tmpRivers.add(new Point2((int) (rect.x) + xSplit, (int) (rect.y) + y));
                }
                chunkRects.add(new Rect(rect.x, rect.y, xSplit - 1, rect.height));
                chunkRects.add(new Rect(rect.x + xSplit + 1, rect.y, rect.width - xSplit - 1, rect.height));
            }else if (rect.height > 3){
                int ySplit = rand.random(1, (int)rect.height - 1);
                chunkRects.remove(rect);
                for (int x = 0; x < rect.width + 1; x++){
                    tmpRivers.add(new Point2((int) (rect.x) + x, (int) (rect.y) + ySplit));
                }
                chunkRects.add(new Rect(rect.x, rect.y, rect.width, ySplit - 1));
                chunkRects.add(new Rect(rect.x, rect.y + ySplit + 1, rect.width, rect.height - ySplit - 1));
            }
        }

        private void setSpawnAndCore(){
            float length = borderSize/3.2f;
            float angel = rand.random(360f);
            Vec2 trns = Tmp.v1.trns(angel, length);
            int spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
                coreX = (int)(-trns.x + width/2f), coreY = (int)(-trns.y + height/2f);
            tiles.getn(spawnX, spawnY).setOverlay(Blocks.spawn);
            tiles.getn(coreX, coreY).setFloor(Blocks.coreZone.asFloor());
            Schematics.placeLoadout(NHContent.nhBaseLoadout, coreX, coreY);
        }

        private void setRule(){
            float difficulty = sector == null ? rand.random(0.4f, 1f) : sector.threat;

            if(state.rules.sector.preset != null)return;

            state.rules.winWave = Mathf.round(150 * difficulty, 5);
            state.rules.weather.clear();
            state.rules.weather.add(new Weather.WeatherEntry(NHWeathers.quantumStorm, 3 * Time.toMinutes, 8 * Time.toMinutes, 0.25f * Time.toMinutes, 0.75f * Time.toMinutes));
            state.rules.spawns = NHOverride.generate(difficulty, new Rand(sector.id), false, false, false);
            state.rules.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
        }

        private void PassTile(Intc2 r){
            for(Tile tile : tiles){
                floor = tile.floor();
                block = tile.block();
                ore = tile.overlay();
                r.get(tile.x, tile.y);
                tile.setFloor(floor.asFloor());
                tile.setBlock(block);
                tile.setOverlay(ore);
            }
        }
    }

    public class Chunk{
        public boolean natural = false;
        public boolean river = false;
    }
}
