package newhorizon.expand.units.probe;

import arc.math.Mathf;
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

    public static boolean canStay(ProbeEntity factory, Seq<ProbeEntity> probes) {
        if (!insideWorld(factory, factory.x, factory.y)) return false;
        for (ProbeEntity other : probes) {
            if (other == factory || !other.isValid() || !other.factory || other.id() > factory.id()) continue;
            Tile destination = moving(other) ? ProbeColonyManager.targetTile(other.miningTargetTile) : null;
            if (moving(other) && destination == null) continue;
            float anchorX = destination == null ? other.x : destination.worldx();
            float anchorY = destination == null ? other.y : destination.worldy();
            if (overlaps(factory, factory.x, factory.y, other, anchorX, anchorY)) return false;
        }
        return true;
    }

    public static boolean canPlace(ProbeEntity factory, float x, float y, Seq<ProbeEntity> probes) {
        if (!insideWorld(factory, x, y)) return false;
        for (ProbeEntity other : probes) {
            if (other == factory || !other.isValid() || (!other.factory && !other.factoryCandidate)) continue;
            Tile destination = ProbeColonyManager.targetTile(other.miningTargetTile);
            if ((!other.factory || moving(other)) && destination == null) continue;
            float anchorX = destination == null ? other.x : destination.worldx();
            float anchorY = destination == null ? other.y : destination.worldy();
            if (overlaps(factory, x, y, other, anchorX, anchorY)) return false;
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

    private static boolean insideWorld(ProbeEntity factory, float x, float y) {
        float extent = factory.factoryRadius * 2f;
        return x >= tilesize && y >= tilesize
                && x + extent <= world.unitWidth() - tilesize
                && y + extent <= world.unitHeight() - tilesize;
    }

    private static boolean overlaps(ProbeEntity factory, float x, float y, ProbeEntity other, float otherX, float otherY) {
        float extent = factory.factoryRadius * 2f;
        float otherExtent = other.factoryRadius * 2f;
        return x < otherX + otherExtent + GAP && x + extent + GAP > otherX
                && y < otherY + otherExtent + GAP && y + extent + GAP > otherY;
    }
}
