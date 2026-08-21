package newhorizon.expand.map;

import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.struct.ShortSeq;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.PlanetMesh;
import mindustry.graphics.g3d.PlanetParams;
import newhorizon.content.NHColor;

/** Ring-world structure with distance-based terrain detail and glowing reverse-side traces. */
public class RingWorldMesh extends PlanetMesh {
    private static final int bodySegments = 384;
    private static final float sqrt3 = Mathf.sqrt(3f);

    private Seq<Mesh> nearTerrain;
    private Seq<Mesh> farTerrain;

    public RingWorldMesh(RingWorldPlanet planet) {
        super(planet, buildStructure(planet), Shaders.unlit);
        nearTerrain = buildIndexedTerrain(planet, 5);
        farTerrain = Seq.with(buildTerrain(planet, 2));
    }

    public RingWorldMesh() {
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform) {
        super.render(params, projection, transform);

        Seq<Mesh> terrain = params.planet == planet && params.zoom < 2.35f ? nearTerrain : farTerrain;
        if (terrain == null) return;
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.apply();
        for (Mesh part : terrain) part.render(shader, Gl.triangles);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (nearTerrain != null) nearTerrain.each(Mesh::dispose);
        if (farTerrain != null) farTerrain.each(Mesh::dispose);
    }

    private static Mesh buildStructure(RingWorldPlanet planet) {
        FloatSeq vertices = new FloatSeq(70000 * 7);
        Color backing = Color.valueOf("09111e");
        Color reverse = Color.valueOf("070c15");
        Color rim = Color.valueOf("313d57");

//        cylinder(vertices, planet.innerRadius, -planet.campaignHalfWidth, planet.campaignHalfWidth, bodySegments, backing, true);
        cylinder(vertices, planet.outerRadius, -planet.halfWidth, planet.halfWidth, bodySegments, reverse, false);
        rim(vertices, planet.innerRadius, planet.outerRadius, planet.halfWidth, bodySegments, rim, true);
        rim(vertices, planet.innerRadius, planet.outerRadius, -planet.halfWidth, bodySegments, rim, false);

        addInnerShoulders(vertices, planet);
        addReverseTraces(vertices, planet);
        return mesh(vertices);
    }

    /** Near and far LODs sample the vanilla terrain palette on a flat inner wall. */
    private static Seq<Mesh> buildIndexedTerrain(RingWorldPlanet planet, int detailMultiplier) {
        planet.generator.seed = planet.generator.baseSeed;

        int columns = planet.columns * detailMultiplier;
        float hexRadius = Mathf.PI2 * planet.innerRadius / (columns * 1.5f);
        float rowSpacing = sqrt3 * hexRadius;
        int rows = Mathf.ceil(planet.campaignHalfWidth * 2f / rowSpacing) + 2;
        float surfaceRadius = planet.innerRadius;
        Seq<Mesh> result = new Seq<>();
        TerrainChunk chunk = new TerrainChunk();
        Vec3 source = new Vec3();
        Color terrain = new Color();

        for (int column = 0; column < columns; column++) {
            float u = column * 1.5f * hexRadius;
            float stagger = (column & 1) == 0 ? 0f : rowSpacing * 0.5f;

            for (int row = 0; row < rows; row++) {
                float v = -planet.campaignHalfWidth + row * rowSpacing + stagger;
                if (v > planet.campaignHalfWidth + 0.001f) continue;

                planet.getSourcePoint(u, v, source);
                if (planet.generator.skip(source)) continue;

                terrain.set(Color.white);
                planet.generator.getColor(source, terrain);
                terrain.a(1f);
                Vec3[] top = new Vec3[6];

                for (int corner = 0; corner < 6; corner++) {
                    float angle = corner * 60f * Mathf.degreesToRadians;
                    float cornerU = u + Mathf.cos(angle) * hexRadius * 0.972f;
                    float cornerV = Mathf.clamp(v + Mathf.sin(angle) * hexRadius * 0.972f,
                            -planet.campaignHalfWidth, planet.campaignHalfWidth);
                    top[corner] = planet.getSurfacePoint(cornerU, cornerV, surfaceRadius, new Vec3());
                }

                Vec3 normal = new Vec3(-Mathf.cos(u / planet.innerRadius), 0f, -Mathf.sin(u / planet.innerRadius));
                chunk.addSurface(top, normal, terrain);

                if (chunk.cells >= TerrainChunk.maxCells) {
                    result.add(chunk.build());
                    chunk = new TerrainChunk();
                }
            }
        }

        if (chunk.cells > 0) result.add(chunk.build());
        return result;
    }

    private static Mesh buildTerrain(RingWorldPlanet planet, int detailMultiplier) {
        planet.generator.seed = planet.generator.baseSeed;

        int columns = planet.columns * detailMultiplier;
        float hexRadius = Mathf.PI2 * planet.innerRadius / (columns * 1.5f);
        float rowSpacing = sqrt3 * hexRadius;
        int rows = Mathf.ceil(planet.campaignHalfWidth * 2f / rowSpacing) + 2;
        float surfaceRadius = planet.innerRadius;
        FloatSeq vertices = new FloatSeq(50000 * 7);

        Vec3 source = new Vec3();
        Color terrain = new Color();

        for (int column = 0; column < columns; column++) {
            float u = column * 1.5f * hexRadius;
            float stagger = (column & 1) == 0 ? 0f : rowSpacing * 0.5f;

            for (int row = 0; row < rows; row++) {
                float v = -planet.campaignHalfWidth + row * rowSpacing + stagger;
                if (v > planet.campaignHalfWidth + 0.001f) continue;

                planet.getSourcePoint(u, v, source);
                if (planet.generator.skip(source)) continue;

                terrain.set(Color.white);
                planet.generator.getColor(source, terrain);
                terrain.a(1f);
                float scale = 0.982f;

                Vec3[] top = new Vec3[6];
                for (int corner = 0; corner < 6; corner++) {
                    float angle = corner * 60f * Mathf.degreesToRadians;
                    float cornerU = u + Mathf.cos(angle) * hexRadius * scale;
                    float cornerV = Mathf.clamp(v + Mathf.sin(angle) * hexRadius * scale,
                            -planet.campaignHalfWidth, planet.campaignHalfWidth);
                    top[corner] = planet.getSurfacePoint(cornerU, cornerV, surfaceRadius, new Vec3());
                }

                Vec3 normal = new Vec3(-Mathf.cos(u / planet.innerRadius), 0f, -Mathf.sin(u / planet.innerRadius));
                topHex(vertices, top, normal, terrain);

            }
        }

        return mesh(vertices);
    }

    private static void topHex(FloatSeq vertices, Vec3[] corner, Vec3 normal, Color color) {
        triangle(vertices, corner[0], corner[1], corner[2], normal, color);
        triangle(vertices, corner[0], corner[2], corner[3], normal, color);
        triangle(vertices, corner[0], corner[3], corner[4], normal, color);
        triangle(vertices, corner[0], corner[4], corner[5], normal, color);
    }

    /** Metal continuation above and below the campaign surface. */
    private static void addInnerShoulders(FloatSeq vertices, RingWorldPlanet planet) {
        int columns = 120;
        float step = Mathf.PI2 / columns;
        Color base = Pal.darkerMetal.cpy().lerp(Color.valueOf("28344c"), 0.35f);
        Color backing = Color.valueOf("09111e");
        Color inset = base.cpy().mul(0.72f);

        for (int side = -1; side <= 1; side += 2) {
            float minY = side < 0 ? -planet.halfWidth + 0.08f : planet.campaignHalfWidth + 0.08f;
            float maxY = side < 0 ? -planet.campaignHalfWidth - 0.08f : planet.halfWidth - 0.08f;
            float minY2 = side < 0 ? -planet.halfWidth  : planet.campaignHalfWidth;
            float maxY2 = side < 0 ? -planet.campaignHalfWidth : planet.halfWidth;
            for (int column = 0; column < columns; column++) {
                float a0 = column * step;
                float a1 = column * step + step * 0.055f;
                float a2 = (column + 1f) * step - step * 0.055f;
                float a3 = (column + 1f) * step;
                float middle = (a1 + a2) * 0.5f;
                Vec3 normal = new Vec3(-Mathf.cos(middle), 0f, -Mathf.sin(middle));

                float pad = 0.02f;

                //边框1
                quad(vertices, point(a0, planet.innerRadius, minY2),
                        point(a3, planet.innerRadius, minY2),
                        point(a3, planet.innerRadius, minY),
                        point(a0, planet.innerRadius, minY), normal, backing);

                quad(vertices, point(a0, planet.innerRadius, maxY),
                        point(a3, planet.innerRadius, maxY),
                        point(a3, planet.innerRadius, maxY2),
                        point(a0, planet.innerRadius, maxY2), normal, backing);

                quad(vertices, point(a0, planet.innerRadius, minY - pad),
                        point(a1, planet.innerRadius, minY - pad),
                        point(a1, planet.innerRadius, maxY + pad),
                        point(a0, planet.innerRadius, maxY + pad), normal, backing);

                quad(vertices, point(a2, planet.innerRadius, minY - pad),
                        point(a3, planet.innerRadius, minY - pad),
                        point(a3, planet.innerRadius, maxY + pad),
                        point(a2, planet.innerRadius, maxY + pad), normal, backing);

                float angleInset = step * 0.20f;
                float yInset = (maxY - minY) * 0.20f;

                //边框2
                quad(vertices, point(a1, planet.innerRadius, minY),
                        point(a2, planet.innerRadius, minY),
                        point(a2, planet.innerRadius, minY + yInset),
                        point(a1, planet.innerRadius, minY + yInset), normal, base);

                quad(vertices, point(a1, planet.innerRadius, maxY - yInset),
                        point(a2, planet.innerRadius, maxY - yInset),
                        point(a2, planet.innerRadius, maxY),
                        point(a1, planet.innerRadius, maxY), normal, base);

                quad(vertices, point(a1, planet.innerRadius, minY + yInset - pad),
                        point(a1 + angleInset, planet.innerRadius, minY + yInset - pad),
                        point(a1 + angleInset, planet.innerRadius, maxY - yInset + pad),
                        point(a1, planet.innerRadius, maxY - yInset + pad), normal, base);

                quad(vertices, point(a2 - angleInset, planet.innerRadius, minY + yInset - pad),
                        point(a2, planet.innerRadius, minY + yInset - pad),
                        point(a2, planet.innerRadius, maxY - yInset + pad),
                        point(a2 - angleInset, planet.innerRadius, maxY - yInset + pad), normal, base);

                //内部
                quad(vertices, point(a1 + angleInset, planet.innerRadius, minY + yInset),
                        point(a2 - angleInset, planet.innerRadius, minY + yInset),
                        point(a2 - angleInset, planet.innerRadius, maxY - yInset),
                        point(a1 + angleInset, planet.innerRadius, maxY - yInset), normal, inset);
            }
        }
    }

    /** Restored initial reverse-side design: dark shell with blue-violet energy traces. */
    private static void addReverseTraces(FloatSeq vertices, RingWorldPlanet planet) {
        float radius = planet.outerRadius + 0.2f;
        Color primary = NHColor.lightSkyFront.cpy();
        Color secondary = NHColor.darkEnrFront.cpy().lerp(Color.white, 0.22f);

        for (int line = -4; line <= 4; line++) {
            float centerY = line / 4.5f * planet.halfWidth;
            float width = line == 0 ? 0.07f : 0.035f;
            traceBand(vertices, radius, centerY - width, centerY + width, bodySegments,
                    line % 2 == 0 ? primary : secondary);
        }

        int ribs = 48;
        float angularWidth = 0.018f;
        for (int i = 0; i < ribs; i++) {
            float angle = i / (float) ribs * Mathf.PI2;
            float span = planet.halfWidth * (0.28f + (i % 5) * 0.12f);
            float offset = ((i * 37) % 11 - 5) / 5f * (planet.halfWidth - span) * 0.65f;
            traceRib(vertices, radius, angle - angularWidth, angle + angularWidth,
                    Mathf.clamp(offset - span, -planet.halfWidth, planet.halfWidth),
                    Mathf.clamp(offset + span, -planet.halfWidth, planet.halfWidth),
                    i % 3 == 0 ? secondary : primary);
        }
    }

    private static void traceBand(FloatSeq vertices, float radius, float minY, float maxY, int segments, Color color) {
        for (int i = 0; i < segments; i++) {
            float a1 = i / (float) segments * Mathf.PI2;
            float a2 = (i + 1f) / segments * Mathf.PI2;
            Vec3 normal = new Vec3(Mathf.cos((a1 + a2) * 0.5f), 0f, Mathf.sin((a1 + a2) * 0.5f));
            quad(vertices, point(a1, radius, minY), point(a1, radius, maxY),
                    point(a2, radius, maxY), point(a2, radius, minY), normal, color);
        }
    }

    private static void traceRib(FloatSeq vertices, float radius, float a1, float a2,
                                 float minY, float maxY, Color color) {
        Vec3 normal = new Vec3(Mathf.cos((a1 + a2) * 0.5f), 0f, Mathf.sin((a1 + a2) * 0.5f));
        quad(vertices, point(a1, radius, minY), point(a1, radius, maxY),
                point(a2, radius, maxY), point(a2, radius, minY), normal, color);
    }

    /** Keeps each indexed VBO below the unsigned-short vertex limit. */
    private static class TerrainChunk {
        static final int maxCells = 5000;
        final FloatSeq vertices = new FloatSeq(maxCells * 6 * 7);
        final ShortSeq indices = new ShortSeq(maxCells * 12);
        int cells;

        void addSurface(Vec3[] top, Vec3 normal, Color color) {
            int start = cells * 6;
            for (Vec3 point : top) vertex(vertices, point, normal, color);

            index(start, 0, 1, 2);
            index(start, 0, 2, 3);
            index(start, 0, 3, 4);
            index(start, 0, 4, 5);
            cells++;
        }

        private void index(int start, int a, int b, int c) {
            indices.add((short) (start + a), (short) (start + b), (short) (start + c));
        }

        Mesh build() {
            Mesh mesh = new Mesh(true, vertices.size / 7, indices.size,
                    VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color);
            mesh.setVertices(vertices.items, 0, vertices.size);
            mesh.setIndices(indices.items, 0, indices.size);
            return mesh;
        }
    }

    private static Mesh mesh(FloatSeq vertices) {
        Mesh mesh = new Mesh(true, vertices.size / 7, 0,
                VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color);
        mesh.setVertices(vertices.items, 0, vertices.size);
        return mesh;
    }

    private static void cylinder(FloatSeq vertices, float radius, float minY, float maxY, int segments, Color color, boolean inward) {
        for (int i = 0; i < segments; i++) {
            float a1 = i / (float) segments * Mathf.PI2;
            float a2 = (i + 1f) / segments * Mathf.PI2;
            Vec3 low1 = point(a1, radius, minY);
            Vec3 low2 = point(a2, radius, minY);
            Vec3 high2 = point(a2, radius, maxY);
            Vec3 high1 = point(a1, radius, maxY);
            float middle = (a1 + a2) * 0.5f;
            Vec3 normal = new Vec3(Mathf.cos(middle), 0f, Mathf.sin(middle)).scl(inward ? -1f : 1f);
            if (inward) quad(vertices, low1, low2, high2, high1, normal, color);
            else quad(vertices, low1, high1, high2, low2, normal, color);
        }
    }

    private static void rim(FloatSeq vertices, float inner, float outer, float y, int segments, Color color, boolean top) {
        Vec3 normal = new Vec3(0f, top ? 1f : -1f, 0f);
        for (int i = 0; i < segments; i++) {
            float a1 = i / (float) segments * Mathf.PI2;
            float a2 = (i + 1f) / segments * Mathf.PI2;
            Vec3 inner1 = point(a1, inner, y), inner2 = point(a2, inner, y);
            Vec3 outer2 = point(a2, outer, y), outer1 = point(a1, outer, y);
            if (top) quad(vertices, inner1, inner2, outer2, outer1, normal, color);
            else quad(vertices, inner1, outer1, outer2, inner2, normal, color);
        }
    }

    private static Vec3 point(float angle, float radius, float y) {
        return new Vec3(Mathf.cos(angle) * radius, y, Mathf.sin(angle) * radius);
    }

    private static void quad(FloatSeq vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 normal, Color color) {
        triangle(vertices, a, b, c, normal, color);
        triangle(vertices, a, c, d, normal, color);
    }

    private static void triangle(FloatSeq vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 normal, Color color) {
        vertex(vertices, a, normal, color);
        vertex(vertices, b, normal, color);
        vertex(vertices, c, normal, color);
    }

    private static void vertex(FloatSeq vertices, Vec3 point, Vec3 normal, Color color) {
        vertices.add(point.x, point.y, point.z, normal.x);
        vertices.add(normal.y, normal.z, color.toFloatBits());
    }
}
