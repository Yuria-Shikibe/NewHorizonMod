package newhorizon.expand.units.probe;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.MultiPacker;
import mindustry.type.UnitType;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.util.graphic.DrawFunc;

import java.util.Arrays;

public final class ProbeFactoryRenderer {
    private static final ObjectMap<UnitType, BeamShape> beamShapes = new ObjectMap<>();

    private ProbeFactoryRenderer() {
    }

    public static float constructionRadius(ProbeEntity factory) {
        return Math.min(factory.factoryRadius * 0.62f, factory.selectedProduction == null
                ? 24f : factory.selectedProduction.hitSize * 0.65f + 14f);
    }

    public static Vec2 orbitPosition(ProbeEntity factory, int partIndex, float time, Vec2 result) {
        float seed = ((factory.id() * 31) & 1023) * (Mathf.pi * 2f / 1024f);
        float phase = seed + partIndex * 1.73f;
        float angle = seed * Mathf.radDeg + partIndex * 90f + time * (0.32f + partIndex * 0.023f)
                + Mathf.sin(time / 173f + phase) * 24f;
        float innerRadius = constructionRadius(factory) + 12f;
        float outerRadius = Math.max(innerRadius, factory.factoryRadius - 12f);
        float radius = Mathf.lerp(innerRadius, outerRadius,
                0.5f + Mathf.sin(time / 113f + phase) * 0.22f + Mathf.sin(time / 61f + phase * 1.7f) * 0.17f);
        return result.trns(angle, radius).add(factory.x + factory.factoryRadius, factory.y + factory.factoryRadius);
    }

    public static void cacheBeamShapes(MultiPacker packer) {
        beamShapes.clear();
        for (UnitType type : Vars.content.units()) {
            TextureRegion region = Core.atlas.find(type.name + "-outline", type.name);
            if (!region.found()) continue;
            PixmapRegion pixmap = packer.get(region);
            if (pixmap == null || pixmap.pixmap.isDisposed()) continue;
            BeamShape shape = new BeamShape(pixmap);
            if (shape.edges.length > 0) beamShapes.put(type, shape);
        }
    }

    public static void drawPartLink(ProbeEntity factory, float sourceX, float sourceY, float deployment) {
        if (deployment <= 0f || !factory.factory || ProbeFactoryLayout.moving(factory)
                || !ProbeFactoryParts.stationary(factory)) return;
        UnitType production = factory.selectedProduction;
        if (production == null || production.region == null || !production.region.found()) return;
        float centerX = factory.x + factory.factoryRadius;
        float centerY = factory.y + factory.factoryRadius;
        float radius = Math.max(production.hitSize * 0.5f, 4f);
        float previousLayer = Draw.z();
        float previousColor = Draw.getColorPacked();
        float previousMix = Draw.getMixColorPacked();
        float previousStroke = Lines.getStroke();
        Blending previousBlending = Draw.getBlend();
        Draw.z(Layer.buildBeam);
        Draw.blend();
        Draw.mixcol();
        Lines.stroke(1f, factory.team.color);
        Draw.alpha(Mathf.clamp(deployment));
        BeamShape shape = beamShapes.get(production);
        if (shape == null) {
            Drawf.buildBeam(sourceX, sourceY, centerX, centerY, radius);
        } else {
            TextureRegion region = production.outlineRegion != null && production.outlineRegion.found()
                    ? production.outlineRegion : production.region;
            float width = region.width * region.scl() * Draw.xscl;
            float height = region.height * region.scl() * Draw.yscl;
            shape.draw(sourceX, sourceY, centerX, centerY, width, height,
                    factory.rotation - 90f, Vars.renderer.animateShields);
        }
        Lines.stroke(previousStroke);
        Draw.blend(previousBlending);
        Draw.color(previousColor);
        Draw.mixcol(previousMix);
        Draw.z(previousLayer);
    }

    public static void draw(ProbeEntity factory) {
        if (!factory.isValid() || !factory.factory || ProbeFactoryLayout.moving(factory)
                || !ProbeFactoryParts.stationary(factory)) return;
        float radius = factory.factoryRadius;
        float centerX = factory.x + radius;
        float centerY = factory.y + radius;
        float top = factory.y + radius * 2f;
        float previousLayer = Draw.z();
        Draw.z(Layer.effect);
        Draw.color(factory.team.color, 0.025f);
        Fill.rect(centerX, centerY, radius * 2f, radius * 2f);
        Draw.color(factory.team.color, 0.28f);
        Lines.stroke(0.7f);
        Lines.rect(factory.x, factory.y, radius * 2f, radius * 2f);

        float progress = Mathf.clamp(factory.productionProgress);
        float ringRadius = constructionRadius(factory);
        Draw.color(factory.team.color, 0.16f);
        Lines.circle(centerX, centerY, ringRadius + 7f);
        for (int corner = 0; corner < 4; corner++) {
            float cornerX = factory.x + (corner == 1 || corner == 2 ? radius * 2f : 0f);
            float cornerY = factory.y + (corner >= 2 ? radius * 2f : 0f);
            float inwardX = cornerX < centerX ? 1f : -1f;
            float inwardY = cornerY < centerY ? 1f : -1f;
            Draw.color(factory.team.color, 0.85f);
            Lines.stroke(1.5f);
            Lines.line(cornerX, cornerY + inwardY * 12f, cornerX, cornerY);
            Lines.line(cornerX, cornerY, cornerX + inwardX * 12f, cornerY);
            Draw.color(factory.team.color, 0.6f);
            Lines.stroke(1f);
            Lines.arc(centerX, centerY, ringRadius, 0.16f, corner * 90f + Time.time * 0.35f, 12);
            if (corner != 0) drawGhost(factory, cornerX, cornerY);
        }
        if (progress > 0f) {
            Draw.color(factory.team.color, Color.white, 0.25f);
            Lines.stroke(1.6f);
            Lines.arc(centerX, centerY, ringRadius + 4f, progress, 90f, 48);
        }

        UnitType production = factory.selectedProduction;
        if (production != null && production.region != null && production.region.found()) {
            Draw.z(Layer.buildBeam + 1.1f);
            if (production.outlineRegion != null && production.outlineRegion.found()) {
                Draw.color(factory.team.color, 0.2f);
                Draw.mixcol(factory.team.color, 1f);
                Draw.rect(production.outlineRegion, centerX, centerY, factory.rotation - 90f);
            }
            Draw.color(factory.team.color, 0.12f);
            Draw.mixcol(factory.team.color, 1f);
            Draw.rect(production.region, centerX, centerY, factory.rotation - 90f);
            Draw.mixcol();
            Draw.draw(Layer.buildBeam + 1.2f, () -> {
                Drawf.construct(centerX, centerY, production.region, factory.team.color,
                        factory.rotation - 90f, progress, 0.8f, Time.time);
                Draw.reset();
            });
            drawProgress(factory, centerX, top);
        }
        Draw.reset();
        Draw.z(previousLayer);
    }

    private static void drawGhost(ProbeEntity factory, float x, float y) {
        Draw.color(factory.team.color, 0.3f);
        Lines.stroke(0.7f);
        Lines.circle(x, y, factory.hitSize * 0.65f);
        Draw.color(Color.white, 0.28f);
        Draw.mixcol(factory.team.color, 0.85f);
        Draw.rect(factory.type.region, x, y, factory.rotation - 90f);
        if (factory.type.cellRegion != null && factory.type.cellRegion.found()) {
            Draw.rect(factory.type.cellRegion, x, y, factory.rotation - 90f);
        }
        Draw.mixcol();
    }

    private static void drawProgress(ProbeEntity factory, float centerX, float top) {
        if (factory.factoryLabels == null) factory.factoryLabels = new Labels();
        Labels labels = factory.factoryLabels;
        labels.update(factory);
        float width = Mathf.clamp(factory.factoryRadius * 2f - 16f, 100f, 220f);
        Draw.z(Layer.overlayUI);
        Draw.color(Color.black, 0.65f);
        Fill.rect(centerX, top + 21f, width + 12f, 40f);
        Draw.color(factory.team.color, 0.38f);
        Lines.stroke(0.7f);
        Lines.line(centerX - width / 2f, top + 39f, centerX + width / 2f, top + 39f);
        drawBar(centerX, top + 18f, width, 3f, factory.productionProgress, factory.team.color, 0.95f);
        drawBar(centerX, top + 5f, width, 1.6f, ProbeColonyManager.resourceProgress(factory), factory.team.color, 0.6f);
        Draw.reset();
        DrawFunc.drawText(labels.title, centerX, top + 34f, 1.1f);
        DrawFunc.drawText(labels.progressText, centerX, top + 25f, 0.95f);
        DrawFunc.drawText(labels.resourceText, centerX, top + 12f, 0.95f);
    }

    private static void drawBar(float x, float y, float width, float height, float progress, Color color, float alpha) {
        Draw.color(color, 0.15f);
        Fill.rect(x, y, width, height);
        float filled = width * Mathf.clamp(progress);
        if (filled <= 0f) return;
        Draw.color(color, Color.white, 0.2f);
        Draw.alpha(alpha);
        Fill.rect(x - width / 2f + filled / 2f, y, filled, height);
        Draw.color(Color.white, alpha);
        Fill.rect(x - width / 2f + filled, y, 0.8f, height);
    }

    private static final class BeamShape {
        private final float[] edges;
        private final float[] visibleEdges;
        private final double[] angles;
        private final Vec2 first = new Vec2();
        private final Vec2 second = new Vec2();
        private int visibleSize;

        private BeamShape(PixmapRegion pixmap) {
            boolean[] opaque = new boolean[pixmap.width * pixmap.height];
            for (int row = 0; row < pixmap.height; row++) {
                for (int column = 0; column < pixmap.width; column++) {
                    opaque[row * pixmap.width + column] = pixmap.getA(column, row) > 0;
                }
            }
            FloatSeq outline = new FloatSeq();
            for (int row = 0; row <= pixmap.height; row++) {
                int start = 0;
                int previous = 0;
                for (int column = 0; column <= pixmap.width; column++) {
                    int direction = column == pixmap.width ? 0
                            : (row > 0 && opaque[(row - 1) * pixmap.width + column] ? 1 : 0)
                            - (row < pixmap.height && opaque[row * pixmap.width + column] ? 1 : 0);
                    if (direction == previous) continue;
                    if (previous != 0) {
                        addEdge(outline, pixmap, previous > 0 ? start : column, row,
                                previous > 0 ? column : start, row);
                    }
                    start = column;
                    previous = direction;
                }
            }
            for (int column = 0; column <= pixmap.width; column++) {
                int start = 0;
                int previous = 0;
                for (int row = 0; row <= pixmap.height; row++) {
                    int direction = row == pixmap.height ? 0
                            : (column > 0 && opaque[row * pixmap.width + column - 1] ? 1 : 0)
                            - (column < pixmap.width && opaque[row * pixmap.width + column] ? 1 : 0);
                    if (direction == previous) continue;
                    if (previous != 0) {
                        addEdge(outline, pixmap, column, previous > 0 ? row : start,
                                column, previous > 0 ? start : row);
                    }
                    start = row;
                    previous = direction;
                }
            }
            edges = outline.toArray();
            visibleEdges = new float[edges.length / 4 * 5];
            angles = new double[edges.length / 2];
        }

        private static void addEdge(FloatSeq outline, PixmapRegion pixmap, int startX, int startY,
                                    int endX, int endY) {
            outline.add(startX / (float) pixmap.width - 0.5f, 0.5f - startY / (float) pixmap.height);
            outline.add(endX / (float) pixmap.width - 0.5f, 0.5f - endY / (float) pixmap.height);
        }

        private void draw(float sourceX, float sourceY, float centerX, float centerY,
                          float width, float height, float rotation, boolean filled) {
            if (Mathf.zero(width) || Mathf.zero(height)) return;
            float cos = Mathf.cosDeg(rotation);
            float sin = Mathf.sinDeg(rotation);
            float localSourceX = ((sourceX - centerX) * cos + (sourceY - centerY) * sin) / width;
            float localSourceY = (-(sourceX - centerX) * sin + (sourceY - centerY) * cos) / height;
            visibleSize = 0;
            int angleCount = 0;
            for (int index = 0; index < edges.length; index += 4) {
                float edgeX = edges[index + 2] - edges[index];
                float edgeY = edges[index + 3] - edges[index + 1];
                float cross = edgeX * (localSourceY - edges[index + 1]) - edgeY * (localSourceX - edges[index]);
                if (cross >= 0f) continue;
                visibleEdges[visibleSize++] = edges[index];
                visibleEdges[visibleSize++] = edges[index + 1];
                visibleEdges[visibleSize++] = edgeX;
                visibleEdges[visibleSize++] = edgeY;
                visibleEdges[visibleSize++] = cross;
                angles[angleCount++] = Math.atan2(edges[index + 1] - localSourceY, edges[index] - localSourceX);
                angles[angleCount++] = Math.atan2(edges[index + 3] - localSourceY, edges[index + 2] - localSourceX);
            }
            if (angleCount == 0) return;
            Arrays.sort(angles, 0, angleCount);
            int firstEdge = -1;
            int previousEdge = -1;
            boolean started = false;
            for (int index = 0; index < angleCount; index++) {
                double start = angles[index];
                double end = index + 1 < angleCount ? angles[index + 1] : angles[0] + Math.PI * 2d;
                if (end <= start) continue;
                int edge = closestEdge((start + end) * 0.5d, localSourceX, localSourceY);
                if (!started) {
                    firstEdge = edge;
                    started = true;
                } else if (!filled && (edge < 0) != (previousEdge < 0)) {
                    intersection(edge < 0 ? previousEdge : edge, start, localSourceX, localSourceY, first);
                    drawLine(sourceX, sourceY, centerX, centerY, width, height, cos, sin, first);
                }
                if (filled && edge >= 0) {
                    intersection(edge, start, localSourceX, localSourceY, first);
                    intersection(edge, end, localSourceX, localSourceY, second);
                    Fill.tri(sourceX, sourceY,
                            transformedX(centerX, width, height, cos, sin, first),
                            transformedY(centerY, width, height, cos, sin, first),
                            transformedX(centerX, width, height, cos, sin, second),
                            transformedY(centerY, width, height, cos, sin, second));
                }
                previousEdge = edge;
            }
            if (!filled && started && (firstEdge < 0) != (previousEdge < 0)) {
                intersection(firstEdge < 0 ? previousEdge : firstEdge, angles[0], localSourceX, localSourceY, first);
                drawLine(sourceX, sourceY, centerX, centerY, width, height, cos, sin, first);
            }
        }

        private int closestEdge(double angle, float sourceX, float sourceY) {
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);
            double distance = Double.POSITIVE_INFINITY;
            int closest = -1;
            for (int index = 0; index < visibleSize; index += 5) {
                double cross = directionX * visibleEdges[index + 3] - directionY * visibleEdges[index + 2];
                if (cross >= 0d) continue;
                double segmentPosition = ((visibleEdges[index] - sourceX) * directionY
                        - (visibleEdges[index + 1] - sourceY) * directionX) / cross;
                double rayDistance = visibleEdges[index + 4] / cross;
                if (rayDistance <= 0d || segmentPosition < 0d || segmentPosition > 1d) continue;
                if (rayDistance < distance) {
                    closest = index;
                    distance = rayDistance;
                }
            }
            return closest;
        }

        private void intersection(int index, double angle, float sourceX, float sourceY, Vec2 result) {
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);
            double cross = directionX * visibleEdges[index + 3] - directionY * visibleEdges[index + 2];
            if (Math.abs(cross) < 1e-12d) {
                float startX = visibleEdges[index];
                float startY = visibleEdges[index + 1];
                float endX = startX + visibleEdges[index + 2];
                float endY = startY + visibleEdges[index + 3];
                float startDistance = Mathf.dst2(sourceX, sourceY, startX, startY);
                float endDistance = Mathf.dst2(sourceX, sourceY, endX, endY);
                result.set(startDistance <= endDistance ? startX : endX,
                        startDistance <= endDistance ? startY : endY);
                return;
            }
            float along = Mathf.clamp((float) (((visibleEdges[index] - sourceX) * directionY
                    - (visibleEdges[index + 1] - sourceY) * directionX) / cross));
            result.set(visibleEdges[index] + visibleEdges[index + 2] * along,
                    visibleEdges[index + 1] + visibleEdges[index + 3] * along);
        }

        private static void drawLine(float sourceX, float sourceY, float centerX, float centerY,
                                     float width, float height, float cos, float sin, Vec2 point) {
            Lines.line(sourceX, sourceY, transformedX(centerX, width, height, cos, sin, point),
                    transformedY(centerY, width, height, cos, sin, point));
        }

        private static float transformedX(float centerX, float width, float height,
                                          float cos, float sin, Vec2 point) {
            return centerX + point.x * width * cos - point.y * height * sin;
        }

        private static float transformedY(float centerY, float width, float height,
                                          float cos, float sin, Vec2 point) {
            return centerY + point.x * width * sin + point.y * height * cos;
        }
    }

    public static final class Labels {
        private UnitType type;
        private int percent = -1;
        private int seconds = -1;
        private boolean ready;
        private float amount = -1f;
        private float requirement = -1f;
        private String title;
        private String progressText;
        private String resourceText;

        private void update(ProbeEntity factory) {
            UnitType production = factory.selectedProduction;
            if (type != production) {
                type = production;
                title = Core.bundle.format("mod.ui.probe-fabricating", production.localizedName);
            }
            int nextPercent = (int) (Mathf.clamp(factory.productionProgress) * 100f);
            int nextSeconds = Mathf.ceil(ProbeProductionPlan.buildTime(production) * (1f - Mathf.clamp(factory.productionProgress)) / Time.toSeconds);
            boolean nextReady = factory.productionRequirement > 0f && factory.productionContribution >= factory.productionRequirement;
            if (percent != nextPercent || seconds != nextSeconds || ready != nextReady) {
                percent = nextPercent;
                seconds = nextSeconds;
                ready = nextReady;
                progressText = ready ? Core.bundle.format("mod.ui.probe-build-progress", percent, seconds)
                        : Core.bundle.get("mod.ui.probe-waiting-resources");
            }
            float nextAmount = Math.round(Math.min(factory.productionContribution, factory.productionRequirement) * 10f) / 10f;
            if (amount != nextAmount || requirement != factory.productionRequirement) {
                amount = nextAmount;
                requirement = factory.productionRequirement;
                resourceText = Core.bundle.format("mod.ui.probe-resource-progress",
                        Strings.autoFixed(amount, 1), Strings.autoFixed(requirement, 1));
            }
        }
    }
}
