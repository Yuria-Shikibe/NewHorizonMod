package newhorizon.expand.map.planet;

import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
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
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.TileGen;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import newhorizon.content.*;
import newhorizon.content.blocks.EnvironmentBlock;

import static mindustry.Vars.state;
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

    @SuppressWarnings("InnerClassMayBeStatic")
    public class MidanthaPlanetGenerator extends PlanetGenerator {
        //planet
        public float heightScl = 0.9f, octaves = 8, persistence = 0.7f, heightPow = 3f, heightMult = 1.6f;

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
            return 2000 * 1.07f * 6f / 4f;
        }


        float rawHeight(Vec3 position){
            return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 10f + position.x, 10f + position.y, 10f + position.z);
        }

        @Override
        public void genTile(Vec3 position, TileGen tile){
            tile.floor = EnvironmentBlock.metalFloorPlain;
            tile.block = Blocks.air;
        }

        @Override
        protected void generate(){
            rand.setSeed(seed + sector.id);

            genRiver();
            //genShoreline();

            placeLoadout();

            setRule();
        }

        private void placeLoadout(){
            float length = width/2.6f;
            Vec2 trns = Tmp.v1.trns(rand.random(360f), length);
            int spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f), endX = (int)(-trns.x + width/2f), endY = (int)(-trns.y + height/2f);
            Schematics.placeLoadout(NHContent.nhBaseLoadout, spawnX, spawnY);
            tiles.getn(endX, endY).setOverlay(Blocks.spawn);
        }

        private void genRiver(){
            float len = width / 6f;
            float ang = rand.random(360f);
            Vec2 trns = Tmp.v1.trns(ang, len).cpy();
            for (int rot = 0; rot < 4; rot++){
                genLine((int)(width/2f + trns.x), (int)(height/2f + trns.y), rot * 90);
                genLine((int)(width/2f - trns.x), (int)(height/2f - trns.y), rot * 90);
            }
        }

        private void genLine(int x, int y, int direction){
            int curX = x, curY = y, lastX, lastY;
            for (int i = 0; i < 100; i++){
                lastX = curX;
                lastY = curY;
                int ang = Mathf.chance(0.8)? Mathf.random(-1, 1) * 45 + direction: direction;
                int len = Mathf.random(20, 60);
                Tmp.v1.trns(ang, len);
                curX = lastX + (int)Tmp.v1.x;
                curY = lastY + (int)Tmp.v1.y;
                if ((curX > width || curX < 0) && (curY > height || curY < 0)) break;
                drawLine(curX, curY, lastX, lastY, 7, NHBlocks.quantumField, NHBlocks.quantumFieldDeep);
                drawLine(curX, curY, lastX, lastY, 4, NHBlocks.quantumFieldDeep, null);
            }
            /*
            do {
                lastX = curX;
                lastY = curY;
                float ang = Mathf.random(-2, 2) * 45 + direction;
                float len = Mathf.random(16, 32);
                Tmp.v1.trns(ang, len).add(x, y);
                curX = (int)Tmp.v1.x;
                curY = (int)Tmp.v1.y;
                drawLine(curX, curY, lastX, lastY);
            } while ((curX < width || curX > 0) && (curY < height || curY > 0));

             */
        }

        public void drawCircle(int size, int x, int y, Cons<Tile> drawer){
            for(int rx = -size; rx <= size; rx++){
                for(int ry = -size; ry <= size; ry++){
                    if(Mathf.within(rx, ry, size - 0.5f + 0.0001f)){
                        int wx = x + rx, wy = y + ry;

                        if(wx < 0 || wy < 0 || wx >= width || wy >= height){
                            continue;
                        }

                        drawer.get(tiles.get(wx, wy));
                    }
                }
            }
        }

        private void drawLine(int x1, int y1, int x2, int y2, int rad, Block floor, Block ignore){
            Bresenham2.line(x1, y1, x2, y2, (x, y) -> {
                drawCircle(rad, x, y, tile -> {
                    if (tile != null && tile.floor() != ignore) {
                        tile.setFloor(floor.asFloor());
                    }
                });
            });
        }

        private void setRule(){
            float difficulty = sector == null ? rand.random(0.4f, 1f) : sector.threat;

            if(state.rules.sector.preset != null)return;

            state.rules.winWave = Mathf.round(150 * difficulty, 5);
            state.rules.weather.clear();
            state.rules.weather.add(new Weather.WeatherEntry(NHWeathers.quantumStorm, 3 * Time.toMinutes, 8 * Time.toMinutes, 0.25f * Time.toMinutes, 0.75f * Time.toMinutes));
            state.rules.spawns = NHOverride.generate(difficulty, new Rand(sector.id), false, false, false);
        }
    }
}
