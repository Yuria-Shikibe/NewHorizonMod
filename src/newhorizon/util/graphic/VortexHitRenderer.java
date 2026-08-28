package newhorizon.util.graphic;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.struct.FloatSeq;
import arc.util.Disposable;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import newhorizon.content.NHShaders;
import newhorizon.content.NHShaders.ShieldQuantumBlurShader;
import newhorizon.expand.entities.SharedShieldField;
import newhorizon.expand.entities.SharedShieldFields;
import newhorizon.expand.entities.VortexEvent;
import arc.struct.Seq;

public class VortexHitRenderer implements Disposable {
    public static final float VORTEX_RENDER_LAYER = Layer.end + 1f;

    private final FrameBuffer mask = new FrameBuffer();
    private final FrameBuffer shield = new FrameBuffer();
    private final FrameBuffer glowNear = new FrameBuffer();
    private final FrameBuffer glowFar = new FrameBuffer();
    private final FrameBuffer blurTemp = new FrameBuffer();
    private final ShieldQuantumBlurShader blurHorizontal = new ShieldQuantumBlurShader("VFX_quantumShieldBlurH");
    private final ShieldQuantumBlurShader blurVertical = new ShieldQuantumBlurShader("VFX_quantumShieldBlurV");
    private final Seq<SharedShieldField> groups = new Seq<>(false, 16, SharedShieldField.class);
    private int width = -1, height = -1;

    public void update() {
        VortexEvent.update();
    }

    public boolean hasActiveField() {
        for (SharedShieldField field : SharedShieldFields.all()) {
            if (field.active()) return true;
        }
        return false;
    }

    public void draw() {
        if (Vars.headless || NHShaders.shieldQuantum == null || !hasActiveField()) return;

        resize();
        NHShaders.shieldQuantum.resetState();
        NHShaders.shieldQuantum.texture = mask.getTexture();
        fillShaderState();

        Draw.draw(VORTEX_RENDER_LAYER, () -> {
            // Submit all world geometry before changing the render target.
            Draw.flush();
            mask.begin(Color.clear);
            captureMask();
            Draw.flush();
            mask.end();

            renderShield();
            Draw.flush();
        });
    }

    private void captureMask() {
        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;

            for (var source : field.iterable()) {
                if (!(source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector projector)) continue;
                float radius = projector.displayRadius(source);
                if (radius <= 0.01f) continue;

                Draw.color(Color.white);
                Fill.poly(source.x, source.y, projector.sides, radius, projector.shieldRotation);
            }
        }
    }

    private void resize() {
        int w = Math.max(2, Core.graphics.getWidth());
        int h = Math.max(2, Core.graphics.getHeight());
        if (w == width && h == height) return;

        width = w;
        height = h;
        mask.resize(w, h);
        shield.resize(w, h);
        glowNear.resize(w, h);
        glowFar.resize(w, h);
        blurTemp.resize(w, h);
        clear(shield);
        clear(glowNear);
        clear(glowFar);
        clear(blurTemp);
    }

    private void renderShield() {
        // Intermediate buffers must receive exact RGBA values without blending.
        Blending.disabled.apply();
        shield.begin(Color.clear);
        mask.blit(NHShaders.shieldQuantum);
        shield.end();

        blurHorizontal.radius = 1f;
        blurTemp.begin();
        shield.blit(blurHorizontal);
        blurTemp.end();

        blurVertical.radius = 1f;
        glowNear.begin();
        blurTemp.blit(blurVertical);
        glowNear.end();

        blurHorizontal.radius = 3f;
        blurTemp.begin();
        glowNear.blit(blurHorizontal);
        blurTemp.end();

        blurVertical.radius = 3f;
        glowFar.begin();
        blurTemp.blit(blurVertical);
        glowFar.end();

        NHShaders.shieldQuantumComposite.glowNear = glowNear.getTexture();
        NHShaders.shieldQuantumComposite.glowFar = glowFar.getTexture();

        // Composite transparently over the already-rendered world.
        Blending.normal.apply();
        shield.blit(NHShaders.shieldQuantumComposite);
        Draw.flush();
        drawProjectorOutlines();
        Draw.flush();
    }

    /** Draw each boundary independently so enemy fields never share one mask edge. */
    private void drawProjectorOutlines() {
        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;
            drawFieldOutline(field);
        }
        Draw.reset();
    }

    /** Draw only the outside edges of the union of one shared field's polygons. */
    private void drawFieldOutline(SharedShieldField field) {
        Lines.stroke(1.5f);
        if (field.team() == null) return;
        Draw.color(field.team().color);
        Seq<Building> sources = new Seq<>(false, field.sourceCount(), Building.class);
        for (var source : field.iterable()) {
            if (source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector && source.isValid()) sources.add(source);
        }
        FloatSeq splits = new FloatSeq(32);
        for (Building source : sources) {
            var projector = (newhorizon.expand.block.defence.QuantumVortexProjector)source.block;
            int sides = Math.max(projector.sides, 3);
            float radius = projector.displayRadius(source);
            if (radius <= 0.01f) continue;
            for (int edge = 0; edge < sides; edge++) {
                float ax = vertexX(source, projector, edge, sides, radius);
                float ay = vertexY(source, projector, edge, sides, radius);
                float bx = vertexX(source, projector, (edge + 1) % sides, sides, radius);
                float by = vertexY(source, projector, (edge + 1) % sides, sides, radius);
                splits.clear();
                splits.add(0f);
                splits.add(1f);
                for (Building other : sources) {
                    if (other == source) continue;
                    var otherProjector = (newhorizon.expand.block.defence.QuantumVortexProjector)other.block;
                    int otherSides = Math.max(otherProjector.sides, 3);
                    float otherRadius = otherProjector.displayRadius(other);
                    // Add the exact parameter interval covered by the other
                    // convex polygon. This handles collinear/shared edges,
                    // which cannot be found reliably by line intersection.
                    addCoverageInterval(ax, ay, bx, by, other, otherProjector, otherSides, otherRadius, splits);
                }
                splits.sort();
                for (int i = 0; i + 1 < splits.size; i++) {
                    float t0 = splits.get(i), t1 = splits.get(i + 1);
                    if (t1 - t0 < 0.0001f) continue;
                    float tm = (t0 + t1) * 0.5f;
                    float mx = Mathf.lerp(ax, bx, tm), my = Mathf.lerp(ay, by, tm);
                    boolean covered = false;
                    for (Building other : sources) {
                        if (other == source) continue;
                        var otherProjector = (newhorizon.expand.block.defence.QuantumVortexProjector)other.block;
                        if (Intersector.isInRegularPolygon(Math.max(otherProjector.sides, 3), other.x, other.y,
                                otherProjector.displayRadius(other), otherProjector.shieldRotation, mx, my)) {
                            covered = true;
                            break;
                        }
                    }
                    if (!covered) {
                        Lines.line(Mathf.lerp(ax, bx, t0), Mathf.lerp(ay, by, t0),
                                Mathf.lerp(ax, bx, t1), Mathf.lerp(ay, by, t1));
                    }
                }
            }
        }
    }

    private static float vertexX(Building source, newhorizon.expand.block.defence.QuantumVortexProjector projector,
                                 int index, int sides, float radius) {
        float angle = projector.shieldRotation + index * 360f / sides;
        return source.x + Mathf.cosDeg(angle) * radius;
    }

    private static float vertexY(Building source, newhorizon.expand.block.defence.QuantumVortexProjector projector,
                                 int index, int sides, float radius) {
        float angle = projector.shieldRotation + index * 360f / sides;
        return source.y + Mathf.sinDeg(angle) * radius;
    }

    private static void addCoverageInterval(float ax, float ay, float bx, float by, Building polygon,
                                             newhorizon.expand.block.defence.QuantumVortexProjector projector,
                                             int sides, float radius, FloatSeq splits) {
        float dx = bx - ax, dy = by - ay;
        float low = 0f, high = 1f;
        float lineLength2 = dx * dx + dy * dy;
        if (lineLength2 <= 0.000001f) return;
        final float epsilon = 0.001f;
        for (int i = 0; i < sides; i++) {
            float x0 = vertexX(polygon, projector, i, sides, radius);
            float y0 = vertexY(polygon, projector, i, sides, radius);
            float x1 = vertexX(polygon, projector, (i + 1) % sides, sides, radius);
            float y1 = vertexY(polygon, projector, (i + 1) % sides, sides, radius);
            float ex = x1 - x0, ey = y1 - y0;
            float c = ex * (ay - y0) - ey * (ax - x0);
            float d = ex * dy - ey * dx;
            if (Math.abs(d) < 0.00001f) {
                if (Math.abs(c) > epsilon) continue;

                // Collinear edges need special handling. Treating the whole
                // edge as covered drops valid outer edges when two polygons
                // lie on opposite sides, or when their coincident edge is
                // shorter than the source edge. Project the other polygon
                // onto this line and only cover the interval it actually
                // occupies, provided its interior is on the source's inside
                // side of the edge.
                // Both regular polygons are counter-clockwise, so the source
                // interior is to the left of its directed edge. The other
                // polygon's center tells us which side its interior occupies;
                // this remains valid even when its coincident edge is shorter
                // and does not contain the source edge midpoint.
                float centerSide = dx * (polygon.y - ay) - dy * (polygon.x - ax);
                if (centerSide <= epsilon) return;

                float minT = Float.POSITIVE_INFINITY, maxT = Float.NEGATIVE_INFINITY;
                for (int k = 0; k < sides; k++) {
                    float vx = vertexX(polygon, projector, k, sides, radius) - ax;
                    float vy = vertexY(polygon, projector, k, sides, radius) - ay;
                    float t = (vx * dx + vy * dy) / lineLength2;
                    minT = Math.min(minT, t);
                    maxT = Math.max(maxT, t);
                }
                splits.add(Mathf.clamp(minT));
                splits.add(Mathf.clamp(maxT));
                return;
            } else if (d > 0f) {
                low = Math.max(low, (-epsilon - c) / d);
            } else {
                high = Math.min(high, (-epsilon - c) / d);
            }
            if (low > high) return;
        }
        splits.add(Mathf.clamp(low));
        splits.add(Mathf.clamp(high));
    }

    private void clear(FrameBuffer buffer) {
        buffer.begin(Color.clear);
        buffer.end();
    }

    private void fillShaderState() {
        NHShaders.ShieldQuantumShader shader = NHShaders.shieldQuantum;
        groups.clear();

        for (SharedShieldField field : SharedShieldFields.all()) {
            if (!field.active()) continue;
            int group = groups.size;
            groups.add(field);

            for (var source : field.iterable()) {
                if (!(source.block instanceof newhorizon.expand.block.defence.QuantumVortexProjector projector)) continue;
                float radius = projector.displayRadius(source);
                if (radius <= 0.01f || shader.getFieldCount() >= 24) continue;

                shader.addField(source.x, source.y, radius, source.team.color, field.hit, group);
            }
        }

        for (VortexEvent event : VortexEvent.active) {
            if (event == null || shader.eventCount >= 24 || event.field == null) continue;
            int group = groups.indexOf(event.field, true);
            if (group < 0 || group >= 24) continue;
            shader.addEvent(event.x, event.y, event.time, group);
        }
    }

    @Override
    public void dispose() {
        mask.dispose();
        shield.dispose();
        glowNear.dispose();
        glowFar.dispose();
        blurTemp.dispose();
    }
}
