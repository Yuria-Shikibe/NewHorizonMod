package newhorizon.content;

import arc.graphics.Color;
import arc.util.Time;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.ui.dialogs.PlanetDialog;
import mindustry.world.meta.Env;
import newhorizon.expand.map.DysonRingMesh;
import newhorizon.expand.map.MidanthaPlanetGenerator;

import static mindustry.graphics.g3d.PlanetRenderer.outlineColor;
import static mindustry.graphics.g3d.PlanetRenderer.outlineRad;

public class NHPlanets {
    public static Planet midantha;

    public static void load() {
        midantha = new Planet("midantha", Planets.sun, 1f, 2) {{
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            iconColor = NHColor.darkEnrColor;

            meshLoader = () -> new HexMesh(this, 6);
            gridMeshLoader = () -> MeshBuilder.buildPlanetGrid(grid, outlineColor, outlineRad * radius * 1.05f);

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
                    new DysonRingMesh(this, 2.30f, 0.20f, 729, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.50f, 0.20f, 2941, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.70f, 0.20f, 3834, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.302f, 0.10f, 729, NHColor.darkEnrFront, NHColor.darkEnrColor, true),
                    new DysonRingMesh(this, 2.502f, 0.10f, 2941, NHColor.darkEnrFront, NHColor.darkEnrColor, true),
                    new DysonRingMesh(this, 2.702f, 0.10f, 3834, NHColor.darkEnrFront, NHColor.darkEnrColor, true)
            );
            landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
            atmosphereRadIn = 0.12f;
            atmosphereRadOut = 0.45f;

            startSector = 1;
            defaultEnv = Env.terrestrial | NHContent.radioactive;
        }};
    }
}
