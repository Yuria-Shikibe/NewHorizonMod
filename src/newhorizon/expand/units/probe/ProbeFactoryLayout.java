package newhorizon.expand.units.probe;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import mindustry.world.Tile;
import newhorizon.expand.units.unitEntity.ProbeEntity;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public final class ProbeFactoryLayout {
    private static final float RESOURCE_DISTANCE = 10f * tilesize;
    public static final float GAP = 48f;

    private ProbeFactoryLayout() {
    }

    public static boolean moving(ProbeEntity factory) {
        return factory.factory && factory.miningTargetTile != -1;
    }

    private static float assistantClearance(ProbeEntity assistant, ProbeEntity factory) {
        float spacing = assistant.productionAssistCount * (assistant.hitSize + tilesize);
        return Math.max(assistant.hitSize * 0.5f + tilesize * 2f,
                (spacing - factory.factoryRadius * 8f) / (Mathf.pi * 2f));
    }

    public static Vec2 assistantOrbitPosition(ProbeEntity assistant, ProbeEntity factory, float time, Vec2 result) {
        return assistantOrbitPosition(assistant, factory, time, result, null);
    }

    public static Vec2 assistantOrbitPosition(ProbeEntity assistant, ProbeEntity factory, float time, Vec2 result, Vec2 velocity) {
        float radius = factory.factoryRadius;
        float clearance = assistantClearance(assistant, factory);
        float segment = radius * 2f + Mathf.pi * clearance * 0.5f;
        float perimeter = segment * 4f;
        float speed = Math.min(1.2f, factory.type.speed * 0.4f);
        float distance = Mathf.mod(time * speed + factory.id() * 17f
                + perimeter * assistant.productionAssistIndex / Math.max(1, assistant.productionAssistCount), perimeter);
        int side = (int) (distance / segment);
        float offset = distance - side * segment;
        if (offset < radius * 2f) {
            result.set(radius + clearance, offset - radius);
            if (velocity != null) velocity.set(0f, speed);
        } else {
            float angle = (offset - radius * 2f) / clearance * Mathf.radDeg;
            result.trns(angle, clearance).add(radius, radius);
            if (velocity != null) velocity.trns(angle + 90f, speed);
        }
        if (velocity != null) velocity.rotate(side * 90f);
        return result.rotate(side * 90f).add(factory.x + radius, factory.y + radius);
    }

    public static boolean inAssistantOrbit(ProbeEntity assistant, ProbeEntity factory) {
        float extent = factory.factoryRadius * 2f;
        float nearestX = Mathf.clamp(assistant.x, factory.x, factory.x + extent);
        float nearestY = Mathf.clamp(assistant.y, factory.y, factory.y + extent);
        float distance = assistant.dst2(nearestX, nearestY);
        float minimum = assistant.hitSize * 0.5f + tilesize * 0.5f;
        float maximum = assistantClearance(assistant, factory) + tilesize * 2f;
        return distance >= minimum * minimum && distance <= maximum * maximum;
    }

    public static boolean canStay(ProbeEntity factory, Seq<ProbeEntity> probes) {
        if (!insideWorld(factory.factoryRadius, factory.x, factory.y)) return false;
        for (ProbeEntity other : probes) {
            if (other == factory || !other.isValid() || !other.factory || other.id() > factory.id()) continue;
            Tile destination = moving(other) ? ProbeColonyManager.targetTile(other.miningTargetTile) : null;
            if (moving(other) && destination == null) continue;
            float anchorX = destination == null ? other.x : destination.worldx();
            float anchorY = destination == null ? other.y : destination.worldy();
            if (overlaps(factory.factoryRadius, factory.x, factory.y, other, anchorX, anchorY)) return false;
        }
        return true;
    }

    public static boolean canPlace(ProbeEntity factory, float x, float y, Seq<ProbeEntity> probes) {
        return canPlace(factory, x, y, factory.factoryRadius, probes);
    }

    public static boolean canPlace(ProbeEntity factory, float x, float y, float radius, Seq<ProbeEntity> probes) {
        if (!insideWorld(radius, x, y)) return false;
        for (ProbeEntity other : probes) {
            if (other == factory || !other.isValid() || (!other.factory && !other.factoryCandidate)) continue;
            Tile destination = ProbeColonyManager.targetTile(other.miningTargetTile);
            if ((!other.factory || moving(other)) && destination == null) continue;
            float anchorX = destination == null ? other.x : destination.worldx();
            float anchorY = destination == null ? other.y : destination.worldy();
            if (overlaps(radius, x, y, other, anchorX, anchorY)) return false;
        }
        return true;
    }

    public static Tile findAnchor(ProbeEntity factory, Tile resource, Seq<ProbeEntity> probes) {
        for (int ring = 0; ring < 2; ring++) {
            float distance = RESOURCE_DISTANCE + ring * tilesize * 2f;
            for (int attempt = 0; attempt < 16; attempt++) {
                float angle = attempt * 22.5f;
                float x = Mathf.round((resource.worldx() + Mathf.cosDeg(angle) * distance) / tilesize) * tilesize;
                float y = Mathf.round((resource.worldy() + Mathf.sinDeg(angle) * distance) / tilesize) * tilesize;
                Tile anchor = world.tileWorld(x, y);
                if (anchor == null || Mathf.dst(x, y, resource.worldx(), resource.worldy()) < RESOURCE_DISTANCE) continue;
                float nearX = Mathf.clamp(resource.worldx(), x, x + factory.factoryRadius * 2f);
                float nearY = Mathf.clamp(resource.worldy(), y, y + factory.factoryRadius * 2f);
                if (Mathf.dst(nearX, nearY, resource.worldx(), resource.worldy()) < tilesize * 3f) continue;
                if (canPlace(factory, x, y, probes)) return anchor;
            }
        }
        return null;
    }

    private static boolean insideWorld(float radius, float x, float y) {
        float extent = radius * 2f;
        return x >= tilesize && y >= tilesize
                && x + extent <= world.unitWidth() - tilesize
                && y + extent <= world.unitHeight() - tilesize;
    }

    private static boolean overlaps(float radius, float x, float y, ProbeEntity other, float otherX, float otherY) {
        float extent = radius * 2f;
        float otherExtent = other.factoryRadius * 2f;
        return x < otherX + otherExtent + GAP && x + extent + GAP > otherX
                && y < otherY + otherExtent + GAP && y + extent + GAP > otherY;
    }
}
