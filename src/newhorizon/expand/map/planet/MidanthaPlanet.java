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
import mindustry.ai.Astar;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.content.Loadouts;
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
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.campaign.LaunchPad;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.TallBlock;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.content.*;
import newhorizon.content.blocks.EnvironmentBlock;

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

    public static class MidanthaPlanetGenerator extends PlanetGenerator {

        public float heightScl = 0.9f, octaves = 8, persistence = 0.7f, heightPow = 3f, heightMult = 1.6f;

        public static float airThresh = 0.13f, airScl = 14;

        public static final Seq<Rect> tmpRects = new Seq<>();
        public static Seq<Tile> tmpTiles = new Seq<>();
        public static Seq<Vec2[]> tmpRivers = new Seq<>();

        Block[] terrain1 = {EnvironmentBlock.metalFloorPlain, EnvironmentBlock.metalFloorPlain, EnvironmentBlock.metalFloorPlainQuantum, EnvironmentBlock.metalFloorPlain, EnvironmentBlock.metalFloorPlain, EnvironmentBlock.metalFloorPlain, EnvironmentBlock.metalFloorPlain, NHBlocks.quantumFieldDeep, EnvironmentBlock.metalFloorPlain, NHBlocks.conglomerateRock, NHBlocks.quantumFieldDeep};


        {
            baseSeed = 5;
            defaultLoadout = Loadouts.basicBastion;
        }

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
            return 2000 * 1.07f * 6f / 3.5f;
        }

        float rawHeight(Vec3 position){
            return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 10f + position.x, 10f + position.y, 10f + position.z);
        }

        float rawTemp(Vec3 position){
            return position.dst(0, 0, 1)*2.2f - Simplex.noise3d(seed, 8, 0.54f, 1.4f, 10f + position.x, 10f + position.y, 10f + position.z) * 2.9f;
        }

        Block getBlock(Vec3 position){
            Tmp.v32.set(position);
            float height = rawHeight(position);
            Tmp.v31.set(position);
            height *= 1.2f;
            height = Mathf.clamp(height);
            return terrain1[Mathf.clamp((int)(height * terrain1.length), 0, terrain1.length - 1)];
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
            //create rects for terrain
            getRects();
            rectSetFloor(EnvironmentBlock.metalFloorPlain);
            //create outline for quantum river
            blend(NHBlocks.quantumFieldDeep, NHBlocks.quantumField, 3);
            shrinkRect(3);
            //create cliffs
            rectCliff();
            shrinkRect(1);
            rectCliff();
            shrinkRect(1);
            rectCliff();
            shrinkRect(1);
            //set core zone and enemy drop
            float length = width/3.2f;
            float angel = rand.random(360f);
            Vec2 trns = Tmp.v1.trns(angel, length);
            int spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
                coreX = (int)(-trns.x + width/2f), coreY = (int)(-trns.y + height/2f);
            //check nearest rect for core and enemy drop
            Rect spawnRect = new Rect(), coreRect = new Rect();
            for (Rect rect: tmpRects){
                Tmp.r1.setCentered(spawnX, spawnY, 20);
                if(rect.overlaps(Tmp.r1)){
                    spawnRect = rect;
                }
                Tmp.r1.setCentered(coreX, coreY, 20);
                if(rect.overlaps(Tmp.r1)){
                    coreRect = rect;
                }
            }
            //check for darkness, this is awful, todo
            //for (int i = 0; i < 5; i++){
            //    spawnRect.getCenter(Tmp.v1);
            //    if (world.getDarkness((int) Tmp.v1.x, (int) Tmp.v1.y) >= 1f){
            //        trns.rotate(15 * i);
            //        spawnX = (int)(trns.x + width/2f); spawnY = (int)(trns.y + height/2f);
            //    }
            //}



            //erase(spawnX, spawnY, 22);

            //tmpTiles = pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 70f : 0f) + maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan);


            //brush(path, 8);
            //erase(coreX, coreY, 15);

            //median(12, 0.6, NHBlocks.quantumField);

            //blend(NHBlocks.quantumFieldDeep, NHBlocks.quantumField, 7);

            //pass((x, y) -> {
            //    if(floor.asFloor().isDeep()){
            //        float noise = noise(x + 342, y + 541, 7, 0.8f, 120f, 1.5f);
            //        if(noise > 0.82f){
            //            floor = NHBlocks.quantumField;
            //        }
            //    }
            //});

            //inverseFloodFill(tiles.getn(spawnX, spawnY));

            //erase(coreX, coreY, 6);

            //todo resource generation
            /*
            pass((x, y) -> {
                if(block != Blocks.air){
                    if(nearAir(x, y)){
                        if(block == NHBlocks.metalWall && noise(x + 78, y, 4, 0.7f, 33f, 1f) > 0.52f){
                            ore = Blocks.wallOreBeryllium;
                        }
                    }
                }else if(!nearWall(x, y)){
                    if(noise(x + 150, y + x*2 + 100, 4, 3.8f, 55f, 1f) > 0.816f){
                        ore = Blocks.oreTitanium;
                    }

                    if(noise(x + 134, y - 134, 5, 4f, 45f, 1f) > 0.73f){
                        ore = Blocks.oreLead;
                    }

                    if(noise(x + 644, y - 538, 5.1, 2f, 125f, 1f) > 0.737f){
                        ore = Blocks.oreCopper;
                    }

                    if(noise(x + 344 + y*0.35f, y - 538, 5, 6f, 45f, 1f) > 0.75f){
                        ore = Blocks.oreCoal;
                    }

                    if(noise(x + 244, y - 138, 6, 3f, 35f, 1f) > 0.8f){
                        ore = Blocks.oreBeryllium;
                    }

                    if(noise(x + 578, y - 238, 4, 2.08f, 85f, 1f) > 0.793f){
                        ore = Blocks.oreTungsten;
                    }

                    if(noise(y - 1234, x - 938, 6, 2.28f, 15f, 1f) > 0.880383f){
                        ore = NHBlocks.oreZeta;
                    }

                    if(noise(x + 999, y + 600, 4, 5.63f, 45f, 1f) > 0.8422f){
                        ore = Blocks.oreThorium;
                    }
                }
            });

             */


            /*
            pass((x, y) -> {
                int x1 = x - x % 3 + 30;
                int y1 = y - y % 3 + 30;

                if((x1 % 70 == 0 || y1 % 70 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x + 30, y + 30, 4, 0.66f, 75f, 2f) > 0.85f || Mathf.chance(0.035)){
                        floor = Blocks.metalFloor2;
                    }
                }

                if((x % 85 == 0 || y % 85 == 0) && !floor.asFloor().isLiquid){
                    if(difficulty > 0.815f){
                        floor = NHBlocks.armorAncient;
                    }else if(noise(x, y, 7, 0.67f, 55f, 3f) > 0.835f || Mathf.chance(0.175)){
                        floor = Blocks.metalFloor5;
                    }
                }

                if((x % 50 == 0 || y % 50 == 0) && !floor.asFloor().isLiquid){
                    if(noise(x, y, 5, 0.7f, 75f, 3f) > 0.8125f || Mathf.chance(0.075)){
                        floor = NHBlocks.quantumFieldDisturbing;
                    }
                }

                if((nearWall(x, y) || floor == Blocks.metalFloor2) && Mathf.chance(0.015)){
                    block = NHBlocks.metalTower;
                }
            });

             */

            //remove props near ores, they're too annoying
            /*
            pass((x, y) -> {
                if(ore.asFloor().wallOre || block.itemDrop != null || (block == Blocks.air && ore != Blocks.air)){
                    removeWall(x, y, 3, b -> b instanceof TallBlock);
                }
            });

            for(Tile tile : tiles){
                if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                    tile.setOverlay(Blocks.air);
                }
            }

             */

            //blend(NHBlocks.quantumFieldDisturbing, Blocks.darkPanel3, 1);

            //tmpTiles = pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 50f : 0f), Astar.manhattan);

            //Geometry.circle(endX, endY, 12, ((x, y) -> {
            //    Tile tile = tiles.get(x, y);
            //    if(tile != null && tile.floor().isLiquid){
            //        tile.setFloor(NHBlocks.quantumField.asFloor());
            //    }
            //}));

            /*
            continualDraw(path, NHBlocks.quantumField, 4, ((x0, y0) -> {
                Floor f = tiles.getn(x0, y0).floor();
                boolean b = f.isDeep();
                if(b && noise(x0, y0 * x0, 6, 0.7f, 25f, 3f) > 0.4125f){
                    rand.setSeed((long)x0 + y0 << 8);
                    if(rand.chance(0.22f))draw(x0, y0, NHBlocks.quantumField, 4, ((x1, y1) -> {
                        Floor f1 = tiles.getn(x1, y1).floor();
                        if(f1 == NHBlocks.quantumFieldDisturbing){
                            tiles.getn(x1, y1).setFloor(EnvironmentBlock.metalFloorPlain);
                            return false;
                        }
                        return f1.isDeep();
                    }));
                }

                else if(f == NHBlocks.quantumFieldDisturbing){
                    draw(x0, y0, EnvironmentBlock.metalFloorPlain, 4, ((x1, y1) -> tiles.getn(x1, y1).floor() == NHBlocks.quantumFieldDisturbing));
                }

                return b;
            }));

             */


            //median(5, 0.46, NHBlocks.quantumField);

            //decoration(0.017f);

            //trimDark();



            //vents
            /*
            int minVents = rand.random(22, 33);
            int ventCount = 0;
            over: while(ventCount < minVents){
                outer:
                for(Tile tile : tiles){
                    Floor floor = tile.floor();
                    if((floor == EnvironmentBlock.metalFloorPlain) && rand.chance(0.002)){
                        int radius = 2;
                        for(int x = -radius; x <= radius; x++){
                            for(int y = -radius; y <= radius; y++){
                                Tile other = tiles.get(x + tile.x, y + tile.y);
                                if(other == null || (other.floor() != EnvironmentBlock.metalFloorPlain) || other.block().solid){
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

             */





            setSpawn(spawnRect);
            setCore(coreRect);

            tmpTiles = pathfind(spawnX, spawnY, coreX, coreY, tile -> (tile.floor().isDeep() ? 0 : 500f), Astar.manhattan);

            setRule(difficulty);
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
