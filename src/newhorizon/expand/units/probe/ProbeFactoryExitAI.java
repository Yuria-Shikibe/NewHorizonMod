package newhorizon.expand.units.probe;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import mindustry.ai.UnitCommand;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import newhorizon.expand.units.unitEntity.ProbeEntity;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public final class ProbeFactoryExitAI extends CommandAI {
    private final int factoryId;
    private float anchorX;
    private float anchorY;
    private float extent;
    private int exitAttempt;
    private float nextRetry;

    public ProbeFactoryExitAI(ProbeEntity factory) {
        factoryId = factory.id();
        anchorX = factory.x;
        anchorY = factory.y;
        extent = factory.factoryRadius * 2f;
        command = UnitCommand.moveCommand;
    }

    @Override
    public boolean hasCommand() {
        return true;
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground) {
        return null;
    }

    @Override
    public void updateUnit() {
        Unit owner = Groups.unit.getByID(factoryId);
        if (owner instanceof ProbeEntity factory && factory.isValid() && factory.factory && factory.team == unit.team
                && !ProbeFactoryLayout.moving(factory)
                && (anchorX != factory.x || anchorY != factory.y || extent != factory.factoryRadius * 2f)) {
            anchorX = factory.x;
            anchorY = factory.y;
            extent = factory.factoryRadius * 2f;
            clearCommands();
            nextRetry = 0f;
        }
        float margin = unit.hitSize / 2f + tilesize;
        if ((unit.x < anchorX - margin || unit.x > anchorX + extent + margin
                || unit.y < anchorY - margin || unit.y > anchorY + extent + margin)
                && ProbeColonyManager.canExitAt(unit, unit.x, unit.y)) {
            ProbeColonyManager.finishFactoryExit(unit, factoryId);
            return;
        }
        command = UnitCommand.moveCommand;
        if (targetPos == null && Time.time >= nextRetry) {
            chooseExit();
            nextRetry = Time.time + 120f;
        }
        if (targetPos != null) super.updateUnit();
        else unit.vel.setZero();
    }

    private void chooseExit() {
        float margin = unit.hitSize / 2f + tilesize;
        float centerX = anchorX + extent / 2f;
        float centerY = anchorY + extent / 2f;
        for (int attempt = 0; attempt < 8; attempt++) {
            float angle = (unit.id() + exitAttempt++) * 45f;
            float directionX = Mathf.cosDeg(angle);
            float directionY = Mathf.sinDeg(angle);
            float distance = (extent / 2f + margin + tilesize * 8f) / Math.max(Math.abs(directionX), Math.abs(directionY));
            float x = centerX + directionX * distance;
            float y = centerY + directionY * distance;
            if (x < margin || y < margin || x > world.unitWidth() - margin || y > world.unitHeight() - margin) continue;
            Tile tile = world.tileWorld(x, y);
            if (tile == null || !unit.canPass(tile.x, tile.y) || !ProbeColonyManager.canExitAt(unit, x, y)) continue;
            commandPosition(new Vec2(x, y));
            return;
        }
    }
}
