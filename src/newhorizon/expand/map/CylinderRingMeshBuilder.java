package newhorizon.expand.map;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.Tmp;
import arc.struct.Seq;

import java.nio.FloatBuffer;

public class CylinderRingMeshBuilder {
    private static final boolean packNormals =
            Core.gl30 != null && (Core.app.isMobile() || Core.graphics.getGLVersion().atLeast(3, 3));

    public static Mesh build(float radius, float height, int segments, Color color, Color color2) {
        return build(radius, height, segments, color, color2, 0f, false);
    }

    public static Mesh build(float radius, float height, int segments, Color color, Color color2, float colorPhase) {
        return build(radius, height, segments, color, color2, colorPhase, false);
    }

    public static Mesh build(float radius, float height, int segments, Color color, Color color2, float colorPhase, boolean energyRing) {
        int vertices = segments * 18;

        Seq<VertexAttribute> attributes = Seq.with(
                VertexAttribute.position3,
                packNormals ? VertexAttribute.packedNormal : VertexAttribute.normal,
                VertexAttribute.color
        );

        Mesh mesh = new Mesh(true, vertices, 0, attributes.toArray(VertexAttribute.class));
        mesh.getVerticesBuffer().limit(mesh.getVerticesBuffer().capacity());
        mesh.getVerticesBuffer().position(0);

        FloatBuffer buf = mesh.getVerticesBuffer();
        buf.clear();

        int stride = packNormals ? 5 : 7;
        float[] floats = new float[stride];

        float half = height / 2f;
        float panelCount = energyRing ? 48f : 36f;
        float gap = energyRing ? 0.028f : 0.045f;

        for (int i = 0; i < segments; i++) {
            float a1 = (float) i / segments * Mathf.PI2;
            float a2 = (float) (i + 1) / segments * Mathf.PI2;

            Vec3 p1 = new Vec3(Mathf.cos(a1) * radius, half, Mathf.sin(a1) * radius);
            Vec3 p2 = new Vec3(Mathf.cos(a2) * radius, half, Mathf.sin(a2) * radius);
            Vec3 p3 = new Vec3(Mathf.cos(a2) * radius, -half, Mathf.sin(a2) * radius);
            Vec3 p4 = new Vec3(Mathf.cos(a1) * radius, -half, Mathf.sin(a1) * radius);

            float innerR = radius * (energyRing ? 0.975f : 0.962f);

            Vec3 p5 = new Vec3(Mathf.cos(a1) * innerR, half, Mathf.sin(a1) * innerR);
            Vec3 p6 = new Vec3(Mathf.cos(a2) * innerR, half, Mathf.sin(a2) * innerR);
            Vec3 p7 = new Vec3(Mathf.cos(a2) * innerR, -half, Mathf.sin(a2) * innerR);
            Vec3 p8 = new Vec3(Mathf.cos(a1) * innerR, -half, Mathf.sin(a1) * innerR);

            float col1o = vertexColor(a1, 1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col2o = vertexColor(a2, 1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col3o = vertexColor(a2, -1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col4o = vertexColor(a1, -1f, color, color2, colorPhase, panelCount, gap, energyRing);

            float col1i = vertexColor(a1, 1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col2i = vertexColor(a2, 1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col3i = vertexColor(a2, -1f, color, color2, colorPhase, panelCount, gap, energyRing);
            float col4i = vertexColor(a1, -1f, color, color2, colorPhase, panelCount, gap, energyRing);

            Vec3 normalOuter = new Vec3(p1.x, 0, p1.z).nor();

            vert(buf, floats, p1, normalOuter, col1o);
            vert(buf, floats, p2, normalOuter, col2o);
            vert(buf, floats, p3, normalOuter, col3o);

            vert(buf, floats, p1, normalOuter, col1o);
            vert(buf, floats, p3, normalOuter, col3o);
            vert(buf, floats, p4, normalOuter, col4o);

            Vec3 normalUp = new Vec3(0, -1, 0);

            vert(buf, floats, p1, normalUp, col1o);
            vert(buf, floats, p2, normalUp, col2o);
            vert(buf, floats, p6, normalUp, col2i);
            vert(buf, floats, p1, normalUp, col1o);
            vert(buf, floats, p6, normalUp, col2i);
            vert(buf, floats, p5, normalUp, col1i);

            Vec3 normalDown = new Vec3(0, 1, 0);

            vert(buf, floats, p8, normalDown, col4i);
            vert(buf, floats, p7, normalDown, col3i);
            vert(buf, floats, p3, normalDown, col3o);
            vert(buf, floats, p8, normalDown, col4i);
            vert(buf, floats, p3, normalDown, col3o);
            vert(buf, floats, p4, normalDown, col4o);
        }

        mesh.getVerticesBuffer().limit(mesh.getVerticesBuffer().position());
        return mesh;
    }

    private static float vertexColor(float angle, float yNorm, Color color, Color color2, float colorPhase, float panelCount, float gap, boolean energyRing) {
        float panelT = angle / Mathf.PI2 * panelCount;
        float panelFrac = panelT - (int) panelT;

        if (panelFrac < gap || panelFrac > 1f - gap) {
            Tmp.c1.set(color).a(0f);
            return Tmp.c1.toFloatBits();
        }

        float inner = (panelFrac - gap) / (1f - 2f * gap);
        float edgeBright = 1f - Math.abs(inner - 0.5f) * 2f;
        edgeBright = Mathf.pow(edgeBright, energyRing ? 1.8f : 3.5f);

        float blend = 0.5f + 0.5f * Mathf.sin(angle * 2f + colorPhase);
        float stripe = 0.9f + 0.1f * Mathf.sin(angle * panelCount * 5f + colorPhase * 2.5f);

        Tmp.c1.set(color).lerp(color2, blend * 0.4f + edgeBright * 0.6f).mul(stripe);

        if (energyRing) {
            float rim = Mathf.pow(Math.abs(yNorm), 0.4f);
            Tmp.c1.a(Tmp.c1.a * (0.18f + 0.82f * rim) * (0.55f + 0.45f * edgeBright));
        } else {
            int panelIdx = (int) panelT;
            Tmp.c1.mul(panelIdx % 2 == 0 ? 1f : 0.78f);
            float rim = Mathf.pow(Math.abs(yNorm), 1.6f);
            Tmp.c1.lerp(color2, rim * 0.25f);
        }

        return Tmp.c1.toFloatBits();
    }

    private static void vert(FloatBuffer buf, float[] floats, Vec3 p, Vec3 normal, float color) {
        floats[0] = p.x;
        floats[1] = p.y;
        floats[2] = p.z;

        if (packNormals) {
            floats[3] = packNormal(normal.x, normal.y, normal.z);
            floats[4] = color;
        } else {
            floats[3] = normal.x;
            floats[4] = normal.y;
            floats[5] = normal.z;
            floats[6] = color;
        }

        buf.put(floats);
    }

    private static float packNormal(float x, float y, float z) {
        int xs = x < -1f / 512f ? 1 : 0;
        int ys = y < -1f / 512f ? 1 : 0;
        int zs = z < -1f / 512f ? 1 : 0;

        int vi =
                zs << 29 | ((int) (z * 511 + (zs << 9)) & 511) << 20 |
                        ys << 19 | ((int) (y * 511 + (ys << 9)) & 511) << 10 |
                        xs << 9 | ((int) (x * 511 + (xs << 9)) & 511);

        return Float.intBitsToFloat(vi);
    }
}
