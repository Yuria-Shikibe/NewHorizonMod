package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g3d.VertexBatch3D;
import arc.math.Mathf;
import arc.math.geom.Ray;
import arc.math.geom.Vec3;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.graphics.g3d.PlanetGrid.Corner;
import mindustry.graphics.g3d.PlanetGrid.Ptile;
import mindustry.graphics.g3d.SunMesh;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.type.Weather.WeatherEntry;
import mindustry.world.meta.Env;
import newhorizon.content.blocks.SpecialBlock;
import newhorizon.expand.map.DysonRingMesh;
import newhorizon.expand.map.MidanthaPlanetGenerator;
import newhorizon.expand.map.RingWorldMesh;
import newhorizon.expand.map.RingWorldPlanet;

import static mindustry.graphics.g3d.PlanetRenderer.outlineColor;
import static mindustry.graphics.g3d.PlanetRenderer.outlineRad;

public class NHPlanets {
    public static Planet midantha, blueGiant;
    public static RingWorldPlanet ringWorld;
    private static final float sectorGridScale = 1.05f;
    private static final float ringGlowOffset = 0.008f;
    private static final float ringDragSensitivity = 0.25f;
    private static Sector lastRingSelection;
    private static final Vec3 lastRingCamPos = new Vec3();
    private static boolean ringCamPositionInitialized;

    public static void load() {
        blueGiant = new SelectableStar("blue-giant", 6.5f) {{
            bloom = true;
            visible = false;
            accessible = false;
            alwaysUnlocked = false;
            hasAtmosphere = false;
            updateLighting = false;
            camRadius = 16f;
            minZoom = 0.75f;
            maxZoom = 1.15f;
            clipRadius = 7.5f;
            lightColor = Color.valueOf("b9dcff");
            iconColor = Color.valueOf("78bfff");

            meshLoader = () -> new SunMesh(
                    this, 5,
                    6, 0.34, 1.65, 1.2, 1,
                    1.12f,
                    Color.valueOf("315cff"),
                    Color.valueOf("3987ff"),
                    Color.valueOf("61b7ff"),
                    Color.valueOf("8dd7ff"),
                    Color.valueOf("c5edff"),
                    Color.valueOf("f3fbff")
            );
        }};

        ringWorld = new RingWorldPlanet("ring-world", blueGiant, 45f, 46.25f, 7f, 120, 4) {{
            visible = false;
            accessible = false;
            alwaysUnlocked = false;
            allowCampaignRules = true;
            autoAssignPlanet = false;
            icon = "commandRally";
            iconColor = NHColor.lightSky;
            defaultCore = SpecialBlock.coreConflux;
            hasAtmosphere = false;
            updateLighting = false;
            bloom = true;
            clipRadius = 52f;
            camRadius = 3f;
            minZoom = 0.6f;
            maxZoom = 11f;
            panelScale = 0.985f;

            generator = new MidanthaPlanetGenerator();
            meshLoader = () -> new RingWorldMesh(this);
            gridMeshLoader = () -> buildGridMesh(outlineColor);

            ruleSetter = r -> {
                r.waves = true;
                r.waveTeam = Team.blue;
                r.placeRangeCheck = false;
                r.hideSpawns = false;
                r.waveSpacing = 60 * Time.toSeconds;
                r.initialWaveSpacing = 5f * Time.toMinutes;
                r.hideBannedBlocks = true;
                r.spawns = NHPostProcess.generate(0.8f, false);
                r.loadout = ItemStack.list(NHItems.titanium, 1000, NHItems.tungsten, 1000, NHItems.silicon, 1000, NHItems.zeta, 1000);

                Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
                teamRule.rtsAi = false;
                teamRule.unitBuildSpeedMultiplier = 1f;
                teamRule.buildSpeedMultiplier = 1f;
            };

            startSector = columns * (rows / 2) + columns / 2;
            defaultEnv = Env.terrestrial | NHContent.radioactive;
        }};

        midantha = new MidanthaPlanet("midantha", Planets.sun, 1f, 2) {{
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            allowCampaignRules = true;
            iconColor = NHColor.darkEnrColor;
            defaultCore = SpecialBlock.coreConflux;
            clipRadius = 2.8f;

            meshLoader = () -> new HexMesh(this, 6);
            gridMeshLoader = () -> MeshBuilder.buildPlanetGrid(grid, outlineColor, outlineRad * radius * sectorGridScale);

            ruleSetter = r -> {
                r.waves = true;
                r.waveTeam = Team.blue;
                r.placeRangeCheck = false;
                r.hideSpawns = false;
                r.waveSpacing = 60 * Time.toSeconds;
                r.initialWaveSpacing = 5f * Time.toMinutes;
                r.hideBannedBlocks = true;
                r.spawns = NHPostProcess.generate(0.8f, false);
                r.loadout = ItemStack.list(NHItems.titanium, 1000, NHItems.tungsten, 1000, NHItems.silicon, 1000, NHItems.zeta, 1000);
                NHSectorPresents.applyPrimaryBaseStartingLoadout(r);

                Rules.TeamRule teamRule = r.teams.get(r.defaultTeam);
                teamRule.rtsAi = false;
                teamRule.unitBuildSpeedMultiplier = 1f;
                teamRule.buildSpeedMultiplier = 1f;
                NHSectorPresents.applyCampaignMapRules(r);

                r.weather.removeAll(entry -> entry.weather == NHWeathers.quantumField);
                WeatherEntry quantumWeather = new WeatherEntry(NHWeathers.quantumField);
                quantumWeather.always = true;
                r.weather.add(quantumWeather);
            };
            generator = new MidanthaPlanetGenerator();

            cloudMeshLoader = () -> new MultiMesh(
                    new DysonRingMesh(this, 2.30f, 0.20f, 729, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.50f, 0.20f, 2941, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.70f, 0.20f, 3834, NHColor.darkEnr, NHColor.darkEnr.cpy().lerp(Pal.darkerMetal, 0.5f)),
                    new DysonRingMesh(this, 2.30f + ringGlowOffset, 0.10f, 729, NHColor.darkEnrFront, NHColor.darkEnrColor, true),
                    new DysonRingMesh(this, 2.50f + ringGlowOffset, 0.10f, 2941, NHColor.darkEnrFront, NHColor.darkEnrColor, true),
                    new DysonRingMesh(this, 2.70f + ringGlowOffset, 0.10f, 3834, NHColor.darkEnrFront, NHColor.darkEnrColor, true)
            );
            landCloudColor = atmosphereColor = Color.valueOf("3c1b8f");
            atmosphereRadIn = 0.12f;
            atmosphereRadOut = 0.45f;

            startSector = 1;
            defaultEnv = Env.terrestrial | NHContent.radioactive;
        }};
    }

    /** Applied at universeDrawBegin, after vanilla computes zoom distance but before the system is rendered. */
    public static void updateRingWorldCamera() {
        if (Vars.headless || ringWorld == null || Vars.ui == null || Vars.ui.planet == null ||
                !Vars.ui.planet.hasParent()) return;
        var cam = Vars.renderer.planets.cam;
        if (Vars.ui.planet.state.planet != ringWorld) {
            lastRingSelection = null;
            ringCamPositionInitialized = false;
            cam.near = 1f;
            return;
        }
        if (Vars.ui.planet.state.otherCamPos != null) {
            ringCamPositionInitialized = false;
            return;
        }
        cam.near = 6f;

        var dialog = Vars.ui.planet;
        Sector selected = dialog.selected != null && dialog.selected.planet == ringWorld ? dialog.selected : null;
        boolean changedSelection = selected != lastRingSelection;
        if (selected != null && changedSelection) {
            float length = dialog.state.camPos.len();
            ringWorld.getSectorCenter(selected.id, ringWorld.innerRadius, dialog.state.camPos);
            dialog.state.camPos.setLength(length);
        }

        ringWorld.constrainCamera(dialog.state.camPos);
        boolean sectorView = selected != null && dialog.state.zoom < 2.05f;
        if (sectorView && ringCamPositionInitialized && !changedSelection) {
            // PlanetDialog applies rotation at a scale intended for a radius-one
            // sphere. Reduce the resulting angular delta for the much larger ring.
            Vec3 target = Tmp.v31.set(dialog.state.camPos);
            dialog.state.camPos.set(lastRingCamPos).setLength(target.len()).slerp(target, ringDragSensitivity);
            ringWorld.constrainCamera(dialog.state.camPos);
        }
        if (sectorView) {
            lastRingCamPos.set(dialog.state.camPos);
            ringCamPositionInitialized = true;
        } else {
            ringCamPositionInitialized = false;
        }
        lastRingSelection = selected;
        var camera = dialog.planets.cam;

        if (sectorView) {
            // Sector mode: stand between the star and the selected inner wall,
            // then look outward. The star is now behind the camera.
            ringWorld.applyCampaignCamera(camera, dialog.state.camPos, dialog.state.zoom);
        } else {
            camera.position.set(ringWorld.position).add(dialog.state.camPos);
            camera.lookAt(ringWorld.position);
        }
        camera.update();

        if (sectorView) {
            // Vanilla resolves hovering before the custom inner-ring camera is
            // installed. Re-run picking with the camera that will be rendered.
            dialog.hovered = ringWorld.getSector(camera.getMouseRay());
        }

        if (sectorView) {
            dialog.state.camUp.set(0f, -1f, 0f);
            dialog.state.camDir.set(Tmp.v31.set(camera.direction).scl(-1f));
        } else {
            dialog.state.camUp.set(camera.up);
            dialog.state.camDir.set(camera.direction);
        }
        dialog.planets.projector.proj(camera.combined);
        dialog.planets.batch.proj(camera.combined);
    }

    /** Keeps sector overlays on the raised grid surface used by Midantha. */
    private static class MidanthaPlanet extends Planet {
        MidanthaPlanet(String name, Planet parent, float radius, int sectorSize) {
            super(name, parent, radius, sectorSize);
        }

        private float sectorSurfaceRadius(float offset) {
            return outlineRad * radius * sectorGridScale + offset * radius;
        }

        @Override
        public void fill(VertexBatch3D batch, Sector sector, Color color, float offset) {
            float radius = sectorSurfaceRadius(offset);
            for (int i = 0; i < sector.tile.corners.length; i++) {
                Corner corner = sector.tile.corners[i];
                Corner next = sector.tile.corners[(i + 1) % sector.tile.corners.length];
                batch.tri(Tmp.v31.set(corner.v).setLength(radius), Tmp.v32.set(next.v).setLength(radius), Tmp.v33.set(sector.tile.v).setLength(radius), color);
            }
        }

        @Override
        public void drawBorders(VertexBatch3D batch, Sector sector, Color base, float alpha) {
            Color color = Tmp.c1.set(base).a((base.a + 0.3f + Mathf.absin(Time.globalTime, 5f, 0.3f)) * alpha);
            float innerRadius = radius;
            float outerRadius = sectorSurfaceRadius(0.001f);

            for (int i = 0; i < sector.tile.corners.length; i++) {
                Corner corner = sector.tile.corners[i];
                Corner next = sector.tile.corners[(i + 1) % sector.tile.corners.length];

                Tmp.v31.set(corner.v).setLength(outerRadius);
                Tmp.v32.set(next.v).setLength(outerRadius);
                Tmp.v33.set(corner.v).setLength(innerRadius);
                batch.tri2(Tmp.v31, Tmp.v32, Tmp.v33, color);

                Tmp.v31.set(next.v).setLength(outerRadius);
                Tmp.v32.set(next.v).setLength(innerRadius);
                Tmp.v33.set(corner.v).setLength(innerRadius);
                batch.tri2(Tmp.v31, Tmp.v32, Tmp.v33, color);
            }
        }

        @Override
        public void drawSelection(VertexBatch3D batch, Sector sector, Color color, float stroke, float length) {
            float radius = sectorSurfaceRadius(length);

            for (int i = 0; i < sector.tile.corners.length; i++) {
                Corner next = sector.tile.corners[(i + 1) % sector.tile.corners.length];
                Corner corner = sector.tile.corners[i];

                next.v.scl(radius);
                corner.v.scl(radius);
                sector.tile.v.scl(radius);

                Tmp.v31.set(corner.v).sub(sector.tile.v).setLength(corner.v.dst(sector.tile.v) - stroke).add(sector.tile.v);
                Tmp.v32.set(next.v).sub(sector.tile.v).setLength(next.v.dst(sector.tile.v) - stroke).add(sector.tile.v);
                batch.quad(corner.v, next.v, Tmp.v32, Tmp.v31, color);

                sector.tile.v.scl(1f / radius);
                next.v.scl(1f / radius);
                corner.v.scl(1f / radius);
            }
        }
    }

    /** PlanetDialog only lists landable bodies; one non-playable display sector makes the star selectable. */
    private static class SelectableStar extends Planet {
        SelectableStar(String name, float radius) {
            super(name, null, radius);
            sectors.add(new Sector(this, Ptile.empty));
        }

        @Override
        public @Nullable Vec3 intersect(Ray ray, float radius) {
            if (Vars.ui.planet != null && Vars.ui.planet.state.planet == ringWorld) {
                return null;
            }
            return super.intersect(ray, radius);
        }
    }
}
