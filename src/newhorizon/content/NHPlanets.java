package newhorizon.content;

import arc.graphics.Color;
import arc.graphics.g3d.VertexBatch3D;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.graphics.g3d.PlanetGrid.Corner;
import mindustry.type.ItemStack;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.world.meta.Env;
import newhorizon.expand.map.DysonRingMesh;
import newhorizon.expand.map.MidanthaPlanetGenerator;

import static mindustry.graphics.g3d.PlanetRenderer.outlineColor;
import static mindustry.graphics.g3d.PlanetRenderer.outlineRad;

public class NHPlanets {
    public static Planet midantha;
    private static final float sectorGridScale = 1.05f;

    public static void load() {
        midantha = new MidanthaPlanet("midantha", Planets.sun, 1f, 2) {{
            visible = true;
            accessible = true;
            alwaysUnlocked = true;
            allowCampaignRules = true;
            iconColor = NHColor.darkEnrColor;

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
                NHSectorPresents.applyLandingPointMapRules(r);
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
}
