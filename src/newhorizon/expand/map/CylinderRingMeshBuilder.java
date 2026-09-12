package newhorizon.expand.map;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Tmp;

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
        segments = Math.max(3, segments);
        int vertices = segments * 24;

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
        float innerR = radius * (energyRing ? 0.975f : 0.962f);

        Vec3 p1 = new Vec3(), p2 = new Vec3(), p3 = new Vec3(), p4 = new Vec3();
        Vec3 p5 = new Vec3(), p6 = new Vec3(), p7 = new Vec3(), p8 = new Vec3();
        Vec3 normalOuter = new Vec3(), normalInner = new Vec3();
        Vec3 normalUp = new Vec3(0f, 1f, 0f), normalDown = new Vec3(0f, -1f, 0f);

        for (int segment = 0; segment < segments; segment++) {
            float a1 = (float) segment / segments * Mathf.PI2;
            float a2 = (float) (segment + 1) / segments * Mathf.PI2;

            setPoint(p1, a1, radius, half);
            setPoint(p2, a2, radius, half);
            setPoint(p3, a2, radius, -half);
            setPoint(p4, a1, radius, -half);
            setPoint(p5, a1, innerR, half);
            setPoint(p6, a2, innerR, half);
            setPoint(p7, a2, innerR, -half);
            setPoint(p8, a1, innerR, -half);

            float col1 = vertexColor(a1, color, color2, colorPhase, energyRing);
            float col2 = vertexColor(a2, color, color2, colorPhase, energyRing);

            float middle = (a1 + a2) * 0.5f;
            normalOuter.set(Mathf.cos(middle), 0f, Mathf.sin(middle));
            normalInner.set(normalOuter).scl(-1f);

            // Outer and inner walls make the ring visible from either side with back-face culling enabled.
            triangle(buf, floats, p1, p2, p3, normalOuter, col1, col2, col2);
            triangle(buf, floats, p1, p3, p4, normalOuter, col1, col2, col1);
            triangle(buf, floats, p5, p8, p7, normalInner, col1, col1, col2);
            triangle(buf, floats, p5, p7, p6, normalInner, col1, col2, col2);

            // Top and bottom surfaces use outward-facing winding and normals.
            triangle(buf, floats, p1, p6, p2, normalUp, col1, col2, col2);
            triangle(buf, floats, p1, p5, p6, normalUp, col1, col1, col2);
            triangle(buf, floats, p8, p3, p7, normalDown, col1, col2, col2);
            triangle(buf, floats, p8, p4, p3, normalDown, col1, col1, col2);
        }

        mesh.getVerticesBuffer().limit(mesh.getVerticesBuffer().position());
        return mesh;
    }

    private static void setPoint(Vec3 point, float angle, float radius, float y) {
        point.set(Mathf.cos(angle) * radius, y, Mathf.sin(angle) * radius);
    }

    private static float vertexColor(float angle, Color color, Color color2, float colorPhase, boolean energyRing) {
        float blend = 0.5f + 0.5f * Mathf.sin(angle * 2f + colorPhase);
        float stripe = 0.94f + 0.06f * Mathf.sin(angle * 8f + colorPhase * 2.5f);

        Tmp.c1.set(color).lerp(color2, blend).mul(stripe);

        if (energyRing) {
            Tmp.c1.a(Tmp.c1.a * 0.82f);
        }

        return Tmp.c1.toFloatBits();
    }

    private static void triangle(FloatBuffer buf, float[] floats, Vec3 a, Vec3 b, Vec3 c, Vec3 normal, float colorA, float colorB, float colorC) {
        vert(buf, floats, a, normal, colorA);
        vert(buf, floats, b, normal, colorB);
        vert(buf, floats, c, normal, colorC);
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
