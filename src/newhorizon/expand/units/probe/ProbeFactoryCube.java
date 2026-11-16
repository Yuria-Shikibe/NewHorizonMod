package newhorizon.expand.units.probe;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import newhorizon.content.NHColor;
import newhorizon.expand.units.unitEntity.ProbeEntity;

public final class ProbeFactoryCube {
    private static final int[][] FACES = {
            {0, 2, 6, 4}, {1, 5, 7, 3},
            {0, 4, 5, 1}, {2, 3, 7, 6},
            {0, 1, 3, 2}, {4, 6, 7, 5}
    };
    private static final float BACK_LINK_LAYER = Layer.buildBeam + 1.02f;
    private static final float BACK_CUBE_LAYER = Layer.buildBeam + 1.06f;
    private static final float FRONT_LINK_LAYER = Layer.buildBeam + 1.23f;
    private static final float FRONT_CUBE_LAYER = Layer.buildBeam + 1.26f;
    private static final Color BACK_LINK_COLOR = NHColor.ancient.cpy().lerp(NHColor.ancientLightMid, 0.1f);
    private static final Color FRONT_LINK_COLOR = NHColor.ancient.cpy().lerp(NHColor.ancientLightMid, 0.4f);

    private final Vec3[] vertices = new Vec3[8];
    private final Vec2[] projected = new Vec2[8];
    private final Mat3D rotation = new Mat3D();
    private final Vec3 source = new Vec3();
    private final Seq<Link> links = new Seq<>(8);
    private int linkCount;
    private int frontMask;
    private int factoryId = Integer.MIN_VALUE;
    private float phase;
    private float updateTime = Float.NaN;
    private float centerX;
    private float centerY;
    private float halfSize;
    private UnitType production;

    public ProbeFactoryCube() {
        for (int index = 0; index < vertices.length; index++) {
            vertices[index] = new Vec3();
            projected[index] = new Vec2();
        }
    }

    public void update(ProbeEntity factory) {
        float nextCenterX = factory.x + factory.factoryRadius;
        float nextCenterY = factory.y + factory.factoryRadius;
        if (updateTime == Time.time && centerX == nextCenterX && centerY == nextCenterY
                && production == factory.selectedProduction) return;
        updateTime = Time.time;
        centerX = nextCenterX;
        centerY = nextCenterY;
        if (production != factory.selectedProduction) {
            production = factory.selectedProduction;
            TextureRegion region = production.outlineRegion != null && production.outlineRegion.found()
                    ? production.outlineRegion : production.region;
            float width = region.width * region.scl();
            float height = region.height * region.scl();
            halfSize = Math.max(18f, Mathf.sqrt(width * width + height * height) * 0.5f + 6f);
        }
        if (factoryId != factory.id()) {
            factoryId = factory.id();
            phase = Mathf.randomSeed(factoryId, 360f);
        }
        rotation.idt().rotate(Vec3.Z, phase * 0.61f + Time.time * 0.11f)
                .rotate(Vec3.X, 28f + Time.time * 0.17f)
                .rotate(Vec3.Y, 37f + phase + Time.time * 0.23f);
        float cameraDistance = halfSize * 8f;
        frontMask = 0;
        for (int index = 0; index < vertices.length; index++) {
            Vec3 vertex = vertices[index].set((index & 1) == 0 ? -halfSize : halfSize,
                    (index & 2) == 0 ? -halfSize : halfSize, (index & 4) == 0 ? -halfSize : halfSize);
            Mat3D.rot(vertex, rotation);
            if (vertex.z >= 0f) frontMask |= 1 << index;
            float perspective = cameraDistance / (cameraDistance - vertex.z);
            projected[index].set(centerX + vertex.x * perspective, centerY + vertex.y * perspective);
        }
    }

    public void draw() {
        for (int index = 0; index < vertices.length; index++) {
            for (int axis = 1; axis <= 4; axis <<= 1) {
                if ((index & axis) == 0) drawEdge(index, index | axis);
            }
            float depth = vertices[index].z;
            Draw.z(depth < 0f ? BACK_CUBE_LAYER : FRONT_CUBE_LAYER);
            Draw.color(NHColor.ancient, NHColor.ancientLightMid, depth > 0f ? 0.7f : 0.15f);
            Draw.alpha(depth < 0f ? 0.45f : 0.95f);
            Fill.square(projected[index].x, projected[index].y, depth < 0f ? 0.75f : 1.1f, 45f);
        }
    }

    public void drawLink(float sourceX, float sourceY, float deployment) {
        beginLinks();
        addLink(sourceX, sourceY, deployment, true);
        drawLinks();
    }

    public void beginLinks() {
        linkCount = 0;
    }

    public void addLink(float sourceX, float sourceY, float deployment, boolean filled) {
        if (deployment <= 0f) return;
        Mat3D.unrotate(source.set(sourceX - centerX, sourceY - centerY, 0f), rotation);
        int faceIndex = source.x < 0f ? 0 : 1;
        float largest = Math.abs(source.x);
        if (Math.abs(source.y) > largest) {
            largest = Math.abs(source.y);
            faceIndex = source.y < 0f ? 2 : 3;
        }
        if (Math.abs(source.z) > largest) faceIndex = source.z < 0f ? 4 : 5;
        int[] face = FACES[faceIndex];
        int nearest = 0;
        float distance = Float.MAX_VALUE;
        for (int index = 0; index < face.length; index++) {
            float candidate = projected[face[index]].dst2(sourceX, sourceY);
            if (candidate < distance) {
                distance = candidate;
                nearest = index;
            }
        }
        if (linkCount == links.size) links.add(new Link());
        Link link = links.get(linkCount++);
        link.sourceX = sourceX;
        link.sourceY = sourceY;
        link.corner = face[nearest];
        link.next = face[(nearest + 1) % face.length];
        link.previous = face[(nearest + face.length - 1) % face.length];
        link.vertexMask = (1 << link.corner) | (1 << link.next) | (1 << link.previous);
        link.alpha = Mathf.clamp(deployment);
        link.filled = filled;
    }

    public void drawLinks() {
        if (Vars.renderer != null && Vars.renderer.animateShields) {
            Draw.z(BACK_LINK_LAYER);
            Draw.color(NHColor.ancient);
            for (int index = 0; index < linkCount; index++) {
                Link link = links.get(index);
                if (!link.filled) continue;
                Draw.alpha(link.alpha * 0.055f);
                Fill.tri(link.sourceX, link.sourceY, projected[link.corner].x, projected[link.corner].y,
                        projected[link.next].x, projected[link.next].y);
                Fill.tri(link.sourceX, link.sourceY, projected[link.previous].x, projected[link.previous].y,
                        projected[link.corner].x, projected[link.corner].y);
            }
        }
        Lines.stroke(0.75f);
        drawLinkEdges(false);
        drawLinkEdges(true);
    }

    private void drawLinkEdges(boolean front) {
        int visibleMask = front ? frontMask : ~frontMask & 255;
        Draw.z(front ? FRONT_LINK_LAYER : BACK_LINK_LAYER);
        Draw.color(front ? FRONT_LINK_COLOR : BACK_LINK_COLOR);
        for (int index = 0; index < linkCount; index++) {
            Link link = links.get(index);
            int targets = link.vertexMask & visibleMask;
            if (targets == 0) continue;
            Draw.alpha(link.alpha * (front ? 0.65f : 0.35f));
            while (targets != 0) {
                int corner = Integer.numberOfTrailingZeros(targets);
                Lines.line(link.sourceX, link.sourceY, projected[corner].x, projected[corner].y);
                targets &= targets - 1;
            }
        }
    }

    private void drawEdge(int firstIndex, int secondIndex) {
        Vec3 first = vertices[firstIndex];
        Vec3 second = vertices[secondIndex];
        Vec2 firstPoint = projected[firstIndex];
        Vec2 secondPoint = projected[secondIndex];
        if (first.z * second.z < 0f) {
            float fraction = -first.z / (second.z - first.z);
            float splitX = centerX + Mathf.lerp(first.x, second.x, fraction);
            float splitY = centerY + Mathf.lerp(first.y, second.y, fraction);
            drawEdgeSegment(firstPoint.x, firstPoint.y, splitX, splitY, first.z * 0.5f);
            drawEdgeSegment(splitX, splitY, secondPoint.x, secondPoint.y, second.z * 0.5f);
        } else {
            drawEdgeSegment(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, (first.z + second.z) * 0.5f);
        }
    }

    private void drawEdgeSegment(float firstX, float firstY, float secondX, float secondY, float depth) {
        float light = Mathf.clamp(0.5f + depth / (halfSize * 3.464102f));
        Draw.z(depth < 0f ? BACK_CUBE_LAYER : FRONT_CUBE_LAYER);
        Draw.color(NHColor.ancient, NHColor.ancientLightMid, light);
        Draw.alpha(Mathf.lerp(0.3f, 0.95f, light));
        Lines.stroke(Mathf.lerp(0.7f, 1.3f, light));
        Lines.line(firstX, firstY, secondX, secondY);
    }

    private static final class Link {
        private float sourceX;
        private float sourceY;
        private float alpha;
        private int corner;
        private int next;
        private int previous;
        private int vertexMask;
        private boolean filled;
    }
}
