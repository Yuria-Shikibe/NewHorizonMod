package newhorizon.expand.map.planet;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.GridBits;
import arc.struct.Seq;
import arc.util.Structs;
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
import mindustry.graphics.g3d.PlanetGrid;
import mindustry.maps.generators.PlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.content.*;
import newhorizon.expand.map.SchematicUtil;

import static mindustry.Vars.state;
import static newhorizon.content.NHPlanets.ceito;

public class MidanthaPlanet extends Planet {
    public static final int MidanthaEnv = 1 << 8;
    public MidanthaPlanet() {
        super("midantha-test", ceito, 1);
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

        grid = PlanetGrid.create(3);

        sectors.ensureCapacity(grid.tiles.length);
        for(int i = 0; i < grid.tiles.length; i++){
            sectors.add(new Sector(this, PlanetGrid.Ptile.empty));
        }

        //sectorApproxRadius = sectors.first().tile.v.dst(sectors.first().tile.corners[0].v);

    }

    public static class MidanthaPlanetGenerator extends PlanetGenerator {

        public float heightScl = 0.9f, octaves = 8, persistence = 0.7f, heightPow = 3f, heightMult = 1.6f;

        public static final Seq<Rect> tmpRects = new Seq<>();
        public static Seq<Tile> tmpTiles = new Seq<>();
        public static Seq<Vec2[]> tmpRivers = new Seq<>();

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

        public int getSectorSize(Sector sector){
            int res = (int)(sector.rect.radius * getSizeScl());
            return res % 2 == 0 ? res : res + 1;
        }

        float rawHeight(Vec3 position){
            return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 10f + position.x, 10f + position.y, 10f + position.z);
        }

        float rawTemp(Vec3 position){
            return position.dst(0, 0, 1)*2.2f - Simplex.noise3d(seed, 8, 0.54f, 1.4f, 10f + position.x, 10f + position.y, 10f + position.z) * 2.9f;
        }

        @Override
        public void genTile(Vec3 position, TileGen tile){
            tile.floor = NHBlocks.quantumFieldDeep;
            tile.block = Blocks.air;
        }

        @Override
        protected void generate(){
            rand.setSeed(seed + sector.id);

            //todo difficulty
            float difficulty = sector == null ? rand.random(0.4f, 1f) : sector.threat;
            float length = width/3.2f;
            float angel = rand.random(360f);
            Vec2 trns = Tmp.v1.trns(angel, length);
            int spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
                coreX = (int)(-trns.x + width/2f), coreY = (int)(-trns.y + height/2f);
            tiles.getn(spawnX, spawnY).setOverlay(Blocks.spawn);
            tiles.getn(coreX, coreY).setFloor(Blocks.coreZone.asFloor());
            //SchematicUtil.placeTerrain(NHContent.terrainTest, coreX, coreY);
            Schematics.placeLoadout(NHContent.nhBaseLoadout, coreX, coreY);
            setRule(difficulty);

            //tmpTiles = pathfind(spawnX, spawnY, coreX, coreY, tile -> (tile.floor().isDeep() ? 0 : 500f), Astar.manhattan);

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

        private void getRects(){
            rand.setSeed(sector.id + seed);
            tmpRects.clear();
            tmpRivers.clear();
            tmpTiles.clear();
            Rect rect = new Rect(0, 0, width, height);
            tmpRects.add(rect);

            spiltRect();
        };

        private void getBridgeCandidate(){

        }

        private void spiltRect(){
            int minSize = 180;
            int maxSize = 200;
            //todo use max count
            int maxRectCount = 12;
            for (Rect rect: tmpRects){
                if (rect.width >= rect.height && rect.width > minSize && Mathf.chance(rect.width / maxSize)){
                    int splitLineStroke = 5;
                    int splitWidth = rand.random(minSize/4, (int)rect.width - minSize/4 - splitLineStroke),
                        splitWidthAll = splitWidth + splitLineStroke;

                    Rect rectSpilt1 = new Rect(rect.x, rect.y, splitWidth, rect.height);
                    Rect rectSpilt2 = new Rect(rect.x + splitWidthAll, rect.y, rect.width - splitWidthAll, rect.height);

                    tmpRects.remove(rect);
                    tmpRects.add(rectSpilt1, rectSpilt2);

                    tmpRivers.add(new Vec2[]{new Vec2(rect.x + splitWidth + 2, rect.y), new Vec2(rect.x + splitWidth + 2, rect.y + rect.height - 1)});
                }else if(rect.width < rect.height && rect.height > minSize && Mathf.chance(rect.height / maxSize)){
                    int splitLineStroke = 5;
                    int splitHeight = rand.random(minSize/4, (int)rect.height - minSize/4 - splitLineStroke),
                        splitHeightAll = splitHeight + splitLineStroke;

                    Rect rectSpilt1 = new Rect(rect.x, rect.y, rect.width, splitHeight);
                    Rect rectSpilt2 = new Rect(rect.x, rect.y + splitHeightAll, rect.width, rect.height - splitHeightAll);

                    tmpRects.remove(rect);
                    tmpRects.add(rectSpilt1, rectSpilt2);

                    tmpRivers.add(new Vec2[]{new Vec2(rect.x, rect.y + splitHeight + 2), new Vec2(rect.x + rect.width - 1, rect.y + splitHeight + 2)});

                }
            }
            if (tmpRects.find(rect -> rect.width > minSize || rect.height > minSize) != null){
                spiltRect();
            }
        }

        private void shrinkRect(int dst){
            for (Rect rect: tmpRects){
                rect.x += dst;
                rect.y += dst;
                rect.width -= dst * 2f;
                rect.height -= dst * 2f;
            }
        }

        private void rectCliff(){
            for (Rect rect: tmpRects){
                int x1 = (int) rect.x, y1 = (int) rect.y,
                    x2 = (int) (x1 + rect.width - 1), y2 = (int) (y1 + rect.height - 1);
                for (int y = y1; y < y2; y++){
                    tiles.get(x2, y).data = -125;
                    tiles.get(x1, y).data = 56;
                    tiles.get(x2, y).setBlock(Blocks.cliff);
                    tiles.get(x1, y).setBlock(Blocks.cliff);
                }
                for (int x = x1; x < x2; x++){
                    tiles.get(x, y2).data = 14;
                    tiles.get(x, y1).data = -32;
                    tiles.get(x, y2).setBlock(Blocks.cliff);
                    tiles.get(x, y1).setBlock(Blocks.cliff);
                }

                tiles.get(x2, y2).data = -113;
                tiles.get(x1, y2).data = 62;
                tiles.get(x1, y1).data = -8;
                tiles.get(x2, y1).data = -29;

                tiles.get(x2, y2).setBlock(Blocks.cliff);
                tiles.get(x1, y2).setBlock(Blocks.cliff);
                tiles.get(x1, y1).setBlock(Blocks.cliff);
                tiles.get(x2, y1).setBlock(Blocks.cliff);
            }
        }

        private void rectSetFloor(Block floor){
            for (Rect rect: tmpRects){
                for(int i = 0; i < rect.width; i++){
                    for(int j = 0; j < rect.height; j++){
                        Tile tile = tiles.get((int)(i + rect.x), (int)(j + rect.y));
                        tile.setFloor(floor.asFloor());
                    }
                }
            }
        }

        private void setSpawn(Rect spawnRect){
            spawnRect.getCenter(Tmp.v1);
            tiles.getn((int)Tmp.v1.x, (int)Tmp.v1.y).setOverlay(Blocks.spawn);
        }

        private void setCore(Rect coreRect){
            coreRect.getCenter(Tmp.v1);
            tiles.getn((int)Tmp.v1.x, (int)Tmp.v1.y).setFloor(Blocks.coreZone.asFloor());
            for(Point2 p : Geometry.d8){
                Tile other = tiles.getn((int)Tmp.v1.x + p.x, (int)Tmp.v1.y + p.y);
                other.setFloor(Blocks.coreZone.asFloor());
            }
            Schematics.placeLoadout(NHContent.nhBaseLoadout, (int)Tmp.v1.x, (int)Tmp.v1.y);
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
            state.rules.tags.put(NHInbuiltEvents.APPLY_KEY, "true");
            state.rules.tags.put("MapRect", tmpRects.toString());
            if(rawTemp(sector.tile.v) < 0.65f){
                state.rules.bannedBlocks.addAll(Vars.content.blocks().copy().select(b -> b instanceof LaunchPad));
            }
        }
    }

    public interface DrawBoolf{
        boolean get(int x, int y);
    }
}
