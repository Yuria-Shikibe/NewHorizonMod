package newhorizon.content;

import arc.graphics.Color;
import arc.struct.ShortSeq;
import arc.util.Time;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.*;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import newhorizon.expand.map.DysonRingMesh;
import newhorizon.expand.map.MidanthaPlanetGenerator;

public class NHPlanets {
    public static Planet midantha;

    public static void load() {
        midantha = new Planet("midantha", Planets.sun, 1f, 2){{
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            iconColor = NHColor.darkEnrColor;

            meshLoader = () -> new HexMesh(this, 6);

            //meshLoader = () -> new NHModMesh(
            //        this, 6, 5, 0.3, 1.7, 1.2, 1.4, 1.1f,
            //        NHColor.darkEnrColor.cpy().lerp(ammonia, 0.75f).mul(1.05f).lerp(Color.black, 0.2f),
            //        NHColor.darkEnrColor.cpy().lerp(ammonia, 0.75f).mul(1.05f).lerp(Color.black, 0.2f),
            //        NHColor.darkEnrColor.cpy().lerp(ammonia, 0.75f).mul(1.05f).lerp(Color.black, 0.2f),
            //        NHColor.darkEnrColor.cpy().lerp(ammonia, 0.75f),
            //        NHColor.darkEnrFront.cpy().lerp(ammonia, 0.75f).lerp(Color.white, 0.2f),

            //        erode.cpy().lerp(Color.black, 0.3f),
            //        erode.cpy().lerp(Color.black, 0.2f),
            //        erode.cpy().lerp(Color.black, 0.1f),
            //        erode.cpy().lerp(Color.white, 0.1f),
            //        erode.cpy().lerp(Color.white, 0.2f),

            //        snow.cpy().lerp(Color.black, 0.4f),
            //        snow.cpy().lerp(Color.black, 0.3f),
            //        snow.cpy().lerp(Color.black, 0.2f)
            //);

            ruleSetter = r -> {
                r.waves = true;
                r.waveTeam = Team.blue;
                r.placeRangeCheck = false;
                r.showSpawns = true;
                r.waveSpacing = 60 * Time.toSeconds;
                r.initialWaveSpacing = 5f * Time.toMinutes;
                r.hideBannedBlocks = true;
                r.spawns = NHPostProcess.generate(0.8f, false);
                r.loadout = ItemStack.list(NHItems.titanium, 1000, NHItems.tungsten, 1000, NHItems.silicon, 1000, NHItems.zeta, 1000);

                Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
                teamRule.rtsAi = false;
                teamRule.unitBuildSpeedMultiplier = 5f;
                teamRule.buildSpeedMultiplier = 3f;
            };

            generator = new MidanthaPlanetGenerator();

            cloudMeshLoader = () -> new MultiMesh(
                    //new DysonSphereMesh(this, 0.35f),
                    new DysonRingMesh(this, 2.300f, 0.28f, 729, Pal.darkMetal, Pal.darkerMetal),
                    new DysonRingMesh(this, 2.500f, 0.28f, 2941, Pal.darkMetal, Pal.darkerMetal),
                    new DysonRingMesh(this, 2.700f, 0.28f, 3834, Pal.darkMetal, Pal.darkerMetal),
                    new DysonRingMesh(this, 2.305f, 0.19f, 729, NHColor.darkEnrColor, NHColor.darkEnrColor),
                    new DysonRingMesh(this, 2.505f, 0.19f, 2941, NHColor.darkEnrColor, NHColor.darkEnrColor),
                    new DysonRingMesh(this, 2.705f, 0.19f, 3834, NHColor.darkEnrColor, NHColor.darkEnrColor)
            );

            iconColor = NHColor.darkEnrColor;
            landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
            atmosphereRadIn = 0.12f;
            atmosphereRadOut = 0.39f;
        }};
    }
}
