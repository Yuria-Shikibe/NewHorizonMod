package newhorizon.expand.units.probe;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import newhorizon.content.NHColor;
import newhorizon.expand.units.unitEntity.ProbeEntity;
import newhorizon.util.graphic.DrawFunc;

public final class ProbeFactoryRenderer {
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

    private static TextureRegion unitOutlineRegion(ProbeEntity factory) {
        UnitType production = factory.selectedProduction;
        if (production == null) return null;
        TextureRegion outline = production.outlineRegion != null && production.outlineRegion.found()
                ? production.outlineRegion : production.region;
        return outline != null && outline.found() ? outline : null;
    }

    private static Vec2 outlineIntersection(ProbeEntity factory, TextureRegion outline, float sourceX, float sourceY,
                                            Vec2 result) {
        float centerX = factory.x + factory.factoryRadius;
        float centerY = factory.y + factory.factoryRadius;
        float halfWidth = outline.width * outline.scl() * 0.5f;
        float halfHeight = outline.height * outline.scl() * 0.5f;
        float rotation = factory.rotation - 90f;
        float cos = Mathf.cosDeg(rotation);
        float sin = Mathf.sinDeg(rotation);
        float dx = sourceX - centerX;
        float dy = sourceY - centerY;
        float localX = dx * cos + dy * sin;
        float localY = -dx * sin + dy * cos;
        float scaleX = Math.abs(localX) < 0.001f ? Float.POSITIVE_INFINITY : halfWidth / Math.abs(localX);
        float scaleY = Math.abs(localY) < 0.001f ? Float.POSITIVE_INFINITY : halfHeight / Math.abs(localY);
        float scale = Math.min(1f, Math.min(scaleX, scaleY));
        float edgeX = localX * scale;
        float edgeY = localY * scale;
        return result.set(centerX + edgeX * cos - edgeY * sin, centerY + edgeX * sin + edgeY * cos);
    }

    public static void drawPartLink(ProbeEntity factory, float sourceX, float sourceY, float deployment) {
        TextureRegion outline = unitOutlineRegion(factory);
        if (deployment <= 0f || outline == null) return;
        float centerX = factory.x + factory.factoryRadius;
        float centerY = factory.y + factory.factoryRadius;
        float distance = Mathf.dst(sourceX, sourceY, centerX, centerY);
        if (distance <= 0.001f) return;
        float directionX = (sourceX - centerX) / distance;
        float directionY = (sourceY - centerY) / distance;
        float normalX = -directionY;
        float normalY = directionX;
        float halfWidth = Mathf.clamp(factory.factoryRadius * 0.018f, 1.2f, 2.5f);
        float firstX = sourceX + normalX * halfWidth;
        float firstY = sourceY + normalY * halfWidth;
        float secondX = sourceX - normalX * halfWidth;
        float secondY = sourceY - normalY * halfWidth;
        Vec2 firstEnd = outlineIntersection(factory, outline, firstX, firstY, Tmp.v1);
        Vec2 secondEnd = outlineIntersection(factory, outline, secondX, secondY, Tmp.v2);
        float previousLayer = Draw.z();
        float previousColor = Draw.getColorPacked();
        float previousMix = Draw.getMixColorPacked();
        Draw.z(Layer.effect - 0.02f);
        Draw.mixcol();
        float startColor = Tmp.c1.set(NHColor.ancientLightMid).a(0.1f * deployment).toFloatBits();
        float endColor = Tmp.c1.a(0.22f * deployment).toFloatBits();
        Fill.quad(firstX, firstY, startColor, secondX, secondY, startColor,
                secondEnd.x, secondEnd.y, endColor, firstEnd.x, firstEnd.y, endColor);
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
            if (production.outlineRegion != null && production.outlineRegion.found()) {
                Draw.color(factory.team.color, 0.2f);
                Draw.mixcol(factory.team.color, 1f);
                Draw.rect(production.outlineRegion, centerX, centerY, factory.rotation - 90f);
            }
            Draw.color(factory.team.color, 0.12f);
            Draw.mixcol(factory.team.color, 1f);
            Draw.rect(production.region, centerX, centerY, factory.rotation - 90f);
            Draw.mixcol();
            Draw.draw(Layer.effect + 0.01f, () -> {
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
